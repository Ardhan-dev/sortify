package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.*;
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
    private final TransaksiRepository transaksiRepo;
    private final DetailTransaksiRepository detailTransaksiRepo;
    private final WargaRepository wargaRepo;
    private final StaffRepository staffRepo;

    public LaporanService(LaporanSampahRepository laporanRepo,
                          KategoriSampahRepository kategoriRepo,
                          UserRepository userRepo,
                          PointHistoryRepository pointHistoryRepo,
                          PembayaranRepository pembayaranRepo,
                          TransaksiRepository transaksiRepo,
                          DetailTransaksiRepository detailTransaksiRepo,
                          WargaRepository wargaRepo,
                          StaffRepository staffRepo) {
        this.laporanRepo = laporanRepo;
        this.kategoriRepo = kategoriRepo;
        this.userRepo = userRepo;
        this.pointHistoryRepo = pointHistoryRepo;
        this.pembayaranRepo = pembayaranRepo;
        this.transaksiRepo = transaksiRepo;
        this.detailTransaksiRepo = detailTransaksiRepo;
        this.wargaRepo = wargaRepo;
        this.staffRepo = staffRepo;
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
        laporan.setStatus(LaporanSampah.StatusLaporan.MENUNGGU_PEMBAYARAN);
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
        User warga = laporan.getWarga();
        if (warga == null) {
            throw new RuntimeException("Warga tidak ditemukan untuk laporan #" + laporanId);
        }

        laporan.setPetugas(petugas);
        laporan.setBeratFinal(laporan.getBerat());
        laporan.setStatus(LaporanSampah.StatusLaporan.SELESAI);
        laporanRepo.save(laporan);

        int poin = hitungPoin(laporan.getJenisSampah(), laporan.getBerat());
        warga.setTotalPoints(warga.getTotalPoints() + poin);
        userRepo.save(warga);

        PointHistory history = new PointHistory();
        history.setWarga(warga);
        history.setAmount(poin);
        history.setType(PointHistory.PointType.EARN);
        history.setDescription("Poin ACC laporan #" + laporanId + " (" + laporan.getJenisSampah() + " " + laporan.getBerat() + " kg)");
        pointHistoryRepo.save(history);

        Warga entitasWarga = warga.getWarga();
        if (entitasWarga == null) {
            entitasWarga = wargaRepo.findByUser(warga)
                    .orElseThrow(() -> new RuntimeException("Data warga tidak ditemukan untuk User #" + warga.getId()));
        }
        Staff entitasStaff = petugas.getStaff();
        if (entitasStaff == null) {
            entitasStaff = staffRepo.findByUser(petugas)
                    .orElseThrow(() -> new RuntimeException("Data staff tidak ditemukan untuk User #" + petugas.getId()));
        }

        Transaksi transaksi = new Transaksi();
        transaksi.setWarga(entitasWarga);
        transaksi.setStaff(entitasStaff);
        transaksi.setStatus(Transaksi.StatusTransaksi.SELESAI);
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        transaksi.setTotalBerat(laporan.getBerat());
        transaksi.setTotalPoin((double) poin);
        transaksiRepo.save(transaksi);

        if (laporan.getJenisSampah() != null) {
            String namaKategori = laporan.getJenisSampah().name();
            KategoriSampah kategori = kategoriRepo.findByNamaKategoriIgnoreCase(namaKategori).orElse(null);
            if (kategori != null) {
                DetailTransaksi detail = new DetailTransaksi();
                detail.setTransaksi(transaksi);
                detail.setKategori(kategori);
                detail.setBerat(laporan.getBerat());
                detail.setSubtotalPoin(poin);
                detailTransaksiRepo.save(detail);
            }
        }
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

        Warga entitasWarga = warga.getWarga();
        if (entitasWarga == null) {
            entitasWarga = wargaRepo.findByUser(warga)
                    .orElseThrow(() -> new RuntimeException("Data warga tidak ditemukan untuk User #" + warga.getId()));
        }
        User petugasLaporan = laporan.getPetugas();
        Staff entitasStaff = null;
        if (petugasLaporan != null) {
            entitasStaff = petugasLaporan.getStaff();
            if (entitasStaff == null) {
                entitasStaff = staffRepo.findByUser(petugasLaporan)
                        .orElse(null);
            }
        }

        Transaksi transaksi = new Transaksi();
        transaksi.setWarga(entitasWarga);
        transaksi.setStaff(entitasStaff);
        transaksi.setStatus(Transaksi.StatusTransaksi.SELESAI);
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        transaksi.setTotalBerat(beratFinal);
        transaksi.setTotalPoin((double) poin);
        transaksiRepo.save(transaksi);

        if (laporan.getJenisSampah() != null) {
            String namaKategori = laporan.getJenisSampah().name();
            KategoriSampah kategori = kategoriRepo.findByNamaKategoriIgnoreCase(namaKategori).orElse(null);
            if (kategori != null) {
                DetailTransaksi detail = new DetailTransaksi();
                detail.setTransaksi(transaksi);
                detail.setKategori(kategori);
                detail.setBerat(beratFinal);
                detail.setSubtotalPoin(poin);
                detailTransaksiRepo.save(detail);
            }
        }
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


