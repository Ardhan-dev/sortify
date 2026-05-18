package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.LaporanSampah;
import com.SortifyTeam.Sortify.model.Pembayaran;
import com.SortifyTeam.Sortify.repository.PembayaranRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PembayaranService {

    private final PembayaranRepository pembayaranRepo;

    public PembayaranService(PembayaranRepository pembayaranRepo) {
        this.pembayaranRepo = pembayaranRepo;
    }

    @Transactional
    public Pembayaran buatPembayaran(LaporanSampah laporan) {
        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setLaporan(laporan);
        pembayaran.setTotalPembayaran(laporan.getBerat() * 5000);
        pembayaran.setStatusPembayaran(Pembayaran.StatusPembayaran.PENDING);
        return pembayaranRepo.save(pembayaran);
    }

    public Pembayaran getPembayaranByLaporan(LaporanSampah laporan) {
        return pembayaranRepo.findByLaporan(laporan)
                .orElse(null);
    }

    @Transactional
    public void bayar(Long laporanId, String metodePembayaran) {
        LaporanSampah laporan = new LaporanSampah();
        laporan.setId(laporanId);
        Pembayaran pembayaran = pembayaranRepo.findByLaporan(laporan)
                .orElseThrow(() -> new RuntimeException("Pembayaran tidak ditemukan"));
        pembayaran.setMetodePembayaran(metodePembayaran);
        pembayaran.setStatusPembayaran(Pembayaran.StatusPembayaran.BERHASIL);
        pembayaran.setPaidAt(LocalDateTime.now());
        pembayaranRepo.save(pembayaran);
    }

    public long countByStatus(Pembayaran.StatusPembayaran status) {
        return pembayaranRepo.countByStatusPembayaran(status);
    }
}


