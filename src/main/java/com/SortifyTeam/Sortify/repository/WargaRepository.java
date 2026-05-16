package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.Warga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WargaRepository extends JpaRepository<Warga, Long> {
    Optional<Warga> findByUsername(String username);
}