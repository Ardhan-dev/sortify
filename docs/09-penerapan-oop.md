# PENERAPAN KONSEP OOP (Object-Oriented Programming) PADA APLIKASI SORTIFY

---

## A. ENCAPSULATION (Enkapsulasi)

**Definisi:** Enkapsulasi adalah konsep menyembunyikan data (fields) di dalam class dan hanya menyediakan akses terkontrol melalui method getter dan setter. Tujuannya melindungi integritas data dan menyembunyikan kompleksitas internal.

### A.1 Implementasi menggunakan Lombok @Data

Semua **15 entity class** menggunakan anotasi `@Data` dari Lombok yang secara otomatis menghasilkan getter dan setter untuk setiap field private.

**File:** `src/main/java/com/SortifyTeam/Sortify/model/User.java`

```java
@Entity
@Table(name = "users")
@Data   // ← Otomatis generate: getter, setter, toString, equals, hashCode
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // private — tidak bisa diakses langsung

    @Column(unique = true, nullable = false)
    private String username;            // private

    @Column(nullable = false)
    private String password;            // private

    @Column(nullable = false)
    private String fullName;            // private

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                  // private

    @Column(nullable = false)
    private int totalPoints = 0;        // private

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;  // private

    @Column(nullable = false)
    private int warningCount = 0;       // private

    private String fotoProfil;          // private
}
```

**Penjelasan:**

1. Semua field dideklarasikan sebagai `private` — tidak bisa diakses dari luar class.
2. Lombok `@Data` secara otomatis menghasilkan getter (`getId()`, `getUsername()`, `getRole()`) dan setter (`setId()`, `setUsername()`, `setRole()`).
3. Akses ke data hanya melalui method publik, bukan langsung ke field.
4. Contoh penggunaan di Controller:
   ```java
   user.getUsername();      // ✅ benar — melalui getter
   user.username;           // ❌ salah — private, tidak bisa diakses langsung
   ```

**Bukti pada entity lain:**

| Entity Class | File | Baris |
|-------------|------|-------|
| Warga | `model/Warga.java` | 11 |
| Staff | `model/Staff.java` | 11 |
| DropPoint | `model/DropPoint.java` | 8 |
| Transaksi | `model/Transaksi.java` | 13 |
| TransaksiDetail | `model/TransaksiDetail.java` | 9 |
| KategoriSampah | `model/KategoriSampah.java` | 8 |
| ItemSampah | `model/ItemSampah.java` | 8 |
| RewardItem | `model/RewardItem.java` | 11 |
| PenukaranReward | `model/PenukaranReward.java` | 9 |
| Notifikasi | `model/Notifikasi.java` | 9 |
| PointHistory | `model/PointHistory.java` | 9 |
| LogAktivitas | `model/LogAktivitas.java` | 9 |
| Pengumuman | `model/Pengumuman.java` | 9 |
| Warning | `model/Warning.java` | 9 |

### A.2 Implementasi Manual Getter/Setter pada DTO

Tidak semua class menggunakan Lombok. DTO (Data Transfer Object) dibuat manual untuk kontrol penuh.

**File:** `src/main/java/com/SortifyTeam/Sortify/dto/RegisterDTO.java`

```java
public class RegisterDTO {
    private String username;            // field private
    private String password;            // field private
    private String confirmPassword;     // field private
    private String namaDepan;           // field private
    private String namaBelakang;        // field private
    private String alamat;              // field private
    private String noTelepon;           // field private
    private Long dropPointId;           // field private

    // Getter dan Setter manual untuk akses terkontrol
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getNamaDepan() { return namaDepan; }
    public void setNamaDepan(String namaDepan) { this.namaDepan = namaDepan; }

    public String getNamaBelakang() { return namaBelakang; }
    public void setNamaBelakang(String namaBelakang) { this.namaBelakang = namaBelakang; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }

    public Long getDropPointId() { return dropPointId; }
    public void setDropPointId(Long dropPointId) { this.dropPointId = dropPointId; }
}
```

### A.3 Computed Getter dengan @Transient

Enkapsulasi tidak hanya untuk data mentah, tetapi juga untuk hasil komputasi.

**File:** `src/main/java/com/SortifyTeam/Sortify/model/User.java` (baris 52-66)

```java
@Transient   // ← Field ini tidak disimpan ke database
public String getPangkat() {
    // Logika perhitungan pangkat disembunyikan di dalam class
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
```

**Analisis:**
- Field `totalPoints` tetap private — tidak bisa diubah sembarangan.
- Method `getPangkat()` menggunakan nilai `totalPoints` untuk menghitung pangkat.
- Pengguna class (misal template Thymeleaf) cukup panggil `${user.pangkat}` tanpa tahu logika perhitungannya.
- Jika aturan pangkat berubah, cukup edit satu method, tidak perlu ubah di banyak tempat.

**File:** `src/main/java/com/SortifyTeam/Sortify/model/Warga.java` (baris 40-50)

```java
@Transient
public String getPangkat() {
    // Delegasikan ke User — menerapkan enkapsulasi berlapis
    if (user == null) return "Bronze (Eco-Starter)";
    return user.getPangkat();
}

@Transient
public String getBadgePangkat() {
    if (user == null) return "secondary";
    return user.getBadgePangkat();
}
```

---

## B. INHERITANCE (Pewarisan)

**Definisi:** Inheritance memungkinkan sebuah class mewarisi properti dan method dari class lain (parent class) atau interface. Ini mendukung reusability dan hierarki class.

### B.1 Repository Inheritance (extends JpaRepository)

Semua **14 repository interface** mewarisi dari `JpaRepository` yang merupakan turunan dari `PagingAndSortingRepository` → `CrudRepository` → `Repository`.

Hierarki inheritance:

```
Repository<T, ID>              ← interface paling atas
    ↑
CrudRepository<T, ID>          ← method CRUD dasar (save, findById, findAll, delete)
    ↑
PagingAndSortingRepository<T, ID>  ← method pagination & sorting
    ↑
JpaRepository<T, ID>           ← method tambahan (flush, batch)
    ↑
    ┌────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┐
User  War  Sta  DP   KTg  Item Trx  TrxD Rwd  Pnk  Ntf  PH   Log  Png  Wrn
Repo  g    ff   Repo Repo Repo Repo etil ewar Repo Repo Akt Repo um   Repo
      Repo Repo            Repo Repo Repo d   Repo                  Repo
```

**Tabel Repository beserta method yang diwarisi:**

| Repository | File | Entity | Method Warisan |
|-----------|------|--------|---------------|
| UserRepository | `repository/UserRepository.java:16` | User | `save()`, `findById()`, `findAll()`, `count()`, `delete()`, `findAll(Pageable)`, `findAll(Sort)` |
| WargaRepository | `repository/WargaRepository.java:12` | Warga | Sama |
| StaffRepository | `repository/StaffRepository.java:11` | Staff | Sama |
| DropPointRepository | `repository/DropPointRepository.java:10` | DropPoint | Sama |
| KategoriSampahRepository | `repository/KategoriSampahRepository.java:9` | KategoriSampah | Sama |
| ItemSampahRepository | `repository/ItemSampahRepository.java:11` | ItemSampah | Sama |
| TransaksiRepository | `repository/TransaksiRepository.java:16` | Transaksi | Sama |
| TransaksiDetailRepository | `repository/TransaksiDetailRepository.java:13` | TransaksiDetail | Sama |
| RewardItemRepository | `repository/RewardItemRepository.java:14` | RewardItem | Sama |
| PenukaranRewardRepository | `repository/PenukaranRewardRepository.java:16` | PenukaranReward | Sama |
| NotifikasiRepository | `repository/NotifikasiRepository.java:11` | Notifikasi | Sama |
| PointHistoryRepository | `repository/PointHistoryRepository.java:11` | PointHistory | Sama |
| LogAktivitasRepository | `repository/LogAktivitasRepository.java:9` | LogAktivitas | Sama |
| PengumumanRepository | `repository/PengumumanRepository.java:9` | Pengumuman | Sama |
| WarningRepository | `repository/WarningRepository.java:11` | Warning | Sama |

**Contoh deklarasi:**

```java
// Semua 14 repository mewarisi JpaRepository
public interface UserRepository extends JpaRepository<User, Long> { }
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> { }
public interface RewardItemRepository extends JpaRepository<RewardItem, Long> { }
// ... dan seterusnya
```

**Manfaat:**
- Tidak perlu menulis method CRUD manual — semua diwarisi dari parent.
- Konsistensi — setiap repository memiliki method yang sama.
- Fitur tambahan seperti pagination dan sorting otomatis tersedia.

### B.2 Implementasi Interface Spring Security

Beberapa class mengimplementasikan interface dari framework Spring untuk menyesuaikan perilaku sistem.

**File:** `src/main/java/com/SortifyTeam/Sortify/config/CustomUserDetailsService.java` (baris 15)

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    // UserDetailsService adalah interface dari Spring Security

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + username));

        // Cek status akun
        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new DisabledException("Akun Anda telah di-suspend. Hubungi admin.");
        }
        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new DisabledException("Akun Anda telah diblokir permanen.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

**Analisis:**
- `UserDetailsService` adalah interface Spring Security dengan method `loadUserByUsername()`.
- `CustomUserDetailsService` mengimplementasikan interface tersebut untuk menyesuaikan cara loading user dari database MySQL.
- Spring Security akan memanggil method ini secara otomatis saat user login.

**File:** `src/main/java/com/SortifyTeam/Sortify/config/CustomAuthSuccessHandler.java` (baris 13)

```java
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {
    // AuthenticationSuccessHandler adalah interface dari Spring Security

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");

        // Redirect berdasarkan role
        switch (role) {
            case "ROLE_ADMIN" ->    response.sendRedirect("/admin/dashboard");
            case "ROLE_PETUGAS" ->  response.sendRedirect("/petugas/dashboard");
            default ->              response.sendRedirect("/warga/dashboard");
        }
    }
}
```

**File:** `src/main/java/com/SortifyTeam/Sortify/config/CustomAuthFailureHandler.java` (baris 13)

```java
@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {
    // AuthenticationFailureHandler adalah interface dari Spring Security

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String redirectUrl = "/login?error=true";
        if (exception instanceof DisabledException) {
            String message = exception.getMessage();
            if (message != null && message.contains("diblokir")) {
                redirectUrl = "/login?banned=true";
            } else {
                redirectUrl = "/login?suspended=true";
            }
        }
        response.sendRedirect(response.encodeRedirectURL(redirectUrl));
    }
}
```

**File:** `src/main/java/com/SortifyTeam/Sortify/config/WebConfig.java` (baris 12)

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // WebMvcConfigurer adalah interface dari Spring MVC

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Konfigurasi agar folder uploads bisa diakses dari browser
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString();
        if (!location.endsWith("/")) { location += "/"; }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
```

### B.3 Implementasi CommandLineRunner (Data Seeder)

**File:** `src/main/java/com/SortifyTeam/Sortify/config/InitialDataLoader.java` (baris 25)

```java
@Component
public class InitialDataLoader implements CommandLineRunner {
    // CommandLineRunner adalah interface Spring Boot

    @Override
    @Transactional
    public void run(String... args) {
        // Method ini otomatis dijalankan saat aplikasi start
        // Isinya: seeder user, kategori sampah, item sampah, reward
    }
}
```

**File:** `src/main/java/com/SortifyTeam/Sortify/seed/DropPointSeeder.java` (baris 9)

```java
@Component
public class DropPointSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // Seeder untuk data drop point
    }
}
```

**Rangkuman Inheritance:**

| Class | Mewarisi dari | Method yang di-override | Tujuan |
|-------|--------------|------------------------|--------|
| 14 Repository | `JpaRepository<T, Long>` | — (menambah query kustom) | CRUD otomatis |
| CustomUserDetailsService | `UserDetailsService` | `loadUserByUsername()` | Auth dari DB |
| CustomAuthSuccessHandler | `AuthenticationSuccessHandler` | `onAuthenticationSuccess()` | Redirect post-login |
| CustomAuthFailureHandler | `AuthenticationFailureHandler` | `onAuthenticationFailure()` | Handle error login |
| WebConfig | `WebMvcConfigurer` | `addResourceHandlers()` | Serve file upload |
| InitialDataLoader | `CommandLineRunner` | `run()` | Seed data awal |
| DropPointSeeder | `CommandLineRunner` | `run()` | Seed drop point |

---

## C. POLYMORPHISM (Polimorfisme)

**Definisi:** Polimorfisme adalah kemampuan objek untuk merespon method yang sama dengan cara yang berbeda. Terdapat dua jenis: **overriding** (method dengan implementasi berbeda di subclass) dan **overloading** (method dengan nama sama tapi parameter berbeda).

### C.1 Method Overriding (@Override)

**Contoh 1:** Override pada AuthenticationSuccessHandler

**File:** `src/main/java/com/SortifyTeam/Sortify/config/CustomAuthSuccessHandler.java` (baris 15)

```java
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override   // ← Menandakan method ini meng-override method dari interface
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String role = authentication.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");

        // Perilaku berbeda tergantung role (polymorphism via switch)
        switch (role) {
            case "ROLE_ADMIN" ->    response.sendRedirect("/admin/dashboard");
            case "ROLE_PETUGAS" ->  response.sendRedirect("/petugas/dashboard");
            default ->              response.sendRedirect("/warga/dashboard");
        }
    }
}
```

**Penjelasan:** Interface `AuthenticationSuccessHandler` mendefinisikan kontrak `onAuthenticationSuccess()`, tetapi implementasi konkretnya bisa berbeda-beda. Spring Security akan memanggil method ini setelah login berhasil, dan eksekusi diarahkan ke override dari `CustomAuthSuccessHandler`.

**Contoh 2:** Runtime Polymorphism dengan instance of

**File:** `src/main/java/com/SortifyTeam/Sortify/config/CustomAuthFailureHandler.java` (baris 15-28)

```java
@Override
public void onAuthenticationFailure(HttpServletRequest request,
                                    HttpServletResponse response,
                                    AuthenticationException exception) throws IOException {

    String redirectUrl = "/login?error=true";

    // instanceof: mengecek tipe runtime dari exception
    if (exception instanceof DisabledException) {
        // Perilaku berbeda tergantung jenis error
        String message = exception.getMessage();
        if (message != null && message.contains("diblokir")) {
            redirectUrl = "/login?banned=true";     // Banned
        } else {
            redirectUrl = "/login?suspended=true";  // Suspended
        }
    }
    // Jika BadCredentialsException → redirectUrl tetap "/login?error=true"

    response.sendRedirect(response.encodeRedirectURL(redirectUrl));
}
```

**Penjelasan:** Parameter `AuthenticationException` bisa berupa berbagai subclass:
- `DisabledException` → akun suspended/banned
- `BadCredentialsException` → username/password salah
- `LockedException` → akun terkunci

Method ini memeriksa tipe konkret exception menggunakan `instanceof`, lalu menentukan response yang sesuai. Ini adalah **runtime polymorphism**.

**Contoh 3:** Polymorphism pada Collections

Di seluruh controller, koleksi data dideklarasikan menggunakan interface `List<T>` bukan implementasi konkret `ArrayList<T>`.

```java
// WargaController.java — deklarasi menggunakan interface List
List<Transaksi> transaksiList = transaksiService.getTransaksiByWarga(warga);
List<User> leaderboard = userRepo.findTop5ByOrderByTotalPointsDesc();
List<Notifikasi> notifikasiList = notifService.getNotifikasiBelumDibaca(warga);

// AdminController.java
List<KategoriSampah> kategoriList = kategoriRepo.findByIsActiveTrue();
List<PenukaranReward> semuaPenukaran = rewardService.getAllPenukaran();
List<LogAktivitas> logList = logService.getSemuaLog();

// PetugasController.java
List<DropPoint> dropPoints = dropPointService.getAll();
List<RewardItem> items = rewardService.getAllRewardItems();
```

**Penjelasan:** Walaupun method-method tersebut mengembalikan `ArrayList` atau `LinkedList` secara internal, kode polimorfis tetap menggunakan interface `List`. Keuntungannya:
- Jika implementasi berubah (misal dari `ArrayList` ke `LinkedList`), kode pemanggil tidak perlu diubah.
- Lebih fleksibel dan mengikuti prinsip **Program to Interface, not Implementation**.

### C.2 Method Overloading

**File:** `src/main/java/com/SortifyTeam/Sortify/service/NotifikasiService.java` (baris 41-56)

```java
@Service
public class NotifikasiService {

    // OVERLOAD 1: tandaiDibaca dengan parameter Long (ID notifikasi)
    @Transactional
    public void tandaiDibaca(Long id) {
        Notifikasi notif = notifRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notifikasi tidak ditemukan: " + id));
        notif.setRead(true);
        notifRepo.save(notif);
    }

    // OVERLOAD 2: tandaiDibaca dengan parameter List<Notifikasi> (banyak notifikasi)
    @Transactional
    public void tandaiDibaca(List<Notifikasi> daftar) {
        for (Notifikasi n : daftar) {
            n.setRead(true);
        }
        notifRepo.saveAll(daftar);
    }
}
```

**Penjelasan:**
- Kedua method memiliki **nama yang sama**: `tandaiDibaca`
- **Parameter berbeda**:
  - `tandaiDibaca(Long id)` → untuk menandai satu notifikasi berdasarkan ID
  - `tandaiDibaca(List<Notifikasi> daftar)` → untuk menandai banyak notifikasi sekaligus
- **Compiler** menentukan method mana yang dipanggil berdasarkan jumlah dan tipe argumen.
- **Penggunaan:**
  ```java
  notifService.tandaiDibaca(notifId);          // Memanggil overload 1
  notifService.tandaiDibaca(daftarNotifikasi); // Memanggil overload 2
  ```

### C.3 Polymorphism via Enum + Switch Expression

**File:** `src/main/java/com/SortifyTeam/Sortify/controller/TransaksiWebController.java` (baris 239)

```java
private boolean isValidStatusTransition(StatusTransaksi oldStatus, StatusTransaksi newStatus) {
    return switch (oldStatus) {
        case PENDING    -> newStatus == DIPROSES || newStatus == DITOLAK || newStatus == DIBATALKAN;
        case DIPROSES   -> newStatus == SELESAI || newStatus == PENDING;
        case SELESAI,
             DITOLAK,
             DIBATALKAN -> false;  // Final state — tidak bisa berubah
    };
}
```

**Penjelasan:** Status transaksi memiliki perilaku yang berbeda-beda tergantung nilai enum-nya. `switch` expression memberikan perilaku polimorfis — setiap status menentukan transisi yang valid secara berbeda.

---

## D. ABSTRACTION (Abstraksi)

**Definisi:** Abstraksi adalah konsep menyembunyikan detail implementasi dan hanya menampilkan fungsionalitas esensial. Pengguna class cukup tahu **apa yang dilakukan** method, bukan **bagaimana cara kerjanya**.

### D.1 Abstraksi via Interface Repository

Spring Data JPA menggunakan **Query Method** — method dideklarasikan sebagai interface, implementasi dibuat otomatis.

**File:** `src/main/java/com/SortifyTeam/Sortify/repository/TransaksiRepository.java`

```java
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {

    // Abstraksi: "cari transaksi berdasarkan status, urutkan tanggal descending"
    // Implementasi SQL dibuat OTOMATIS oleh Spring Data JPA
    List<Transaksi> findByStatusOrderByTanggalTransaksiDesc(StatusTransaksi status);

    // Query kompleks dengan @Query
    @Query("SELECT MONTH(t.tanggalTransaksi) as bulan, SUM(t.totalBerat) as total " +
           "FROM Transaksi t WHERE YEAR(t.tanggalTransaksi) = :tahun " +
           "GROUP BY MONTH(t.tanggalTransaksi) ORDER BY MONTH(t.tanggalTransaksi)")
    List<Object[]> getMonthlyBerat(@Param("tahun") int tahun);
}
```

**Analisis:**
- Controller/Service hanya memanggil: `transaksiRepo.findByStatusOrderByTanggalTransaksiDesc(PENDING)`
- Tidak perlu tahu: bagaimana SQL ditulis, bagaimana koneksi database, bagaimana result set dipetakan ke object.
- Spring Data JPA mengimplementasikan query berdasarkan **nama method** secara otomatis (Query DSL).

**Contoh method abstrak lainnya:**

| Repository | Method Abstrak | Query yang Dihasilkan |
|-----------|----------------|----------------------|
| UserRepository | `findByUsername(String username)` | `SELECT * FROM users WHERE username = ?` |
| ItemSampahRepository | `findByNamaItemContainingIgnoreCase(String keyword)` | `SELECT * FROM item_sampah WHERE nama_item LIKE %?%` |
| KategoriSampahRepository | `findByNamaKategoriIgnoreCase(String nama)` | `SELECT * FROM kategori_sampah WHERE LOWER(nama_kategori) = LOWER(?)` |
| DropPointRepository | `findByAktifTrueOrderByNamaAsc()` | `SELECT * FROM drop_point WHERE aktif = TRUE ORDER BY nama ASC` |
| PenukaranRewardRepository | `findByKodePenukaran(String kode)` | `SELECT * FROM penukaran_reward WHERE kode_penukaran = ?` |

### D.2 Abstraksi via Service Layer

Service layer menyembunyikan kompleksitas logika bisnis. Controller hanya berinteraksi dengan method-method service yang sederhana.

**File:** `src/main/java/com/SortifyTeam/Sortify/service/PointService.java`

```java
@Service
public class PointService {

    private final UserRepository userRepo;
    private final PointHistoryRepository pointHistoryRepo;

    public PointService(UserRepository userRepo, PointHistoryRepository pointHistoryRepo) {
        this.userRepo = userRepo;
        this.pointHistoryRepo = pointHistoryRepo;
    }

    @Transactional
    public void tambahPoint(User warga, int amount, String description) {
        // Detail implementasi yang DISEMBUNYIKAN dari Controller:

        // 1. Pessimistic Lock — mencegah race condition
        User lockedWarga = userRepo.findByIdWithLock(warga.getId())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // 2. Update total poin
        lockedWarga.setTotalPoints(lockedWarga.getTotalPoints() + amount);
        userRepo.save(lockedWarga);

        // 3. Catat histori poin
        tambahPointHistory(lockedWarga, amount, PointType.EARN, description);
    }

    private void tambahPointHistory(User warga, int amount, PointType type, String description) {
        PointHistory history = new PointHistory();
        history.setUser(warga);
        history.setAmount(amount);
        history.setType(type);
        history.setDescription(description);
        pointHistoryRepo.save(history);
    }
}
```

**Analisis:**

Controller hanya perlu memanggil:
```java
pointService.tambahPoint(warga, 100, "Setor sampah Organik");
```

Controller **tidak perlu tahu** bahwa di dalamnya terjadi:
1. **Pessimistic Lock** — `userRepo.findByIdWithLock()` mengunci baris database agar tidak ada perubahan bersamaan
2. **Update SQL** — `userRepo.save()` menjalankan UPDATE query
3. **Insert histori** — menyimpan riwayat penambahan poin ke tabel `point_history`
4. **Transaksi database** — anotasi `@Transactional` memastikan semua operasi ALL-OR-NOTHING

### D.3 Abstraksi via Service Method — TransaksiService

**File:** `src/main/java/com/SortifyTeam/Sortify/service/TransaksiService.java`

Method-method service yang menyembunyikan kompleksitas:

```java
@Service
public class TransaksiService {
    // Controller hanya perlu tahu method-method ini:

    // 1. Buat transaksi baru
    @Transactional
    public Transaksi buatLaporanDropPoint(Warga warga, String foto, String lokasi,
                                          String detail, List<Long> idKategoriList,
                                          List<Double> beratList) { ... }
    // Di dalamnya: validasi, save transaksi, save details, kirim notifikasi, catat log

    // 2. Proses transaksi (PENDING → DIPROSES)
    @Transactional
    public Transaksi prosesTransaksi(Long idTransaksi, Staff staff) { ... }
    // Di dalamnya: validasi state, update status, kirim notifikasi, catat log

    // 3. Selesaikan transaksi (DIPROSES → SELESAI)
    @Transactional
    public Transaksi selesaikanTransaksi(Long idTransaksi, List<Long> detailIds,
                                         List<Double> beratFinalList,
                                         String foto, Staff staff) { ... }
    // Di dalamnya: validasi, update berat final, hitung poin, update status,
    //              tambah poin ke user, kirim notifikasi, catat log

    // 4. Batalkan transaksi (PENDING → DIBATALKAN)
    @Transactional
    public Transaksi batalTransaksi(Long idTransaksi, User user) { ... }

    // 5. Tolak transaksi (PENDING → DITOLAK)
    @Transactional
    public Transaksi tolakTransaksi(Long idTransaksi, String alasan, Staff staff) { ... }
}
```

### D.4 Abstraksi via GlobalExceptionHandler

**File:** `src/main/java/com/SortifyTeam/Sortify/exception/GlobalExceptionHandler.java`

```java
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model, HttpServletRequest request) {
        // Abstraksi: semua error 400 ditangani di satu tempat
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestUrl", request.getRequestURI());
        return "error/custom-error";
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntime(RuntimeException ex, Model model, HttpServletRequest request) {
        // Abstraksi: semua error 500 ditangani di satu tempat
        log.error("Runtime error di {} : {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Terjadi kesalahan pada server");
        model.addAttribute("errorDetail", ex.getMessage());
        model.addAttribute("requestUrl", request.getRequestURI());
        return "error/custom-error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model, HttpServletRequest request) {
        // Catch-all untuk exception yang tidak tertangani
        log.error("Error tidak terduga di {} : {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Terjadi kesalahan yang tidak terduga");
        model.addAttribute("requestUrl", request.getRequestURI());
        return "error/custom-error";
    }
}
```

**Analisis:**
- Controller tidak perlu lagi menulis blok `try-catch` di setiap method.
- Cukup lempar `throw new IllegalArgumentException("...")` dan GlobalExceptionHandler menangani sisanya.
- Semua error ditangani secara terpusat dengan format response yang konsisten.
- Detail teknis (stack trace, query error) disembunyikan dari user, diganti pesan yang user-friendly.

### D.5 Abstraksi via GlobalControllerAdvice

**File:** `src/main/java/com/SortifyTeam/Sortify/config/GlobalControllerAdvice.java`

```java
@ControllerAdvice
public class GlobalControllerAdvice {

    private final PengumumanService pengumumanService;

    public GlobalControllerAdvice(PengumumanService pengumumanService) {
        this.pengumumanService = pengumumanService;
    }

    @ModelAttribute("pengumumanAktif")
    public List<Pengumuman> pengumumanAktif() {
        // Data ini akan tersedia di SEMUA halaman tanpa perlu coding di tiap controller
        return pengumumanService.getAktif();
    }
}
```

**Analisis:**
- Tanpa menulis satu baris kode pun di controller, semua template HTML bisa mengakses `${pengumumanAktif}`.
- Detail query database disembunyikan di balik `pengumumanService.getAktif()`.
- Jika logika berubah (misal hanya ambil 5 pengumuman terbaru), cukup edit satu tempat.
- Contoh penggunaan di template:
  ```html
  <div th:each="p : ${pengumumanAktif}">
      <h3 th:text="${p.judul}">Judul</h3>
      <p th:text="${p.konten}">Konten</p>
  </div>
  ```

---

## E. KONSEP OOP TAMBAHAN

### E.1 Enum (Type Safety)

Enums memberikan type safety dan membatasi nilai yang valid.

**File:** `src/main/java/com/SortifyTeam/Sortify/model/User.java`

```java
public class User {
    // Enum sebagai tipe data — lebih aman daripada String biasa
    public enum Role { ADMIN, PETUGAS, WARGA }

    public enum AccountStatus { ACTIVE, SUSPENDED, BANNED }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // Hanya bisa berisi ADMIN, PETUGAS, atau WARGA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;
}
```

**File:** `src/main/java/com/SortifyTeam/Sortify/model/Transaksi.java`

```java
public class Transaksi {
    public enum StatusTransaksi { PENDING, DIPROSES, SELESAI, DITOLAK, DIBATALKAN }

    @Enumerated(EnumType.STRING)
    private StatusTransaksi status;  // Hanya 5 nilai valid
}
```

**Manfaat:**
- Compiler mendeteksi kesalahan ketik: `setStatus(PENDING)` ✅ vs `setStatus("pending")` ❌
- IDE memberikan autocomplete untuk nilai enum
- Refactoring mudah — jika nama enum berubah, semua referensi ikut berubah

### E.2 Association, Aggregation, Composition

**Composition — @OneToMany dengan cascade dan orphanRemoval**

**File:** `src/main/java/com/SortifyTeam/Sortify/model/Transaksi.java` (baris 39-42)

```java
// KOMPOSISI: TransaksiDetail tidak bisa hidup tanpa Transaksi
@OneToMany(mappedBy = "transaksi", cascade = CascadeType.ALL, orphanRemoval = true)
@ToString.Exclude @EqualsAndHashCode.Exclude
private List<TransaksiDetail> details = new ArrayList<>();
```

**Penjelasan:** `orphanRemoval = true` berarti jika `TransaksiDetail` dihapus dari list, record di database akan otomatis terhapus. Ini hubungan **Composition** — child tidak bisa eksis tanpa parent.

**Aggregation — @ManyToOne dengan fetch LAZY**

**File:** `src/main/java/com/SortifyTeam/Sortify/model/Transaksi.java` (baris 19-25)

```java
// AGGREGATION: Transaksi memiliki Warga, tapi Warga bisa ada tanpa Transaksi
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_warga")
private Warga warga;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_staff")
private Staff staff;
```

**Association — @OneToOne**

**File:** `src/main/java/com/SortifyTeam/Sortify/model/Warga.java` (baris 27-32)

```java
// ASSOCIATION: Warga memiliki satu User, User memiliki satu Warga
@OneToOne
@JoinColumn(name = "user_id")
@ToString.Exclude @EqualsAndHashCode.Exclude @JsonIgnore
private User user;
```

### E.3 Lifecycle Callbacks (@PrePersist, @PreUpdate)

**File:** `src/main/java/com/SortifyTeam/Sortify/model/PenukaranReward.java` (baris 41-44)

```java
@PrePersist   // Method ini dijalankan SEBELUM record disimpan pertama kali
protected void onCreate() {
    // Otomatis mengisi tanggal penukaran
    tanggalPenukaran = LocalDateTime.now();
}
```

**File:** `src/main/java/com/SortifyTeam/Sortify/model/Pengumuman.java` (baris 30-36)

```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
}

@PreUpdate   // Method ini dijalankan SEBELUM record di-update
protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
```

**Manfaat:**
- Waktu pembuatan diisi otomatis — programmer tidak perlu manual set `setCreatedAt()`.
- Konsisten — format tanggal selalu sama.
- Tidak bisa "lupa" mengisi timestamp.

### E.4 Dependency Injection (Constructor Injection)

Semua service dan controller menggunakan constructor injection — prinsip OOP **Dependency Injection**.

**File:** `src/main/java/com/SortifyTeam/Sortify/service/TransaksiService.java` (baris 26-38)

```java
@Service
public class TransaksiService {
    private final TransaksiRepository transaksiRepo;
    private final PointService pointService;
    private final KategoriSampahRepository kategoriRepo;
    private final NotifikasiService notifikasiService;
    private final UserRepository userRepo;
    private final LogAktivitasService logAktivitasService;

    // Constructor Injection — semua dependency diterima melalui constructor
    public TransaksiService(TransaksiRepository transaksiRepo,
                            PointService pointService,
                            KategoriSampahRepository kategoriRepo,
                            NotifikasiService notifikasiService,
                            UserRepository userRepo,
                            LogAktivitasService logAktivitasService) {
        this.transaksiRepo = transaksiRepo;
        this.pointService = pointService;
        this.kategoriRepo = kategoriRepo;
        this.notifikasiService = notifikasiService;
        this.userRepo = userRepo;
        this.logAktivitasService = logAktivitasService;
    }
}
```

**Manfaat:**
- **Loose Coupling** — TransaksiService bergantung pada interface/class abstrak, bukan implementasi konkret.
- **Testability** — dependency bisa diganti dengan mock object saat unit test.
- **Reusability** — dependency bisa digunakan bersama oleh beberapa class.

---

## F. RINGKASAN

| Pilar OOP | Jumlah Bukti | Contoh Penerapan |
|-----------|-------------|------------------|
| **Encapsulation** | 15 Entity + 2 DTO | `private fields` + `@Data` + `getter/setter` manual + `@Transient` computed getter |
| **Inheritance** | 14 Repository + 6 Config class | `extends JpaRepository`, `implements UserDetailsService`, `implements CommandLineRunner` |
| **Polymorphism** | 7+ @Override + 1 Overloading + runtime instanceof | `CustomAuthSuccessHandler`, `NotifikasiService.tandaiDibaca()`, `List<>` interface, `instanceof` |
| **Abstraction** | 14 Repository + 12 Service + 1 ExceptionHandler + 1 ControllerAdvice | Query method, service layer, `@ControllerAdvice`, `GlobalExceptionHandler` |
| **Tambahan** | Enum, Composition, Aggregation, @PrePersist, Dependency Injection | `Role`, `AccountStatus`, `@OneToMany(cascade=ALL)`, Constructor Injection |
