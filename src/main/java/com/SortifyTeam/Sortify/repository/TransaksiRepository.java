package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.model.Warga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    List<Transaksi> findByStatusOrderByTanggalTransaksiDesc(Transaksi.StatusTransaksi status);
    List<Transaksi> findByStatusInOrderByTanggalTransaksiDesc(List<Transaksi.StatusTransaksi> statuses);
    List<Transaksi> findByWargaOrderByTanggalTransaksiDesc(Warga warga);
    List<Transaksi> findByWargaAndStatusOrderByTanggalTransaksiDesc(Warga warga, Transaksi.StatusTransaksi status);
    List<Transaksi> findByStatus(Transaksi.StatusTransaksi status);

    @Query("SELECT FUNCTION('MONTH', t.tanggalTransaksi), COALESCE(SUM(t.totalBerat), 0) " +
           "FROM Transaksi t WHERE t.status = 'SELESAI' AND FUNCTION('YEAR', t.tanggalTransaksi) = :year " +
           "GROUP BY FUNCTION('MONTH', t.tanggalTransaksi) ORDER BY FUNCTION('MONTH', t.tanggalTransaksi)")
    List<Object[]> getMonthlyBerat(@Param("year") int year);
}
