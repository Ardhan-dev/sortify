package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KategoriSampahRepository extends JpaRepository<KategoriSampah, Long> {
    java.util.Optional<KategoriSampah> findByNamaKategoriIgnoreCase(String namaKategori);
}