package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warga")
public class WargaWebController {

    @Autowired
    private WargaRepository wargaRepo;

    // LIST
    @GetMapping
    public String halamanWarga(Model model) {
        model.addAttribute("daftarWarga", wargaRepo.findAll());
        return "warga-view";
    }

    // FORM TAMBAH
    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("warga", new Warga());
        return "warga-form";
    }

    // SIMPAN TAMBAH
    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Warga warga) {
        wargaRepo.save(warga);
        return "redirect:/warga";
    }

    // FORM EDIT
    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        Warga warga = wargaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan: " + id));
        model.addAttribute("warga", warga);
        return "warga-form";
    }

    // SIMPAN EDIT
    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id, @ModelAttribute Warga warga) {
        warga.setIdWarga(id);
        wargaRepo.save(warga);
        return "redirect:/warga";
    }

    // HAPUS
    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id) {
        wargaRepo.deleteById(id);
        return "redirect:/warga";
    }
}