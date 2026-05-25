package com.SortifyTeam.Sortify.config;

import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.model.RewardItem;
import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.repository.RewardItemRepository;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RewardItemRepository rewardItemRepo;
    private final KategoriSampahRepository kategoriRepo;
    private final WargaRepository wargaRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepo,
                             RewardItemRepository rewardItemRepo,
                             KategoriSampahRepository kategoriRepo,
                             WargaRepository wargaRepo,
                             StaffRepository staffRepo,
                             PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.rewardItemRepo = rewardItemRepo;
        this.kategoriRepo = kategoriRepo;
        this.wargaRepo = wargaRepo;
        this.staffRepo = staffRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private User buatUser(String username, String rawPassword, String fullName, User.Role role, int points) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setRole(role);
        user.setTotalPoints(points);
        return userRepo.save(user);
    }

    @Override
    public void run(String... args) {
        if (!userRepo.existsByUsername("admin")) {
            buatUser("admin", "password", "Admin Sistem", User.Role.ADMIN, 0);
        }
        if (!userRepo.existsByUsername("adminku")) {
            buatUser("adminku", "1234578", "Admin Utama Sortify", User.Role.ADMIN, 0);
        }
        if (!userRepo.existsByUsername("petugas")) {
            buatUser("petugas", "password", "Petugas Lapangan", User.Role.PETUGAS, 0);
        }
        if (!userRepo.existsByUsername("petugas0")) {
            buatUser("petugas0", "12345678", "Petugas Nol", User.Role.PETUGAS, 0);
        }
        if (!userRepo.existsByUsername("warga")) {
            buatUser("warga", "password", "Warga Biasa", User.Role.WARGA, 5000);
        }

        if (!wargaRepo.findByUsername("warga").isPresent()) {
            Warga warga = new Warga();
            warga.setNama("Warga Biasa");
            warga.setUsername("warga");
            warga.setPassword(passwordEncoder.encode("password"));
            warga.setAlamat("Jl. Contoh No. 123, Kota");
            warga.setNoHp("081234567890");
            warga.setUser(userRepo.findByUsername("warga").orElse(null));
            wargaRepo.save(warga);
        } else {
            wargaRepo.findByUsername("warga").ifPresent(w -> {
                if (w.getUser() == null) {
                    w.setUser(userRepo.findByUsername("warga").orElse(null));
                    wargaRepo.save(w);
                }
            });
        }

        userRepo.findByRole(User.Role.WARGA).forEach(u -> {
            if (wargaRepo.findByUser(u).isEmpty()) {
                if (wargaRepo.findByUsername(u.getUsername()).isPresent()) {
                    Warga w = wargaRepo.findByUsername(u.getUsername()).get();
                    w.setUser(u);
                    wargaRepo.save(w);
                } else {
                    Warga w = new Warga();
                    w.setNama(u.getFullName());
                    w.setUsername(u.getUsername());
                    w.setPassword(u.getPassword());
                    w.setAlamat("-");
                    w.setNoHp("-");
                    w.setUser(u);
                    wargaRepo.save(w);
                }
            }
        });

        if (!staffRepo.findByUsername("petugas").isPresent()) {
            Staff staff = new Staff();
            staff.setNama("Petugas Lapangan");
            staff.setUsername("petugas");
            staff.setPassword(passwordEncoder.encode("password"));
            staff.setNip("199001012015041001");
            staff.setJabatan("Staff Lapangan");
            staff.setUser(userRepo.findByUsername("petugas").orElse(null));
            staffRepo.save(staff);
        } else {
            staffRepo.findByUsername("petugas").ifPresent(s -> {
                if (s.getUser() == null) {
                    s.setUser(userRepo.findByUsername("petugas").orElse(null));
                    staffRepo.save(s);
                }
            });
        }

        if (!staffRepo.findByUsername("petugas0").isPresent()) {
            Staff staff = new Staff();
            staff.setNama("Petugas Nol");
            staff.setUsername("petugas0");
            staff.setPassword(passwordEncoder.encode("12345678"));
            staff.setNip("199501012020042002");
            staff.setJabatan("Koordinator");
            staff.setUser(userRepo.findByUsername("petugas0").orElse(null));
            staffRepo.save(staff);
        } else {
            staffRepo.findByUsername("petugas0").ifPresent(s -> {
                if (s.getUser() == null) {
                    s.setUser(userRepo.findByUsername("petugas0").orElse(null));
                    staffRepo.save(s);
                }
            });
        }

        userRepo.findByRole(User.Role.PETUGAS).forEach(u -> {
            if (staffRepo.findByUser(u).isEmpty()) {
                if (staffRepo.findByUsername(u.getUsername()).isPresent()) {
                    Staff s = staffRepo.findByUsername(u.getUsername()).get();
                    s.setUser(u);
                    staffRepo.save(s);
                } else {
                    Staff s = new Staff();
                    s.setNama(u.getFullName());
                    s.setUsername(u.getUsername());
                    s.setPassword(u.getPassword());
                    s.setNip("-");
                    s.setJabatan("-");
                    s.setUser(u);
                    staffRepo.save(s);
                }
            }
        });

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

        if (kategoriRepo.count() == 0) {
            KategoriSampah[] seeds = {
                buatKategori("ORGANIK", 100, "Pisahkan dari plastik dan kemasan. Cocok untuk kompos. Cacah dahulu agar proses pengomposan lebih cepat."),
                buatKategori("ANORGANIK", 75, "Cuci dan keringkan dahulu sebelum disetor. Pisahkan berdasarkan jenis: plastik, kertas, logam, atau kaca."),
                buatKategori("B3", 200, "JANGAN dibuang ke tempat sampah biasa! Kembalikan ke drop box B3 terdekat. Simpan dalam wadah asli yang tertutup rapat."),
                buatKategori("KERTAS", 50, "Lepaskan selotip, stapler, dan sampul plastik. Simpan di tempat kering. Kertas basah tidak diterima."),
                buatKategori("PLASTIK", 30, "Bersihkan dari sisa makanan dan keringkan. Plastik keras (ember, kursi) bernilai lebih tinggi dari plastik tipis."),
                buatKategori("LOGAM", 150, "Pisahkan dari material non-logam. Logam campuran diterima. Kabel tembaga bernilai sangat tinggi."),
                buatKategori("KACA", 40, "Cuci bersih. Bungkus dengan koran atau kain sebelum dibawa untuk mencegah pecah. Pecahan kaca diterima."),
                buatKategori("ELEKTRONIK", 250, "Hapus data pribadi sebelum menyetor. Lepaskan baterai jika memungkinkan. Jangan membongkar perangkat."),
            };
            for (KategoriSampah k : seeds) {
                kategoriRepo.save(k);
            }
        }
    }

    private KategoriSampah buatKategori(String nama, int poin, String instruksi) {
        KategoriSampah k = new KategoriSampah();
        k.setNamaKategori(nama);
        k.setPoinPerKg(poin);
        k.setInstruksiPenanganan(instruksi);
        return k;
    }
}


