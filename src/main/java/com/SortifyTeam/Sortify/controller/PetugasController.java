package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.service.LaporanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/petugas")
public class PetugasController {

    private final UserRepository userRepo;
    private final LaporanService laporanService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public PetugasController(UserRepository userRepo,
                             LaporanService laporanService) {
        this.userRepo = userRepo;
        this.laporanService = laporanService;
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
        laporanService.accLaporan(id, petugas);
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/laporan/tolak/{id}")
    public String tolakLaporan(@PathVariable Long id) {
        laporanService.tolakLaporan(id);
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/laporan/selesai/{id}")
    @Transactional
    public String selesaikanLaporan(@PathVariable Long id,
                                    @RequestParam("beratFinal") double beratFinal,
                                    @RequestParam("foto") MultipartFile foto) {
        if (foto.isEmpty()) {
            throw new RuntimeException("Foto bukti harus diupload");
        }
        if (beratFinal <= 0) {
            throw new RuntimeException("Berat final harus lebih dari 0");
        }

        String filename = simpanFoto(foto);

        laporanService.selesaikanDenganFoto(id, beratFinal, filename);

        return "redirect:/petugas/dashboard";
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
            throw new RuntimeException("Gagal menyimpan foto: " + e.getMessage(), e);
        }
    }
}


