package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy = "transaksi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailTransaksi> details;
}