# BAB IV: PERANCANGAN SISTEM

## 4.1 Class Diagram

Berikut adalah class diagram yang menggambarkan struktur kelas pada aplikasi Sortify.

*Gambar Class Diagram dapat dilihat pada file `diagrams/class-diagram.puml`*

### Entity Relationship

```
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│     User     │1      1│    Warga     │        │  DropPoint   │
│──────────────│────────│──────────────│        │──────────────│
│ id           │        │ idWarga      │        │ id           │
│ username     │        │ nama         │*       │ nama         │
│ password     │        │ alamat       │────────│ alamat       │
│ fullName     │        │ noHp         │       1│ latitude     │
│ role         │        │ username     │        │ longitude    │
│ totalPoints  │        └──────────────┘        │ aktif        │
│ accountStatus │               │1              └──────────────┘
│ warningCount │               │                   1
│ fotoProfil   │               │                   │
└──────────────┘               │                   │
       1│                      │                   │
        │ 1        ┌──────────────┐                 │
        ├──────────│    Staff     │                 │
        │          │──────────────│                 │
        │          │ idStaff      │                 │
        │          │ nama         │                 │
        │          │ nip          │                 │
        │          │ jabatan      │                 │
        │          │ username     │                 │
        │          └──────────────┘                 │
        │                                           │
        │1                                          │
        │       ┌──────────────────┐                │
        │       │   Transaksi      │                │
        │       │──────────────────│                │
        │       │ idTransaksi      │                │
        │       │ tanggalTransaksi │                │
        │       │ totalBerat       │                │
        │       │ totalPoin        │                │
        │       │ status           │                │
        │       │ fotoLaporanWarga │                │
        │       │ fotoBuktiTimbangan│               │
        │       │ lokasi           │                │
        │       └──────────────────┘                │
        │              1│                           │
        │               │                           │
        │               │*                          │
        │       ┌──────────────────┐   ┌──────────────────┐
        │       │ TransaksiDetail  │   │ KategoriSampah   │
        │       │──────────────────│   │──────────────────│
        │       │ id               │*  │ idKategori       │
        │       │ beratEstimasi    │───│ namaKategori     │
        │       │ beratFinal       │   │ poinPerKg        │
        │       │ subTotalPoin     │   │ instruksiPenanganan│
        │       └──────────────────┘   │ isActive         │
        │                              └──────────────────┘
        │                                      1│
        │                                      │*
        │                              ┌──────────────────┐
        │                              │   ItemSampah     │
        │                              │──────────────────│
        │                              │ id               │
        │                              │ namaItem         │
        │                              │ deskripsi        │
        │                              │ instruksiPenanganan│
        │                              │ isActive         │
        │                              └──────────────────┘
        │
        │1          ┌──────────────────┐       ┌──────────────────┐
        │           │  PenukaranReward │       │   RewardItem     │
        │           │──────────────────│       │──────────────────│
        │           │ id               │       │ id               │
        │           │ kodePenukaran    │*      │ namaBarang       │
        │           │ status           │───────│ pointNeeded      │
        │           │ fotoBukti        │      1│ stock            │
        │           │ tanggalPenukaran │       │ isActive         │
        │           └──────────────────┘       └──────────────────┘
        │
        │1
        │       ┌──────────────────┐
        │       │   Notifikasi     │
        │       │──────────────────│
        │       │ id               │
        │       │ pesan            │
        │       │ isRead           │
        │       │ createdAt        │
        │       └──────────────────┘
        │
        │1
        │       ┌──────────────────┐
        │       │   PointHistory   │
        │       │──────────────────│
        │       │ id               │
        │       │ amount           │
        │       │ type (EARN/SPEND)│
        │       │ description      │
        │       │ createdAt        │
        │       └──────────────────┘
        │
        │1
        │       ┌──────────────────┐
        │       │    Warning       │
        │       │──────────────────│
        │       │ id               │
        │       │ issuedBy         │
        │       │ reason           │
        │       │ type             │
        │       │ createdAt        │
        │       └──────────────────┘
```

## 4.2 ERD (Entity Relationship Diagram)

*Gambar ERD dapat dilihat pada file `diagrams/erd.puml`*

**Database:** `kelola_sampah`
**Total Tabel:** 15 tabel

| Tabel | Primary Key | Foreign Key |
|-------|-------------|-------------|
| users | id | — |
| warga | id_warga | user_id → users(id), drop_point_id → drop_point(id) |
| staff | id_staff | user_id → users(id) |
| drop_point | id | — |
| kategori_sampah | id_kategori | — |
| item_sampah | id | id_kategori → kategori_sampah(id_kategori) |
| transaksi | id_transaksi | id_warga → warga(id_warga), id_staff → staff(id_staff) |
| transaksi_detail | id | id_transaksi → transaksi(id_transaksi), id_kategori → kategori_sampah(id_kategori) |
| reward_item | id | — |
| penukaran_reward | id | warga_id → users(id), reward_item_id → reward_item(id) |
| notifikasi | id | warga_id → users(id) |
| point_history | id | warga_id → users(id) |
| log_aktivitas | id | — |
| pengumuman | id | — |
| warnings | id | target_user_id → users(id) |

## 4.3 Flowchart

### 4.3.1 Flowchart Login

*Gambar dapat dilihat pada file `diagrams/flowchart-login.puml`*

```
[Mulai] → Input Username & Password → Validasi →
  ├─ Gagal → [Tampilkan Error] → Kembali ke Login
  └─ Berhasil → Cek Account Status →
       ├─ BANNED → [Tampilkan Error Banned]
       ├─ SUSPENDED → [Tampilkan Error Suspended]
       └─ ACTIVE → Redirect berdasarkan Role →
            ├─ ADMIN → /admin/dashboard
            ├─ PETUGAS → /petugas/dashboard
            └─ WARGA → /warga/dashboard
```

### 4.3.2 Flowchart Transaksi Setor Sampah

*Gambar dapat dilihat pada file `diagrams/flowchart-transaksi.puml`*

```
[Warga] → Pilih Drop Point → Pilih Kategori Sampah →
Input Berat Estimasi → Upload Foto → Submit →
[System] Simpan Transaksi (PENDING) → Kirim Notifikasi Petugas →
[Petugas] Lihat Antrian → Proses (DIPROSES) →
Timbang Berat Final → Upload Foto Bukti → Selesai (SELESAI) →
[System] Tambah Poin ke Akun Warga → Kirim Notifikasi Warga
```

### 4.3.3 Flowchart Penukaran Reward

*Gambar dapat dilihat pada file `diagrams/flowchart-reward.puml`*

```
[Warga] Pilih Reward → Cek Poin Cukup? →
  ├─ Tidak → [Info Poin Tidak Cukup]
  └─ Ya → Cek Stock? →
       ├─ Habis → [Info Stock Habis]
       └─ Ada → System: Kurangi Poin & Stock, Generate Kode AMDAL →
            Kirim Notifikasi Petugas →
            [Petugas] Verifikasi Kode AMDAL + Upload Foto →
            Cocok? → [SUDAH_DIAMBIL]
            └─ Tidak → [DIBATALKAN] → Refund Poin + Stock
```

### 4.3.4 State Machine Status Transaksi

*Gambar dapat dilihat pada file `diagrams/state-transaksi.puml`*

```
                    ┌──────────┐
                    │ PENDING  │
                    └────┬─────┘
                    │    │    │
              ┌─────┘    │    └──────┐
              ▼          ▼           ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │DIPROSES  │ │DITOLAK   │ │DIBATALKAN│
        └────┬─────┘ └──────────┘ └──────────┘
             ▼
        ┌──────────┐
        │ SELESAI  │
        └──────────┘
```

## 4.4 Sequence Diagram

### 4.4.1 Sequence Diagram Setor Sampah

*Gambar dapat dilihat pada file `diagrams/sequence-transaksi.puml`*

```
Warga → WargaController: POST /warga/transaksi/tambah
WargaController → TransaksiService: buatLaporanDropPoint()
TransaksiService → TransaksiRepository: save(transaksi)
TransaksiService → NotifikasiService: buatNotifikasi(petugas)
TransaksiService → LogAktivitasService: catatAktivitas()
TransaksiService → TransaksiRepository: return transaksi
WargaController → Warga: Redirect /warga/dashboard
```

## 4.5 Arsitektur Sistem

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│           Thymeleaf Templates (35 files)                 │
│           @Controller classes (15 files)                 │
├─────────────────────────────────────────────────────────┤
│                   Business Layer                         │
│           @Service classes (12 files)                    │
├─────────────────────────────────────────────────────────┤
│                   Data Access Layer                      │
│           Repository interfaces (14 files)               │
│           Entity classes (15 files)                      │
├─────────────────────────────────────────────────────────┤
│                   Database Layer                         │
│           MySQL (15 tables)                              │
└─────────────────────────────────────────────────────────┘
```
