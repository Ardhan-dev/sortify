USE `kelola_sampah`;

-- Suntik Data Log Aktivitas
INSERT INTO `log_aktivitas` (`id`, `aksi`, `deskripsi`, `role`, `username`, `waktu`) VALUES
  (1, 'REGISTRASI_WARGA', 'Warga baru mendaftar: Kai Yon (Kaiheila)', 'WARGA', 'Kaiheila', '2026-05-30 01:49:52.758043'),
  (2, 'TUKAR_REWARD', 'Menukar Gula 1 kg (750 poin) — sisa poin: 25', 'WARGA', 'Kaiheila', '2026-05-30 02:44:52.532284'),
  (3, 'TUKAR_REWARD', 'Menukar Beras 2kg (1000 poin) di Drop Point Alun-Alun Kota — sisa poin: 24555', 'WARGA', 'Kaiheila', '2026-05-30 02:56:00.267693'),
  (4, 'TUKAR_REWARD', 'Menukar Beras 2kg (1000 poin) di Drop Point Alun-Alun Kota — sisa poin: 23555', 'WARGA', 'Kaiheila', '2026-05-30 03:02:49.883213'),
  (5, 'TUKAR_REWARD', 'Menukar Beras 2kg (1000 poin) di Drop Point Alun-Alun Kota — sisa poin: 22555', 'WARGA', 'Kaiheila', '2026-06-01 15:10:07.681719'),
  (6, 'TAMBAH_REWARD', 'Menambah reward Beras Anak Raja 5 kg (25000 poin, stok 5)', 'PETUGAS', 'petugas', '2026-06-06 00:07:43.333863'),
  (7, 'TUKAR_REWARD', 'Menukar Beras Anak Raja 5 kg (25000 poin) — sisa poin: 198055', 'WARGA', 'Kaiheila', '2026-06-06 00:08:22.920520'),
  (8, 'REGISTRASI_WARGA', 'Warga baru mendaftar: Koe (Koe)', 'WARGA', 'Koe', '2026-06-06 02:21:28.583542'),
  (9, 'REGISTRASI_WARGA', 'Warga baru mendaftar: Kai (Kai)', 'WARGA', 'Kai', '2026-06-06 02:30:49.361485'),
  (10, 'TUKAR_REWARD', 'Menukar Beras Anak Raja 5 kg (25000 poin) — sisa poin: 975000', 'WARGA', 'Kai', '2026-06-06 02:34:06.609323'),
  (11, 'WARNING_USER', 'Memberi peringatan ke warga — test\r\n', 'PETUGAS', 'admin', '2026-06-06 02:52:06.020806'),
  (12, 'WARNING_USER', 'Memberi peringatan ke warga — test2', 'PETUGAS', 'admin', '2026-06-06 02:52:14.933416'),
  (13, 'SUSPEND_USER', 'Menonaktifkan akun warga — test3\r\n', 'PETUGAS', 'admin', '2026-06-06 02:52:24.846434'),
  (14, 'SUSPEND_USER', 'Menonaktifkan akun Kai — test', 'PETUGAS', 'admin', '2026-06-06 02:53:57.259533'),
  (15, 'ACTIVATE_USER', 'Mengaktifkan kembali akun warga', 'ADMIN', 'admin', '2026-06-06 02:54:24.516862'),
  (16, 'ACTIVATE_USER', 'Mengaktifkan kembali akun Kai', 'ADMIN', 'admin', '2026-06-06 02:54:27.358656'),
  (17, 'TUKAR_REWARD', 'Menukar Beras Anak Raja 5 kg (25000 poin) — sisa poin: 950000', 'WARGA', 'Kai', '2026-06-06 04:21:17.712500');

-- Suntik Akun Tambahan yang Belum Ada di Seeder
INSERT INTO `users` (`id`, `full_name`, `password`, `role`, `total_points`, `username`, `account_status`, `warning_count`) VALUES
  (6, 'Kai Yon', '$2a$10$jbzcOm88WvZO48dCkRPs/ey0zd9wwnQFaexBKWuiNGWWY7uwMxGuu', 'WARGA', 198055, 'Kaiheila', 'ACTIVE', 0),
  (7, 'Joe', '$2a$10$MLEZnWND6nLql4pzsxg.quPQaiIEwCBk.qNN3YCJzaLn4tT1fE1mK', 'WARGA', 0, 'Joe', 'ACTIVE', 0),
  (8, 'Koe', '$2a$10$vpLqIdFnQv3.qsGrMOSZsOMJRCshzJE9hQj5GgQsk2ggtKoELs8H2', 'WARGA', 0, 'Koe', 'ACTIVE', 0),
  (9, 'Kai', '$2a$10$0O4UHY8Vw0edYxy7Djx5KeslnTP2QaOHzki.YWBGZo4oKXN7Sc70K', 'WARGA', 950000, 'Kai', 'ACTIVE', 0),
  (10, 'Jokowi', '$2a$10$YPGLKOQkCOx3yC220s2unuM9lTmRT0O9mnpdo7sioHye6nBRgeo/e', 'PETUGAS', 0, 'Jokowi', 'ACTIVE', 0);

-- Suntik Profil Warga Ekstra
INSERT INTO `warga` (`id_warga`, `alamat`, `nama`, `no_hp`, `username`, `user_id`) VALUES
  (2, 'Jalan raya purwakarta', 'Kai Yon', '878123456', 'Kaiheila', 6),
  (3, '-', 'Joe', '-', 'Joe', 7),
  (4, 'Jalan Antarasi no 12, Jakarta Barat', 'Koe', '082312412441', 'Koe', 8),
  (5, 'Jalan Cikutra Raya no 21', 'Kai', '0823124124412', 'Kai', 9);