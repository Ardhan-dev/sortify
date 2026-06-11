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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @PersistenceContext
    private EntityManager entityManager;

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

    private void updatePassword(String username, String rawPassword) {
        userRepo.findByUsername(username).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepo.save(user);
            log.info("===== PASSWORD UPDATED: username={}, password={} =====", username, rawPassword);
        });
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            entityManager.createNativeQuery(
                "ALTER TABLE staff MODIFY COLUMN password VARCHAR(255) NULL"
            ).executeUpdate();
        } catch (Exception e) {
            log.warn("Fix kolom password staff: {}", e.getMessage());
        }

        try {
            entityManager.createNativeQuery(
                "ALTER TABLE transaksi MODIFY COLUMN status VARCHAR(20)"
            ).executeUpdate();
        } catch (Exception e) {
            log.warn("Fix kolom status transaksi: {}", e.getMessage());
        }

        try {
            entityManager.createNativeQuery(
                "UPDATE kategori_sampah SET is_active = TRUE WHERE is_active IS NULL"
            ).executeUpdate();
        } catch (Exception e) {
            log.warn("Fix is_active kategori_sampah: {}", e.getMessage());
        }

        try {
            entityManager.createNativeQuery(
                "UPDATE item_sampah SET is_active = TRUE WHERE is_active IS NULL"
            ).executeUpdate();
        } catch (Exception e) {
            log.warn("Fix is_active item_sampah: {}", e.getMessage());
        }

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

        // Force update all seed passwords to "12345"
        updatePassword("admin", "12345");
        updatePassword("adminku", "12345");
        updatePassword("petugas", "12345");
        updatePassword("petugas0", "12345");
        updatePassword("warga", "12345");

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
                    {"Batang Pohon", "Batang kayu dan dahan dari pohon", "Potong kecil-kecil atau keringkan untuk kayu bakar."},
                    {"Bunga Layu", "Bunga potong atau tanaman hias yang sudah layu", "Keringkan atau langsung masukkan ke kompos."},
                    {"Biji-Bijian Busuk", "Biji jagung, kacang, padi yang sudah busuk", "Taburkan ke tanah atau campur ke pakan ternak."},
                    {"Bubur/Sup Basi", "Bubur atau sup yang sudah basi", "Tiriskan cairannya, buang ampasnya ke komposter."},
                    {"Eceng Gondok", "Gulma air eceng gondok", "Jemur hingga kering, bisa difermentasi jadi pupuk cair."},
                    {"Ikan Busuk", "Ikan yang sudah membusuk", "Kubur dalam tanah (bukan di komposter terbuka) karena baunya."},
                    {"Jerami", "Jerami atau sisa panen padi/gandum", "Cacah halus atau dijadikan mulsa untuk tanaman."},
                    {"Kelapa Muda", "Serabut dan tempurung kelapa", "Serabut bisa jadi cocopeat, tempurung bisa jadi arang."},
                    {"Kertas Minyak Bekas", "Kertas pembungkus makanan berminyak", "Buang minyaknya dengan dilap, baru buang ke organik."},
                    {"Kulit Bawang", "Kulit bawang merah dan bawang putih", "Keringkan bisa jadi pewarna alami atau dimasukkan ke kompos."},
                    {"Kulit Jagung", "Pembungkus dan rambut jagung", "Keringkan untuk kerajinan atau masukkan ke kompos."},
                    {"Kulit Kacang", "Kulit kacang tanah, mete, almond", "Remukkan, taburkan ke tanah sebagai mulsa."},
                    {"Kulit Kentang", "Kulit kentang dari dapur", "Pastikan tidak berkecambah, potong kecil-kecil."},
                    {"Lumut", "Lumut yang tumbuh di dinding atau tanah", "Angkat dan masukkan ke kompos."},
                    {"Nasi Beku", "Nasi yang sudah lama di freezer", "Cairkan, tiriskan, buang ke komposter."},
                    {"Pakan Hewan Bekas", "Pakan ayam, ikan, atau kucing yang basi", "Campurkan dengan kompos atau kubur dalam tanah."},
                    {"Potongan Rambut", "Rambut manusia dari potongan rambut", "Taburkan di taman, mengandung nitrogen tinggi."},
                    {"Rebung Tua", "Rebung bambu yang sudah keras", "Cacah tipis-tipis atau rebus dulu sebelum dikompos."},
                    {"Roti Basi/Rempah", "Roti, kue, dan pastry yang sudah basi", "Remukkan, keringkan sebentar baru dimasukkan ke kompos."},
                    {"Sabut Kelapa", "Sabut kelapa tua", "Bisa jadi media tanam atau masukkan ke kompos secara bertahap."},
                    {"Sayur Busuk", "Sayuran yang sudah layu atau busuk", "Buang bagian yang benar-benar busuk, potong kecil-kecil."},
                    {"Serbuk Gergaji", "Serbuk kayu dari hasil gergajian", "Taburkan tipis-tipis di antara lapisan kompos."},
                    {"Sisa Bumbu Dapur", "Sisa bumbu seperti cabai, jahe, kunyit", "Cacah halus, campur rata di komposter."},
                    {"Sisa Kemangi/Pandan", "Daun aromatik sisa masak", "Cacah halus, langsung masukkan ke kompos."},
                    {"Sisa Mie/Roti", "Mie instan basi atau roti berjamur", "Hancurkan, campur dengan tanah sebelum dikompos."},
                    {"Sisa Tahu/Tempe Busuk", "Tahu dan tempe yang sudah busuk", "Hancurkan, campurkan ke komposter sebagai sumber nitrogen."},
                    {"Sisa Tepung", "Tepung terigu, beras, tapioka bekas", "Taburkan tipis agar tidak menggumpal."},
                    {"Tangkai Sayur", "Tangkai bayam, kangkung, sawi", "Potong kecil-kecil agar cepat terurai."},
                    {"Tandan Pisang", "Tandan dan pelepah pisang", "Cacah atau iris tipis-tipis."},
                    {"Tebu", "Ampas tebu atau batang tebu", "Cacah halus, butuh waktu lebih lama untuk terurai."},
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
                    {"Akrilik", "Potongan akrilik bekas display atau hiasan", "Kumpulkan terpisah, bisa didaur ulang menjadi lembaran baru."},
                    {"Alumunium Foil", "Alumunium foil bekas pembungkus makanan", "Bersihkan dari sisa makanan, kumpulkan jadi bola-bola kecil."},
                    {"Banner/MMT", "Spanduk bekas, banner, dan baliho", "Bersihkan dari debu, kumpulkan untuk diolah menjadi tas/kantong."},
                    {"Besi Beton", "Potongan besi beton atau konstruksi", "Kumpulkan di tempat khusus logam, hati-hati dengan karat."},
                    {"Biskaleng/Kaleng Kue", "Kaleng bekas biskuit, kue kering", "Kosongkan bersih, bisa dipakai ulang untuk wadah atau disetor."},
                    {"Buku Telepon", "Buku telepon atau direktori lawas", "Buang sampul kerasnya, kumpulkan bersama kertas bekas."},
                    {"CD/DVD Bekas", "CD, DVD, atau kaset bekas", "Kumpulkan di wadah khusus, bisa dijual ke pengepul."},
                    {"Cermin", "Cermin pecah atau bekas tidak terpakai", "Bungkus dengan kardus/plastik, labeli PECAH BELAH."},
                    {"Dus Mi Instan", "Dus kemasan mi instan (karton)", "Bongkar lipatannya, tumpuk pipih bersama kardus lain."},
                    {"Ember Plastik", "Ember dan baskom plastik bekas", "Bersihkan dari sisa kotoran, tumpuk atau tindih agar tidak memakan tempat."},
                    {"Gantungan Baju", "Gantungan baju plastik atau besi", "Kumpulkan yang masih bagus untuk dipakai ulang, yang rusak disetor."},
                    {"Gelas Kaca", "Gelas kaca pecah atau retak", "Bungkus koran, masukkan wadah khusus, JANGAN di dalam plastik biasa."},
                    {"Gipsum", "Potongan gipsum bekas renovasi", "Kumpulkan terpisah, debunya bisa berbahaya jika dihirup."},
                    {"Handuk Bekas", "Handuk, kain lap, dan kain perca", "Cuci bersih, potong-potong untuk kain lap atau sumbu kompor."},
                    {"Helm Bekas", "Helm bekas yang sudah retak atau rusak", "Lepaskan busa dan pelapis, kumpulkan bagian plastik kerasnya."},
                    {"Induk Kunci/Gembok", "Anak kunci dan gembok rusak", "Kumpulkan di wadah logam campuran."},
                    {"Jarum Pentul/Klip", "Jarum pentul, klip kertas, dan stapler", "Hati-hati dengan ujungnya, kumpulkan dalam kaleng atau wadah tertutup."},
                    {"Jas Hujan", "Jas hujan plastik bekas", "Bersihkan dari debu, keringkan, lipat rapi."},
                    {"Kabel Ties", "Kabel ties plastik bekas pakai", "Kumpulkan sekalian, bisa dilelehkan bersama plastik lain."},
                    {"Kain Perca", "Kain perca sisa jahit-menjahit", "Pisahkan berdasarkan jenis bahan (katun, poliester, dll)."},
                    {"Kain Songket/Tenun", "Kain tradisional bekas", "Jika masih bagus, sumbangkan. Jika rusak, bisa jadi kerajinan."},
                    {"Kalender Bekas", "Kalender kertas bekas tahun lalu", "Buang ring/kawat spiral, kumpulkan kertasnya."},
                    {"Kantong Kresek", "Kantong kresek hitam dan warna bekas", "Bersihkan, lipat rapi, kumpulkan di satu wadah."},
                    {"Karung Goni", "Karung goni atau karung beras bekas", "Kebersihkan debu, bisa dipakai ulang atau untuk pot tanaman."},
                    {"Karung Plastik", "Karung plastik bekas pakan atau pupuk", "Balikkan dan bersihkan, lipat rapi untuk disetor."},
                    {"Kayu Bekas", "Potongan kayu dari bangunan atau palet", "Cabut paku, kumpulkan sesuai ukuran."},
                    {"Kemasan Blister", "Kemasan blister plastik untuk obat atau aksesoris", "Pisahkan dari lapisan alumunium, kumpulkan plastiknya."},
                    {"Kemasan Makanan Ringan", "Bungkus snack, keripik, dan permen", "Bersihkan dari remahan, kumpulkan di botol plastik (ecobrick)."},
                    {"Kemasan Pasta Gigi", "Tube pasta gigi bekas", "Gunting, bersihkan sisa pasta, kumpulkan."},
                    {"Kemasan Sabun", "Botol sabun, sampo, dan pembersih", "Bilas bersih sampai tidak berbusa, keringkan, lepaskan label."},
                    {"Kemasan Sachet", "Sachet bekas kopi, susu, saus", "Gunting bersih, bilas sisa, kumpulkan untuk ecobrick."},
                    {"Kendang/Sterofoam Bekas", "Sterofoam bekas elektronik atau makanan", "Cacah kecil untuk mengurangi volume, jangan dibakar."},
                    {"Keramik", "Potongan keramik, ubin, dan porselen", "Bungkus rapi, labeli TAJAM, kumpulkan di wadah khusus."},
                    {"Kertas Amplas", "Kertas amplas atau sandpaper bekas", "Kumpulkan di wadah khusus, debunya bisa menggores."},
                    {"Kertas Bungkus Nasi", "Kertas pembungkus nasi atau makanan", "Buang sisa makanan, lap minyaknya, kumpulkan."},
                    {"Kertas HVS/Print", "Kertas HVS bekas fotokopi dan print", "Kumpulkan rapi, tidak perlu dibuang sampul/spiralnya."},
                    {"Kertas Kado", "Kertas kado bekas dan pita hadiah", "Lipat rapi jika masih bagus, atau buang ke kertas bekas."},
                    {"Kertas Nasi Bakar", "Kertas pembungkus nasi bakar", "Buang sisa makanan, lap dengan tisu."},
                    {"Kertas Struk", "Kertas struk belanja/ATM (thermal paper)", "Kumpulkan terpisah, mengandung BPA yang sulit didaur ulang."},
                    {"Koper Bekas", "Koper dan tas besar bekas", "Lepaskan resleting, roda, dan lapisan kain, pisahkan material."},
                    {"Korek Api", "Korek api gas bekas", "Pastikan benar-benar habis gasnya, kumpulkan di wadah logam."},
                    {"Kotak Makan Plastik", "Bekas kotak makan plastik (tupperware)", "Cuci bersih, bisa dipakai ulang atau disetor."},
                    {"Kursi Plastik", "Kursi dan meja plastik rusak", "Cacah atau bongkar untuk dikumpulkan dengan plastik keras."},
                    {"Label/Hologram", "Label sticker dan hologram bekas", "Kumpulkan di lembaran plastik atau kertas."},
                    {"Lakban/Selotip", "Lakban dan selotip bekas", "Gulung jadi satu dalam wadah, terbuat dari campuran bahan."},
                    {"Laptop Bekas", "Laptop atau komputer bekas yang tidak terpakai", "Barang elektronik bernilai, jual ke pengepul khusus e-waste."},
                    {"Lemari Bekas", "Lemari kayu atau partikel board bekas", "Bongkar menjadi bagian-bagian, copot engsel dan gagang logam."},
                    {"Logam Campuran", "Campuran logam kecil seperti mur, baut, paku", "Kumpulkan dalam kaleng/wadah besi, setorkan ke pengepul."},
                    {"Mainan Plastik", "Mainan anak dari plastik yang rusak", "Bersihkan, lepaskan baterai, kumpulkan."},
                    {"Majalah/Katalog", "Majalah bekas, katalog, dan brosur", "Sampul keras dilepas, kumpulkan dengan kertas bekas."},
                    {"Map/Snelhekter", "Map kertas atau plastik bekas", "Kosongkan isinya, pisahkan map plastik dengan kertas."},
                    {"Mika Plastik", "Mika plastik bening bekas jilid atau kemasan", "Pisahkan dari kertas, kumpulkan sesuai ukuran."},
                    {"Nampan/Tray Plastik", "Nampan plastik bekas kemasan makanan", "Bilas bersih, kumpulkan di wadah plastik keras."},
                    {"Paket Kardus", "Kardus bekas paket online lengkap dengan bubble wrap", "Pisahkan bubble wrap, bongkar kardus."},
                    {"Palet Kayu", "Palet kayu bekas pengiriman barang", "Cabut paku, kayu bisa dipakai ulang untuk furnitur sederhana."},
                    {"Paku/Sekrup", "Paku, sekrup, dan baut bekas", "Kumpulkan di wadah besi/magnet untuk dipisahkan."},
                    {"Paku Tembak", "Paku tembak/stapler bekas", "Hati-hati saat mengambil, kumpulkan di kaleng tertutup."},
                    {"Panci/Wajan Bekas", "Peralatan dapur dari logam bekas", "Bersihkan dari sisa minyak, bisa disetor sebagai besi tua."},
                    {"Papan Kayu", "Papan kayu bekas bangunan", "Kumpulkan dan bisa dipotong ulang untuk proyek baru."},
                    {"Pecahan Kaca", "Kaca pecah dari jendela atau botol", "Bungkus dengan koran tebal, labeli PECAH BELAH."},
                    {"Pembungkus Bubble", "Bubble wrap plastik bekas paket", "Jangan ditiup ulang, bisa dikumpulkan untuk di daur ulang."},
                    {"Pena/Pensil Bekas", "Pena, pensil, spidol yang sudah habis", "Lepaskan tutupnya, pastikan spidol benar-benar kering."},
                    {"Penggaris Plastik", "Penggaris plastik bekas", "Kumpulkan bersama sampah plastik keras."},
                    {"Penghapus", "Penghapus karet bekas", "Potong kecil-kecil, kumpulkan."},
                    {"Pipa PVC", "Potongan pipa PVC bekas", "Bersihkan dari lem/semen, potong kecil untuk mengurangi volume."},
                    {"Plastik Kemasan Detergen", "Kemasan plastik deterjen bubuk/cair", "Bilas bersih dari sisa deterjen, keringkan."},
                    {"Piring/Mangkuk Pecah", "Piring atau mangkuk keramik/pecah belah", "Bungkus rapi, labeli PECAH BELAH."},
                    {"Raket Bekas", "Raket badminton/tenis bekas", "Pisahkan frame logam dengan senar, kumpulkan terpisah."},
                    {"Rantai Besi", "Rantai besi atau sepeda bekas", "Kumpulkan di wadah logam, bisa dijual ke pengepul."},
                    {"Rempah-rempah Kering", "Bumbu kemasan yang sudah kedaluwarsa (kemasan)", "Kosongkan isinya ke organik, plastik kemasan ke anorganik."},
                    {"Resleting Bekas", "Resleting dari pakaian atau tas bekas", "Potong dari kainnya, kumpulkan bagian logam/plastiknya."},
                    {"Ringkusan Nasi", "Kertas atau daun pembungkus nasi", "Pisahkan kertas dari daun, daun ke organik."},
                    {"Sendal/Sepatu Bekas", "Sendal dan sepatu bekas yang sudah rusak", "Pisahkan sol karet dengan bagian atas kain/kulit."},
                    {"Sendok/Garpu Plastik", "Sendok, garpu, dan pisau plastik bekas", "Bilas bersih dari sisa makanan, keringkan."},
                    {"Seng Bekas", "Seng gelombang atau atap bekas", "Hati-hati dengan pinggiran tajam, kumpulkan di tempat logam."},
                    {"Sisa Kabel Tembaga", "Potongan kabel tembaga atau serabut", "Kupas pembungkusnya untuk mendapatkan tembaga murni."},
                    {"Sisa Kain Pelapis", "Kain pelapis furnitur atau sofa bekas", "Cuci bersih, potong kecil untuk kain lap atau keset."},
                    {"Sol Karet", "Sol sepatu karet bekas", "Kumpulkan di wadah karet, bisa diolah ulang untuk lantai."},
                    {"Spidol Bekas", "Spidol whiteboard atau marker bekas", "Pastikan benar-benar kering, kumpulkan di wadah plastik."},
                    {"Sterofoam Lembaran", "Sterofoam tipis dari kemasan makanan", "Bersihkan sisa makanan, keringkan, tumpuk rapi."},
                    {"Stiker Bekas", "Stiker dan label bekas", "Kumpulkan di satu lembar kertas atau plastik."},
                    {"Stopmap/Ordner", "Stopmap dan ordner plastik bekas", "Lepaskan ring logam, kumpulkan plastiknya."},
                    {"Tali Rafia", "Tali rafia bekas ikat", "Kumpulkan dan lilit jadi satu gulungan."},
                    {"Tali Plastik", "Tali plastik atau nilon bekas", "Potong kecil-kecil atau kumpulkan jadi satu."},
                    {"Tangki Air Bekas", "Tangki air plastik atau fiberglass bekas", "Kosongkan bersih, potong jika perlu untuk transportasi."},
                    {"Tas Belanja", "Tas belanja non-woven atau plastik bekas", "Cuci bersih, bisa dipakai ulang atau setorkan."},
                    {"Tas/Koper", "Tas, ransel, dan koper bekas", "Lepaskan resleting dan aksesoris logam, pisahkan material."},
                    {"Teh Celup Bekas", "Kantong teh celup bekas (kemasan)", "Gunting, buang ampas teh ke organik, kantung ke anorganik."},
                    {"Tempat Pensil", "Tempat pensil atau kotak alat tulis", "Kosongkan, kumpulkan di wadah plastik keras."},
                    {"Tembaga/Kuningan", "Potongan tembaga atau kuningan bekas", "Bernilai jual tinggi, kumpulkan terpisah dari logam lain."},
                    {"Tempurung Kelapa", "Tempurung kelapa keras", "Bisa jadi arang aktif atau kerajinan, atau digerus untuk kompos."},
                    {"Tisu Basah Bekas", "Tisu basah kemasan (bukan organik)", "Kumpulkan ke anorganik karena mengandung serat sintetis."},
                    {"Topi Bekas", "Topi kain atau plastik bekas", "Cuci bersih, lepaskan aksesoris logam/plastik."},
                    {"Triplek/MDF", "Potongan triplek atau MDF bekas", "Jangan dibakar (mengandung lem formaldehida), kumpulkan di kayu."},
                    {"Tutup Botol", "Tutup botol plastik maupun logam", "Kumpulkan terpisah dari botolnya."},
                    {"Wafer/Kemasan Wafer", "Kemasan wafer dan biskuit (plastik + alumunium)", "Kumpulkan di ecobrick atau waste-to-energy."},
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
                    {"Alat Catok Rambut", "Catok, curling iron, dan hair dryer rusak", "Pisahkan dari kabel, kumpulkan di wadah e-waste."},
                    {"Alat Kesehatan Bekas", "Alat suntik bekas, jarum infus, lancet", "Wajib masukkan ke safety box khusus, serahkan ke puskesmas."},
                    {"Ampas Cat Tembok", "Sisa cat tembok atau kaleng cat bekas", "Tutup rapat kaleng, keringkan dengan menyerap pasir/kertas."},
                    {"Amplas Bekas", "Kertas amplas bekas yang mengandung debu logam/timah", "Kumpulkan dalam wadah tertutup, debunya berbahaya."},
                    {"Asbes Bekas", "Lembaran asbes atau eternit bekas bangunan", "Basahi dulu sebelum dibongkar agar debunya tidak terhirup."},
                    {"Bahan Kimia Foto", "Cairan fixer, developer foto bekas", "Tempatkan di wadah asli, serahkan ke pengelola khusus."},
                    {"Bahan Pembersih Kolam", "Kaporit, algaesida, dan pembersih kolam", "Tutup rapat wadah asli, jauhkan dari jangkauan anak."},
                    {"Bahan Pembersih Toilet", "Pembersih toilet dan pemutih pakaian di wadah asli", "Jangan dicampur dengan pembersih lain (gas beracun)."},
                    {"Baterai Handphone", "Baterai HP dan powerbank yang mengembang", "JANGAN dibuang di tempat sampah biasa! Resiko kebakaran."},
                    {"Baterai Kancing", "Baterai kancing (button cell) jam atau kalkulator", "Simpan di wadah tertutup rapat, jangan sampai tertelan."},
                    {"Batu Baterai Laptop", "Baterai lithium laptop/powerbank rusak", "Keluarkan dari laptop, simpan di wadah tahan api."},
                    {"Bell/Buzzer Elektronik", "Bel rumah atau buzzer elektronik rusak", "Kumpulkan bersama e-waste, lepaskan baterai."},
                    {"Besi Berkarat Beracun", "Besi yang terkontaminasi oli, cat, atau bahan kimia", "Bersihkan dulu jika memungkinkan, jika tidak kumpulkan sebagai B3."},
                    {"Beton Bertulang", "Beton bekas bangunan yang mengandung kawat", "Kumpulkan di tempat khusus limbah konstruksi."},
                    {"Blender/Mixer Rusak", "Blender, mixer, atau food processor rusak", "Pisahkan bagian kaca/plastik dari motor elektronik."},
                    {"Bohlam LED", "Bohlam LED rusak", "Bungkus dengan kardus, jangan dipecahkan karena mengandung komponen elektronik."},
                    {"Bubuk Pemadam Api", "Tabung pemadam api yang sudah kedaluwarsa", "Kembalikan ke service center pemadam api, jangan dibuang."},
                    {"Cairan Antibiotik", "Obat-obatan cair (sirup) yang sudah kedaluwarsa", "Kembalikan ke apotek untuk disposal khusus."},
                    {"Cairan Cat Semprot", "Kaleng cat semprot yang masih ada isinya", "Semprotkan ke kardus bekas sampai habis, baru buang kalengnya."},
                    {"Cairan Desinfektan", "Desinfektan dan hand sanitizer kedaluwarsa", "Encerkan dengan air banyak dulu sebelum dibuang ke saluran."},
                    {"Cairan Fotokopi", "Toner dan tinta fotokopi bekas", "Kumpulkan di wadah tertutup, serahkan ke pengelola khusus."},
                    {"Cairan Pembersih Lantai", "Pembersih lantai dan pembersih kaca kedaluwarsa", "Jangan dicampur, buang sesuai petunjuk kemasan."},
                    {"Cairan Pendingin (Freon)", "Freon AC atau kulkas bekas", "Hanya boleh ditangani teknisi bersertifikat."},
                    {"Cairan Pemutih", "Pemutih pakaian dalam wadah asli", "Jangan dicampur dengan amonia (gas beracun)."},
                    {"Cairan Pengawet", "Formalin atau pengawet makanan ilegal", "Serahkan ke BPOM atau laboratorium kimia terdekat."},
                    {"Cairan Raksa (Merkuri)", "Merkuri cair dari termometer atau alat ukur", "JANGAN disentuh langsung, pakai sarung tangan nitril."},
                    {"Cat Minyak/Thinner", "Cat minyak dan thinner bekas", "Simpan di wadah tertutup rapat di tempat sejuk."},
                    {"Cat Rambut", "Produk cat rambut dan bahan kimianya", "Kumpulkan kemasan yang sudah habis, jangan dibuang ke wastafel."},
                    {"CPU / Prosesor Bekas", "CPU komputer, motherboard, dan komponen PC rusak", "Lepaskan baterai CMOS, kumpulkan bagian elektronik."},
                    {"Deterjen Cair/Sabun", "Deterjen dan sabun cair kedaluwarsa", "Encerkan dan buang sedikit demi sedikit."},
                    {"Dupa/Hio Bekas", "Sisa dupa, hio, dan aroma terapi", "Padamkan total, rendam air sebelum dibuang."},
                    {"Filter AC", "Filter udara AC dan penyaring udara bekas", "Kumpulkan di wadah tertutup, debunya berbahaya jika terhirup."},
                    {"Filter Oli", "Filter oli kendaraan bekas", "Tiriskan oli selama 24 jam, kumpulkan filter sebagai B3."},
                    {"Genset Bekas", "Genset dan mesin bermotor bekas", "Kuras oli dan bahan bakar dulu, kumpulkan body logamnya."},
                    {"Handphone Rusak", "HP, tablet, dan smartphone rusak total", "Jangan dibuang di tempat sampah biasa, serahkan ke drop box e-waste."},
                    {"Hard Disk Bekas", "HDD atau SSD bekas komputer", "Hapus data dengan software khusus, baru setorkan."},
                    {"Insektisida/Pestisida", "Pestisida, fungisida, dan insektisida kedaluwarsa", "Simpan di tempat sejuk dan gelap, jauh dari makanan."},
                    {"Isi Ulang Tinta Printer", "Tinta printer dan cartridge bekas", "Jangan dibongkar, serahkan ke tempat pengisian ulang tinta."},
                    {"Jarum Suntik / Lancet", "Jarum suntik bekas pakai (non-medis)", "Wajib dimasukkan ke wadah anti-tusuk (safety box)."},
                    {"Kaleng Bekas Cat", "Kaleng bekas cat tembok atau cat kayu", "Biarkan kering dengan tutup terbuka, baru kumpulkan."},
                    {"Kaleng Semprot Nyamuk", "Kaleng semprotan anti nyamuk dan serangga", "Kosongkan di tempat terbuka, jangan dibakar."},
                    {"Kamera Bekas", "Kamera digital atau analog rusak", "Lepaskan baterai, kumpulkan sebagai e-waste."},
                    {"Karbol/Pembersih Lantai", "Karbol dan pembersih lantai kedaluwarsa", "Encerkan dengan air, buang sedikit demi sedikit."},
                    {"Karet Ban Bekas", "Ban bekas kendaraan dan karet industri", "Kumpulkan di tempat khusus ban bekas untuk didaur ulang."},
                    {"Kateter / Selang Medis", "Selang medis bekas (non-infeksius)", "Potong-potong, kumpulkan dalam wadah tertutup."},
                    {"Kawat Las", "Kawat las dan solder bekas", "Kumpulkan di wadah logam, mengandung timah hitam."},
                    {"Kemasan Bahan Kimia", "Botol/kaleng bekas bahan kimia laboratorium", "Bilas 3 kali dengan air, buang air bilasannya sebagai B3."},
                    {"Kemasan Isi Ulang Pembersih", "Kemasan refill sabun, shampo, pelembut", "Bilas bersih 3 kali, baru buang ke anorganik (jika bersih)."},
                    {"Kemasan Racun Tikus", "Kemasan bekas racun tikus dan serangga", "JANGAN dibuka sembarangan, kumpulkan di wadah double plastik."},
                    {"Kertas Karbon", "Kertas karbon bekas dan kertas NCR", "Kumpulkan di wadah khusus karena mengandung zat kimia."},
                    {"Keyboard / Mouse", "Keyboard dan mouse komputer rusak", "Lepaskan kabelnya, kumpulkan sebagai e-waste."},
                    {"Kipas Angin Rusak", "Kipas angin, exhaust fan, dan blower rusak", "Pisahkan motor listrik dari body plastik/logam."},
                    {"Kompor Gas Bekas", "Kompor gas dan tabung portable bekas", "Pastikan tidak ada sisa gas, lepaskan regulatornya."},
                    {"Kulkas Bekas", "Kulkas, freezer, dan AC bekas", "Hanya boleh dibongkar oleh teknisi untuk mengambil freon."},
                    {"Kunyit / Pewarna Alami", "Sisa pewarna alami (indigosol, dll)", "Kumpulkan di wadah terpisah, jangan dibuang ke sungai."},
                    {"Laptop Rusak", "Laptop atau notebook yang rusak total", "Lepaskan baterai, hard disk, serahkan ke pengepul e-waste."},
                    {"Lem / Perekat", "Lem super, lem kayu, lem kertas bekas", "Biarkan kering dalam wadah asli, baru buang."},
                    {"Lemari Es Bekas", "Kulkas dan freezer bekas", "Freon harus dikuras oleh teknisi bersertifikat."},
                    {"Limbah Cat Tembok", "Sisa cat tembok yang sudah dicampur air", "Keringkan dengan menyerap ke kardus/koran bekas."},
                    {"Limbah Farmasi", "Limbah obat-obatan dari rumah sakit atau klinik", "Harus ditangani oleh pihak ketiga bersertifikat."},
                    {"Limbah Oli", "Oli mesin bekas kendaraan", "Tampung di jerigen bersih, jangan tercampur air/sampah."},
                    {"Limbah Sablon", "Limbah cair sablon dan tinta sablon", "Kumpulkan di wadah kedap udara, serahkan ke pengolah limbah."},
                    {"Limbah Tahu", "Limbah cair pabrik tahu (whey)", "Kandungan asam tinggi, perlu dinetralisir dulu sebelum dibuang."},
                    {"Limbah Tekstil", "Limbah pewarna kain dan fixation", "Kumpulkan di wadah tertutup, jangan dibuang ke drainase."},
                    {"Limbah Tinta", "Tinta printer, tinta sablon, tinta stempel", "Kumpulkan di botol kaca, serahkan ke pengepul khusus."},
                    {"Limbah Udara (Filter)", "Filter udara bekas dari industri", "Gunakan APD saat membongkar, kumpulkan di wadah tertutup."},
                    {"Limbah Wsatewater", "Sludge atau lumpur hasil pengolahan air limbah", "Perlu penanganan khusus oleh pihak ketiga."},
                    {"Lilin Bekas", "Lilin bekas dan sisa lilin aromaterapi", "Lelehkan dan saring, bisa dipakai ulang. Jika tidak, buang ke B3."},
                    {"Liyin / Alkohol", "Alkohol dan antiseptik kedaluwarsa", "Jangan dibuang ke api terbuka, encerkan dengan air."},
                    {"Logam Berat (Timah)", "Potongan timah, solder, dan logam berat lain", "Kumpulkan di wadah khusus, jauhkan dari anak-anak."},
                    {"LPG / Gas Lain", "Tabung gas LPG mini atau gas isi ulang", "Kembalikan ke agen resmi, jangan dibuang."},
                    {"Mainan Elektronik", "Mainan elektronik bekas (baterai + sirkuit)", "Lepaskan baterai, kumpulkan di wadah e-waste."},
                    {"Mesin Cuci Bekas", "Mesin cuci dan pengering rusak", "Kuras airnya, lepaskan selang dan kabel."},
                    {"Mesin Fotokopi", "Mesin fotokopi dan printer besar bekas", "Mengandung toner dan drum, butuh penanganan khusus."},
                    {"Mesin Jahit Elektrik", "Mesin jahit listrik rusak", "Pisahkan motor dari body mesin."},
                    {"Microwave / Oven", "Microwave dan oven listrik rusak", "Mengandung komponen elektronik, jangan dibongkar sendiri."},
                    {"Minuman Keras (Alkohol)", "Minuman keras kedaluwarsa atau sisa pesta", "Tuang di tanah/area terbuka, jauh dari sumber api."},
                    {"Modem / Router", "Modem, router, dan switch jaringan rusak", "Lepaskan adaptor dan kabel, kumpulkan sebagai e-waste."},
                    {"Monitor LCD/LED", "Monitor komputer dan TV rusak", "Mengandung kaca dan cairan berbahaya, butuh penanganan khusus."},
                    {"Morton / Pupuk Kimia", "Pupuk kimia kedaluwarsa atau sisa panen", "Simpan di wadah asli, jangan campur dengan pupuk lain."},
                    {"Neraca / Timbangan Digital", "Timbangan digital dan analog bekas", "Lepaskan baterai, kumpulkan bagian elektronik."},
                    {"Obat Gosok/Balsem", "Obat gosok, balsem, dan minyak kayu putih kedaluwarsa", "Kumpulkan di wadah asli, serahkan ke apotek."},
                    {"Obat Nyamuk Bakar", "Sisa obat nyamuk bakar dan elektrik", "Padamkan total, basahi dengan air sebelum dibuang."},
                    {"Obat Nyamuk Elektrik", "Obat nyamuk elektrik (cairan/mat) bekas", "Kosongkan cairan ke tisu/kain, buang wadahnya."},
                    {"Obat Tetes Mata", "Obat tetes mata dan telinga kedaluwarsa", "Kembalikan ke apotik untuk disposal yang tepat."},
                    {"Pakaian Terkena Kimia", "Pakaian atau lap yang terkena bahan kimia berbahaya", "Jangan dicampur cucian lain, kumpulkan di wadah khusus."},
                    {"Pemadam Api Ringan", "APAR (tabung pemadam) yang sudah kedaluwarsa", "Kembalikan ke jasa isi ulang APAR."},
                    {"Pembersih Kaca", "Pembersih kaca dan pembersih furnitur kedaluwarsa", "Semprotkan ke kain perca sampai habis, buang kainnya."},
                    {"Pembersih Oven", "Pembersih oven dan pemanggang kedaluwarsa", "Gunakan sesuai petunjuk sampai habis, jangan dibuang langsung."},
                    {"Pembungkus Baterai", "Plastik/kardus pembungkus baterai baru", "Buang ke anorganik (sudah bersih)."},
                    {"Pembungkus Makanan Beracun", "Kemasan yang terkontaminasi pestisida/kimia", "Double bag dengan plastik tebal, labeli B3."},
                    {"Pengusir Serangga (Ultrasonik)", "Alat pengusir tikus/serangga elektronik rusak", "Lepaskan baterai/adaptor, kumpulkan sebagai e-waste."},
                    {"Pengharum Ruangan Elektrik", "Pengharum ruangan elektrik rusak", "Kosongkan cairan isinya dulu, baru buang alatnya."},
                    {"Pensil Mekanik Bekas", "Pensil mekanik dan isinya (timah)", "Kumpulkan, timahnya mengandung grafit berbahaya jika dihirup."},
                    {"Perangkat Jaringan", "Access point, antenna wifi, hub rusak", "Kumpulkan sebagai e-waste elektronik."},
                    {"Peralatan Dapur Elektrik", "Rice cooker, magic com, dan dispenser rusak", "Pisahkan kabel power, kumpulkan elemen elektroniknya."},
                    {"Pestisida Padat", "Pestisida butiran atau bubuk kedaluwarsa", "Jangan dibuang di saluran air! Bungkus rapat dan labeli."},
                    {"Petasan / Mercon", "Petasan dan kembang api bekas atau sisa", "Rendam dalam air selama 24 jam sebelum dibuang."},
                    {"Piringan Hitam", "Piringan hitam dan vinyl bekas", "Kumpulkan untuk didaur ulang menjadi kerajinan."},
                    {"Power Supply", "Power supply dan adaptor listrik rusak", "Kumpulkan di wadah e-waste, lepaskan kabelnya."},
                    {"Printer Bekas", "Printer rusak, termasuk cartridge dan toner", "Keluarkan cartridge, serahkan ke program daur ulang."},
                    {"Proyektor / LCD", "Proyektor dan LCD proyektor rusak", "Mengandung lampu merkuri dan komponen optik."},
                    {"Racun Serangga Semprot", "Semprotan anti serangga yang masih ada isinya", "Semprotkan ke kardus di tempat terbuka sampai habis."},
                    {"Racun Tikus Butiran", "Racun tikus butiran/warlock", "Kumpulkan sisa dengan sendok, jangan disentuh tangan langsung."},
                    {"Radio / Tape / Speaker", "Radio, tape, dan speaker aktif rusak", "Lepaskan baterai jika ada, kumpulkan sebagai e-waste."},
                    {"Raksa (Mercury)", "Merkuri dari tambang, laboratorium, atau termometer", "JANGAN disentuh! Butuh penanganan tim khusus berbahaya."},
                    {"Remote / Control", "Remote TV, AC, dan perangkat elektronik rusak", "Lepaskan baterai, kumpulkan di e-waste."},
                    {"Residu Cat Semprot", "Kaleng cat semprot yang sudah disemprotkan", "Lubangi kaleng sebelum dibuang ke besi tua."},
                    {"Residu Lem", "Sisa lem pada wadah yang sudah mengering", "Kumpulkan wadah ke anorganik jika sudah benar-benar kering."},
                    {"Residu Minyak Goreng", "Minyak goreng bekas pakai jelantah", "Tampung di botol, jangan dibuang ke saluran air."},
                    {"Residu Tinta Sablon", "Tinta sablon sisa dan lap yang terkena tinta", "Kumpulkan di wadah kedap udara."},
                    {"Rokok Elektrik (Vape)", "Vape, pod, dan cartridge rokok elektrik bekas", "Lepaskan baterai, kumpulkan bagian cairan sisa sebagai B3."},
                    {"Router / Switch Rusak", "Perangkat jaringan yang tidak berfungsi", "Kumpulkan di e-waste jaringan."},
                    {"Ruang Isolasi Medis", "Limbah medis dari ruang isolasi infeksius", "Harus ditangani oleh pihak medis berwenang."},
                    {"Sabun / Shampo Kedaluwarsa", "Sabun, shampo, dan produk mandi kedaluwarsa", "Encerkan dan buang sedikit demi sedikit ke saluran."},
                    {"Safety Box Medis", "Safety box berisi jarum suntik bekas", "Wajib diserahkan ke puskesmas/faskes terdekat."},
                    {"Selang Infus / Kateter", "Selang medis yang sudah tidak terpakai", "Potong kecil-kecil, kumpulkan di wadah tertutup."},
                    {"Setrika Rusak", "Setrika uap atau setrika biasa rusak", "Kosongkan airnya, lepaskan kabel."},
                    {"Sisa Cat Kayu", "Sisa cat kayu dan cat besi dalam kaleng", "Tutup rapat kaleng, simpan di tempat sejuk."},
                    {"Sisa Cat Minyak", "Sisa cat minyak dan thinner", "Simpan di wadah kaca atau kaleng asli, labeli B3."},
                    {"Sisa Dempul / Plamir", "Sisa dempul tembok dan plamir", "Biarkan mengering di wadah, baru buang."},
                    {"Sisa Lem / Solasi", "Lem kertas dan solasi bekas", "Kumpulkan di satu wadah jika sudah kering."},
                    {"Sisa Oli Samping", "Oli samping motor 2-tak kedaluwarsa", "Kumpulkan di jerigen kecil, serahkan ke bengkel."},
                    {"Sisa Semen / Mortar", "Sisa semen dan mortar instan", "Biarkan mengeras, baru buang sebagai limbah konstruksi."},
                    {"Sisa Tinta Printer", "Refill tinta printer dan toner bekas", "Simpan di wadah asli, jangan tumpah."},
                    {"Sludge Industri", "Lumpur hasil pengolahan air limbah pabrik", "Butuh disposal khusus oleh vendor berlisensi."},
                    {"Spray Fixative", "Semprotan fixative dan varnish cat", "Semprot di area ventilasi baik sampai habis."},
                    {"Stabilizer / Stavolt", "Stavolt dan stabilizer listrik rusak", "Mengandung kumparan tembaga dan komponen elektronik."},
                    {"Tabung Elpiji Mini", "Tabung gas portable (blue/filano) bekas", "Kembalikan ke agen isi ulang resmi."},
                    {"Tabung Gas 3kg/12kg", "Tabung gas elpiji kosong", "Kembalikan ke pangkalan gas terdekat."},
                    {"Tabung Oksigen", "Tabung oksigen medis kosong", "Kembalikan ke distributor alat kesehatan."},
                    {"Tabung Pemadam CO2", "APAR karbon dioksida yang kedaluwarsa", "Kembalikan ke jasa perawatan APAR."},
                    {"Tali Karet Bekas", "Karet gelang dan tali karet bekas", "Kumpulkan, bisa dilebur menjadi karet daur ulang."},
                    {"Tanah / Air Terkontaminasi", "Tanah atau air yang terkena tumpahan B3", "Serap dengan pasir/zeolit, kumpulkan di wadah tertutup."},
                    {"Tang / Obeng Bekas", "Alat tangan bekas yang terkontaminasi kimia", "Bersihkan dulu dengan pelarut, baru setorkan ke logam."},
                    {"Tegangan Tinggi", "Kapasitor dan trafo tegangan tinggi rusak", "Hanya boleh ditangani oleh teknisi listrik."},
                    {"Televisi CRT/LED", "TV tabung (CRT) atau LED rusak", "CRT mengandung timbal dan fosfor, butuh penanganan khusus."},
                    {"Termometer Alkohol", "Termometer alkohol atau galium rusak", "Lebih aman dari raksa tapi tetap kumpulkan di B3."},
                    {"Tikar Listrik", "Tikar listrik penghangat rusak", "Lepaskan kabel, kumpulkan kabelnya sebagai e-waste."},
                    {"Timah Solder", "Limbah timah solder dari reparasi elektronik", "Kumpulkan di wadah logam, bernilai jual sebagai scrap."},
                    {"Tinta Cetak Sablon", "Limbah tinta sablon dan emulsi", "Kumpulkan di wadah kedap udara."},
                    {"Tinta Refill Printer", "Botol tinta printer refill bekas", "Jangan dibuang di wastafel, kumpulkan di wadah khusus."},
                    {"Toner Bekas Printer", "Catridge toner bekas printer laser", "Kembalikan ke vendor untuk program refill/daur ulang."},
                    {"Trafo / Adaptor", "Trafo step down dan adaptor listrik rusak", "Kumpulkan di e-waste, mengandung kumparan tembaga."},
                    {"TV Tabung Bekas", "Televisi tabung (CRT) yang rusak", "Mengandung timbal dan fosfor, butuh penanganan khusus."},
                    {"UPS / AVR Bekas", "UPS dan AVR listrik yang rusak", "Lepaskan baterai UPS, kumpulkan terpisah."},
                    {"Vaksin / Serum Kedaluwarsa", "Vaksin dan serum kedaluwarsa", "Harus dimusnahkan di incinerator medis."},
                    {"VCR / DVD Player", "Pemutar VCR, DVD, dan Blu-Ray rusak", "Kumpulkan sebagai e-waste elektronik."},
                    {"Ventilator / Alat Medis", "Alat medis elektronik bekas", "Serahkan ke puskesmas atau distributor alat medis."},
                    {"Water Heater Listrik", "Pemanas air listrik rusak", "Kosongkan air, lepaskan kabel dan elemen pemanas."},
                    {"X-Ray / Film Foto", "Film rontgen dan foto bekas", "Mengandung perak, kumpulkan di wadah khusus."},
                    {"Zat Warna Tekstil", "Pewarna tekstil dan batik sintetis", "Jangan dibuang ke saluran air, endapkan dulu dengan tawas."},
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
        k.setIsActive(true);
        return k;
    }

    private void buatItemSampah(String nama, String deskripsi, String instruksi, KategoriSampah kategori) {
        ItemSampah item = new ItemSampah();
        item.setNamaItem(nama);
        item.setDeskripsi(deskripsi);
        item.setInstruksiPenanganan(instruksi);
        item.setKategoriSampah(kategori);
        item.setIsActive(true);
        itemSampahRepo.save(item);
    }
}


