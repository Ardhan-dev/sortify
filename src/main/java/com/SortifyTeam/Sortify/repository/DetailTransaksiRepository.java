package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.DetailTransaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailTransaksiRepository extends JpaRepository<DetailTransaksi, Long> {
}