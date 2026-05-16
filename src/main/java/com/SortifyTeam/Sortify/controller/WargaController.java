package com.SortifyTeam.Sortify.controller;

import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/warga")
public class WargaController {
    @Autowired
    private WargaRepository wargaRepo;

    @GetMapping
    public List<Warga> getAll() {
        return wargaRepo.findAll();
    }

    @PostMapping
    public Warga save(@RequestBody Warga warga) {
        return wargaRepo.save(warga);
    }
}