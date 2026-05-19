package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KategoriSampahService {

    private final KategoriSampahRepository kategoriRepo;

    public KategoriSampahService(KategoriSampahRepository kategoriRepo) {
        this.kategoriRepo = kategoriRepo;
    }

    public List<KategoriSampah> getSemua() {
        return kategoriRepo.findAll();
    }

    public KategoriSampah getById(Long id) {
        return kategoriRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan: " + id));
    }

    @Transactional
    public KategoriSampah simpan(String namaKategori, Integer poinPerKg, String instruksiPenanganan) {
        KategoriSampah k = new KategoriSampah();
        k.setNamaKategori(namaKategori);
        k.setPoinPerKg(poinPerKg);
        k.setInstruksiPenanganan(instruksiPenanganan);
        return kategoriRepo.save(k);
    }

    @Transactional
    public KategoriSampah update(Long id, String namaKategori, Integer poinPerKg, String instruksiPenanganan) {
        KategoriSampah k = getById(id);
        k.setNamaKategori(namaKategori);
        k.setPoinPerKg(poinPerKg);
        k.setInstruksiPenanganan(instruksiPenanganan);
        return kategoriRepo.save(k);
    }

    @Transactional
    public void hapus(Long id) {
        kategoriRepo.deleteById(id);
    }

    public long count() {
        return kategoriRepo.count();
    }
}
