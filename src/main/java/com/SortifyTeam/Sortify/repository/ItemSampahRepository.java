package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.model.KategoriSampah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemSampahRepository extends JpaRepository<ItemSampah, Long> {
    List<ItemSampah> findByNamaItemContainingIgnoreCase(String namaItem);
    List<ItemSampah> findByKategoriSampah(KategoriSampah kategori);
    List<ItemSampah> findAllByOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
    List<ItemSampah> findByIsActiveTrueOrderByKategoriSampahNamaKategoriAscNamaItemAsc();
    List<ItemSampah> findByNamaItemContainingIgnoreCaseAndIsActiveTrue(String namaItem);
}
