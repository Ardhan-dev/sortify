package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.TransaksiDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaksiDetailRepository extends JpaRepository<TransaksiDetail, Long> {
}
