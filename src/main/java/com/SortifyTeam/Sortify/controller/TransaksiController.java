package com.SortifyTeam.Sortify.controller; 

import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; // Menggunakan * agar semua anotasi (GetMapping, PostMapping, dll) terangkut
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transaksi")
public class TransaksiController {

    @Autowired
    private TransaksiRepository transaksiRepo;

    @GetMapping
    public List<Transaksi> getRiwayat() {
        return transaksiRepo.findAll(); 
    }

    @PostMapping
    public Transaksi catatSetoran(@RequestBody Transaksi transaksi) {
        // Set tanggal otomatis ke waktu sekarang sesuai logika sistem
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        return transaksiRepo.save(transaksi);
    }
}