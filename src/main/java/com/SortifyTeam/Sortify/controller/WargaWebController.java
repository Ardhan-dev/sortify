package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/warga")
public class WargaWebController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private WargaRepository wargaRepo;

    @GetMapping
    public String halamanWarga(Model model) {
        List<User> users = userRepo.findByRole(User.Role.WARGA);
        model.addAttribute("daftarWarga", users);
        return "warga-view";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("warga", new Warga());
        return "warga-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Warga warga) {
        wargaRepo.save(warga);
        return "redirect:/admin/warga";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        Warga warga = wargaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan: " + id));
        model.addAttribute("warga", warga);
        return "warga-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id, @ModelAttribute Warga warga) {
        warga.setIdWarga(id);
        wargaRepo.save(warga);
        return "redirect:/admin/warga";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id) {
        wargaRepo.deleteById(id);
        return "redirect:/admin/warga";
    }
}
