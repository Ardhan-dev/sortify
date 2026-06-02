package com.SortifyTeam.Sortify.service;

import com.SortifyTeam.Sortify.model.PointHistory;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.PointHistoryRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointService {

    private final PointHistoryRepository pointHistoryRepo;
    private final UserRepository userRepo;

    public PointService(PointHistoryRepository pointHistoryRepo, UserRepository userRepo) {
        this.pointHistoryRepo = pointHistoryRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public void tambahPoint(User warga, int amount, String description) {
        User lockedWarga = userRepo.findByIdWithLock(warga.getId())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        lockedWarga.setTotalPoints(lockedWarga.getTotalPoints() + amount);
        userRepo.save(lockedWarga);
        tambahPointHistory(lockedWarga, amount, PointHistory.PointType.EARN, description);
    }

    public void tambahPointHistory(User user, int amount, PointHistory.PointType type, String description) {
        PointHistory history = new PointHistory();
        history.setUser(user);
        history.setAmount(amount);
        history.setType(type);
        history.setDescription(description);
        pointHistoryRepo.save(history);
    }

    public List<PointHistory> getRiwayatPoint(User user) {
        return pointHistoryRepo.findByUserOrderByCreatedAtDesc(user);
    }
}


