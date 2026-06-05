package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/warga")
public class WargaController {

    private final UserRepository userRepo;
    private final RewardService rewardService;
    private final PointService pointService;
    private final TransaksiService transaksiService;
    private final WargaRepository wargaRepo;
    private final NotifikasiService notifService;
    private final KategoriSampahRepository kategoriRepo;
    private final FileStorageService fileStorageService;
    private final PdfStrukService pdfStrukService;
    private final PasswordEncoder passwordEncoder;

    public WargaController(UserRepository userRepo,
                           RewardService rewardService,
                           PointService pointService,
                           TransaksiService transaksiService,
                           WargaRepository wargaRepo,
                           NotifikasiService notifService,
                           KategoriSampahRepository kategoriRepo,
                           FileStorageService fileStorageService,
                           PdfStrukService pdfStrukService,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.rewardService = rewardService;
        this.pointService = pointService;
        this.transaksiService = transaksiService;
        this.wargaRepo = wargaRepo;
        this.notifService = notifService;
        this.kategoriRepo = kategoriRepo;
        this.fileStorageService = fileStorageService;
        this.pdfStrukService = pdfStrukService;
        this.passwordEncoder = passwordEncoder;
    }

    private User getCurrentUser(Authentication auth) {
        return userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    private Warga getCurrentWargaEntity(User user) {
        Warga warga = user.getWarga();
        if (warga == null) {
            warga = wargaRepo.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Data warga tidak ditemukan"));
        }
        return warga;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Warga {} sedang membuka halaman Dashboard/Profil Warga", auth.getName());
        User warga = getCurrentUser(auth);
        model.addAttribute("warga", warga);
        model.addAttribute("totalPoints", warga.getTotalPoints());

        Warga entitasWarga = getCurrentWargaEntity(warga);
        List<Transaksi> transaksiList = transaksiService.getTransaksiByWarga(entitasWarga);
        if (transaksiList.size() > 20) transaksiList = transaksiList.subList(0, 20);
        model.addAttribute("transaksiList", transaksiList);

        // Leaderboard: Top 5 warga by total points
        List<User> leaderboard = userRepo.findTop5ByOrderByTotalPointsDesc()
                .stream()
                .filter(u -> u.getRole() == User.Role.WARGA)
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("leaderboard", leaderboard);

        // Notifikasi
        long countNotif = notifService.countBelumDibaca(warga);
        List<Notifikasi> notifikasiList = notifService.getNotifikasiBelumDibaca(warga);
        model.addAttribute("notifikasiList", notifikasiList);
        model.addAttribute("countNotif", countNotif);
        if (!notifikasiList.isEmpty()) {
            notifService.tandaiDibaca(notifikasiList);
        }

        return "profil";
    }

    @GetMapping("/transaksi/tambah")
    public String formTambahTransaksi(Model model) {
        model.addAttribute("kategoriList", kategoriRepo.findByIsActiveTrue());
        return "warga-form-transaksi";
    }

    @PostMapping("/transaksi/tambah")
    public String simpanTransaksi(Authentication auth,
                                    @RequestParam("fotoLaporanWarga") MultipartFile fotoLaporanWarga,
                                    @RequestParam("idKategori") List<Long> idKategori,
                                    @RequestParam("beratEstimasi") List<Double> beratEstimasi,
                                    @RequestParam("lokasi") String lokasi,
                                    @RequestParam(value = "detail", required = false, defaultValue = "") String detail,
                                    RedirectAttributes redirectAttributes) {
        if (fotoLaporanWarga.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Foto laporan warga harus diupload.");
            return "redirect:/warga/transaksi/tambah";
        }

        if (idKategori == null || idKategori.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Minimal satu jenis sampah harus diisi.");
            return "redirect:/warga/transaksi/tambah";
        }

        if (lokasi == null || lokasi.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Lokasi harus diisi.");
            return "redirect:/warga/transaksi/tambah";
        }

        try {
            User user = getCurrentUser(auth);
            Warga warga = getCurrentWargaEntity(user);
            String namaFoto = fileStorageService.storeFile(fotoLaporanWarga, null);
            transaksiService.buatLaporanDropPoint(warga, namaFoto, lokasi, detail, idKategori, beratEstimasi);
            redirectAttributes.addFlashAttribute("success", "Laporan berhasil dikirim! Menunggu verifikasi petugas.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal membuat transaksi: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal membuat laporan: " + e.getMessage());
        }
        return "redirect:/warga/dashboard";
    }

    @GetMapping("/reward")
    public String halamanReward(Authentication auth, Model model) {
        User warga = getCurrentUser(auth);
        model.addAttribute("warga", warga);
        model.addAttribute("rewardList", rewardService.getRewardTersedia());
        model.addAttribute("riwayatPenukaran", rewardService.getRiwayatPenukaran(warga));
        model.addAttribute("riwayatPoint", pointService.getRiwayatPoint(warga));
        return "warga-reward";
    }

    @PostMapping("/reward/tukar/{id}")
    public String tukarReward(Authentication auth, @PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        User warga = getCurrentUser(auth);
        try {
            PenukaranReward penukaran = rewardService.tukarReward(warga, id);
            redirectAttributes.addFlashAttribute("kodePenukaran", penukaran.getKodePenukaran());
            redirectAttributes.addFlashAttribute("success", "Penukaran berhasil! Silakan ambil reward Anda di Kantor Sortify pada jam kerja.");
        } catch (RuntimeException e) {
            log.error("[ERROR] Gagal menukar reward #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal menukar reward: " + e.getMessage());
        }
        return "redirect:/warga/reward";
    }

    @GetMapping("/transaksi/struk/{id}")
    public ResponseEntity<byte[]> unduhStruk(Authentication auth, @PathVariable Long id) {
        User warga = getCurrentUser(auth);
        Transaksi transaksi = transaksiService.getTransaksiById(id);

        if (transaksi.getWarga() == null || transaksi.getWarga().getUser() == null
                || !transaksi.getWarga().getUser().getId().equals(warga.getId())) {
            return ResponseEntity.badRequest().build();
        }

        if (!Transaksi.StatusTransaksi.SELESAI.equals(transaksi.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes = pdfStrukService.generateStruk(transaksi);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "struk-transaksi-" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/ubah-password")
    public String formUbahPassword() {
        return "ubah-password";
    }

    @PostMapping("/ubah-password")
    public String ubahPassword(Authentication auth,
                                 @RequestParam("passwordLama") String passwordLama,
                                 @RequestParam("passwordBaru") String passwordBaru,
                                 @RequestParam("konfirmasiPassword") String konfirmasiPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        if (!passwordEncoder.matches(passwordLama, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password lama salah.");
            return "redirect:/warga/ubah-password";
        }

        if (!passwordBaru.equals(konfirmasiPassword)) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi password baru tidak cocok.");
            return "redirect:/warga/ubah-password";
        }

        if (passwordBaru.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password baru minimal 6 karakter.");
            return "redirect:/warga/ubah-password";
        }

        user.setPassword(passwordEncoder.encode(passwordBaru));
        userRepo.save(user);
        redirectAttributes.addFlashAttribute("success", "Password berhasil diubah.");
        return "redirect:/warga/ubah-password";
    }

    @PostMapping("/transaksi/batal/{id}")
    public String batalTransaksi(Authentication auth, @PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        User warga = getCurrentUser(auth);
        try {
            transaksiService.batalTransaksi(id, warga);
            redirectAttributes.addFlashAttribute("success", "Laporan berhasil dibatalkan.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/warga/dashboard";
    }

    // ── Notifikasi endpoints ──

    @PostMapping("/notifikasi/baca/{id}")
    public String tandaiDibaca(@PathVariable Long id) {
        notifService.tandaiDibaca(id);
        return "redirect:/warga/dashboard";
    }

    @PostMapping("/notifikasi/baca-semua")
    public String tandaiSemuaDibaca(Authentication auth) {
        User warga = getCurrentUser(auth);
        notifService.tandaiSemuaDibaca(warga);
        return "redirect:/warga/dashboard";
    }
}
