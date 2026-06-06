# BAB V: IMPLEMENTASI

## 5.1 Teknologi yang Digunakan

| Teknologi | Versi | Fungsi |
|-----------|-------|--------|
| Java | 21 | Bahasa pemrograman utama |
| Spring Boot | 3.4.6 | Framework aplikasi |
| Spring Security | 6.x | Autentikasi & otorisasi |
| Spring Data JPA | 3.x | ORM database |
| Thymeleaf | 6.x | Template engine |
| MySQL | 8.x | Database |
| OpenPDF | 2.0.3 | Generate PDF struk |
| OpenCSV | 5.9 | Export CSV |
| Lombok | 1.18.x | Pengurangan boilerplate code |
| Maven | 3.x | Build & dependency management |

## 5.2 Struktur Project

```
Sortify/
├── src/main/java/com/SortifyTeam/Sortify/
│   ├── SortifyApplication.java          # Main class
│   ├── config/
│   │   ├── SecurityConfig.java          # Konfigurasi keamanan
│   │   ├── CustomUserDetailsService.java # Load user dari DB
│   │   ├── CustomAuthSuccessHandler.java # Redirect setelah login
│   │   ├── CustomAuthFailureHandler.java # Handle error login
│   │   ├── WebConfig.java               # Konfigurasi resource statis
│   │   ├── GlobalControllerAdvice.java   # Data global ke semua view
│   │   └── InitialDataLoader.java       # Seeder data awal
│   ├── controller/                       # 15 Controller classes
│   ├── service/                          # 12 Service classes
│   ├── repository/                       # 14 Repository interfaces
│   ├── model/                            # 15 Entity classes
│   ├── dto/                              # Data Transfer Objects
│   ├── exception/                        # Global exception handler
│   └── seed/                             # Data seeder
├── src/main/resources/
│   ├── templates/                        # 35+ Thymeleaf templates
│   ├── static/css/                       # CSS files
│   ├── static/js/                        # JavaScript files
│   └── application.properties           # Konfigurasi aplikasi
├── uploads/                              # Folder upload file
├── pom.xml                               # Dependency Maven
└── docs/                                 # Dokumentasi laporan
```

## 5.3 Class Diagram Implementasi

### Entity Classes (Model)

**Package:** `com.SortifyTeam.Sortify.model`

| No | Kelas | Tabel | Anotasi | Relasi |
|----|-------|-------|---------|--------|
| 1 | User | users | @Entity | @OneToOne ke Warga & Staff, @OneToMany ke PenukaranReward, Notifikasi, PointHistory, Warning |
| 2 | Warga | warga | @Entity | @OneToOne ke User, @ManyToOne ke DropPoint, @OneToMany ke Transaksi |
| 3 | Staff | staff | @Entity | @OneToOne ke User |
| 4 | DropPoint | drop_point | @Entity | @OneToMany ke Warga (optional) |
| 5 | KategoriSampah | kategori_sampah | @Entity | @OneToMany ke ItemSampah & TransaksiDetail |
| 6 | ItemSampah | item_sampah | @Entity | @ManyToOne ke KategoriSampah |
| 7 | Transaksi | transaksi | @Entity | @ManyToOne ke Warga & Staff, @OneToMany ke TransaksiDetail |
| 8 | TransaksiDetail | transaksi_detail | @Entity | @ManyToOne ke Transaksi & KategoriSampah |
| 9 | RewardItem | reward_item | @Entity | @OneToMany ke PenukaranReward |
| 10 | PenukaranReward | penukaran_reward | @Entity | @ManyToOne ke User & RewardItem |
| 11 | Notifikasi | notifikasi | @Entity | @ManyToOne ke User |
| 12 | PointHistory | point_history | @Entity | @ManyToOne ke User |
| 13 | LogAktivitas | log_aktivitas | @Entity | Independent |
| 14 | Pengumuman | pengumuman | @Entity | Independent |
| 15 | Warning | warnings | @Entity | @ManyToOne ke User |

### Controller Classes

**Package:** `com.SortifyTeam.Sortify.controller`

| No | Controller | Base Path | Role |
|----|------------|-----------|------|
| 1 | AuthController | — | Public |
| 2 | KamusSampahController | /kamus-sampah | Public |
| 3 | WargaController | /warga | WARGA |
| 4 | PetugasController | /petugas | PETUGAS |
| 5 | AdminController | /admin | ADMIN |
| 6 | AdminDropPointController | /admin/drop-point | ADMIN |
| 7 | AdminKamusController | /admin/kamus | ADMIN |
| 8 | DashboardApiController | /api/dashboard | ADMIN (REST) |
| 9 | ItemSampahWebController | /admin/item-sampah | ADMIN |
| 10 | KategoriSampahWebController | /admin/kategori-sampah | ADMIN |
| 11 | PengumumanController | /admin/pengumuman | ADMIN |
| 12 | PetugasRewardManageController | /petugas/reward/manage | PETUGAS |
| 13 | RewardItemWebController | /admin/reward | ADMIN |
| 14 | StaffWebController | /admin/staff | ADMIN |
| 15 | TransaksiWebController | /admin/transaksi | ADMIN |
| 16 | WargaWebController | /admin/warga | ADMIN |

### Service Classes

**Package:** `com.SortifyTeam.Sortify.service`

| No | Service | Fungsi Utama |
|----|---------|-------------|
| 1 | TransaksiService | Logika transaksi, state management, pessimistic lock |
| 2 | KategoriSampahService | CRUD kategori + soft delete cascade |
| 3 | ItemSampahService | CRUD item + search |
| 4 | RewardService | CRUD reward, penukaran, refund |
| 5 | PointService | Manajemen poin user, pessimistic lock |
| 6 | NotifikasiService | CRUD notifikasi per user |
| 7 | DropPointService | CRUD drop point |
| 8 | WarningService | Warning, suspend, ban, aktivasi |
| 9 | LogAktivitasService | Pencatatan log |
| 10 | PengumumanService | CRUD pengumuman |
| 11 | FileStorageService | Upload & validasi file |
| 12 | PdfStrukService | Generate PDF struk |

## 5.4 Route Map

### Public Routes

| Method | Route | Deskripsi |
|--------|-------|-----------|
| GET | / | Redirect ke dashboard atau kamus-sampah |
| GET | /login | Halaman login |
| GET | /register | Halaman registrasi |
| POST | /register | Proses registrasi |
| GET | /kamus-sampah | Kamus sampah publik |

### Warga Routes (`/warga`)

| Method | Route | Deskripsi |
|--------|-------|-----------|
| GET | /dashboard | Dashboard warga |
| GET | /transaksi/tambah | Form setor sampah |
| POST | /transaksi/tambah | Submit setor sampah |
| POST | /transaksi/batal/{id} | Batalkan transaksi |
| GET | /transaksi/struk/{id} | Download PDF struk |
| GET | /reward | Katalog reward |
| POST | /reward/tukar | Tukar reward |
| GET | /ubah-password | Form ganti password |
| POST | /ubah-password | Proses ganti password |
| POST | /foto-profil | Upload foto profil |
| POST | /notifikasi/baca/{id} | Baca notifikasi |

### Petugas Routes (`/petugas`)

| Method | Route | Deskripsi |
|--------|-------|-----------|
| GET | /dashboard | Dashboard petugas |
| POST | /transaksi/proses/{id} | Proses transaksi |
| POST | /transaksi/selesai/{id} | Selesaikan transaksi |
| POST | /transaksi/tolak/{id} | Tolak transaksi |
| POST | /reward/konfirmasi/{id} | Konfirmasi penukaran |
| POST | /reward/batal/{id} | Batalkan penukaran |
| POST | /user/warning/{id} | Beri warning |
| GET | /reward/manage | Kelola reward |
| POST | /notifikasi/baca/{id} | Baca notifikasi |

### Admin Routes (`/admin`)

| Method | Route | Deskripsi |
|--------|-------|-----------|
| GET | /dashboard | Dashboard admin |
| GET | /monitoring | Monitoring reward |
| GET | /leaderboard | Peringkat warga |
| GET | /logs | Log aktivitas |
| GET | /transaksi/export | Export CSV |
| GET | /kamus | Kamus sampah admin |
| GET | /kategori-sampah | CRUD kategori |
| GET | /item-sampah | CRUD item |
| GET | /reward | CRUD reward |
| GET | /pengumuman | CRUD pengumuman |
| GET | /drop-point | CRUD drop point |
| GET | /transaksi | CRUD transaksi |
| GET | /warga | CRUD warga |
| GET | /staff | CRUD staff |
| GET | /warnings | Manajemen peringatan |
| POST | /user/ban/{id} | Ban user |
| POST | /user/activate/{id} | Aktivasi user |
| POST | /user/warning/{id} | Beri warning |
| POST | /user/suspend/{id} | Suspend user |

### API Routes

| Method | Route | Deskripsi |
|--------|-------|-----------|
| GET | /api/dashboard/chart-data | Data chart (JSON) |

## 5.5 Keamanan

1. **Password Encoding**: BCrypt dengan kekuatan 10
2. **CSRF Protection**: Diaktifkan dengan `CsrfTokenRequestAttributeHandler`
3. **Role-Based Access**: URL pattern dibatasi per role
4. **Account Status Check**: Suspended/banned user tidak bisa login
5. **File Upload Validation**: Hanya JPG/PNG/GIF/WEBP, max 5MB
6. **Pessimistic Lock**: PESSIMISTIC_WRITE pada transaksi poin & reward
7. **Session Management**: Logout hapus session + cookie

## 5.6 Fitur Unggulan

1. **Pessimistic Locking**: Menggunakan `@Lock(PESSIMISTIC_WRITE)` untuk mencegah race condition pada penukaran reward dan penambahan poin.
2. **PDF Generation**: Generate struk transaksi otomatis menggunakan OpenPDF dengan format A6.
3. **CSV Export**: Export data transaksi ke CSV menggunakan OpenCSV.
4. **Auto-Seed Data**: Data awal (user, kategori, item, drop point) di-seed otomatis saat pertama kali aplikasi dijalankan.
5. **Soft Delete**: Semua data penting menggunakan flag `isActive` untuk menghindari kehilangan data.
6. **Auto-Suspend**: Akun warga otomatis di-suspend jika mendapatkan 3 warning.
7. **Global Announcement**: Pengumuman aktif muncul di semua halaman via `@ControllerAdvice`.
8. **Kode AMDAL**: Kode unik 6 digit untuk verifikasi penukaran reward.
9. **State Machine**: Status transaksi memiliki state transition yang ketat (PENDING → DIPROSES → SELESAI | DITOLAK | DIBATALKAN).
10. **Role-Specific Dashboard**: Setiap role memiliki dashboard yang disesuaikan dengan tugasnya.
