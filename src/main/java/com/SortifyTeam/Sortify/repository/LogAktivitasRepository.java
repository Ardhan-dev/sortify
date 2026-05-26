package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.LogAktivitas;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogAktivitasRepository extends JpaRepository<LogAktivitas, Long> {
    List<LogAktivitas> findAllByOrderByWaktuDesc();
}
