# BAB VI: FITUR APLIKASI

## 6.1 Fitur Publik (Tanpa Login)

| No | Fitur | Deskripsi |
|----|-------|-----------|
| 1 | **Kamus Sampah** | Database 250+ jenis sampah yang dikelompokkan dalam 3 kategori (ORGANIK, ANORGANIK, B3). Masing-masing dilengkapi deskripsi, poin per kg, dan instruksi penanganan. Bisa dicari berdasarkan nama dan difilter per kategori. |
| 2 | **Login** | Autentikasi user dengan validasi status akun (ACTIVE/SUSPENDED/BANNED). |
| 3 | **Registrasi** | Pendaftaran warga baru dengan form lengkap (nama, username, password, alamat, no HP, pilihan drop point). |

## 6.2 Fitur Warga

| No | Fitur | Detail |
|----|-------|--------|
| 1 | **Dashboard Warga** | Menampilkan profil warga dengan pangkat, total poin, histori 5 transaksi terakhir, leaderboard peringkat, dan notifikasi terbaru. |
| 2 | **Setor Sampah** | Warga dapat melaporkan setoran sampah dengan memilih drop point tujuan, memilih kategori sampah (bisa lebih dari satu), menginput estimasi berat, menambahkan keterangan, dan mengupload foto sampah. Transaksi otomatis berstatus PENDING. |
| 3 | **Tukar Reward** | Warga dapat melihat katalog reward yang tersedia beserta poin yang dibutuhkan dan ketersediaan stock. Proses penukaran dilengkapi pessimistic lock untuk mencegah race condition. Sistem akan menghasilkan kode AMDAL 6 digit unik sebagai kode verifikasi. |
| 4 | **Download Struk PDF** | Setiap transaksi yang sudah selesai dapat diunduh struknya dalam format PDF (A6). Struk memuat informasi transaksi, detail kategori sampah, berat, poin, dan total. |
| 5 | **Histori Transaksi** | Warga dapat melihat riwayat seluruh transaksi yang pernah dilakukan beserta statusnya. |
| 6 | **Histori Penukaran** | Riwayat penukaran reward beserta status (PENDING/SUDAH_DIAMBIL/DIBATALKAN). |
| 7 | **Riwayat Poin** | Riwayat perubahan poin (earn/spend) lengkap dengan deskripsi. |
| 8 | **Batalkan Transaksi** | Warga dapat membatalkan transaksi yang masih berstatus PENDING. |
| 9 | **Ubah Password** | Fitur ganti password dengan validasi password lama. |
| 10 | **Upload Foto Profil** | Warga dapat mengupload foto profil. Format yang didukung: JPG, PNG, GIF, WEBP. |
| 11 | **Notifikasi** | Notifikasi real-time untuk berbagai event (transaksi diproses, transaksi selesai, penukaran dikonfirmasi, dll). |
| 12 | **Leaderboard** | Menampilkan peringkat warga berdasarkan total poin yang dimiliki. |

## 6.3 Fitur Petugas

| No | Fitur | Detail |
|----|-------|--------|
| 1 | **Dashboard Petugas** | Menampilkan daftar transaksi PENDING yang perlu diproses, transaksi DIPROSES yang perlu diselesaikan, penukaran reward yang menunggu konfirmasi, dan notifikasi. |
| 2 | **Proses Transaksi** | Petugas mengubah status transaksi dari PENDING menjadi DIPROSES, menandakan bahwa petugas sedang menangani setoran tersebut. |
| 3 | **Selesaikan Transaksi** | Petugas melakukan penimbangan final untuk setiap kategori sampah, mengupload foto bukti timbangan, dan menyelesaikan transaksi (DIPROSES → SELESAI). Sistem otomatis menghitung total poin dan menambahkannya ke akun warga. |
| 4 | **Tolak Transaksi** | Petugas dapat menolak transaksi dengan memberikan alasan penolakan (PENDING → DITOLAK). |
| 5 | **Konfirmasi Penukaran** | Petugas memverifikasi penukaran reward dengan memasukkan kode AMDAL yang diberikan warga dan mengupload foto bukti serah terima. |
| 6 | **Batalkan Penukaran** | Petugas dapat membatalkan penukaran reward. Sistem otomatis mengembalikan (refund) poin warga dan stock reward. |
| 7 | **Kelola Reward** | Petugas dapat menambah, mengedit, dan menghapus item reward (nama barang, poin yang dibutuhkan, stock). |
| 8 | **Beri Peringatan** | Petugas dapat memberikan warning ke warga yang melanggar aturan. Jika warning mencapai 3 kali, akun warga otomatis di-suspend. |
| 9 | **Notifikasi** | Mendapat notifikasi ketika ada transaksi baru atau penukaran reward baru. |

## 6.4 Fitur Admin

| No | Fitur | Detail |
|----|-------|--------|
| 1 | **Dashboard Admin** | Dashboard komprehensif dengan grafik jumlah sampah per bulan, distribusi per kategori, statistik (total warga, staff, transaksi, poin, reward). |
| 2 | **Monitoring Reward** | Memantau seluruh aktivitas penukaran reward dengan filter berdasarkan status, nama warga, dan nama barang. |
| 3 | **Leaderboard** | Melihat peringkat seluruh warga berdasarkan total poin. |
| 4 | **Log Aktivitas** | Melihat seluruh log aktivitas pengguna dengan filter (username, role, jenis aksi). |
| 5 | **Export CSV** | Mengexport data transaksi yang sudah selesai ke format CSV. |
| 6 | **Manajemen User** | Admin dapat mengelola data warga (CRUD + detail) dan staff (CRUD + pembuatan akun otomatis). Dilengkapi pengecekan relasi sebelum penghapusan. |
| 7 | **Manajemen Peringatan** | Admin dapat melihat daftar warning, detail warning per user, memberi warning, suspend, ban, dan mengaktifkan kembali akun yang di-suspend. |
| 8 | **Kamus Sampah** | Admin dapat mengelola kategori sampah dan item sampah dalam satu halaman terintegrasi. |
| 9 | **Manajemen Drop Point** | Admin dapat menambah, mengedit, menonaktifkan, dan mengaktifkan drop point. |
| 10 | **Manajemen Pengumuman** | Admin dapat membuat, mengedit, dan menghapus pengumuman. Pengumuman aktif akan tampil di semua halaman aplikasi. |
| 11 | **Manajemen Reward** | Admin dapat mengelola item reward dan mengkonfirmasi penukaran reward (dilengkapi upload foto). |
| 12 | **Manajemen Transaksi** | Admin dapat melihat, menambah, mengedit, dan menghapus transaksi dengan validasi state transition. |

## 6.5 Fitur Sistem & Keamanan

| No | Fitur | Detail |
|----|-------|--------|
| 1 | **Autentikasi** | Login dengan password yang dienkripsi menggunakan BCrypt. |
| 2 | **Role-Based Access** | Setiap endpoint dibatasi berdasarkan role (WARGA, PETUGAS, ADMIN). |
| 3 | **Account Status** | Tiga status akun: ACTIVE, SUSPENDED, BANNED. User suspended/banned tidak bisa login. |
| 4 | **Auto-Suspend** | Akun otomatis di-suspend ketika mendapat 3 warning. |
| 5 | **CSRF Protection** | Semua form dilindungi dari serangan CSRF. |
| 6 | **Pessimistic Locking** | Transaksi poin dan penukaran reward menggunakan pessimistic lock untuk mencegah data race. |
| 7 | **File Upload Validation** | Validasi tipe file (hanya gambar) dan ukuran (max 5MB). |
| 8 | **Soft Delete** | Semua data master menggunakan flag isActive untuk soft delete. |
| 9 | **Data Seeder** | Data awal (user, kategori, item sampah, drop point, reward) di-seed otomatis saat aplikasi pertama kali dijalankan. |
| 10 | **Global Exception Handler** | Semua error ditangani secara terpusat dengan tampilan yang user-friendly. |
| 11 | **Global Announcement** | Pengumuman aktif muncul di semua halaman via GlobalControllerAdvice. |

## 6.6 Tabel Status Transaksi

| Status | Keterangan | Aksi Selanjutnya |
|--------|------------|------------------|
| PENDING | Transaksi baru diajukan warga | Petugas proses atau tolak; Warga batalkan |
| DIPROSES | Sedang diproses petugas | Petugas selesaikan |
| SELESAI | Transaksi selesai, poin sudah ditambahkan | — (final) |
| DITOLAK | Ditolak petugas karena alasan tertentu | — (final) |
| DIBATALKAN | Dibatalkan oleh warga | — (final) |

## 6.7 Tabel Status Penukaran Reward

| Status | Keterangan | Aksi Selanjutnya |
|--------|------------|------------------|
| PENDING | Menunggu konfirmasi petugas | Petugas konfirmasi atau batalkan |
| SUDAH_DIAMBIL | Reward sudah diambil warga | — (final) |
| DIBATALKAN | Dibatalkan, poin & stock dikembalikan | — (final) |
