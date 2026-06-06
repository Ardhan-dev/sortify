package com.SortifyTeam.Sortify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    // ── RELASI KE USER ──
    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drop_point_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DropPoint dropPoint;

    @Transient
    public String getPangkat() {
        if (user == null) return "Bronze (Eco-Starter)";
        return user.getPangkat();
    }

    @Transient
    public String getBadgePangkat() {
        if (user == null) return "secondary";
        return user.getBadgePangkat();
    }
}