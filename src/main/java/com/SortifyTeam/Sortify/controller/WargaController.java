package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.service.LaporanService;
import com.SortifyTeam.Sortify.service.PembayaranService;
import com.SortifyTeam.Sortify.service.PointService;
import com.SortifyTeam.Sortify.service.RewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/warga")
public class WargaController {

    private final UserRepository userRepo;
    private final LaporanService laporanService;
    private final PembayaranService pembayaranService;
    private final RewardService rewardService;
    private final PointService pointService;

    public WargaController(UserRepository userRepo,
                           LaporanService laporanService,
                           PembayaranService pembayaranService,
                           RewardService rewardService,
                           PointService pointService) {
        this.userRepo = userRepo;
        this.laporanService = laporanService;
        this.pembayaranService = pembayaranService;
        this.rewardService = rewardService;
        this.pointService = pointService;
    }

    private User getCurrentUser(Authentication auth) {
        return userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Warga {} sedang membuka halaman Dashboard/Profil Warga", auth.getName());
        User warga = getCurrentUser(auth);
        model.addAttribute("warga", warga);
        model.addAttribute("laporanList", laporanService.getLaporanByWarga(warga));
        model.addAttribute("totalPoints", warga.getTotalPoints());
        return "profil";
    }

    @GetMapping("/laporan/tambah")
    public String formTambahLaporan(Model model) {
        model.addAttribute("jenisList", LaporanSampah.JenisSampah.values());
        return "warga-form-laporan";
    }

    @PostMapping("/laporan/tambah")
    public String simpanLaporan(Authentication auth,
                                @RequestParam String jenisSampah,
                                @RequestParam double berat,
                                @RequestParam String alamatLengkap,
                                @RequestParam(required = false) String catatan) {
        User warga = getCurrentUser(auth);
        laporanService.buatLaporan(warga, jenisSampah, berat, alamatLengkap, catatan);
        return "redirect:/warga/dashboard";
    }

    @GetMapping("/laporan/bayar/{id}")
    public String formBayar(@PathVariable Long id, Model model) {
        LaporanSampah laporan = laporanService.getLaporanById(id);
        Pembayaran pembayaran = pembayaranService.getPembayaranByLaporan(laporan);
        model.addAttribute("laporan", laporan);
        model.addAttribute("pembayaran", pembayaran);
        return "warga-form-bayar";
    }

    @PostMapping("/laporan/bayar/{id}")
    public String prosesBayar(@PathVariable Long id,
                              @RequestParam String metodePembayaran) {
        pembayaranService.bayar(id, metodePembayaran);
        laporanService.prosesLaporan(id);
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
    public String tukarReward(Authentication auth, @PathVariable Long id) {
        User warga = getCurrentUser(auth);
        try {
            rewardService.tukarReward(warga, id);
        } catch (RuntimeException e) {
            return "redirect:/warga/reward?error=" + e.getMessage();
        }
        return "redirect:/warga/reward?success=true";
    }
}


