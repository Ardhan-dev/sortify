package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.PenukaranRewardRepository;
import com.SortifyTeam.Sortify.service.FileStorageService;
import com.SortifyTeam.Sortify.service.NotifikasiService;
import com.SortifyTeam.Sortify.service.TransaksiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/petugas")
public class PetugasController {

    private final TransaksiService transaksiService;
    private final FileStorageService fileStorageService;
    private final PenukaranRewardRepository penukaranRepo;
    private final NotifikasiService notifikasiService;

    public PetugasController(TransaksiService transaksiService,
                             FileStorageService fileStorageService,
                             PenukaranRewardRepository penukaranRepo,
                             NotifikasiService notifikasiService) {
        this.transaksiService = transaksiService;
        this.fileStorageService = fileStorageService;
        this.penukaranRepo = penukaranRepo;
        this.notifikasiService = notifikasiService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Petugas {} sedang membuka halaman Dashboard Petugas", auth.getName());
        List<Transaksi> transaksiPending = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.PENDING);
        List<Transaksi> transaksiDiproses = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.DIPROSES);
        List<PenukaranReward> penukaranPending = penukaranRepo.findAllByOrderByTanggalPenukaranDesc().stream()
                .filter(p -> PenukaranReward.StatusPenukaran.PENDING.equals(p.getStatus()))
                .toList();
        model.addAttribute("transaksiPending", transaksiPending);
        model.addAttribute("transaksiDiproses", transaksiDiproses);
        model.addAttribute("penukaranPending", penukaranPending);
        return "petugas-dashboard";
    }

    @PostMapping("/transaksi/proses/{id}")
    @Transactional
    public String prosesTransaksi(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            transaksiService.prosesTransaksi(id);
            redirectAttributes.addFlashAttribute("success", "Drop point #" + id + " sedang diproses.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal memproses transaksi #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal memproses: " + e.getMessage());
        }
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/transaksi/tolak/{id}")
    @Transactional
    public String tolakTransaksi(@PathVariable Long id,
                                  @RequestParam("alasan") String alasan,
                                  RedirectAttributes redirectAttributes) {
        if (alasan == null || alasan.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Alasan penolakan harus diisi.");
            return "redirect:/petugas/dashboard";
        }
        try {
            transaksiService.tolakTransaksi(id, alasan);
            redirectAttributes.addFlashAttribute("success", "Drop point #" + id + " telah ditolak.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal menolak transaksi #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal menolak: " + e.getMessage());
        }
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/reward/konfirmasi/{id}")
    @Transactional
    public String konfirmasiReward(@PathVariable Long id,
                                    @RequestParam("fotoBukti") MultipartFile fotoBukti,
                                    @RequestParam(value = "kodeVerifikasiKetik", required = false) String kodeVerifikasiKetik,
                                    RedirectAttributes redirectAttributes) {
        log.info("[PROSES] Petugas mengkonfirmasi penyerahan reward #{}", id);

        if (fotoBukti.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Foto bukti penyerahan harus diupload.");
            return "redirect:/petugas/dashboard";
        }

        PenukaranReward penukaran = penukaranRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Penukaran reward tidak ditemukan: " + id));

        if (PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL.equals(penukaran.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Reward #" + id + " sudah diserahkan sebelumnya.");
            return "redirect:/petugas/dashboard";
        }

        if (kodeVerifikasiKetik == null || !kodeVerifikasiKetik.equalsIgnoreCase(penukaran.getKodePenukaran())) {
            redirectAttributes.addFlashAttribute("error", "Kode AMDAL salah!");
            return "redirect:/petugas/dashboard";
        }

        String namaFoto = fileStorageService.storeFile(fotoBukti, "reward");
        penukaran.setFotoBukti(namaFoto);
        penukaran.setStatus(PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL);
        penukaranRepo.save(penukaran);

        User warga = penukaran.getWarga();
        if (warga != null) {
            notifikasiService.buatNotifikasi(warga,
                    "Reward " + penukaran.getRewardItem().getNamaBarang() + " sudah dikonfirmasi dan bisa diambil di Kantor Sortify.");
        }

        redirectAttributes.addFlashAttribute("success", "Reward #" + id + " berhasil dikonfirmasi.");
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/transaksi/selesai/{id}")
    @Transactional
    public String selesaikanTransaksi(@PathVariable Long id,
                                       @RequestParam("detailId") List<Long> detailIds,
                                       @RequestParam("beratFinal") List<Double> beratFinal,
                                       @RequestParam("fotoBuktiTimbangan") MultipartFile fotoBuktiTimbangan,
                                       RedirectAttributes redirectAttributes) {
        if (fotoBuktiTimbangan.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Foto bukti timbangan harus diupload.");
            return "redirect:/petugas/dashboard";
        }
        if (detailIds == null || detailIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Data detail transaksi tidak valid.");
            return "redirect:/petugas/dashboard";
        }

        try {
            String filename = fileStorageService.storeFile(fotoBuktiTimbangan, null);
            transaksiService.selesaikanTransaksi(id, detailIds, beratFinal, filename);
            redirectAttributes.addFlashAttribute("success", "Drop point #" + id + " berhasil diselesaikan.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal menyelesaikan transaksi #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal menyelesaikan: " + e.getMessage());
        }

        return "redirect:/petugas/dashboard";
    }

}


