package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarningRepository extends JpaRepository<Warning, Long> {
    List<Warning> findByTargetUserOrderByCreatedAtDesc(User targetUser);
    List<Warning> findAllByOrderByCreatedAtDesc();
    long countByTargetUserAndType(User targetUser, Warning.WarningType type);
    long countByTargetUser(User targetUser);
}
