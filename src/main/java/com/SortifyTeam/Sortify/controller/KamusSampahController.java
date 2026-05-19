package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.service.KategoriSampahService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
public class KamusSampahController {

    private final KategoriSampahService kategoriService;

    public KamusSampahController(KategoriSampahService kategoriService) {
        this.kategoriService = kategoriService;
    }

    @GetMapping("/kamus-sampah")
    public String kamusSampah(Model model) {
        log.info("[ACCESS] Pengguna membuka halaman Kamus Sampah");
        List<KategoriSampah> daftar = kategoriService.getSemua();
        model.addAttribute("daftarKategori", daftar);
        model.addAttribute("totalKategori", daftar.size());
        return "kamus-sampah";
    }
}
