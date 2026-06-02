package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.PenukaranReward;
import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenukaranRewardRepository extends JpaRepository<PenukaranReward, Long> {
    List<PenukaranReward> findByWargaOrderByTanggalPenukaranDesc(User warga);
    long countByStatus(PenukaranReward.StatusPenukaran status);
    List<PenukaranReward> findAllByOrderByTanggalPenukaranDesc();
    java.util.Optional<PenukaranReward> findByKodePenukaran(String kodePenukaran);
    boolean existsByKodePenukaran(String kodePenukaran);
}
