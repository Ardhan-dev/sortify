package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transaksi")
@Data
public class Transaksi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaksi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_warga")
    private Warga warga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_staff")
    private Staff staff;

    private LocalDateTime tanggalTransaksi;
    private Double totalBerat;
    private Double totalPoin;

    private String fotoLaporanWarga;

    private String fotoBuktiTimbangan;

    private String lokasi;

    private String detail;

    @OneToMany(mappedBy = "transaksi", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TransaksiDetail> details = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private StatusTransaksi status;

    @Column(columnDefinition = "TEXT")
    private String alasanPenolakan;

    public enum StatusTransaksi {
        PENDING, DIPROSES, SELESAI, DITOLAK
    }
}