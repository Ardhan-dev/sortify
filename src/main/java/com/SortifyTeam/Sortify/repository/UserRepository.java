package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsername(String username);
    List<User> findByRole(User.Role role);
    List<User> findTop5ByOrderByTotalPointsDesc();
    long countByRole(User.Role role);
}



