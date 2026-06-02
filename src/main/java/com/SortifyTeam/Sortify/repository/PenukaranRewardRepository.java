package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.PenukaranReward;
import com.SortifyTeam.Sortify.model.RewardItem;
import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenukaranRewardRepository extends JpaRepository<PenukaranReward, Long> {
    List<PenukaranReward> findByUserOrderByTanggalPenukaranDesc(User user);
    boolean existsByRewardItem(RewardItem rewardItem);
    long countByStatus(PenukaranReward.StatusPenukaran status);
    List<PenukaranReward> findAllByOrderByTanggalPenukaranDesc();
    Page<PenukaranReward> findByStatusOrderByTanggalPenukaranDesc(PenukaranReward.StatusPenukaran status, Pageable pageable);
    java.util.Optional<PenukaranReward> findByKodePenukaran(String kodePenukaran);
    boolean existsByKodePenukaran(String kodePenukaran);
}
