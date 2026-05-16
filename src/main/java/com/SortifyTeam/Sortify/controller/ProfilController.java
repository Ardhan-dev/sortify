package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profil")
public class ProfilController {

    private final WargaRepository wargaRepository;

    public ProfilController(WargaRepository wargaRepository) {
        this.wargaRepository = wargaRepository;
    }

    @GetMapping
    public String profilPage(Authentication authentication, Model model) {
        String username = authentication.getName();
        Warga warga = wargaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Warga tidak ditemukan"));
        model.addAttribute("warga", warga);
        return "profil"; // → templates/profil.html
    }
}