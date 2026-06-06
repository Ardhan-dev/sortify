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

        Object[][] data = {
            {"Drop Point TPS Pasar Induk", "Jl. Pasar Induk No. 1, Kelurahan Sukamaju", -6.2015, 106.8412},
            {"Drop Point Kantor Kelurahan", "Jl. Balai Desa No. 10, Kelurahan Sukamaju", -6.2088, 106.8456},
            {"Drop Point Bank Sampah Unit RW 03", "Gg. Melati RT 03 RW 03, Kelurahan Sukamaju", -6.2150, 106.8520},
            {"Drop Point Bank Sampah Unit RW 05", "Gg. Kenanga RT 02 RW 05, Kelurahan Sukamaju", -6.2020, 106.8380},
            {"Drop Point Alun-Alun Kota", "Jl. Merdeka No. 1, Kelurahan Sukamaju", -6.2100, 106.8500},
        };

        for (Object[] d : data) {
            DropPoint dp = new DropPoint();
            dp.setNama((String) d[0]);
            dp.setAlamat((String) d[1]);
            dp.setLatitude((Double) d[2]);
            dp.setLongitude((Double) d[3]);
            dp.setAktif(true);
            repo.save(dp);
        }
    }
}
