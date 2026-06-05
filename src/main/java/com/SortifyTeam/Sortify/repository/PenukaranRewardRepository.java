package com.SortifyTeam.Sortify.repository;

import com.SortifyTeam.Sortify.model.PenukaranReward;
import com.SortifyTeam.Sortify.model.RewardItem;
import com.SortifyTeam.Sortify.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PenukaranRewardRepository extends JpaRepository<PenukaranReward, Long> {
    List<PenukaranReward> findByUserOrderByTanggalPenukaranDesc(User user);
    boolean existsByRewardItem(RewardItem rewardItem);
    long countByStatus(PenukaranReward.StatusPenukaran status);
    List<PenukaranReward> findAllByOrderByTanggalPenukaranDesc();
    Optional<PenukaranReward> findByKodePenukaran(String kodePenukaran);
    boolean existsByKodePenukaran(String kodePenukaran);
    Page<PenukaranReward> findByStatusOrderByTanggalPenukaranDesc(PenukaranReward.StatusPenukaran status, Pageable pageable);
    Page<PenukaranReward> findByStatusInOrderByTanggalPenukaranDesc(List<PenukaranReward.StatusPenukaran> statuses, Pageable pageable);

    @Query("SELECT p FROM PenukaranReward p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:namaWarga IS NULL OR :namaWarga = '' OR LOWER(p.user.fullName) LIKE LOWER(CONCAT('%', :namaWarga, '%'))) AND " +
           "(:namaBarang IS NULL OR :namaBarang = '' OR LOWER(p.rewardItem.namaBarang) LIKE LOWER(CONCAT('%', :namaBarang, '%'))) " +
           "ORDER BY p.tanggalPenukaran DESC")
    List<PenukaranReward> findByFilters(@Param("status") PenukaranReward.StatusPenukaran status,
                                        @Param("namaWarga") String namaWarga,
                                        @Param("namaBarang") String namaBarang);
}
