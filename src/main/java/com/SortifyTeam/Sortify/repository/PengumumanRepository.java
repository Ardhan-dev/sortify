package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.Pengumuman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PengumumanRepository extends JpaRepository<Pengumuman, Long> {
    List<Pengumuman> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Pengumuman> findAllByOrderByCreatedAtDesc();
}
