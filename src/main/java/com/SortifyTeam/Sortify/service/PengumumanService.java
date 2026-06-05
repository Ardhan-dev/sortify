package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.Pengumuman;
import com.SortifyTeam.Sortify.repository.PengumumanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PengumumanService {

    private final PengumumanRepository repo;

    public PengumumanService(PengumumanRepository repo) {
        this.repo = repo;
    }

    public List<Pengumuman> getAktif() {
        return repo.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public List<Pengumuman> getSemua() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public Pengumuman getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Pengumuman tidak ditemukan"));
    }

    @Transactional
    public Pengumuman simpan(String judul, String konten) {
        Pengumuman p = new Pengumuman();
        p.setJudul(judul);
        p.setKonten(konten);
        return repo.save(p);
    }

    @Transactional
    public Pengumuman update(Long id, String judul, String konten, boolean isActive) {
        Pengumuman p = getById(id);
        p.setJudul(judul);
        p.setKonten(konten);
        p.setActive(isActive);
        return repo.save(p);
    }

    @Transactional
    public void hapus(Long id) {
        repo.deleteById(id);
    }
}
