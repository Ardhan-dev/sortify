package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "drop_point")
@Data
public class DropPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    private String alamat;

    private boolean aktif = true;
}
