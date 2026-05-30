package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.PenukaranRewardRepository;
import com.SortifyTeam.Sortify.repository.RewardItemRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RewardService {

    private final RewardItemRepository rewardItemRepo;
    private final PenukaranRewardRepository penukaranRepo;
    private final PointService pointService;
    private final UserRepository userRepo;
    private final LogAktivitasService logAktivitasService;

    public RewardService(RewardItemRepository rewardItemRepo,
                         PenukaranRewardRepository penukaranRepo,
                         PointService pointService,
                         UserRepository userRepo,
                         LogAktivitasService logAktivitasService) {
        this.rewardItemRepo = rewardItemRepo;
        this.penukaranRepo = penukaranRepo;
        this.pointService = pointService;
        this.userRepo = userRepo;
        this.logAktivitasService = logAktivitasService;
    }

    public List<RewardItem> getRewardTersedia() {
        return rewardItemRepo.findByStockGreaterThan(0);
    }

    public List<PenukaranReward> getRiwayatPenukaran(User warga) {
        return penukaranRepo.findByWargaOrderByTanggalPenukaranDesc(warga);
    }

    @Transactional
    public void tukarReward(User warga, Long rewardItemId, String lokasi) {
        RewardItem item = rewardItemRepo.findById(rewardItemId)
                .orElseThrow(() -> new RuntimeException("Reward tidak ditemukan"));

        if (item.getStock() <= 0) {
            throw new RuntimeException("Stok reward habis");
        }

        if (warga.getTotalPoints() < item.getPointNeeded()) {
            throw new RuntimeException("Poin tidak mencukupi");
        }

        warga.setTotalPoints(warga.getTotalPoints() - item.getPointNeeded());
        item.setStock(item.getStock() - 1);

        PenukaranReward penukaran = new PenukaranReward();
        penukaran.setWarga(warga);
        penukaran.setRewardItem(item);
        penukaran.setLokasi(lokasi);

        userRepo.save(warga);
        rewardItemRepo.save(item);
        penukaranRepo.save(penukaran);

        String kode = "RDM-" + penukaran.getId() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        penukaran.setKodePenukaran(kode);
        penukaranRepo.save(penukaran);
        pointService.tambahPointHistory(warga, item.getPointNeeded(),
                PointHistory.PointType.SPEND,
                "Penukaran " + item.getNamaBarang());
        logAktivitasService.catatAktivitas(
            warga.getUsername(),
            warga.getRole().name(),
            "TUKAR_REWARD",
            "Menukar " + item.getNamaBarang() + " (" + item.getPointNeeded() + " poin) di " + lokasi + " — sisa poin: " + warga.getTotalPoints()
        );
    }

    public long countTotalPenukaran() {
        return penukaranRepo.count();
    }

    public List<PenukaranReward> getAllPenukaran() {
        return penukaranRepo.findAllByOrderByTanggalPenukaranDesc();
    }

    public PenukaranReward getPenukaranById(Long id) {
        return penukaranRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Penukaran tidak ditemukan: " + id));
    }

    public Optional<PenukaranReward> getPenukaranByKode(String kode) {
        return penukaranRepo.findByKodePenukaran(kode);
    }

    @Transactional
    public void selesaikanPenukaran(Long penukaranId, String fotoBukti) {
        PenukaranReward p = getPenukaranById(penukaranId);
        p.setStatus(PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL);
        p.setFotoBukti(fotoBukti);
        penukaranRepo.save(p);
    }

    @Transactional
    public PenukaranReward verifikasiPenukaran(String kode, String fotoBukti) {
        PenukaranReward p = penukaranRepo.findByKodePenukaran(kode)
                .orElseThrow(() -> new RuntimeException("Kode penukaran tidak valid: " + kode));
        if (p.getStatus() == PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL) {
            throw new RuntimeException("Penukaran dengan kode " + kode + " sudah diambil sebelumnya.");
        }
        p.setStatus(PenukaranReward.StatusPenukaran.SUDAH_DIAMBIL);
        if (fotoBukti != null && !fotoBukti.isBlank()) {
            p.setFotoBukti(fotoBukti);
        }
        return penukaranRepo.save(p);
    }

    // ── CRUD untuk Admin ──

    public List<RewardItem> getAllRewardItems() {
        return rewardItemRepo.findAll();
    }

    public RewardItem getRewardById(Long id) {
        return rewardItemRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reward tidak ditemukan: " + id));
    }

    @Transactional
    public RewardItem simpanReward(String namaBarang, int pointNeeded, int stock) {
        RewardItem item = new RewardItem();
        item.setNamaBarang(namaBarang);
        item.setPointNeeded(pointNeeded);
        item.setStock(stock);
        return rewardItemRepo.save(item);
    }

    @Transactional
    public RewardItem updateReward(Long id, String namaBarang, int pointNeeded, int stock) {
        RewardItem item = getRewardById(id);
        item.setNamaBarang(namaBarang);
        item.setPointNeeded(pointNeeded);
        item.setStock(stock);
        return rewardItemRepo.save(item);
    }

    @Transactional
    public void hapusReward(Long id) {
        rewardItemRepo.deleteById(id);
    }
}


