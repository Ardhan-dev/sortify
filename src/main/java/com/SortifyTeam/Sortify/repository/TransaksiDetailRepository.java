package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.TransaksiDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaksiDetailRepository extends JpaRepository<TransaksiDetail, Long> {

    @Query("SELECT td.kategoriSampah.namaKategori, COALESCE(SUM(td.beratFinal), 0) " +
           "FROM TransaksiDetail td WHERE td.transaksi.status = 'SELESAI' AND td.beratFinal IS NOT NULL AND td.beratFinal > 0 " +
           "GROUP BY td.kategoriSampah.namaKategori")
    List<Object[]> getBeratPerKategori();

    @Query("SELECT td.kategoriSampah.namaKategori, COALESCE(SUM(td.beratFinal), 0) " +
           "FROM TransaksiDetail td WHERE td.transaksi.status = 'SELESAI' AND td.beratFinal IS NOT NULL AND td.beratFinal > 0 " +
           "AND (:wargaId IS NULL OR td.transaksi.warga.idWarga = :wargaId) " +
           "AND (:kategoriId IS NULL OR td.kategoriSampah.idKategori = :kategoriId) " +
           "GROUP BY td.kategoriSampah.namaKategori")
    List<Object[]> getBeratPerKategoriFiltered(@Param("wargaId") Long wargaId,
                                                @Param("kategoriId") Long kategoriId);
}
