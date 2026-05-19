package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "laporan_sampah")
@Data
public class LaporanSampah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warga_id", nullable = false)
    private User warga;

    @ManyToOne
    @JoinColumn(name = "petugas_id")
    private User petugas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JenisSampah jenisSampah;

    @Column(nullable = false)
    private double berat;

    @Column(nullable = false)
    private String alamatLengkap;

    @Column(columnDefinition = "TEXT")
    private String catatan;

    private String fotoBukti;

    private Double beratFinal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLaporan status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = StatusLaporan.MENUNGGU;
    }

    public enum JenisSampah {
        ORGANIK, ANORGANIK, B3
    }

    public enum StatusLaporan {
        MENUNGGU, DITOLAK, MENUNGGU_PEMBAYARAN, DIPROSES, SELESAI
    }
}

