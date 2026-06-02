package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.model.Warga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    List<Transaksi> findByStatusOrderByTanggalTransaksiDesc(Transaksi.StatusTransaksi status);
    List<Transaksi> findByStatusInOrderByTanggalTransaksiDesc(List<Transaksi.StatusTransaksi> statuses);
    Page<Transaksi> findByStatusIn(List<Transaksi.StatusTransaksi> statuses, Pageable pageable);
    List<Transaksi> findByWargaOrderByTanggalTransaksiDesc(Warga warga);
    List<Transaksi> findByWargaAndStatusOrderByTanggalTransaksiDesc(Warga warga, Transaksi.StatusTransaksi status);
    List<Transaksi> findByStatus(Transaksi.StatusTransaksi status);
    List<Transaksi> findByStaff(Staff staff);

    long countByStatus(Transaksi.StatusTransaksi status);

    @Query("SELECT COALESCE(SUM(t.totalBerat), 0) FROM Transaksi t")
    double sumTotalBerat();

    @Query("SELECT COALESCE(SUM(t.totalBerat), 0) FROM Transaksi t WHERE t.warga.idWarga = :wargaId AND t.status = 'SELESAI'")
    double sumTotalBeratByWarga(@Param("wargaId") Long wargaId);

    @Query("SELECT FUNCTION('MONTH', t.tanggalTransaksi), COALESCE(SUM(t.totalBerat), 0) " +
           "FROM Transaksi t WHERE t.status = 'SELESAI' AND FUNCTION('YEAR', t.tanggalTransaksi) = :year " +
           "GROUP BY FUNCTION('MONTH', t.tanggalTransaksi) ORDER BY FUNCTION('MONTH', t.tanggalTransaksi)")
    List<Object[]> getMonthlyBerat(@Param("year") int year);

    @Query("SELECT FUNCTION('MONTH', td.transaksi.tanggalTransaksi), COALESCE(SUM(td.beratFinal), 0) " +
           "FROM TransaksiDetail td WHERE td.transaksi.status = 'SELESAI' AND td.beratFinal IS NOT NULL " +
           "AND FUNCTION('YEAR', td.transaksi.tanggalTransaksi) = :year " +
           "AND (:wargaId IS NULL OR td.transaksi.warga.idWarga = :wargaId) " +
           "AND (:kategoriId IS NULL OR td.kategoriSampah.idKategori = :kategoriId) " +
           "GROUP BY FUNCTION('MONTH', td.transaksi.tanggalTransaksi) ORDER BY FUNCTION('MONTH', td.transaksi.tanggalTransaksi)")
    List<Object[]> getMonthlyBeratFiltered(@Param("year") int year,
                                           @Param("wargaId") Long wargaId,
                                           @Param("kategoriId") Long kategoriId);
}
