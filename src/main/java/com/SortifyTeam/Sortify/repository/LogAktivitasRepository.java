package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.LogAktivitas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LogAktivitasRepository extends JpaRepository<LogAktivitas, Long> {
    List<LogAktivitas> findAllByOrderByWaktuDesc();

    @Query("SELECT l FROM LogAktivitas l WHERE " +
           "(:username IS NULL OR :username = '' OR l.username LIKE %:username%) AND " +
           "(:role IS NULL OR :role = '' OR l.role = :role) AND " +
           "(:aksi IS NULL OR :aksi = '' OR l.aksi = :aksi) " +
           "ORDER BY l.waktu DESC")
    List<LogAktivitas> findByFilters(@Param("username") String username,
                                      @Param("role") String role,
                                      @Param("aksi") String aksi);

    @Query("SELECT DISTINCT l.aksi FROM LogAktivitas l ORDER BY l.aksi")
    List<String> findDistinctAksi();

    @Query("SELECT DISTINCT l.role FROM LogAktivitas l ORDER BY l.role")
    List<String> findDistinctRole();

    @Query("SELECT DISTINCT l.username FROM LogAktivitas l ORDER BY l.username")
    List<String> findDistinctUsername();
}
