package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.*;
import com.SortifyTeam.Sortify.repository.PenukaranRewardRepository;
import com.SortifyTeam.Sortify.repository.RewardItemRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RewardService {

    private final RewardItemRepository rewardItemRepo;
    private final PenukaranRewardRepository penukaranRepo;
    private final PointService pointService;
    private final UserRepository userRepo;

    public RewardService(RewardItemRepository rewardItemRepo,
                         PenukaranRewardRepository penukaranRepo,
                         PointService pointService,
                         UserRepository userRepo) {
        this.rewardItemRepo = rewardItemRepo;
        this.penukaranRepo = penukaranRepo;
        this.pointService = pointService;
        this.userRepo = userRepo;
    }

    public List<RewardItem> getRewardTersedia() {
        return rewardItemRepo.findByStockGreaterThan(0);
    }

    public List<PenukaranReward> getRiwayatPenukaran(User warga) {
        return penukaranRepo.findByWargaOrderByTanggalPenukaranDesc(warga);
    }

    @Transactional
    public void tukarReward(User warga, Long rewardItemId) {
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

        userRepo.save(warga);
        rewardItemRepo.save(item);
        penukaranRepo.save(penukaran);
        pointService.tambahPointHistory(warga, item.getPointNeeded(),
                PointHistory.PointType.SPEND,
                "Penukaran " + item.getNamaBarang());
    }

    public long countTotalPenukaran() {
        return penukaranRepo.count();
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


