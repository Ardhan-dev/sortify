package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.repository.ItemSampahRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemSampahService {

    private final ItemSampahRepository itemSampahRepo;

    public ItemSampahService(ItemSampahRepository itemSampahRepo) {
        this.itemSampahRepo = itemSampahRepo;
    }

    public List<ItemSampah> getAll() {
        return itemSampahRepo.findAllByOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
    }

    public ItemSampah getById(Long id) {
        return itemSampahRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan: " + id));
    }

    public List<ItemSampah> cari(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return itemSampahRepo.findAllByOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
        }
        return itemSampahRepo.findByNamaItemContainingIgnoreCase(keyword.trim());
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
        if (!itemSampahRepo.existsById(id)) {
            throw new RuntimeException("Item tidak ditemukan: " + id);
        }
        itemSampahRepo.deleteById(id);
    }

    public long count() {
        return itemSampahRepo.count();
    }
}
