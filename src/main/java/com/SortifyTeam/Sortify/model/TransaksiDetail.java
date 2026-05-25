package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "transaksi_detail")
@Data
public class TransaksiDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_transaksi")
    private Transaksi transaksi;

    @ManyToOne
    @JoinColumn(name = "id_kategori")
    private KategoriSampah kategoriSampah;

    private Double beratEstimasi;
    private Double beratFinal;
    private Double subTotalPoin;
}
