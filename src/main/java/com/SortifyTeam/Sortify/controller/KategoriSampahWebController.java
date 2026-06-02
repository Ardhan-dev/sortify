package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.service.KategoriSampahService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/kategori-sampah")
public class KategoriSampahWebController {

    private final KategoriSampahService kategoriService;

    public KategoriSampahWebController(KategoriSampahService kategoriService) {
        this.kategoriService = kategoriService;
    }

    @GetMapping
    public String halamanKategori(Model model) {
        model.addAttribute("daftarKategori", kategoriService.getSemua());
        return "admin-kategori";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("kategori", new KategoriSampah());
        model.addAttribute("isEdit", false);
        return "admin-kategori-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@RequestParam String namaKategori,
                               @RequestParam Integer poinPerKg,
                               @RequestParam(required = false) String instruksiPenanganan) {
        kategoriService.simpan(namaKategori, poinPerKg, instruksiPenanganan);
        return "redirect:/admin/kategori-sampah";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        KategoriSampah k = kategoriService.getById(id);
        model.addAttribute("kategori", k);
        model.addAttribute("isEdit", true);
        return "admin-kategori-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id,
                             @RequestParam String namaKategori,
                             @RequestParam Integer poinPerKg,
                             @RequestParam(required = false) String instruksiPenanganan) {
        kategoriService.update(id, namaKategori, poinPerKg, instruksiPenanganan);
        return "redirect:/admin/kategori-sampah";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            kategoriService.hapus(id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/kategori-sampah";
    }
}
