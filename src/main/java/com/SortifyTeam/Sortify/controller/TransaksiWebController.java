package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.DetailTransaksi;
import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.repository.DetailTransaksiRepository;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/transaksi")
public class TransaksiWebController {

    @Autowired private TransaksiRepository transaksiRepo;
    @Autowired private WargaRepository wargaRepo;
    @Autowired private StaffRepository staffRepo;
    @Autowired private KategoriSampahRepository kategoriRepo;
    @Autowired private DetailTransaksiRepository detailRepo;

    // LIST
    @GetMapping
    @Transactional
    public String halamanTransaksi(Model model) {
        List<Transaksi> daftar = transaksiRepo.findAll();
        double totalBerat = daftar.stream()
                .filter(t -> t.getTotalBerat() != null)
                .mapToDouble(Transaksi::getTotalBerat).sum();
        double totalPoin = daftar.stream()
                .filter(t -> t.getTotalPoin() != null)
                .mapToDouble(Transaksi::getTotalPoin).sum();
        model.addAttribute("daftarTransaksi", daftar);
        model.addAttribute("totalBerat", totalBerat);
        model.addAttribute("totalPoin", totalPoin);
        return "transaksi-view";
    }

    // FORM TAMBAH
    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("transaksi", new Transaksi());
        model.addAttribute("daftarWarga", wargaRepo.findAll());
        model.addAttribute("daftarStaff", staffRepo.findAll());
        model.addAttribute("daftarKategori", kategoriRepo.findAll());
        return "transaksi-form";
    }

    // SIMPAN TAMBAH
    @PostMapping("/tambah")
    @Transactional
    public String simpanTambah(@RequestParam Long idWarga,
                               @RequestParam Long idStaff,
                               @RequestParam List<Long> idKategori,
                               @RequestParam List<Double> beratKategori) {
        Transaksi transaksi = new Transaksi();
        transaksi.setWarga(wargaRepo.findById(idWarga)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan")));
        transaksi.setStaff(staffRepo.findById(idStaff)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan")));
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        transaksi.setDetails(new ArrayList<>());
        transaksiRepo.save(transaksi);

        double totalBerat = 0;
        double totalPoin = 0;

        for (int i = 0; i < idKategori.size(); i++) {
            if (beratKategori.get(i) == null || beratKategori.get(i) <= 0) continue;
            KategoriSampah kategori = kategoriRepo.findById(idKategori.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));
            double berat = beratKategori.get(i);
            int subtotal = (int) (berat * kategori.getPoinPerKg());

            DetailTransaksi detail = new DetailTransaksi();
            detail.setTransaksi(transaksi);
            detail.setKategori(kategori);
            detail.setBerat(berat);
            detail.setSubtotalPoin(subtotal);
            detailRepo.save(detail);

            totalBerat += berat;
            totalPoin += subtotal;
        }

        transaksi.setTotalBerat(totalBerat);
        transaksi.setTotalPoin(totalPoin);
        transaksiRepo.save(transaksi);

        return "redirect:/transaksi";
    }

    // FORM EDIT
    @GetMapping("/edit/{id}")
    @Transactional
    public String formEdit(@PathVariable Long id, Model model) {
        Transaksi transaksi = transaksiRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));
        model.addAttribute("transaksi", transaksi);
        model.addAttribute("daftarWarga", wargaRepo.findAll());
        model.addAttribute("daftarStaff", staffRepo.findAll());
        model.addAttribute("daftarKategori", kategoriRepo.findAll());
        return "transaksi-form";
    }

    // SIMPAN EDIT
    @PostMapping("/edit/{id}")
    @Transactional
    public String simpanEdit(@PathVariable Long id,
                             @RequestParam Long idWarga,
                             @RequestParam Long idStaff,
                             @RequestParam List<Long> idKategori,
                             @RequestParam List<Double> beratKategori) {
        Transaksi transaksi = transaksiRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));
        transaksi.setWarga(wargaRepo.findById(idWarga)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan")));
        transaksi.setStaff(staffRepo.findById(idStaff)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan")));

        detailRepo.deleteAll(transaksi.getDetails());
        transaksi.getDetails().clear();

        double totalBerat = 0;
        double totalPoin = 0;

        for (int i = 0; i < idKategori.size(); i++) {
            if (beratKategori.get(i) == null || beratKategori.get(i) <= 0) continue;
            KategoriSampah kategori = kategoriRepo.findById(idKategori.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));
            double berat = beratKategori.get(i);
            int subtotal = (int) (berat * kategori.getPoinPerKg());

            DetailTransaksi detail = new DetailTransaksi();
            detail.setTransaksi(transaksi);
            detail.setKategori(kategori);
            detail.setBerat(berat);
            detail.setSubtotalPoin(subtotal);
            detailRepo.save(detail);

            totalBerat += berat;
            totalPoin += subtotal;
        }

        transaksi.setTotalBerat(totalBerat);
        transaksi.setTotalPoin(totalPoin);
        transaksiRepo.save(transaksi);
        return "redirect:/transaksi";
    }

    // HAPUS
    @GetMapping("/hapus/{id}")
    @Transactional
    public String hapus(@PathVariable Long id) {
        transaksiRepo.deleteById(id);
        return "redirect:/transaksi";
    }
}