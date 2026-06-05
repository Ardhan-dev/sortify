package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.PenukaranRewardRepository;
import com.SortifyTeam.Sortify.repository.RewardItemRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
public class RewardService {

    private final RewardItemRepository rewardItemRepo;
    private final PenukaranRewardRepository penukaranRepo;
    private final PointService pointService;
    private final UserRepository userRepo;
    private final LogAktivitasService logAktivitasService;
    private final NotifikasiService notifikasiService;

    public RewardService(RewardItemRepository rewardItemRepo,
                         PenukaranRewardRepository penukaranRepo,
                         PointService pointService,
                         UserRepository userRepo,
                         LogAktivitasService logAktivitasService,
                         NotifikasiService notifikasiService) {
        this.rewardItemRepo = rewardItemRepo;
        this.penukaranRepo = penukaranRepo;
        this.pointService = pointService;
        this.userRepo = userRepo;
        this.logAktivitasService = logAktivitasService;
        this.notifikasiService = notifikasiService;
    }

    public List<RewardItem> getRewardTersedia() {
        return rewardItemRepo.findByStockGreaterThanAndIsActiveTrue(0);
    }

    public List<PenukaranReward> getRiwayatPenukaran(User warga) {
        return penukaranRepo.findByUserOrderByTanggalPenukaranDesc(warga);
    }

    @Transactional
    public PenukaranReward tukarReward(User warga, Long rewardItemId) {
        RewardItem item = rewardItemRepo.findByIdWithLock(rewardItemId)
                .orElseThrow(() -> new RuntimeException("Reward tidak ditemukan"));

        if (item.getStock() <= 0) {
            throw new RuntimeException("Stok reward habis");
        }

        User lockedWarga = userRepo.findByIdWithLock(warga.getId())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (lockedWarga.getTotalPoints() < item.getPointNeeded()) {
            throw new RuntimeException("Poin tidak mencukupi");
        }

        lockedWarga.setTotalPoints(lockedWarga.getTotalPoints() - item.getPointNeeded());
        item.setStock(item.getStock() - 1);

        PenukaranReward penukaran = new PenukaranReward();
        penukaran.setUser(lockedWarga);
        penukaran.setRewardItem(item);
        penukaran.setKodePenukaran(generateUniqueKode());

        userRepo.save(lockedWarga);
        rewardItemRepo.save(item);
        penukaran = penukaranRepo.save(penukaran);
        pointService.tambahPointHistory(lockedWarga, item.getPointNeeded(),
                PointHistory.PointType.SPEND,
                "Penukaran " + item.getNamaBarang());
        logAktivitasService.catatAktivitas(
            lockedWarga.getUsername(),
            lockedWarga.getRole().name(),
            "TUKAR_REWARD",
            "Menukar " + item.getNamaBarang() + " (" + item.getPointNeeded() + " poin) — sisa poin: " + lockedWarga.getTotalPoints()
        );
        notifikasiService.buatNotifikasi(lockedWarga,
                "Penukaran berhasil! Kode AMDAL Anda: " + penukaran.getKodePenukaran() + ". Silakan ambil reward " + item.getNamaBarang() + " di Kantor Sortify pada jam kerja.");
        List<User> petugasList = userRepo.findByRole(User.Role.PETUGAS);
        for (User petugas : petugasList) {
            notifikasiService.buatNotifikasi(petugas,
                    "Penukaran reward " + item.getNamaBarang() + " oleh " + lockedWarga.getFullName()
                    + " (" + item.getPointNeeded() + " poin) — segera konfirmasi serah terima.");
        }
        return penukaran;
    }

    public long countTotalPenukaran() {
        return penukaranRepo.count();
    }

    private String generateUniqueKode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        for (int attempt = 0; attempt < 100; attempt++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            String kode = sb.toString();
            if (!penukaranRepo.existsByKodePenukaran(kode)) {
                return kode;
            }
        }
        throw new RuntimeException("Gagal generate kode unik — coba lagi.");
    }

    // ── CRUD untuk Admin ──

    public List<RewardItem> getAllRewardItems() {
        return rewardItemRepo.findAll().stream()
                .filter(RewardItem::getIsActive)
                .toList();
    }

    public RewardItem getRewardById(Long id) {
        return rewardItemRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reward tidak ditemukan: " + id));
    }

    @Transactional
    public RewardItem simpanReward(String namaBarang, int pointNeeded, int stock) {
        if (namaBarang == null || namaBarang.isBlank()) {
            throw new IllegalArgumentException("Nama barang tidak boleh kosong");
        }
        if (pointNeeded < 1) {
            throw new IllegalArgumentException("Poin minimal 1");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stok minimal 0");
        }
        RewardItem item = new RewardItem();
        item.setNamaBarang(namaBarang);
        item.setPointNeeded(pointNeeded);
        item.setStock(stock);
        return rewardItemRepo.save(item);
    }

    @Transactional
    public RewardItem updateReward(Long id, String namaBarang, int pointNeeded, int stock) {
        if (namaBarang == null || namaBarang.isBlank()) {
            throw new IllegalArgumentException("Nama barang tidak boleh kosong");
        }
        if (pointNeeded < 1) {
            throw new IllegalArgumentException("Poin minimal 1");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stok minimal 0");
        }
        RewardItem item = getRewardById(id);
        item.setNamaBarang(namaBarang);
        item.setPointNeeded(pointNeeded);
        item.setStock(stock);
        return rewardItemRepo.save(item);
    }

    @Transactional
    public void hapusReward(Long id) {
        RewardItem item = getRewardById(id);
        item.setIsActive(false);
        rewardItemRepo.save(item);
    }

    @Transactional
    public PenukaranReward batalkanPenukaran(Long penukaranId) {
        PenukaranReward penukaran = penukaranRepo.findById(penukaranId)
                .orElseThrow(() -> new RuntimeException("Penukaran reward tidak ditemukan: " + penukaranId));

        if (!PenukaranReward.StatusPenukaran.PENDING.equals(penukaran.getStatus())) {
            throw new RuntimeException("Penukaran #" + penukaranId + " sudah diproses dan tidak dapat dibatalkan.");
        }

        penukaran.setStatus(PenukaranReward.StatusPenukaran.DIBATALKAN);
        penukaranRepo.save(penukaran);

        User warga = userRepo.findByIdWithLock(penukaran.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        RewardItem item = penukaran.getRewardItem();

        warga.setTotalPoints(warga.getTotalPoints() + item.getPointNeeded());
        item.setStock(item.getStock() + 1);

        userRepo.save(warga);
        rewardItemRepo.save(item);

        pointService.tambahPointHistory(warga, item.getPointNeeded(),
                PointHistory.PointType.EARN,
                "Refund poin pembatalan " + item.getNamaBarang());
        logAktivitasService.catatAktivitas(
            warga.getUsername(),
            warga.getRole().name(),
            "BATAL_REWARD",
            "Penukaran " + item.getNamaBarang() + " dibatalkan — refund " + item.getPointNeeded() + " poin"
        );
        notifikasiService.buatNotifikasi(warga,
                "Penukaran " + item.getNamaBarang() + " dibatalkan oleh petugas. " + item.getPointNeeded() + " poin telah dikembalikan.");

        return penukaran;
    }
}


