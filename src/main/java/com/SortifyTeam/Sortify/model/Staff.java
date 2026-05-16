package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "staff")
@Data
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStaff;

    private String nama;
    private String nip;
    private String jabatan;

    // ── AUTH ──
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
}