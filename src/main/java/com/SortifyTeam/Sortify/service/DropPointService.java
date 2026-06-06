package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.DropPoint;
import com.SortifyTeam.Sortify.repository.DropPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DropPointService {

    private final DropPointRepository repo;

    public DropPointService(DropPointRepository repo) {
        this.repo = repo;
    }

    public List<DropPoint> getAll() {
        return repo.findByAktifTrueOrderByNamaAsc();
    }

    public List<DropPoint> getAllWithInactive() {
        return repo.findAll();
    }

    public DropPoint getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("DropPoint tidak ditemukan: " + id));
    }

    @Transactional
    public DropPoint simpan(String nama, String alamat, Double latitude, Double longitude) {
        if (nama == null || nama.isBlank()) throw new IllegalArgumentException("Nama drop point wajib diisi");

        DropPoint dp = new DropPoint();
        dp.setNama(nama.trim());
        dp.setAlamat(alamat != null ? alamat.trim() : null);
        dp.setLatitude(latitude);
        dp.setLongitude(longitude);
        dp.setAktif(true);
        return repo.save(dp);
    }

    @Transactional
    public DropPoint update(Long id, String nama, String alamat, Double latitude, Double longitude, Boolean aktif) {
        DropPoint dp = getById(id);
        if (nama != null && !nama.isBlank()) dp.setNama(nama.trim());
        if (alamat != null) dp.setAlamat(alamat.trim());
        if (latitude != null) dp.setLatitude(latitude);
        if (longitude != null) dp.setLongitude(longitude);
        if (aktif != null) dp.setAktif(aktif);
        return repo.save(dp);
    }

    @Transactional
    public void hapus(Long id) {
        DropPoint dp = getById(id);
        dp.setAktif(false);
        repo.save(dp);
    }
}
