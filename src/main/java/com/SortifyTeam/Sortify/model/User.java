package com.SortifyTeam.Sortify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private int totalPoints = 0;

    private String fotoProfil;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private Warga warga;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private Staff staff;

    @Transient
    public String getPangkat() {
        if (totalPoints <= 1000) return "Bronze (Eco-Starter)";
        if (totalPoints <= 3000) return "Silver (Eco-Saver)";
        if (totalPoints <= 6000) return "Gold (Eco-Warrior)";
        return "Emerald (Eco-Champion)";
    }

    @Transient
    public String getBadgePangkat() {
        if (totalPoints <= 1000) return "secondary";
        if (totalPoints <= 3000) return "info";
        if (totalPoints <= 6000) return "warning";
        return "success";
    }

    public enum Role {
        ADMIN, PETUGAS, WARGA
    }
}

