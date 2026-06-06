package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.PenukaranRewardRepository;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.service.FileStorageService;
import com.SortifyTeam.Sortify.service.NotifikasiService;
import com.SortifyTeam.Sortify.service.RewardService;
import com.SortifyTeam.Sortify.service.TransaksiService;
import com.SortifyTeam.Sortify.service.WarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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
    private final PenukaranRewardRepository penukaranRepo;
    private final NotifikasiService notifikasiService;
    private final UserRepository userRepo;
    private final RewardService rewardService;
    private final StaffRepository staffRepo;
    private final WarningService warningService;

    public PetugasController(TransaksiService transaksiService,
                             TransaksiRepository transaksiRepo,
                             FileStorageService fileStorageService,
                             PenukaranRewardRepository penukaranRepo,
                             NotifikasiService notifikasiService,
                             UserRepository userRepo,
                             RewardService rewardService,
                             StaffRepository staffRepo,
                             WarningService warningService) {
        this.transaksiService = transaksiService;
        this.transaksiRepo = transaksiRepo;
        this.fileStorageService = fileStorageService;
        this.penukaranRepo = penukaranRepo;
        this.notifikasiService = notifikasiService;
        this.userRepo = userRepo;
        this.rewardService = rewardService;
        this.staffRepo = staffRepo;
        this.warningService = warningService;
    }

    private Staff getCurrentStaff(Authentication auth) {
        User user = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        return staffRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Staff tidak ditemukan untuk user: " + auth.getName()));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model,
                            @RequestParam(defaultValue = "0") int pageT,
                            @RequestParam(defaultValue = "0") int pageR) {
        log.info("[ACCESS] Petugas {} sedang membuka halaman Dashboard Petugas", auth.getName());
        User petugas = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        List<Transaksi> transaksiPending = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.PENDING);
        List<Transaksi> transaksiDiproses = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.DIPROSES);
        List<PenukaranReward> penukaranPending = penukaranRepo.findAllByOrderByTanggalPenukaranDesc().stream()
                .filter(p -> PenukaranReward.StatusPenukaran.PENDING.equals(p.getStatus()))
                .toList();
        model.addAttribute("transaksiPending", transaksiPending);
        model.addAttribute("transaksiDiproses", transaksiDiproses);
        model.addAttribute("penukaranPending", penukaranPending);
        model.addAttribute("totalPending", transaksiPending.size());
        model.addAttribute("totalDiproses", transaksiDiproses.size());

        Page<Transaksi> transaksiHistoryPage = transaksiService.getTransaksiHistory(pageT, 10);
        Page<PenukaranReward> penukaranSelesaiPage = penukaranRepo
                .findByStatusInOrderByTanggalPenukaranDesc(
                        List.of(PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL, PenukaranReward.StatusPenukaran.DIBATALKAN),
                        PageRequest.of(pageR, 10));
        model.addAttribute("transaksiHistory", transaksiHistoryPage.getContent());
        model.addAttribute("transaksiHistoryPage", transaksiHistoryPage);
        model.addAttribute("penukaranSelesai", penukaranSelesaiPage.getContent());
        model.addAttribute("penukaranSelesaiPage", penukaranSelesaiPage);

        long countNotif = notifikasiService.countBelumDibaca(petugas);
        List<Notifikasi> notifikasiList = notifikasiService.getNotifikasiBelumDibaca(petugas);
        model.addAttribute("notifikasiList", notifikasiList);
        model.addAttribute("countNotif", countNotif);
        if (!notifikasiList.isEmpty()) {
            notifikasiService.tandaiDibaca(notifikasiList);
        }
        return "petugas-dashboard";
    }

    @PostMapping("/notifikasi/baca/{id}")
    public String tandaiDibaca(@PathVariable Long id) {
        notifikasiService.tandaiDibaca(id);
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/notifikasi/baca-semua")
    public String tandaiSemuaDibaca(Authentication auth) {
        User petugas = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        notifikasiService.tandaiSemuaDibaca(petugas);
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/transaksi/proses/{id}")
    public String prosesTransaksi(Authentication auth, @PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            Staff staff = getCurrentStaff(auth);
            transaksiService.prosesTransaksi(id, staff);
            redirectAttributes.addFlashAttribute("success", "Drop point #" + id + " sedang diproses.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal memproses transaksi #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal memproses: " + e.getMessage());
        }
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/transaksi/tolak/{id}")
    public String tolakTransaksi(Authentication auth, @PathVariable Long id,
                                   @RequestParam("alasan") String alasan,
                                   RedirectAttributes redirectAttributes) {
        if (alasan == null || alasan.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Alasan penolakan harus diisi.");
            return "redirect:/petugas/dashboard";
        }
        try {
            Staff staff = getCurrentStaff(auth);
            transaksiService.tolakTransaksi(id, alasan, staff);
            redirectAttributes.addFlashAttribute("success", "Drop point #" + id + " telah ditolak.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal menolak transaksi #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal menolak: " + e.getMessage());
        }
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/reward/konfirmasi/{id}")
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

        User warga = penukaran.getUser();
        String namaReward = penukaran.getRewardItem() != null
                ? penukaran.getRewardItem().getNamaBarang() : "Reward";
        if (warga != null) {
            notifikasiService.buatNotifikasi(warga,
                    "Reward " + namaReward + " sudah dikonfirmasi dan bisa diambil di Kantor Sortify.");
        }

        redirectAttributes.addFlashAttribute("success", "Reward #" + id + " berhasil dikonfirmasi.");
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/reward/batal/{id}")
    public String batalReward(@PathVariable Long id,
                               @RequestParam("alasan") String alasan,
                               RedirectAttributes redirectAttributes) {
        if (alasan == null || alasan.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Alasan pembatalan harus diisi.");
            return "redirect:/petugas/dashboard";
        }
        try {
            rewardService.batalkanPenukaran(id, alasan);
            redirectAttributes.addFlashAttribute("success", "Penukaran reward #" + id + " berhasil dibatalkan. Poin dan stok dikembalikan.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/user/warning/{id}")
    public String beriWarning(Authentication auth, @PathVariable Long id,
                               @RequestParam("alasan") String alasan,
                               RedirectAttributes redirectAttributes) {
        if (alasan == null || alasan.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Alasan peringatan harus diisi.");
            return "redirect:/petugas/dashboard";
        }
        try {
            User target = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + id));
            warningService.beriWarning(target, auth.getName(), alasan);
            redirectAttributes.addFlashAttribute("success",
                    "Peringatan berhasil diberikan ke " + target.getFullName() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal memberi peringatan: " + e.getMessage());
        }
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/transaksi/selesai/{id}")
    public String selesaikanTransaksi(Authentication auth, @PathVariable Long id,
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
            Staff staff = getCurrentStaff(auth);
            String filename = fileStorageService.storeFile(fotoBuktiTimbangan, null);
            transaksiService.selesaikanTransaksi(id, detailIds, beratFinal, filename, staff);
            redirectAttributes.addFlashAttribute("success", "Drop point #" + id + " berhasil diselesaikan.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal menyelesaikan transaksi #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal menyelesaikan: " + e.getMessage());
        }

        return "redirect:/petugas/dashboard";
    }

}
