# BAB VII: DEMO APLIKASI

## 7.1 Alur Penggunaan Warga

### 7.1.1 Registrasi Akun

1. Buka halaman `/register`
2. Isi form: Nama, Username, Password, Alamat, No HP, Pilih Drop Point
3. Klik "Daftar"
4. Redirect ke halaman `/login`

### 7.1.2 Login

1. Buka halaman `/login`
2. Masukkan username dan password
3. Sistem validasi: jika akun aktif → redirect ke `/warga/dashboard`
4. Jika akun suspended/banned → tampilkan pesan error

### 7.1.3 Dashboard Warga

Setelah login, warga melihat:
- **Profil**: Foto profil, nama, username, pangkat (berdasarkan total poin)
- **Total Poin**: Jumlah poin yang dimiliki
- **Histori Transaksi**: 5 transaksi terakhir dengan status
- **Leaderboard**: Peringkat warga berdasarkan poin
- **Notifikasi**: Notifikasi terbaru yang belum dibaca

### 7.1.4 Setor Sampah

1. Klik menu "Setor Sampah" atau buka `/warga/transaksi/tambah`
2. Pilih Drop Point tujuan
3. Pilih kategori sampah yang akan disetor (bisa lebih dari satu)
4. Masukkan estimasi berat per kategori (kg)
5. Tambahkan detail/keterangan (opsional)
6. Upload foto sampah
7. Klik "Ajukan"
8. Transaksi tersimpan dengan status PENDING

### 7.1.5 Cek Status Transaksi

- Di dashboard, warga bisa melihat status transaksi terbaru
- Notifikasi akan muncul jika ada perubahan status

### 7.1.6 Tukar Reward

1. Buka halaman `/warga/reward`
2. Lihat daftar reward yang tersedia (nama, poin, stock)
3. Klik "Tukar" pada reward yang diinginkan
4. Sistem cek poin dan stock
5. Jika cukup → kode AMDAL muncul di layar
6. Bawa kode AMDAL ke petugas untuk mengambil reward

### 7.1.7 Download Struk PDF

1. Dari histori transaksi, klik "Download Struk"
2. File PDF akan terunduh secara otomatis

### 7.1.8 Ubah Password

1. Buka halaman `/warga/ubah-password`
2. Masukkan password lama, password baru, konfirmasi password baru
3. Klik "Simpan"

## 7.2 Alur Penggunaan Petugas

### 7.2.1 Login

Login dengan akun petugas → redirect ke `/petugas/dashboard`

### 7.2.2 Dashboard Petugas

- **Daftar Transaksi PENDING**: Setoran baru dari warga yang perlu diproses
- **Daftar Transaksi DIPROSES**: Setoran yang sedang diproses
- **Penukaran Reward PENDING**: Permintaan penukaran yang perlu dikonfirmasi
- **Notifikasi**: Notifikasi aktivitas terbaru

### 7.2.3 Proses Transaksi

1. Dari daftar PENDING, klik "Proses" pada transaksi yang dipilih
2. Status berubah menjadi DIPROSES
3. Warga mendapat notifikasi bahwa transaksi sedang diproses

### 7.2.4 Selesaikan Transaksi

1. Pada transaksi DIPROSES, klik "Selesaikan"
2. Input berat final untuk setiap kategori sampah
3. Upload foto bukti timbangan
4. Klik "Simpan"
5. Status berubah SELESAI
6. Poin otomatis ditambahkan ke akun warga
7. Warga mendapat notifikasi

### 7.2.5 Konfirmasi Penukaran Reward

1. Dari daftar penukaran PENDING, klik "Konfirmasi"
2. Masukkan kode AMDAL dari warga
3. Upload foto bukti serah terima
4. Klik "Konfirmasi"
5. Status berubah SUDAH_DIAMBIL

### 7.2.6 Batalkan Penukaran

1. Dari daftar penukaran PENDING, klik "Batalkan"
2. Masukkan alasan pembatalan
3. Sistem otomatis refund poin dan stock

### 7.2.7 Kelola Reward

1. Buka halaman `/petugas/reward/manage`
2. Lihat daftar reward dengan stock
3. Tambah/Edit/Hapus reward

### 7.2.8 Beri Warning

1. Klik "Warning" pada warga yang melanggar
2. Masukkan alasan
3. Jika warning ≥ 3, akun otomatis di-suspend

## 7.3 Alur Penggunaan Admin

### 7.3.1 Login

Login dengan akun admin → redirect ke `/admin/dashboard`

### 7.3.2 Dashboard Admin

- **Chart Bulanan**: Grafik jumlah sampah per bulan (bar chart)
- **Chart Kategori**: Distribusi sampah per kategori (pie chart)
- **Statistik**: Total warga, staff, transaksi, poin, reward
- **Filter**: Bisa filter chart berdasarkan warga dan kategori

### 7.3.3 Manajemen Data Master

Admin dapat mengelola seluruh data master melalui menu:
- **Warga**: Tambah, edit, detail, hapus warga
- **Staff**: Tambah, edit, hapus staff (dengan pembuatan akun otomatis)
- **Kategori Sampah**: Tambah, edit, hapus kategori
- **Item Sampah**: Tambah, edit, hapus item per kategori
- **Drop Point**: Tambah, edit, aktifkan/nonaktifkan
- **Reward**: Tambah, edit, hapus item reward
- **Pengumuman**: Tambah, edit, hapus pengumuman
- **Transaksi**: Lihat, tambah, edit, hapus transaksi

### 7.3.4 Manajemen Peringatan

1. Buka `/admin/warnings`
2. Lihat daftar semua warning
3. Klik user untuk melihat detail warning
4. Beri warning, suspend, ban, atau aktivasi akun

### 7.3.5 Monitoring & Logging

1. **Monitoring Reward**: Pantau penukaran reward dengan filter
2. **Log Aktivitas**: Lihat semua aktivitas user dengan filter
3. **Leaderboard**: Lihat peringkat warga

### 7.3.6 Export Data

1. Buka `/admin/transaksi/export`
2. File CSV otomatis terunduh berisi data transaksi selesai

## 7.4 Alur Data Transaksi (End-to-End)

```
Warga                    Petugas                   Sistem
  │                         │                        │
  ├─ Submit setor sampah ───┼───────────────────────▶│ PENDING
  │                         │                        │
  │◀──── Notifikasi ────────┼────────────────────────│
  │                         │                        │
  │                         ├─ Proses ──────────────▶│ DIPROSES
  │                         │                        │
  │◀──── Notifikasi ────────┼────────────────────────│
  │                         │                        │
  │                         ├─ Timbang + Selesai ───▶│ SELESAI
  │                         │                        │
  │◀──── Notifikasi ────────┼────────────────────────│
  │◀──── Poin ditambah ─────┼────────────────────────│
```

## 7.5 Screenshot Aplikasi

*(Screenshot dapat ditambahkan di sini)*

### Halaman yang perlu di-screenshot:

1. **Halaman Login** (`/login`)
2. **Halaman Registrasi** (`/register`)
3. **Kamus Sampah** (`/kamus-sampah`)
4. **Dashboard Warga** (`/warga/dashboard`)
5. **Form Setor Sampah** (`/warga/transaksi/tambah`)
6. **Halaman Reward Warga** (`/warga/reward`)
7. **Dashboard Petugas** (`/petugas/dashboard`)
8. **Proses/Selesaikan Transaksi** (modal)
9. **Dashboard Admin** (`/admin/dashboard`)
10. **Admin Kamus Sampah** (`/admin/kamus`)
11. **Manajemen Warga** (`/admin/warga`)
12. **Manajemen Peringatan** (`/admin/warnings`)
13. **Log Aktivitas** (`/admin/logs`)
14. **Struk PDF** (hasil download)
