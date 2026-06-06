# BAB VIII: PENUTUP

## 8.1 Kesimpulan

Berdasarkan hasil perancangan dan implementasi aplikasi **Sortify**, dapat disimpulkan bahwa:

1. Aplikasi Sortify berhasil dibangun sebagai sistem manajemen bank sampah berbasis web menggunakan framework Spring Boot dengan arsitektur MVC (Model-View-Controller).

2. Aplikasi menyediakan tiga role pengguna (Warga, Petugas, Admin) dengan hak akses yang berbeda sesuai dengan kebutuhan masing-masing.

3. Fitur transaksi setor sampah memungkinkan warga untuk menyetor sampah secara terstruktur dengan proses yang transparan mulai dari pengajuan hingga penyelesaian.

4. Sistem poin dan reward memberikan insentif kepada warga untuk aktif menabung sampah, dengan mekanisme pessimistic lock untuk menjaga konsistensi data.

5. Kamus sampah dengan lebih dari 250 item membantu edukasi warga dalam memilah sampah dengan benar.

6. Dashboard monitoring dan log aktivitas memudahkan admin dalam memantau dan mengevaluasi kinerja sistem.

7. Fitur keamanan seperti autentikasi role-based, CSRF protection, dan manajemen status akun (suspend/ban) menjamin keamanan data dan sistem.

## 8.2 Saran

Untuk pengembangan aplikasi Sortify ke depannya, disarankan:

1. **Aplikasi Mobile**: Mengembangkan versi mobile (Android/iOS) agar lebih mudah diakses warga.

2. **Integrasi Pembayaran**: Menambahkan integrasi dengan e-wallet atau bank untuk penarikan poin dalam bentuk uang.

3. **Notifikasi Push**: Mengimplementasikan notifikasi push real-time menggunakan WebSocket atau Firebase.

4. **Analitik Lanjutan**: Menambahkan fitur prediksi tren sampah menggunakan machine learning.

5. **Multi-Bahasa**: Menambahkan dukungan multi-bahasa untuk menjangkau lebih banyak pengguna.

6. **Jadwal Penjemputan**: Fitur penjadwalan penjemputan sampah untuk drop point tertentu.

7. **Gamification**: Menambahkan elemen gamifikasi seperti badge, level, dan tantangan untuk meningkatkan partisipasi warga.

8. **Integrasi Peta**: Menampilkan peta interaktif untuk memudahkan pencarian drop point terdekat.

## 8.3 Penutup

Demikian laporan pengembangan aplikasi Sortify ini disusun. Semoga aplikasi ini dapat bermanfaat dalam pengelolaan sampah dan meningkatkan kesadaran masyarakat akan pentingnya daur ulang. Kritik dan saran yang membangun sangat diharapkan untuk pengembangan aplikasi yang lebih baik di masa mendatang.
