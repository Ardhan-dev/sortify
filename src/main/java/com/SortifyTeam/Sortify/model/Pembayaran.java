package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "pembayaran")
@Data
public class Pembayaran {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "laporan_id", nullable = false)
    private LaporanSampah laporan;

    private String metodePembayaran;

    @Column(nullable = false)
    private double totalPembayaran;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPembayaran statusPembayaran;

    private LocalDateTime paidAt;

    public enum StatusPembayaran {
        PENDING, BERHASIL
    }
}

