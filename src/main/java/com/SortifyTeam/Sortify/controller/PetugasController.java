package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.service.FileStorageService;
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

    public PetugasController(TransaksiService transaksiService,
                             FileStorageService fileStorageService) {
        this.transaksiService = transaksiService;
        this.fileStorageService = fileStorageService;
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


