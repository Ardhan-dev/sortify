package com.SortifyTeam.Sortify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

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

    // ── RELASI KE USER ──
    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @JsonIgnore
    private User user;
}