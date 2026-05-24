package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.Transaksi;
import com.SortifyTeam.Sortify.model.Warga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    List<Transaksi> findByStatusOrderByTanggalTransaksiDesc(Transaksi.StatusTransaksi status);
    List<Transaksi> findByStatusInOrderByTanggalTransaksiDesc(List<Transaksi.StatusTransaksi> statuses);
    List<Transaksi> findByWargaOrderByTanggalTransaksiDesc(Warga warga);
    List<Transaksi> findByWargaAndStatusOrderByTanggalTransaksiDesc(Warga warga, Transaksi.StatusTransaksi status);
}