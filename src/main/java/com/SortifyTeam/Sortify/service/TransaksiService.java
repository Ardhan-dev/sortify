package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransaksiService {

    private final TransaksiRepository transaksiRepo;
    private final PointService pointService;
    private final KategoriSampahRepository kategoriRepo;
    private final NotifikasiService notifikasiService;
    private final UserRepository userRepo;
    private final LogAktivitasService logAktivitasService;

    public TransaksiService(TransaksiRepository transaksiRepo,
                            PointService pointService,
                            KategoriSampahRepository kategoriRepo,
                            NotifikasiService notifikasiService,
                            UserRepository userRepo,
                            LogAktivitasService logAktivitasService) {
        this.transaksiRepo = transaksiRepo;
        this.pointService = pointService;
        this.kategoriRepo = kategoriRepo;
        this.notifikasiService = notifikasiService;
        this.userRepo = userRepo;
        this.logAktivitasService = logAktivitasService;
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

    public Page<Transaksi> getTransaksiHistory(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("tanggalTransaksi").descending());
        return transaksiRepo.findByStatusIn(
                List.of(Transaksi.StatusTransaksi.SELESAI, Transaksi.StatusTransaksi.DITOLAK, Transaksi.StatusTransaksi.DIBATALKAN), pageable);
    }

    public static final double MAX_WEIGHT_KG = 10000.0;

    @Transactional
    public void buatLaporanDropPoint(Warga warga, String namaFoto, String lokasi, String detail,
                                      List<Long> idKategoriList, List<Double> beratEstimasiList) {
        Transaksi transaksi = new Transaksi();
        transaksi.setWarga(warga);
        transaksi.setStatus(Transaksi.StatusTransaksi.PENDING);
        transaksi.setFotoLaporanWarga(namaFoto);
        transaksi.setLokasi(lokasi);
        transaksi.setDetail(detail);
        transaksi.setTanggalTransaksi(LocalDateTime.now());

        double totalBerat = 0;
        List<TransaksiDetail> details = new ArrayList<>();
        if (idKategoriList != null) {
            List<KategoriSampah> kategoriList = kategoriRepo.findAllById(idKategoriList);
            var kategoriMap = kategoriList.stream().collect(Collectors.toMap(KategoriSampah::getIdKategori, k -> k));
            for (int i = 0; i < idKategoriList.size(); i++) {
                Long idKategori = idKategoriList.get(i);
                Double beratEstimasi = (beratEstimasiList != null && i < beratEstimasiList.size())
                        ? beratEstimasiList.get(i) : 0;
                if (idKategori == null || beratEstimasi == null || beratEstimasi <= 0 || beratEstimasi > MAX_WEIGHT_KG) continue;

                KategoriSampah kategori = kategoriMap.get(idKategori);
                if (kategori == null) continue;

                TransaksiDetail td = new TransaksiDetail();
                td.setTransaksi(transaksi);
                td.setKategoriSampah(kategori);
                td.setBeratEstimasi(beratEstimasi);
                details.add(td);
                totalBerat += beratEstimasi;
            }
        }
        transaksi.setDetails(details);
        transaksi.setTotalBerat(totalBerat);
        transaksiRepo.save(transaksi);

        List<User> petugasList = userRepo.findByRole(User.Role.PETUGAS);
        String wargaNama = (warga != null && warga.getUser() != null) ? warga.getUser().getFullName() : "Warga";
        for (User petugas : petugasList) {
            notifikasiService.buatNotifikasi(petugas,
                    "Laporan baru dari " + wargaNama + " (" + String.format("%.1f", totalBerat) + " kg) — segera proses.");
        }
    }

    @Transactional
    public Transaksi prosesTransaksi(Long transaksiId) {
        Transaksi transaksi = getTransaksiById(transaksiId);
        if (!Transaksi.StatusTransaksi.PENDING.equals(transaksi.getStatus())) {
            throw new RuntimeException("Transaksi #" + transaksiId + " sudah diproses sebelumnya.");
        }
        transaksi.setStatus(Transaksi.StatusTransaksi.DIPROSES);
        return transaksiRepo.save(transaksi);
    }

    @Transactional
    public Transaksi batalTransaksi(Long transaksiId, User warga) {
        Transaksi transaksi = getTransaksiById(transaksiId);
        if (transaksi.getStatus() != Transaksi.StatusTransaksi.PENDING) {
            throw new RuntimeException("Transaksi #" + transaksiId + " tidak dapat dibatalkan karena sudah diproses.");
        }
        if (transaksi.getWarga() == null || transaksi.getWarga().getUser() == null
                || !transaksi.getWarga().getUser().getId().equals(warga.getId())) {
            throw new RuntimeException("Anda tidak berhak membatalkan transaksi ini.");
        }
            transaksi.setStatus(Transaksi.StatusTransaksi.DIBATALKAN);
        transaksiRepo.save(transaksi);

        List<User> petugasList = userRepo.findByRole(User.Role.PETUGAS);
        for (User petugas : petugasList) {
            notifikasiService.buatNotifikasi(petugas,
                    "Laporan #" + transaksiId + " dibatalkan oleh " + warga.getFullName());
        }
        logAktivitasService.catatAktivitas(warga.getUsername(), warga.getRole().name(),
                "BATAL_TRANSAKSI", "Warga membatalkan laporan #" + transaksiId);
        return transaksi;
    }

    @Transactional
    public Transaksi tolakTransaksi(Long transaksiId, String alasan) {
        Transaksi transaksi = getTransaksiById(transaksiId);
        if (!Transaksi.StatusTransaksi.PENDING.equals(transaksi.getStatus())) {
            throw new RuntimeException("Transaksi #" + transaksiId + " tidak bisa ditolak karena sudah diproses.");
        }
        transaksi.setStatus(Transaksi.StatusTransaksi.DITOLAK);
        transaksi.setAlasanPenolakan(alasan);
        transaksiRepo.save(transaksi);

        Warga warga = transaksi.getWarga();
        if (warga != null && warga.getUser() != null) {
            notifikasiService.buatNotifikasi(warga.getUser(),
                "Laporan #" + transaksiId + " ditolak. Alasan: " + alasan);
        }
        return transaksi;
    }

    @Transactional
    public Transaksi selesaikanTransaksi(Long transaksiId, List<Long> detailIds, List<Double> beratFinalList, String fotoBuktiTimbangan) {
        Transaksi transaksi = getTransaksiById(transaksiId);
        if (!Transaksi.StatusTransaksi.DIPROSES.equals(transaksi.getStatus())) {
            throw new RuntimeException("Transaksi #" + transaksiId + " harus dalam status DIPROSES terlebih dahulu.");
        }
        if (fotoBuktiTimbangan == null || fotoBuktiTimbangan.isBlank()) {
            throw new RuntimeException("Foto bukti timbangan harus diupload.");
        }

        transaksi.setFotoBuktiTimbangan(fotoBuktiTimbangan);

        double totalBerat = 0;
        double totalPoin = 0;
        StringBuilder detailDesc = new StringBuilder();

        for (int i = 0; i < transaksi.getDetails().size(); i++) {
            TransaksiDetail td = transaksi.getDetails().get(i);
            Double beratFinal = (detailIds != null && i < detailIds.size())
                    ? (beratFinalList != null && i < beratFinalList.size() ? beratFinalList.get(i) : 0)
                    : 0;
            if (beratFinal == null || beratFinal <= 0 || beratFinal > MAX_WEIGHT_KG) continue;

            td.setBeratFinal(beratFinal);
            int poinPerKg = (td.getKategoriSampah() != null && td.getKategoriSampah().getPoinPerKg() != null)
                    ? td.getKategoriSampah().getPoinPerKg() : 100;
            double subPoin = beratFinal * poinPerKg;
            td.setSubTotalPoin(subPoin);

            totalBerat += beratFinal;
            totalPoin += subPoin;

            String namaKategori = td.getKategoriSampah() != null ? td.getKategoriSampah().getNamaKategori() : "-";
            if (detailDesc.length() > 0) detailDesc.append(", ");
            detailDesc.append(namaKategori).append(" ").append(beratFinal).append("kg");
        }

        transaksi.setTotalBerat(totalBerat);
        transaksi.setTotalPoin(totalPoin);
        transaksi.setStatus(Transaksi.StatusTransaksi.SELESAI);
        transaksiRepo.save(transaksi);

        int poinBulat = (int) Math.round(totalPoin);
        Warga entitasWarga = transaksi.getWarga();
        if (entitasWarga != null && entitasWarga.getUser() != null) {
            User userWarga = entitasWarga.getUser();
            pointService.tambahPoint(userWarga, poinBulat,
                    "Poin transaksi #" + transaksiId + " (" + detailDesc + ")");
            notifikasiService.buatNotifikasi(userWarga,
                    "Hore! Transaksi sampah (" + detailDesc + ") berhasil diproses. "
                    + poinBulat + " Poin telah ditambahkan ke saldo Anda!");
        }

        return transaksi;
    }
}
