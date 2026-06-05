package com.SortifyTeam.Sortify.config;

import com.SortifyTeam.Sortify.model.ItemSampah;
import com.SortifyTeam.Sortify.model.KategoriSampah;
import com.SortifyTeam.Sortify.model.RewardItem;
import com.SortifyTeam.Sortify.model.Staff;
import com.SortifyTeam.Sortify.model.User;
import com.SortifyTeam.Sortify.model.Warga;
import com.SortifyTeam.Sortify.repository.ItemSampahRepository;
import com.SortifyTeam.Sortify.repository.KategoriSampahRepository;
import com.SortifyTeam.Sortify.repository.RewardItemRepository;
import com.SortifyTeam.Sortify.repository.StaffRepository;
import com.SortifyTeam.Sortify.repository.UserRepository;
import com.SortifyTeam.Sortify.repository.WargaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RewardItemRepository rewardItemRepo;
    private final KategoriSampahRepository kategoriRepo;
    private final ItemSampahRepository itemSampahRepo;
    private final WargaRepository wargaRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoader(UserRepository userRepo,
                             RewardItemRepository rewardItemRepo,
                             KategoriSampahRepository kategoriRepo,
                             ItemSampahRepository itemSampahRepo,
                             WargaRepository wargaRepo,
                             StaffRepository staffRepo,
                             PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.rewardItemRepo = rewardItemRepo;
        this.kategoriRepo = kategoriRepo;
        this.itemSampahRepo = itemSampahRepo;
        this.wargaRepo = wargaRepo;
        this.staffRepo = staffRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private User buatUser(String username, String fullName, User.Role role, int points) {
        String rawPassword = username + "123";
        log.info("===== AKUN SEEDER: username={}, password={}, role={} =====", username, rawPassword, role);
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
            buatUser("admin", "Admin Sistem", User.Role.ADMIN, 0);
        }
        if (!userRepo.existsByUsername("adminku")) {
            buatUser("adminku", "Admin Utama Sortify", User.Role.ADMIN, 0);
        }
        if (!userRepo.existsByUsername("petugas")) {
            buatUser("petugas", "Petugas Lapangan", User.Role.PETUGAS, 0);
        }
        if (!userRepo.existsByUsername("petugas0")) {
            buatUser("petugas0", "Petugas Nol", User.Role.PETUGAS, 0);
        }
        if (!userRepo.existsByUsername("warga")) {
            buatUser("warga", "Warga Biasa", User.Role.WARGA, 5000);
        }

        if (!wargaRepo.findByUsername("warga").isPresent()) {
            Warga warga = new Warga();
            warga.setNama("Warga Biasa");
            warga.setUsername("warga");
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
                buatKategori("ORGANIK", 100, "Sisa makanan, daun, sayuran, dan bahan organik lainnya. Cocok untuk kompos. Pisahkan dari plastik dan kemasan sebelum disetor."),
                buatKategori("ANORGANIK", 75, "Plastik, kertas, logam, kaca, dan barang daur ulang lainnya. Cuci dan keringkan dahulu sebelum disetor."),
                buatKategori("B3", 200, "Limbah Bahan Berbahaya dan Beracun. JANGAN dicampur dengan sampah biasa! Kembalikan ke drop box B3 terdekat dalam wadah tertutup rapat."),
            };
            for (KategoriSampah k : seeds) {
                kategoriRepo.save(k);
            }
        }

        if (itemSampahRepo.count() == 0) {
            KategoriSampah organik = kategoriRepo.findByNamaKategoriIgnoreCase("ORGANIK").orElse(null);
            KategoriSampah anorganik = kategoriRepo.findByNamaKategoriIgnoreCase("ANORGANIK").orElse(null);
            KategoriSampah b3 = kategoriRepo.findByNamaKategoriIgnoreCase("B3").orElse(null);

            if (organik != null) {
                String[][] organikItems = {
                    {"Daun Kering/Ranting", "Daun kering dari halaman atau taman", "Cacah menjadi potongan kecil agar lebih cepat terkompos."},
                    {"Sisa Sayuran", "Sisa sayuran mentah dari dapur", "Tiriskan airnya, potong kecil-kecil sebelum dimasukkan ke komposter."},
                    {"Sisa Buah/Kulit Buah", "Kulit dan sisa buah-buahan", "Pisahkan dari biji keras, potong kecil-kecil."},
                    {"Sisa Makanan/Nasi Basi", "Nasi dan sisa makanan matang", "Tiriskan kuahnya, jangan campur dengan sampah plastik/kertas."},
                    {"Kulit Telur", "Kulit telur ayam atau bebek", "Remukkan atau tumbuk halus untuk mempercepat penguraian."},
                    {"Ampas Kopi/Teh", "Ampas kopi dan teh bekas seduh", "Keringkan terlebih dahulu atau langsung taburkan ke tanah."},
                    {"Tulang Ayam/Ikan", "Tulang dan duri sisa makanan", "Bersihkan dari sisa daging, bisa dikubur dalam tanah."},
                    {"Rumput/Tanaman Liar", "Rumput hasil potongan taman", "Jemur hingga layu sebelum dimasukkan ke wadah kompos."},
                    {"Tissue Bekas", "Tissue kertas bekas pakai", "Pastikan tidak tercampur bahan kimia, buang ke wadah organik."},
                    {"Kotoran Hewan", "Kotoran hewan ternak atau peliharaan", "Bungkus dengan daun/kertas koran atau masukkan ke biopori khusus."},
                };
                for (String[] item : organikItems) {
                    buatItemSampah(item[0], item[1], item[2], organik);
                }
            }

            if (anorganik != null) {
                String[][] anorganikItems = {
                    {"Botol Plastik", "Botol plastik bekas minuman atau kemasan", "Buang sisa air, lepaskan tutup dan labelnya, lalu remas/geprek untuk menghemat ruang."},
                    {"Gelas Plastik", "Gelas plastik sekali pakai", "Buang sisa minuman, bilas bersih, dan lepaskan segel plastiknya."},
                    {"Kantong Plastik", "Kantong plastik belanja atau kemasan", "Bersihkan dari sisa kotoran, lipat atau kumpulkan dalam satu wadah."},
                    {"Kardus", "Kardus bekas paket atau kemasan", "Kosongkan isinya, bongkar lipatannya, dan tumpuk hingga pipih."},
                    {"Kertas Koran/Buku", "Koran, buku, dan kertas bekas", "Ikat dengan tali rapi, pastikan tidak basah atau terkena minyak."},
                    {"Kaleng Aluminium", "Kaleng minuman ringan dari aluminium", "Buang sisa cairan, bilas bersih, lalu geprek hingga pipih."},
                    {"Kaleng Besi", "Kaleng susu atau makanan dari besi", "Cuci bersih dari sisa makanan/minyak, keringkan agar tidak berkarat."},
                    {"Botol Kaca", "Botol kaca bekas minuman atau saus", "Cuci bersih, keringkan, pisahkan tutupnya. JANGAN dipecahkan."},
                    {"Sedotan Plastik", "Sedotan plastik sekali pakai", "Bersihkan, kumpulkan jadi satu dalam botol plastik (ecobrick)."},
                    {"Styrofoam", "Styrofoam pembungkus makanan atau elektronik", "Cuci bersih dari sisa minyak/makanan, keringkan."},
                };
                for (String[] item : anorganikItems) {
                    buatItemSampah(item[0], item[1], item[2], anorganik);
                }
            }

            if (b3 != null) {
                String[][] b3Items = {
                    {"Baterai Bekas", "Baterai sekali pakai atau isi ulang yang sudah habis", "Pisahkan di wadah kering tertutup (botol kaca/plastik), jauhkan dari panas."},
                    {"Lampu Neon/Bohlam", "Lampu neon, bohlam pijar, atau lampu LED rusak", "Bungkus dengan koran/kardus bekas agar tidak pecah."},
                    {"Aki Kendaraan", "Aki mobil atau motor yang sudah soak", "Jangan membuang cairannya sembarangan, bawa utuh ke tempat pengepul khusus."},
                    {"Semprotan Aerosol", "Kaleng semprot pengharum, cat, atau pestisida", "Kosongkan isinya, JANGAN ditusuk atau dibakar karena mudah meledak."},
                    {"Kemasan Detergen", "Kemasan bekas detergen, pemutih, atau pembersih", "Bilas bersih dengan air, tutup rapat botolnya."},
                    {"Masker Medis", "Masker medis bekas pakai (non-infeksius)", "Gunting talinya, semprot desinfektan, bungkus plastik tertutup sebelum dibuang."},
                    {"Obat Kedaluwarsa", "Obat-obatan yang sudah melewati tanggal kadaluwarsa", "Hancurkan pil/kapsul, campur dengan tanah/ampas kopi, buang wadahnya terpisah."},
                    {"Termometer Raksa", "Termometer air raksa yang pecah atau rusak", "JANGAN sentuh raksa dengan tangan kosong, gunakan sarung tangan, masukkan ke botol tertutup."},
                    {"Kabel/Charger", "Kabel, charger, dan adaptor elektronik rusak", "Gulung rapi, ikat, dan kumpulkan bersama sampah elektronik lainnya."},
                    {"Elektronik Bekas", "Komponen atau perangkat elektronik kecil yang rusak", "Jangan dibongkar sendiri, serahkan ke drop point e-waste."},
                };
                for (String[] item : b3Items) {
                    buatItemSampah(item[0], item[1], item[2], b3);
                }
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

    private void buatItemSampah(String nama, String deskripsi, String instruksi, KategoriSampah kategori) {
        ItemSampah item = new ItemSampah();
        item.setNamaItem(nama);
        item.setDeskripsi(deskripsi);
        item.setInstruksiPenanganan(instruksi);
        item.setKategoriSampah(kategori);
        itemSampahRepo.save(item);
    }
}


