package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "transaksi_detail")
@Data
public class TransaksiDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transaksi")
    @ToString.Exclude
    private Transaksi transaksi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kategori")
    private KategoriSampah kategoriSampah;

    private Double beratEstimasi;
    private Double beratFinal;
    private Double subTotalPoin;
}
