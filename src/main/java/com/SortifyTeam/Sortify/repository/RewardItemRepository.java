package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.RewardItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardItemRepository extends JpaRepository<RewardItem, Long> {
    List<RewardItem> findByStockGreaterThanAndIsActiveTrue(int stock);

    @Query(value = "SELECT * FROM reward_item WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<RewardItem> findByIdWithLock(@Param("id") Long id);
}


