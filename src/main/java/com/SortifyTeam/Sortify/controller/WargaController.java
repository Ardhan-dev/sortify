package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import com.SortifyTeam.Sortify.service.NotifikasiService;
import com.SortifyTeam.Sortify.service.PointService;
import com.SortifyTeam.Sortify.service.RewardService;
import com.SortifyTeam.Sortify.service.TransaksiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
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

    @Value("")
    private String uploadDir;

    public WargaController(UserRepository userRepo,
                           RewardService rewardService,
                           PointService pointService,
                           TransaksiService transaksiService,
                           WargaRepository wargaRepo,
                           NotifikasiService notifService) {
        this.userRepo = userRepo;
        this.rewardService = rewardService;
        this.pointService = pointService;
        this.transaksiService = transaksiService;
        this.wargaRepo = wargaRepo;
        this.notifService = notifService;
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

    private String simpanFoto(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;

            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[UPLOAD] File {} tersimpan sebagai {}", original, filename);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Warga {} sedang membuka halaman Dashboard/Profil Warga", auth.getName());
        User warga = getCurrentUser(auth);
        model.addAttribute("warga", warga);
        model.addAttribute("totalPoints", warga.getTotalPoints());

        Warga entitasWarga = getCurrentWargaEntity(warga);
        List<Transaksi> transaksiList = transaksiService.getTransaksiByWarga(entitasWarga);
        model.addAttribute("transaksiList", transaksiList);

        // Leaderboard: Top 5 warga by total points
        List<User> leaderboard = userRepo.findTop5ByOrderByTotalPointsDesc()
                .stream()
                .filter(u -> u.getRole() == User.Role.WARGA)
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("leaderboard", leaderboard);

        // Notifikasi
        List<Notifikasi> notifikasiList = notifService.getNotifikasiBelumDibaca(warga);
        model.addAttribute("notifikasiList", notifikasiList);

        return "profil";
    }

    @GetMapping("/transaksi/tambah")
    public String formTambahTransaksi(Model model) {
        model.addAttribute("jenisList", Transaksi.JenisSampah.values());
        return "warga-form-transaksi";
    }

    @PostMapping("/transaksi/tambah")
    public String simpanTransaksi(Authentication auth,
                                   @RequestParam("jenisSampah") Transaksi.JenisSampah jenisSampah,
                                   @RequestParam("fotoLaporanWarga") MultipartFile fotoLaporanWarga,
                                   @RequestParam("beratSampah") Double beratSampah,
                                   @RequestParam("lokasi") String lokasi,
                                   @RequestParam(value = "detail", required = false, defaultValue = "") String detail,
                                   RedirectAttributes redirectAttributes) {
        if (fotoLaporanWarga.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Foto laporan warga harus diupload.");
            return "redirect:/warga/transaksi/tambah";
        }

        if (beratSampah == null || beratSampah <= 0) {
            redirectAttributes.addFlashAttribute("error", "Berat sampah harus diisi dan lebih dari 0.");
            return "redirect:/warga/transaksi/tambah";
        }

        if (lokasi == null || lokasi.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Lokasi drop point harus diisi.");
            return "redirect:/warga/transaksi/tambah";
        }

        try {
            User user = getCurrentUser(auth);
            Warga warga = getCurrentWargaEntity(user);
            String namaFoto = simpanFoto(fotoLaporanWarga);
            transaksiService.buatLaporanDropPoint(warga, jenisSampah, namaFoto, beratSampah, lokasi, detail);
            redirectAttributes.addFlashAttribute("success", "Drop point berhasil dilaporkan! Menunggu verifikasi petugas.");
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
            rewardService.tukarReward(warga, id);
            redirectAttributes.addFlashAttribute("success", "Reward berhasil ditukar!");
        } catch (RuntimeException e) {
            log.error("[ERROR] Gagal menukar reward #{}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Gagal menukar reward: " + e.getMessage());
        }
        return "redirect:/warga/reward";
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
