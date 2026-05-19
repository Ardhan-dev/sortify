package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.model.LaporanSampah;
import com.SortifyTeam.Sortify.model.Pembayaran;
import com.SortifyTeam.Sortify.model.PointHistory;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.repository.LaporanSampahRepository;
import com.SortifyTeam.Sortify.repository.PembayaranRepository;
import com.SortifyTeam.Sortify.repository.PointHistoryRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LaporanService {

    private final LaporanSampahRepository laporanRepo;
    private final KategoriSampahRepository kategoriRepo;
    private final UserRepository userRepo;
    private final PointHistoryRepository pointHistoryRepo;
    private final PembayaranRepository pembayaranRepo;

    public LaporanService(LaporanSampahRepository laporanRepo,
                          KategoriSampahRepository kategoriRepo,
                          UserRepository userRepo,
                          PointHistoryRepository pointHistoryRepo,
                          PembayaranRepository pembayaranRepo) {
        this.laporanRepo = laporanRepo;
        this.kategoriRepo = kategoriRepo;
        this.userRepo = userRepo;
        this.pointHistoryRepo = pointHistoryRepo;
        this.pembayaranRepo = pembayaranRepo;
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
        laporan.setStatus(LaporanSampah.StatusLaporan.DIPROSES);
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
    public void selesaikanDenganFoto(Long laporanId, Double beratFinal, String fotoBukti) {
        LaporanSampah laporan = getLaporanById(laporanId);
        User warga = laporan.getWarga();
        if (warga == null) {
            throw new RuntimeException("Warga tidak ditemukan untuk laporan #" + laporanId);
        }

        laporan.setBeratFinal(beratFinal);

        int poin = hitungPoin(laporan.getJenisSampah(), beratFinal);
        warga.setTotalPoints(warga.getTotalPoints() + poin);
        userRepo.save(warga);

        PointHistory history = new PointHistory();
        history.setWarga(warga);
        history.setAmount(poin);
        history.setType(PointHistory.PointType.EARN);
        history.setDescription("Poin laporan #" + laporanId + " (" + laporan.getJenisSampah() + " " + beratFinal + " kg)");
        pointHistoryRepo.save(history);

        laporan.setFotoBukti(fotoBukti);
        laporan.setStatus(LaporanSampah.StatusLaporan.SELESAI);
        laporanRepo.save(laporan);

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setLaporan(laporan);
        pembayaran.setTotalPembayaran(beratFinal * 5000);
        pembayaran.setMetodePembayaran("Tunai");
        pembayaran.setStatusPembayaran(Pembayaran.StatusPembayaran.BERHASIL);
        pembayaran.setPaidAt(LocalDateTime.now());
        pembayaranRepo.save(pembayaran);
    }

    private int hitungPoin(LaporanSampah.JenisSampah jenisSampah, double berat) {
        if (jenisSampah == null) return 0;
        String namaKategori = jenisSampah.name();
        KategoriSampah kategori = kategoriRepo.findByNamaKategoriIgnoreCase(namaKategori).orElse(null);
        if (kategori == null || kategori.getPoinPerKg() == null) {
            return 0;
        }
        return (int) (berat * kategori.getPoinPerKg());
    }

    public List<LaporanSampah> getLaporanSelesai() {
        return laporanRepo.findByStatusOrderByCreatedAtDesc(LaporanSampah.StatusLaporan.SELESAI);
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


