package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.service.ItemSampahService;
import com.SortifyTeam.Sortify.service.KategoriSampahService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/admin/item-sampah")
public class ItemSampahWebController {

    private final ItemSampahService itemSampahService;
    private final KategoriSampahService kategoriService;

    public ItemSampahWebController(ItemSampahService itemSampahService, KategoriSampahService kategoriService) {
        this.itemSampahService = itemSampahService;
        this.kategoriService = kategoriService;
    }

    @GetMapping
    public String halamanItem(Model model) {
        model.addAttribute("daftarItem", itemSampahService.getSemua());
        return "admin-item-sampah";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("item", new ItemSampah());
        model.addAttribute("daftarKategori", kategoriService.getSemua());
        model.addAttribute("isEdit", false);
        return "admin-item-sampah-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@RequestParam String namaItem,
                               @RequestParam(required = false) String deskripsi,
                               @RequestParam(required = false) String instruksiPenanganan,
                               @RequestParam Long idKategori) {
        itemSampahService.simpan(namaItem, deskripsi, instruksiPenanganan, idKategori);
        return "redirect:/admin/item-sampah";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        ItemSampah item = itemSampahService.getById(id);
        model.addAttribute("item", item);
        model.addAttribute("daftarKategori", kategoriService.getSemua());
        model.addAttribute("isEdit", true);
        return "admin-item-sampah-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id,
                             @RequestParam String namaItem,
                             @RequestParam(required = false) String deskripsi,
                             @RequestParam(required = false) String instruksiPenanganan,
                             @RequestParam Long idKategori) {
        itemSampahService.update(id, namaItem, deskripsi, instruksiPenanganan, idKategori);
        return "redirect:/admin/item-sampah";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id) {
        itemSampahService.hapus(id);
        return "redirect:/admin/item-sampah";
    }
}
