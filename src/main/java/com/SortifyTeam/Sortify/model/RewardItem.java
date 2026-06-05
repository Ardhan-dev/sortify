package com.SortifyTeam.Sortify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "reward_item")
@Data
public class RewardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nama barang tidak boleh kosong")
    @Column(nullable = false)
    private String namaBarang;

    @NotNull(message = "Poin tidak boleh kosong")
    @Min(value = 1, message = "Poin minimal 1")
    @Column(nullable = false)
    private int pointNeeded;

    @NotNull(message = "Stok tidak boleh kosong")
    @Min(value = 0, message = "Stok minimal 0")
    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private Boolean isActive = true;
}

