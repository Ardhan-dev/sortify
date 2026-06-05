package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.LogAktivitas;
import com.SortifyTeam.Sortify.repository.LogAktivitasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogAktivitasService {

    private final LogAktivitasRepository logRepo;

    public LogAktivitasService(LogAktivitasRepository logRepo) {
        this.logRepo = logRepo;
    }

    @Transactional
    public void catatAktivitas(String username, String role, String aksi, String deskripsi) {
        LogAktivitas log = new LogAktivitas();
        log.setUsername(username);
        log.setRole(role);
        log.setAksi(aksi);
        log.setDeskripsi(deskripsi);
        log.setWaktu(LocalDateTime.now());
        logRepo.save(log);
    }

    public List<LogAktivitas> getSemuaLog() {
        return logRepo.findAllByOrderByWaktuDesc();
    }

    public long countTotal() {
        return logRepo.count();
    }

    public List<LogAktivitas> getLogByFilter(String username, String role, String aksi) {
        return logRepo.findByFilters(username, role, aksi);
    }

    public List<String> getDistinctAksi() {
        return logRepo.findDistinctAksi();
    }

    public List<String> getDistinctRole() {
        return logRepo.findDistinctRole();
    }

    public List<String> getDistinctUsername() {
        return logRepo.findDistinctUsername();
    }
}
