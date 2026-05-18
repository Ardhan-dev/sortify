package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.LaporanSampah;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.LaporanSampahRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LaporanService {

    private final LaporanSampahRepository laporanRepo;

    public LaporanService(LaporanSampahRepository laporanRepo) {
        this.laporanRepo = laporanRepo;
    }

    @Transactional
    public LaporanSampah buatLaporan(User warga, String jenisSampah, double berat,
                                     String alamatLengkap, String catatan) {
        LaporanSampah laporan = new LaporanSampah();
        laporan.setWarga(warga);
        laporan.setJenisSampah(LaporanSampah.JenisSampah.valueOf(jenisSampah.toUpperCase()));
        laporan.setBerat(berat);
        laporan.setAlamatLengkap(alamatLengkap);
        laporan.setCatatan(catatan);
        laporan.setStatus(LaporanSampah.StatusLaporan.MENUNGGU);
        laporan.setCreatedAt(LocalDateTime.now());
        return laporanRepo.save(laporan);
    }

    public List<LaporanSampah> getLaporanByWarga(User warga) {
        return laporanRepo.findByWargaOrderByCreatedAtDesc(warga);
    }

    public List<LaporanSampah> getLaporanByStatus(LaporanSampah.StatusLaporan status) {
        return laporanRepo.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<LaporanSampah> getLaporanByStatuses(List<LaporanSampah.StatusLaporan> statuses) {
        return laporanRepo.findByStatusInOrderByCreatedAtDesc(statuses);
    }

    public LaporanSampah getLaporanById(Long id) {
        return laporanRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Laporan tidak ditemukan: " + id));
    }

    @Transactional
    public void accLaporan(Long laporanId, User petugas) {
        LaporanSampah laporan = getLaporanById(laporanId);
        laporan.setPetugas(petugas);
        laporan.setStatus(LaporanSampah.StatusLaporan.MENUNGGU_PEMBAYARAN);
        laporanRepo.save(laporan);
    }

    @Transactional
    public void tolakLaporan(Long laporanId) {
        LaporanSampah laporan = getLaporanById(laporanId);
        laporan.setStatus(LaporanSampah.StatusLaporan.DITOLAK);
        laporanRepo.save(laporan);
    }

    @Transactional
    public void prosesLaporan(Long laporanId) {
        LaporanSampah laporan = getLaporanById(laporanId);
        laporan.setStatus(LaporanSampah.StatusLaporan.DIPROSES);
        laporanRepo.save(laporan);
    }

    @Transactional
    public void selesaikanLaporan(Long laporanId) {
        LaporanSampah laporan = getLaporanById(laporanId);
        laporan.setStatus(LaporanSampah.StatusLaporan.SELESAI);
        laporanRepo.save(laporan);
    }

    public long countByStatus(LaporanSampah.StatusLaporan status) {
        return laporanRepo.countByStatus(status);
    }

    public long countTotal() {
        return laporanRepo.count();
    }

    public List<LaporanSampah> getSemuaLaporan() {
        return laporanRepo.findAll();
    }
}


