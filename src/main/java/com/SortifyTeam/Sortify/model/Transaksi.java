package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaksi")
@Data
public class Transaksi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaksi;

    @ManyToOne
    @JoinColumn(name = "id_warga")
    private Warga warga;

    @ManyToOne
    @JoinColumn(name = "id_staff")
    private Staff staff;

    private LocalDateTime tanggalTransaksi;
    private Double totalBerat;
    private Double totalPoin;

    private String fotoLaporanWarga;

    private String fotoBuktiTimbangan;

    private Double beratSampah;

    private String lokasi;

    private String detail;

    @Enumerated(EnumType.STRING)
    private JenisSampah jenisSampah;

    @Enumerated(EnumType.STRING)
    private StatusTransaksi status;

    public enum JenisSampah {
        ORGANIK, ANORGANIK, B3
    }

    public enum StatusTransaksi {
        PENDING, DIPROSES, SELESAI
    }
}