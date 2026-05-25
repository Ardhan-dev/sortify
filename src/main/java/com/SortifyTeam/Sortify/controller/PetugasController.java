package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.service.TransaksiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@Controller
@RequestMapping("/petugas")
public class PetugasController {

    private final TransaksiService transaksiService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public PetugasController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        log.info("[ACCESS] Petugas {} sedang membuka halaman Dashboard Petugas", auth.getName());
        List<Transaksi> transaksiPending = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.PENDING);
        List<Transaksi> transaksiDiproses = transaksiService.getTransaksiByStatus(Transaksi.StatusTransaksi.DIPROSES);
        model.addAttribute("transaksiPending", transaksiPending);
        model.addAttribute("transaksiDiproses", transaksiDiproses);
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
            String filename = simpanFoto(fotoBuktiTimbangan);
            transaksiService.selesaikanTransaksi(id, detailIds, beratFinal, filename);
            redirectAttributes.addFlashAttribute("success", "Drop point #" + id + " berhasil diselesaikan.");
        } catch (Exception e) {
            log.error("[ERROR] Gagal menyelesaikan transaksi #{}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal menyelesaikan: " + e.getMessage());
        }

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
            throw new RuntimeException(e);
        }
    }
}


