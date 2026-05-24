package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransaksiService {

    private final TransaksiRepository transaksiRepo;
    private final PointService pointService;
    private final KategoriSampahRepository kategoriRepo;

    public TransaksiService(TransaksiRepository transaksiRepo,
                            PointService pointService,
                            KategoriSampahRepository kategoriRepo) {
        this.transaksiRepo = transaksiRepo;
        this.pointService = pointService;
        this.kategoriRepo = kategoriRepo;
    }

    public List<Transaksi> getTransaksiByStatus(Transaksi.StatusTransaksi status) {
        return transaksiRepo.findByStatusOrderByTanggalTransaksiDesc(status);
    }

    public List<Transaksi> getTransaksiByStatuses(List<Transaksi.StatusTransaksi> statuses) {
        return transaksiRepo.findByStatusInOrderByTanggalTransaksiDesc(statuses);
    }

    public List<Transaksi> getTransaksiByWarga(Warga warga) {
        return transaksiRepo.findByWargaOrderByTanggalTransaksiDesc(warga);
    }

    public Transaksi getTransaksiById(Long id) {
        return transaksiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan: " + id));
    }

    @Transactional
    public void buatLaporanDropPoint(Warga warga, Transaksi.JenisSampah jenisSampah, String namaFoto,
                                      Double beratSampah, String lokasi, String detail) {
        Transaksi transaksi = new Transaksi();
        transaksi.setWarga(warga);
        transaksi.setJenisSampah(jenisSampah);
        transaksi.setStatus(Transaksi.StatusTransaksi.PENDING);
        transaksi.setFotoLaporanWarga(namaFoto);
        transaksi.setBeratSampah(beratSampah);
        transaksi.setLokasi(lokasi);
        transaksi.setDetail(detail);
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        transaksiRepo.save(transaksi);
    }

    @Transactional
    public Transaksi prosesTransaksi(Long transaksiId) {
        Transaksi transaksi = getTransaksiById(transaksiId);
        if (transaksi.getStatus() != Transaksi.StatusTransaksi.PENDING) {
            throw new RuntimeException("Transaksi #" + transaksiId + " sudah diproses sebelumnya.");
        }
        transaksi.setStatus(Transaksi.StatusTransaksi.DIPROSES);
        return transaksiRepo.save(transaksi);
    }

    @Transactional
    public Transaksi selesaikanTransaksi(Long transaksiId, Double beratSampah, String fotoBuktiTimbangan) {
        Transaksi transaksi = getTransaksiById(transaksiId);
        if (transaksi.getStatus() != Transaksi.StatusTransaksi.DIPROSES) {
            throw new RuntimeException("Transaksi #" + transaksiId + " harus dalam status DIPROSES terlebih dahulu.");
        }
        if (beratSampah == null || beratSampah <= 0) {
            throw new RuntimeException("Berat sampah harus lebih dari 0.");
        }
        if (fotoBuktiTimbangan == null || fotoBuktiTimbangan.isBlank()) {
            throw new RuntimeException("Foto bukti timbangan harus diupload.");
        }

        transaksi.setBeratSampah(beratSampah);
        transaksi.setFotoBuktiTimbangan(fotoBuktiTimbangan);
        transaksi.setTotalBerat(beratSampah);
        int poin = hitungPoin(transaksi);
        transaksi.setTotalPoin((double) poin);
        transaksi.setStatus(Transaksi.StatusTransaksi.SELESAI);
        transaksiRepo.save(transaksi);

        Warga entitasWarga = transaksi.getWarga();
        if (entitasWarga != null && entitasWarga.getUser() != null) {
            User userWarga = entitasWarga.getUser();
            pointService.tambahPoint(userWarga, poin,
                    "Poin transaksi drop-point #" + transaksiId + " (" + beratSampah + " kg)");
        }

        return transaksi;
    }

    private int hitungPoin(Transaksi transaksi) {
        if (transaksi.getBeratSampah() == null || transaksi.getJenisSampah() == null) {
            return (int) (transaksi.getBeratSampah() != null ? transaksi.getBeratSampah() * 100 : 0);
        }
        String namaKategori = transaksi.getJenisSampah().name();
        KategoriSampah kategori = kategoriRepo.findByNamaKategoriIgnoreCase(namaKategori).orElse(null);
        if (kategori == null || kategori.getPoinPerKg() == null) {
            return (int) (transaksi.getBeratSampah() * 100);
        }
        return (int) (transaksi.getBeratSampah() * kategori.getPoinPerKg());
    }
}
