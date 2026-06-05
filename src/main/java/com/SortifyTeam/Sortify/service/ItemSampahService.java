package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.repository.ItemSampahRepository;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemSampahService {

    private final ItemSampahRepository itemSampahRepo;
    private final KategoriSampahRepository kategoriRepo;

    public ItemSampahService(ItemSampahRepository itemSampahRepo, KategoriSampahRepository kategoriRepo) {
        this.itemSampahRepo = itemSampahRepo;
        this.kategoriRepo = kategoriRepo;
    }

    public List<ItemSampah> getSemua() {
        return itemSampahRepo.findAllByOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
    }

    public List<ItemSampah> getAll() {
        return itemSampahRepo.findByIsActiveTrueOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
    }

    public ItemSampah getById(Long id) {
        return itemSampahRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan: " + id));
    }

    public List<ItemSampah> cari(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return itemSampahRepo.findByIsActiveTrueOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
        }
        return itemSampahRepo.findByNamaItemContainingIgnoreCaseAndIsActiveTrue(keyword.trim());
    }

    @Transactional
    public ItemSampah simpan(String namaItem, String deskripsi, String instruksiPenanganan, KategoriSampah kategori) {
        ItemSampah item = new ItemSampah();
        item.setNamaItem(namaItem);
        item.setDeskripsi(deskripsi);
        item.setInstruksiPenanganan(instruksiPenanganan);
        item.setKategoriSampah(kategori);
        return itemSampahRepo.save(item);
    }

    @Transactional
    public ItemSampah update(Long id, String namaItem, String deskripsi, String instruksiPenanganan, KategoriSampah kategori) {
        ItemSampah item = getById(id);
        item.setNamaItem(namaItem);
        item.setDeskripsi(deskripsi);
        item.setInstruksiPenanganan(instruksiPenanganan);
        item.setKategoriSampah(kategori);
        return itemSampahRepo.save(item);
    }

    @Transactional
    public void hapus(Long id) {
        ItemSampah item = getById(id);
        item.setIsActive(false);
        itemSampahRepo.save(item);
    }

    public long count() {
        return itemSampahRepo.count();
    }
}
