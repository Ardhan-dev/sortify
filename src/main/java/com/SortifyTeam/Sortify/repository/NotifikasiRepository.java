package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.Notifikasi;
import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotifikasiRepository extends JpaRepository<Notifikasi, Long> {
    List<Notifikasi> findByUserAndIsReadOrderByCreatedAtDesc(User user, boolean isRead);
    List<Notifikasi> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndIsRead(User user, boolean isRead);
}
