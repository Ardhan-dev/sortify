package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.repository.ItemSampahRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemSampahService {

    private final ItemSampahRepository itemSampahRepo;

    public ItemSampahService(ItemSampahRepository itemSampahRepo) {
        this.itemSampahRepo = itemSampahRepo;
    }

    public List<ItemSampah> cari(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return itemSampahRepo.findAllByOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
        }
        return itemSampahRepo.findByNamaItemContainingIgnoreCase(keyword.trim());
    }

    public long count() {
        return itemSampahRepo.count();
    }
}
