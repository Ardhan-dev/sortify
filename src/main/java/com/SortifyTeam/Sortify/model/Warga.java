package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "warga")
@Data
public class Warga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idWarga;

    private String nama;
    private String alamat;
    private String noHp;

    // ── AUTH ──
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
}