package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detail_transaksi")
@Data
public class DetailTransaksi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetail;

    @ManyToOne
    @JoinColumn(name = "id_transaksi")
    private Transaksi transaksi;

    @ManyToOne
    @JoinColumn(name = "id_kategori")
    private KategoriSampah kategori;

    private Double berat;
    private Integer subtotalPoin;
}