package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.LaporanSampah;
import com.SortifyTeam.Sortify.model.Pembayaran;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PembayaranRepository extends JpaRepository<Pembayaran, Long> {
    Optional<Pembayaran> findByLaporan(LaporanSampah laporan);
    long countByStatusPembayaran(Pembayaran.StatusPembayaran status);
}


