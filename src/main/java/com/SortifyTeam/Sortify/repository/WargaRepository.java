package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WargaRepository extends JpaRepository<Warga, Long> {
    Optional<Warga> findByUsername(String username);
    Optional<Warga> findByUser(User user);
    List<Warga> findAll();
}