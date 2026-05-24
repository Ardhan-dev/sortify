package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/warga")
public class WargaWebController {

    private final UserRepository userRepo;
    private final WargaRepository wargaRepo;
    private final PasswordEncoder passwordEncoder;

    public WargaWebController(UserRepository userRepo, WargaRepository wargaRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.wargaRepo = wargaRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String halamanWarga(Model model) {
        List<User> users = userRepo.findByRole(User.Role.WARGA);
        model.addAttribute("daftarWarga", users);
        return "warga-view";
    }

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("warga", new Warga());
        model.addAttribute("isNew", true);
        return "warga-form";
    }

    @PostMapping("/tambah")
    public String simpanTambah(@ModelAttribute Warga warga) {
        User user = new User();
        user.setUsername(warga.getUsername());
        user.setPassword(passwordEncoder.encode(warga.getPassword()));
        user.setFullName(warga.getNama());
        user.setRole(User.Role.WARGA);
        user.setTotalPoints(0);
        userRepo.save(user);

        warga.setUser(user);
        warga.setPassword(user.getPassword());
        wargaRepo.save(warga);
        return "redirect:/admin/warga";
    }

    @GetMapping("/edit/{id}")
    public String formEdit(@PathVariable Long id, Model model) {
        Warga warga = wargaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan: " + id));
        model.addAttribute("warga", warga);
        model.addAttribute("isNew", false);
        return "warga-form";
    }

    @PostMapping("/edit/{id}")
    public String simpanEdit(@PathVariable Long id, @ModelAttribute Warga warga) {
        Warga existing = wargaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan: " + id));

        existing.setNama(warga.getNama());
        existing.setAlamat(warga.getAlamat());
        existing.setNoHp(warga.getNoHp());

        if (warga.getPassword() != null && !warga.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(warga.getPassword()));

            User user = existing.getUser();
            if (user != null) {
                user.setPassword(existing.getPassword());
                user.setFullName(warga.getNama());
                userRepo.save(user);
            }
        }

        wargaRepo.save(existing);
        return "redirect:/admin/warga";
    }

    @GetMapping("/hapus/{id}")
    public String hapus(@PathVariable Long id) {
        Warga warga = wargaRepo.findById(id).orElse(null);
        if (warga != null) {
            User user = warga.getUser();
            wargaRepo.deleteById(id);
            if (user != null) {
                userRepo.delete(user);
            }
        }
        return "redirect:/admin/warga";
    }
}
