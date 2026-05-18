package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.*;
import com.SortifyTeam.Sortify.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepo;
    private final LaporanService laporanService;
    private final PembayaranService pembayaranService;
    private final RewardService rewardService;

    public AdminController(UserRepository userRepo,
                           LaporanService laporanService,
                           PembayaranService pembayaranService,
                           RewardService rewardService) {
        this.userRepo = userRepo;
        this.laporanService = laporanService;
        this.pembayaranService = pembayaranService;
        this.rewardService = rewardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Admin {} sedang membuka halaman Dashboard Admin", auth.getName());
        model.addAttribute("totalWarga", userRepo.countByRole(User.Role.WARGA));
        model.addAttribute("totalPetugas", userRepo.countByRole(User.Role.PETUGAS));
        model.addAttribute("laporanSelesai", laporanService.countByStatus(LaporanSampah.StatusLaporan.SELESAI));
        model.addAttribute("laporanDitolak", laporanService.countByStatus(LaporanSampah.StatusLaporan.DITOLAK));
        model.addAttribute("transaksiSukses", pembayaranService.countByStatus(Pembayaran.StatusPembayaran.BERHASIL));
        model.addAttribute("rewardDitukar", rewardService.countTotalPenukaran());
        return "index";
    }
}


