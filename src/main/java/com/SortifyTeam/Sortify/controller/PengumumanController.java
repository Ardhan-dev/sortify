package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Pengumuman;
import com.SortifyTeam.Sortify.service.PengumumanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/pengumuman")
public class PengumumanController {

    private final PengumumanService pengumumanService;

    public PengumumanController(PengumumanService pengumumanService) {
        this.pengumumanService = pengumumanService;
    }

    @GetMapping
    public String halamanPengumuman(Model model) {
        model.addAttribute("daftarPengumuman", pengumumanService.getSemua());
        return "admin-pengumuman";
    }

    @PostMapping("/tambah")
    public String tambah(@RequestParam String judul,
                          @RequestParam String konten,
                          RedirectAttributes ra) {
        pengumumanService.simpan(judul, konten);
        ra.addFlashAttribute("success", "Pengumuman berhasil ditambahkan");
        return "redirect:/admin/pengumuman";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                        @RequestParam String judul,
                        @RequestParam String konten,
                        @RequestParam(required = false) boolean isActive,
                        RedirectAttributes ra) {
        pengumumanService.update(id, judul, konten, isActive);
        ra.addFlashAttribute("success", "Pengumuman berhasil diperbarui");
        return "redirect:/admin/pengumuman";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes ra) {
        pengumumanService.hapus(id);
        ra.addFlashAttribute("success", "Pengumuman berhasil dihapus");
        return "redirect:/admin/pengumuman";
    }
}
