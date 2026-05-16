// 1. Deklarasi Package (HARUS Sesuai folder tempat file berada)
package com.SortifyTeam.Sortify.controller; 

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

// 3. ISI CLASS
@RestController
@RequestMapping("/api/kategori")
public class KategoriController {

    @Autowired
    private KategoriSampahRepository kategoriRepo;

    @GetMapping
    public List<KategoriSampah> getKamusPintar() {
        // Mengambil daftar kategori dari database MySQL[cite: 1, 2]
        return kategoriRepo.findAll(); 
    }
}