package com.SortifyTeam.Sortify.config;

import com.SortifyTeam.Sortify.model.RewardItem;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.repository.RewardItemRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RewardItemRepository rewardItemRepo;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepo,
                             RewardItemRepository rewardItemRepo,
                             PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.rewardItemRepo = rewardItemRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private void seedUser(String username, String rawPassword, String fullName, User.Role role, int points) {
        if (userRepo.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFullName(fullName);
            user.setRole(role);
            user.setTotalPoints(points);
            userRepo.save(user);
        }
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "password", "Admin Sistem", User.Role.ADMIN, 0);
        seedUser("petugas", "password", "Petugas Lapangan", User.Role.PETUGAS, 0);
        seedUser("petugas0", "12345678", "Petugas Nol", User.Role.PETUGAS, 0);
        seedUser("warga", "password", "Warga Biasa", User.Role.WARGA, 5000);

        if (rewardItemRepo.count() == 0) {
            RewardItem beras = new RewardItem();
            beras.setNamaBarang("Beras");
            beras.setPointNeeded(1000);
            beras.setStock(50);
            rewardItemRepo.save(beras);

            RewardItem minyak = new RewardItem();
            minyak.setNamaBarang("Minyak");
            minyak.setPointNeeded(1500);
            minyak.setStock(50);
            rewardItemRepo.save(minyak);

            RewardItem gula = new RewardItem();
            gula.setNamaBarang("Gula");
            gula.setPointNeeded(1200);
            gula.setStock(50);
            rewardItemRepo.save(gula);
        }
    }
}


