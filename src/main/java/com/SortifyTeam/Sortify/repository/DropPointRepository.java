package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.DropPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DropPointRepository extends JpaRepository<DropPoint, Long> {
    List<DropPoint> findByAktifTrueOrderByNamaAsc();
}
