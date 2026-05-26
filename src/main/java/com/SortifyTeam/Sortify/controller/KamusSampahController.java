package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.service.ItemSampahService;
import com.SortifyTeam.Sortify.service.KategoriSampahService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
public class KamusSampahController {

    private final KategoriSampahService kategoriService;
    private final ItemSampahService itemSampahService;

    public KamusSampahController(KategoriSampahService kategoriService,
                                  ItemSampahService itemSampahService) {
        this.kategoriService = kategoriService;
        this.itemSampahService = itemSampahService;
    }

    @GetMapping("/kamus-sampah")
    public String kamusSampah(Model model,
                              @RequestParam(required = false) String search) {
        List<KategoriSampah> daftarKategori = kategoriService.getSemua();
        List<ItemSampah> daftarItem = itemSampahService.cari(search);

        model.addAttribute("daftarKategori", daftarKategori);
        model.addAttribute("daftarItem", daftarItem);
        model.addAttribute("totalItem", daftarItem.size());
        model.addAttribute("search", search);
        return "kamus-sampah";
    }
}
