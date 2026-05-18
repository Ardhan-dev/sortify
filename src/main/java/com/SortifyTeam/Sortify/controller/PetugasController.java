package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.service.LaporanService;
import com.SortifyTeam.Sortify.service.PembayaranService;
import com.SortifyTeam.Sortify.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/petugas")
public class PetugasController {

    private final UserRepository userRepo;
    private final LaporanService laporanService;
    private final PembayaranService pembayaranService;
    private final PointService pointService;

    public PetugasController(UserRepository userRepo,
                             LaporanService laporanService,
                             PembayaranService pembayaranService,
                             PointService pointService) {
        this.userRepo = userRepo;
        this.laporanService = laporanService;
        this.pembayaranService = pembayaranService;
        this.pointService = pointService;
    }

    private User getCurrentUser(Authentication auth) {
        return userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Petugas {} sedang membuka halaman Dashboard Petugas", auth.getName());
        List<LaporanSampah> menungguList = laporanService.getLaporanByStatus(LaporanSampah.StatusLaporan.MENUNGGU);
        List<LaporanSampah> diprosesList = laporanService.getLaporanByStatus(LaporanSampah.StatusLaporan.DIPROSES);
        model.addAttribute("menungguList", menungguList);
        model.addAttribute("diprosesList", diprosesList);
        return "petugas-dashboard";
    }

    @PostMapping("/laporan/acc/{id}")
    @Transactional
    public String accLaporan(Authentication auth, @PathVariable Long id) {
        User petugas = getCurrentUser(auth);
        LaporanSampah laporan = laporanService.getLaporanById(id);
        laporanService.accLaporan(id, petugas);
        pembayaranService.buatPembayaran(laporan);
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/laporan/tolak/{id}")
    public String tolakLaporan(@PathVariable Long id) {
        laporanService.tolakLaporan(id);
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/laporan/selesai/{id}")
    @Transactional
    public String selesaikanLaporan(@PathVariable Long id) {
        LaporanSampah laporan = laporanService.getLaporanById(id);
        User warga = laporan.getWarga();

        laporanService.selesaikanLaporan(id);

        pointService.tambahPoint(warga, 500,
                "Reward laporan selesai #" + id);

        return "redirect:/petugas/dashboard";
    }
}


