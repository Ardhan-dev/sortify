package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.service.ItemSampahService;
import com.SortifyTeam.Sortify.service.KategoriSampahService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/kamus")
public class AdminKamusController {

    private final KategoriSampahService kategoriService;
    private final ItemSampahService itemSampahService;

    public AdminKamusController(KategoriSampahService kategoriService,
                                 ItemSampahService itemSampahService) {
        this.kategoriService = kategoriService;
        this.itemSampahService = itemSampahService;
    }

    @GetMapping
    public String halamanKamus(Model model) {
        model.addAttribute("daftarKategori", kategoriService.getSemua());
        model.addAttribute("daftarItem", itemSampahService.getAll());
        model.addAttribute("kategori", new KategoriSampah());
        model.addAttribute("item", new ItemSampah());
        return "admin-kamus";
    }

    @PostMapping("/kategori/tambah")
    public String tambahKategori(@RequestParam String namaKategori,
                                  @RequestParam Integer poinPerKg,
                                  @RequestParam(required = false) String instruksiPenanganan,
                                  RedirectAttributes ra) {
        try {
            kategoriService.simpan(namaKategori, poinPerKg, instruksiPenanganan);
            ra.addFlashAttribute("success", "Kategori '" + namaKategori + "' berhasil ditambahkan");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal menambah kategori: " + e.getMessage());
        }
        return "redirect:/admin/kamus";
    }

    @PostMapping("/kategori/edit/{id}")
    public String editKategori(@PathVariable Long id,
                                @RequestParam String namaKategori,
                                @RequestParam Integer poinPerKg,
                                @RequestParam(required = false) String instruksiPenanganan,
                                RedirectAttributes ra) {
        try {
            kategoriService.update(id, namaKategori, poinPerKg, instruksiPenanganan);
            ra.addFlashAttribute("success", "Kategori berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal mengupdate kategori: " + e.getMessage());
        }
        return "redirect:/admin/kamus";
    }

    @GetMapping("/kategori/hapus/{id}")
    public String hapusKategori(@PathVariable Long id, RedirectAttributes ra) {
        try {
            kategoriService.hapus(id);
            ra.addFlashAttribute("success", "Kategori berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal menghapus kategori: " + e.getMessage());
        }
        return "redirect:/admin/kamus";
    }

    @PostMapping("/item/tambah")
    public String tambahItem(@RequestParam String namaItem,
                              @RequestParam(required = false) String deskripsi,
                              @RequestParam(required = false) String instruksiPenanganan,
                              @RequestParam Long idKategori,
                              RedirectAttributes ra) {
        try {
            KategoriSampah kategori = kategoriService.getById(idKategori);
            itemSampahService.simpan(namaItem, deskripsi, instruksiPenanganan, kategori);
            ra.addFlashAttribute("success", "Item '" + namaItem + "' berhasil ditambahkan");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal menambah item: " + e.getMessage());
        }
        return "redirect:/admin/kamus";
    }

    @PostMapping("/item/edit/{id}")
    public String editItem(@PathVariable Long id,
                            @RequestParam String namaItem,
                            @RequestParam(required = false) String deskripsi,
                            @RequestParam(required = false) String instruksiPenanganan,
                            @RequestParam Long idKategori,
                            RedirectAttributes ra) {
        try {
            KategoriSampah kategori = kategoriService.getById(idKategori);
            itemSampahService.update(id, namaItem, deskripsi, instruksiPenanganan, kategori);
            ra.addFlashAttribute("success", "Item berhasil diperbarui");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal mengupdate item: " + e.getMessage());
        }
        return "redirect:/admin/kamus";
    }

    @GetMapping("/item/hapus/{id}")
    public String hapusItem(@PathVariable Long id, RedirectAttributes ra) {
        try {
            itemSampahService.hapus(id);
            ra.addFlashAttribute("success", "Item berhasil dihapus");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal menghapus item: " + e.getMessage());
        }
        return "redirect:/admin/kamus";
    }
}
