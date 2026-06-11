# MANUAL GUIDE — Sortify

Aplikasi Bank Sampah Digital berbasis Spring Boot + Thymeleaf.

---

## DAFTAR ISI

1. [Panduan Deploy](#1-panduan-deploy)
2. [Akses & Login](#2-akses--login)
3. [Panduan Warga](#3-panduan-warga)
4. [Panduan Petugas](#4-panduan-petugas)
5. [Panduan Admin](#5-panduan-admin)
6. [Fitur Real-time WebSocket](#6-fitur-real-time-websocket)
7. [Landing Page](#7-landing-page)
8. [Struktur Database](#8-struktur-database)

---

## 1. PANDUAN DEPLOY

### 1.1 Prasyarat Server

| Komponen | Versi Min |
|----------|-----------|
| Java | 21 |
| MySQL | 8.0 |
| Nginx | 1.18 (atau versi apapun) |
| RAM | 1 GB (min 512 MB) |

### 1.2 Build Aplikasi

```bash
# Clone / upload project
cd /home/user/sortify

# Beri izin eksekusi mvnw
chmod +x mvnw

# Build (pakai Maven Wrapper biar ga perlu install Maven)
./mvnw clean package -DskipTests

# Hasil: target/Sortify-0.0.1-SNAPSHOT.jar
```

### 1.3 Siapkan Database MySQL

```sql
CREATE DATABASE IF NOT EXISTS db_sortify CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ardhan_user'@'localhost' IDENTIFIED BY 'Admin123';
GRANT ALL PRIVILEGES ON db_sortify.* TO 'ardhan_user'@'localhost';
FLUSH PRIVILEGES;
```

### 1.4 Jalankan Aplikasi

```bash
# Cara 1: Langsung
java -jar target/Sortify-0.0.1-SNAPSHOT.jar \
  --spring.datasource.username=ardhan_user \
  --spring.datasource.password=Admin123 \
  --spring.datasource.url='jdbc:mysql://localhost:3306/db_sortify?createDatabaseIfNotExist=true&serverTimezone=UTC'

# Cara 2: Background (nohup)
nohup java -jar target/Sortify-0.0.1-SNAPSHOT.jar \
  --spring.datasource.username=ardhan_user \
  --spring.datasource.password=Admin123 \
  --spring.datasource.url='jdbc:mysql://localhost:3306/db_sortify?createDatabaseIfNotExist=true&serverTimezone=UTC' \
  > app.log 2>&1 &

# Cara 3: Systemd service (recommended)
```

**Verifikasi:**
```bash
curl -v http://localhost:8081/
# Harusnya balik HTML landing page (bukan 502)
```

### 1.5 Konfigurasi Nginx (PENTING untuk WebSocket)

Buat file `/etc/nginx/sites-available/sortify.ardhan-dev.com`:

```nginx
server {
    listen 80;
    server_name sortify.ardhan-dev.com;

    # Batasi ukuran upload (5MB)
    client_max_body_size 10M;

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # ⚠️ WAJIB: WebSocket / SockJS
    location /ws {
        proxy_pass http://127.0.0.1:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # File uploads
    location /uploads/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
    }
}
```

Aktifkan site:
```bash
sudo ln -s /etc/nginx/sites-available/sortify.ardhan-dev.com /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

### 1.6 Troubleshooting 502 Bad Gateway

| Problem | Penyebab | Solusi |
|---------|----------|--------|
| `connect() failed (111: Unknown error)` | Spring Boot tidak jalan | `ps aux \| grep java` — pastikan ada |
| Port 8081 tidak listening | App crash saat startup | `java -jar ... 2>&1 \| tail -60` lihat error |
| `Access denied for user` | Kredensial MySQL salah | `mysql -u ardan_user -pAdmin123` test manual |
| `Unknown database` | Database belum dibuat | `CREATE DATABASE db_sortify;` |
| Log berisi `ClassNotFoundException` | JAR corrupt | Ulangi `./mvnw clean package` |

### 1.7 Systemd Service (Opsional)

File `/etc/systemd/system/sortify.service`:

```ini
[Unit]
Description=Sortify Spring Boot App
After=network.target mysql.service

[Service]
User=root
WorkingDirectory=/root/sortify
ExecStart=/usr/bin/java -jar /root/sortify/target/Sortify-0.0.1-SNAPSHOT.jar \
  --spring.datasource.username=ardhan_user \
  --spring.datasource.password=Admin123 \
  --spring.datasource.url='jdbc:mysql://localhost:3306/db_sortify?createDatabaseIfNotExist=true&serverTimezone=UTC'
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable sortify
sudo systemctl start sortify
sudo systemctl status sortify
```

---

## 2. AKSES & LOGIN

### 2.1 Akun Default (dari seeder)

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `12345` |
| Petugas | `petugas` | `12345` |
| Warga | `warga` | `12345` |

### 2.2 URL Routes

| URL | Deskripsi | Auth |
|-----|-----------|------|
| `/` | Landing page (hero, drop point, jam buka) | Publik |
| `/kamus-sampah` | Kamus sampah + poin reward | Publik |
| `/login` | Halaman login | Publik |
| `/register` | Registrasi warga baru | Publik |
| `/warga/dashboard` | Dashboard warga | Warga |
| `/warga/transaksi/tambah` | Form setor sampah | Warga |
| `/warga/reward` | Tukar reward | Warga |
| `/petugas/dashboard` | Dashboard petugas | Petugas |
| `/admin/dashboard` | Dashboard admin | Admin |

---

## 3. PANDUAN WARGA

### 3.1 Registrasi
1. Buka `/register`
2. Isi: Nama, Username, Password, Alamat, No HP
3. Pilih **Drop Point** terdekat
4. Klik **Daftar** → redirect ke login

### 3.2 Landing Page & Kamus Sampah
- Tanpa login, warga bisa lihat:
  - **Landing page** (/): info aplikasi, drop point aktif, jam operasional
  - **Kamus Sampah** (/kamus-sampah): jenis sampah, poin per kg, instruksi

### 3.3 Dashboard Warga (`/warga/dashboard`)
Setelah login:
- **Profil**: foto, nama, poin, pangkat
- **Notifikasi real-time** (muncul otomatis tanpa refresh)
- **Poin update real-time** (angka berubah langsung)
- **Riwayat transaksi**: filter by status, klik lihat detail, unduh PDF struk
- Tombol **Lapor Sampah** → setor baru
- Tombol **Reward** → tukar poin

### 3.4 Setor Sampah (`/warga/transaksi/tambah`)
1. Klik **Lapor Sampah**
2. Upload foto sampah
3. Pilih kategori sampah + masukkan estimasi berat
4. Pilih lokasi drop point
5. Klik Kirim → notifikasi real-time ke petugas
6. Status: **PENDING** → **DIPROSES** → **SELESAI**

### 3.5 Tukar Reward (`/warga/reward`)
1. Buka halaman reward
2. Lihat daftar reward + harga poin + stok
3. Klik **Tukar** → dapat kode AMDAL 6 digit
4. Ambil reward di Kantor Sortify dengan menunjukkan kode

### 3.6 Fitur Real-time untuk Warga
- **Notifikasi** muncul sebagai snackbar (pojok kanan bawah)
- **Poin** langsung update di profil tanpa refresh halaman
- Contoh: saat transaksi selesai → notifikasi + poin bertambah otomatis

---

## 4. PANDUAN PETUGAS

### 4.1 Dashboard Petugas (`/petugas/dashboard`)
Petugas melihat:
- **Statistik**: jumlah pending, diproses, reward pending
- **Transaksi Pending**: daftar laporan warga yang menunggu diproses
- **Transaksi Diproses**: yang sedang ditimbang
- **Penukaran Reward Pending**: klaim reward dari warga
- **Riwayat**: transaksi selesai/ditolak + reward selesai/dibatalkan

### 4.2 Proses Transaksi
**Menerima laporan:**
1. Klik **Proses** pada transaksi PENDING
2. Status berubah → DIPROSES
3. Warga dapat notifikasi real-time bahwa transaksi diproses

**Menimbang & menyelesaikan:**
1. Klik **Selesai** pada transaksi DIPROSES
2. Input **berat final** untuk setiap kategori
3. Upload **foto bukti timbangan**
4. Klik Konfirmasi → poin otomatis ditambahkan ke warga

**Menolak laporan:**
1. Klik **Tolak**
2. Isi alasan penolakan
3. Warga dapat notifikasi real-time

### 4.3 Konfirmasi Reward
1. Klik **Konfirmasi** pada penukaran reward
2. Scan atau ketik **Kode AMDAL** dari warga
3. Upload foto bukti serah terima
4. Klik Konfirmasi

### 4.4 Fitur Real-time untuk Petugas
- **Transaksi baru** muncul otomatis (tidak perlu refresh)
- **Notifikasi** muncul sebagai snackbar
- **Data pending/diproses** update real-time

---

## 5. PANDUAN ADMIN

### 5.1 Dashboard Admin (`/admin/dashboard`)
- **Statistik** (real-time): total warga, petugas, transaksi, berat, reward
- **Chart** (real-time): grafik berat per bulan + komposisi per kategori
- **Filter chart**: filter by warga dan kategori sampah
- **Navigasi Cepat** ke semua menu manajemen

### 5.2 Menu Admin
| Menu | Fungsi |
|------|--------|
| **Data Warga** | CRUD warga |
| **Sektor Staff** | Manajemen petugas |
| **Transaksi Setoran** | Lihat semua transaksi |
| **Kamus Sampah** | Kelola kategori + item sampah |
| **Monitoring** | Rekap transaksi + filter |
| **Pengumuman** | Billboard di landing page |
| **Reward Item** | CRUD reward |
| **Drop Point** | Kelola lokasi + peta |
| **Log Aktivitas** | Audit trail sistem |
| **Leaderboard** | Peringkat warga |
| **Warnings** | Suspend/ban akun |

### 5.3 Fitur Real-time untuk Admin
- **Statistik** (angka) update otomatis tanpa refresh
- **Chart** otomatis re-fetch data saat ada perubahan

---

## 6. FITUR REAL-TIME WEBSOCKET

### 6.1 Cara Kerja
- **Teknologi**: STOMP over SockJS (WebSocket fallback)
- **Endpoint**: `/ws` (SockJS)
- **Broker**: Simple broker untuk `/topic` dan `/queue`

### 6.2 Topik WebSocket

| Topik | Pengirim | Penerima | Trigger |
|-------|----------|----------|---------|
| `/topic/admin/dashboard` | Backend (TransaksiService, dll) | Admin | Data berubah |
| `/topic/petugas/transaksi` | Backend | Petugas | Transaksi baru/berubah |
| `/topic/petugas/notifikasi` | NotifikasiService | Petugas | Notifikasi baru |
| `/user/{username}/queue/notifikasi` | NotifikasiService | User spesifik | Notifikasi personal |
| `/user/{username}/queue/poin` | PointService | Warga | Poin berubah |

### 6.3 Event Real-time yang Dipicu

| Aksi | Event |
|------|-------|
| Warga submit laporan | Petugas dapat notifikasi + refresh daftar pending |
| Petugas proses transaksi | Warga dapat notifikasi |
| Petugas tolak transaksi | Warga dapat notifikasi |
| Petugas selesaikan transaksi | Warga dapat notifikasi + poin update + admin stats refresh |
| Petugas konfirmasi reward | Warga dapat notifikasi |
| Admin/Petugas batalkan reward | Warga dapat notifikasi + refund poin |
| Warga tukar reward | Petugas dapat notifikasi + admin stats refresh |

### 6.4 Nginx Config untuk WebSocket
Tanpa konfigurasi berikut, WebSocket TIDAK akan bekerja:

```nginx
location /ws {
    proxy_pass http://127.0.0.1:8081;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

---

## 7. LANDING PAGE

Halaman `/` (publik) menampilkan:

| Section | Konten |
|---------|--------|
| **Header** | Logo + tombol Masuk / Daftar |
| **Hero** | "Setor sampah, dapatkan poin & reward" + CTA |
| **Statistik** | Total warga, petugas, transaksi, berat |
| **Drop Point** | Kartu lokasi aktif (hanya `aktif = true`) |
| **Jam Operasional** | Senin-Sabtu (08:00-16:00), Minggu libur |
| **CTA** | Daftar / Masuk |
| **Footer** | Copyright |

---

## 8. STRUKTUR DATABASE

### 8.1 Tabel Utama

| Tabel | Fungsi |
|-------|--------|
| `users` | Akun semua role (admin, petugas, warga) |
| `warga` | Data detail warga (relasi 1-1 ke users) |
| `staff` | Data detail petugas (relasi 1-1 ke users) |
| `kategori_sampah` | Kategori sampah + poin per kg |
| `item_sampah` | Item dalam kategori |
| `transaksi` | Laporan setoran sampah |
| `transaksi_detail` | Detail kategori per transaksi |
| `drop_point` | Lokasi drop point |
| `reward_item` | Daftar reward yang bisa ditukar |
| `penukaran_reward` | Riwayat penukaran reward |
| `point_history` | Riwayat mutasi poin |
| `notifikasi` | Notifikasi per user |
| `log_aktivitas` | Audit trail |
| `pengumuman` | Billboard landing page |
| `warning` | Pelanggaran/suspend/ban |

### 8.2 State Machine Transaksi

```
PENDING  -->  DIPROSES  -->  SELESAI
  |              |
  v              v
DITOLAK       PENDING (cancel)
DIBATALKAN
```

### 8.3 Role User

| Role | Hak Akses |
|------|-----------|
| `WARGA` | Dashboard, setor sampah, tukar reward, histori |
| `PETUGAS` | Dashboard, verifikasi transaksi, timbang, konfirmasi reward |
| `ADMIN` | Dashboard, semua CRUD, monitoring, leaderboard, warnings |
