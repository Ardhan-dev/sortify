package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.PointHistory;
import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByWargaOrderByCreatedAtDesc(User warga);
}


