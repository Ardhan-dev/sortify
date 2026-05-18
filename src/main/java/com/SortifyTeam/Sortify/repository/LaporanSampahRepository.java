package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.LaporanSampah;
import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaporanSampahRepository extends JpaRepository<LaporanSampah, Long> {
    List<LaporanSampah> findByWargaOrderByCreatedAtDesc(User warga);
    List<LaporanSampah> findByStatusOrderByCreatedAtDesc(LaporanSampah.StatusLaporan status);
    List<LaporanSampah> findByPetugasAndStatusOrderByCreatedAtDesc(User petugas, LaporanSampah.StatusLaporan status);
    List<LaporanSampah> findByStatusInOrderByCreatedAtDesc(List<LaporanSampah.StatusLaporan> statuses);
    long countByStatus(LaporanSampah.StatusLaporan status);
    long count();
}


