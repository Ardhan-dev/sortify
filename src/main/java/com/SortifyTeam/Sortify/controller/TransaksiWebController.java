package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.repository.TransaksiRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/transaksi")
public class TransaksiWebController {

    private final TransaksiRepository transaksiRepo;
    private final WargaRepository wargaRepo;
    private final StaffRepository staffRepo;
    private final KategoriSampahRepository kategoriRepo;

    public TransaksiWebController(TransaksiRepository transaksiRepo,
                                   WargaRepository wargaRepo,
                                   StaffRepository staffRepo,
                                   KategoriSampahRepository kategoriRepo) {
        this.transaksiRepo = transaksiRepo;
        this.wargaRepo = wargaRepo;
        this.staffRepo = staffRepo;
        this.kategoriRepo = kategoriRepo;
    }

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

    @GetMapping("/tambah")
    public String formTambah(Model model) {
        model.addAttribute("transaksi", new Transaksi());
        model.addAttribute("daftarWarga", wargaRepo.findAll());
        model.addAttribute("daftarStaff", staffRepo.findAll());
        model.addAttribute("daftarKategori", kategoriRepo.findAll());
        return "transaksi-form";
    }

    @PostMapping("/tambah")
    @Transactional
    public String simpanTambah(@RequestParam Long idWarga,
                                @RequestParam Long idStaff,
                                @RequestParam List<Long> idKategori,
                                @RequestParam List<Double> beratKategori,
                                @RequestParam(required = false, defaultValue = "") String detail) {
        Transaksi transaksi = new Transaksi();
        transaksi.setWarga(wargaRepo.findById(idWarga)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan")));
        transaksi.setStaff(staffRepo.findById(idStaff)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan")));
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        transaksi.setStatus(Transaksi.StatusTransaksi.PENDING);
        transaksi.setDetail(detail);
        transaksiRepo.save(transaksi);

        double totalBerat = 0;
        double totalPoin = 0;

        for (int i = 0; i < idKategori.size(); i++) {
            if (beratKategori.get(i) == null || beratKategori.get(i) <= 0) continue;
            KategoriSampah kategori = kategoriRepo.findById(idKategori.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));
            double berat = beratKategori.get(i);
            int subtotal = (int) (berat * kategori.getPoinPerKg());

            totalBerat += berat;
            totalPoin += subtotal;
        }

        transaksi.setTotalBerat(totalBerat);
        transaksi.setTotalPoin(totalPoin);
        transaksi.setBeratSampah(totalBerat);
        transaksiRepo.save(transaksi);

        return "redirect:/admin/transaksi";
    }

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

    @PostMapping("/edit/{id}")
    @Transactional
    public String simpanEdit(@PathVariable Long id,
                              @RequestParam Long idWarga,
                              @RequestParam Long idStaff,
                              @RequestParam List<Long> idKategori,
                              @RequestParam List<Double> beratKategori,
                              @RequestParam(required = false, defaultValue = "") String detail) {
        Transaksi transaksi = transaksiRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan"));
        transaksi.setWarga(wargaRepo.findById(idWarga)
                .orElseThrow(() -> new IllegalArgumentException("Warga tidak ditemukan")));
        transaksi.setStaff(staffRepo.findById(idStaff)
                .orElseThrow(() -> new IllegalArgumentException("Staff tidak ditemukan")));
        transaksi.setDetail(detail);

        double totalBerat = 0;
        double totalPoin = 0;

        for (int i = 0; i < idKategori.size(); i++) {
            if (beratKategori.get(i) == null || beratKategori.get(i) <= 0) continue;
            KategoriSampah kategori = kategoriRepo.findById(idKategori.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Kategori tidak ditemukan"));
            double berat = beratKategori.get(i);
            int subtotal = (int) (berat * kategori.getPoinPerKg());

            totalBerat += berat;
            totalPoin += subtotal;
        }

        transaksi.setTotalBerat(totalBerat);
        transaksi.setTotalPoin(totalPoin);
        transaksi.setBeratSampah(totalBerat);
        transaksiRepo.save(transaksi);
        return "redirect:/admin/transaksi";
    }

    @GetMapping("/hapus/{id}")
    @Transactional
    public String hapus(@PathVariable Long id) {
        transaksiRepo.deleteById(id);
        return "redirect:/admin/transaksi";
    }
}
