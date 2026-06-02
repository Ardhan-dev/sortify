package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.repository.ItemSampahRepository;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.repository.TransaksiDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KategoriSampahService {

    private final KategoriSampahRepository kategoriRepo;
    private final TransaksiDetailRepository transaksiDetailRepo;
    private final ItemSampahRepository itemSampahRepo;

    public KategoriSampahService(KategoriSampahRepository kategoriRepo,
                                 TransaksiDetailRepository transaksiDetailRepo,
                                 ItemSampahRepository itemSampahRepo) {
        this.kategoriRepo = kategoriRepo;
        this.transaksiDetailRepo = transaksiDetailRepo;
        this.itemSampahRepo = itemSampahRepo;
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
        KategoriSampah kategori = getById(id);
        if (transaksiDetailRepo.existsByKategoriSampah(kategori)) {
            throw new IllegalStateException(
                    "Kategori '" + kategori.getNamaKategori() + "' masih digunakan di detail transaksi dan tidak dapat dihapus.");
        }
        if (!itemSampahRepo.findByKategoriSampah(kategori).isEmpty()) {
            throw new IllegalStateException(
                    "Kategori '" + kategori.getNamaKategori() + "' masih memiliki item sampah dan tidak dapat dihapus.");
        }
        kategoriRepo.deleteById(id);
    }

    public long count() {
        return kategoriRepo.count();
    }
}
