package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "kategori_sampah")
@Data
public class KategoriSampah {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKategori;

    private String namaKategori; 
    private Integer poinPerKg;

    @Column(columnDefinition = "TEXT")
    private String instruksiPenanganan;

    @Column(nullable = false)
    private Boolean isActive = true;
}