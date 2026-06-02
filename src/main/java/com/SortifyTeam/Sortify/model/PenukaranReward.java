package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "penukaran_reward")
@Data
public class PenukaranReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warga_id", nullable = false)
    private User warga;

    @ManyToOne
    @JoinColumn(name = "reward_item_id", nullable = false)
    private RewardItem rewardItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPenukaran status = StatusPenukaran.PENDING;

    private String fotoBukti;

    @Column(unique = true, length = 6)
    private String kodePenukaran;

    @Column(nullable = false)
    private LocalDateTime tanggalPenukaran;

    @PrePersist
    protected void onCreate() {
        tanggalPenukaran = LocalDateTime.now();
    }

    public enum StatusPenukaran {
        PENDING, SUDAH_DIAMBIL
    }
}

