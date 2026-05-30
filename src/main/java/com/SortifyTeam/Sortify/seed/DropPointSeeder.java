package com.SortifyTeam.Sortify.seed;

import com.SortifyTeam.Sortify.model.DropPoint;
import com.SortifyTeam.Sortify.repository.DropPointRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DropPointSeeder implements CommandLineRunner {

    private final DropPointRepository repo;

    public DropPointSeeder(DropPointRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        String[][] data = {
            {"Drop Point TPS Pasar Induk", "Jl. Pasar Induk No. 1, Kelurahan Sukamaju"},
            {"Drop Point Kantor Kelurahan", "Jl. Balai Desa No. 10, Kelurahan Sukamaju"},
            {"Drop Point Bank Sampah Unit RW 03", "Gg. Melati RT 03 RW 03, Kelurahan Sukamaju"},
            {"Drop Point Bank Sampah Unit RW 05", "Gg. Kenanga RT 02 RW 05, Kelurahan Sukamaju"},
            {"Drop Point Alun-Alun Kota", "Jl. Merdeka No. 1, Kelurahan Sukamaju"},
        };

        for (String[] d : data) {
            DropPoint dp = new DropPoint();
            dp.setNama(d[0]);
            dp.setAlamat(d[1]);
            dp.setAktif(true);
            repo.save(dp);
        }
    }
}
