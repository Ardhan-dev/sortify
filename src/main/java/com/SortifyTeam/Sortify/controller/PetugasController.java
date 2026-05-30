package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import com.SortifyTeam.Sortify.service.FileStorageService;
import com.SortifyTeam.Sortify.service.RewardService;
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
    private final TransaksiRepository transaksiRepo;
    private final FileStorageService fileStorageService;
    private final RewardService rewardService;

    public PetugasController(TransaksiService transaksiService,
                             TransaksiRepository transaksiRepo,
                             FileStorageService fileStorageService,
                             RewardService rewardService) {
        this.transaksiService = transaksiService;
        this.transaksiRepo = transaksiRepo;
        this.fileStorageService = fileStorageService;
        this.rewardService = rewardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Petugas {} sedang membuka halaman Dashboard Petugas", auth.getName());
        List<Transaksi> transaksiPending = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.PENDING);
        List<Transaksi> transaksiDiproses = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.DIPROSES);
        List<Transaksi> transaksiSelesai = transaksiRepo.findByStatusOrderByTanggalTransaksiDesc(Transaksi.StatusTransaksi.SELESAI);
        model.addAttribute("transaksiPending", transaksiPending);
        model.addAttribute("transaksiDiproses", transaksiDiproses);
        model.addAttribute("transaksiSelesai", transaksiSelesai);
        model.addAttribute("totalPending", transaksiPending.size());
        model.addAttribute("totalDiproses", transaksiDiproses.size());
        model.addAttribute("totalSelesai", transaksiSelesai.size());
        model.addAttribute("penukaranPending", rewardService.getAllPenukaran().stream().filter(
                p -> p.getStatus() == PenukaranReward.StatusPenukaran.PENDING).count());
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

    @GetMapping("/reward/verifikasi")
    public String halamanVerifikasiReward(Model model) {
        model.addAttribute("penukaranPending", rewardService.getAllPenukaran().stream().filter(
                p -> p.getStatus() == PenukaranReward.StatusPenukaran.PENDING).count());
        return "petugas-verifikasi-reward";
    }

    @PostMapping("/reward/verifikasi")
    public String verifikasiReward(@RequestParam("kode") String kode,
                                    @RequestParam(value = "fotoBukti", required = false) MultipartFile fotoBukti,
                                    RedirectAttributes redirectAttributes) {
        kode = kode.trim().toUpperCase();
        try {
            String namaFoto = null;
            if (fotoBukti != null && !fotoBukti.isEmpty()) {
                namaFoto = fileStorageService.storeFile(fotoBukti, null);
            }
            rewardService.verifikasiPenukaran(kode, namaFoto);
            redirectAttributes.addFlashAttribute("success", "Penukaran dengan kode " + kode + " berhasil diverifikasi!");
        } catch (RuntimeException e) {
            log.error("[ERROR] Gagal verifikasi reward {}: {}", kode, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Gagal: " + e.getMessage());
        }
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


