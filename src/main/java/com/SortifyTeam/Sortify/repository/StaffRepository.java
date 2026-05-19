package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Staff findByNip(String nip);
    Optional<Staff> findByUsername(String username);
    Optional<Staff> findByUser(User user);
}