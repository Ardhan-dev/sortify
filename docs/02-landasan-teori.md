# BAB II: LANDASAN TEORI

## 2.1 Java

Java adalah bahasa pemrograman berorientasi objek (OOP) yang dikembangkan oleh Sun Microsystems (sekarang dimiliki Oracle). Java menggunakan konsep "Write Once, Run Anywhere" (WORA) yang berarti kode Java dapat dijalankan di berbagai platform tanpa perlu dikompilasi ulang. Java memiliki garbage collection otomatis, strong typing, dan ekosistem library yang sangat luas.

## 2.2 Spring Boot

Spring Boot adalah framework pengembangan aplikasi berbasis Java yang mempermudah pembuatan aplikasi production-grade dengan konfigurasi minimal. Spring Boot menyediakan:

- **Auto Configuration**: Konfigurasi otomatis berdasarkan dependency yang ditambahkan.
- **Embedded Server**: Server Tomcat terintegrasi, tidak perlu deploy WAR secara terpisah.
- **Starter Dependencies**: Dependency siap pakai untuk berbagai kebutuhan (web, database, security, dll).
- **Production-Ready**: Fitur monitoring, metrics, dan health check.

### Arsitektur Spring Boot (MVC Pattern)

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Controller  │────▶│   Service    │────▶│  Repository  │
│ (Web Layer)  │     │ (Business)   │     │   (Data)     │
└──────────────┘     └──────────────┘     └──────┬───────┘
       │                                          │
       ▼                                          ▼
  Thymeleaf                                  MySQL/DB
  (View)
```

1. **Controller**: Menangani request HTTP dan mengembalikan response (view/JSON).
2. **Service**: Berisi logika bisnis aplikasi.
3. **Repository**: Layer akses data menggunakan JPA/Hibernate.

## 2.3 Thymeleaf

Thymeleaf adalah template engine Java untuk aplikasi web yang mendukung HTML natural template. Thymeleaf memungkinkan server-side rendering dengan sintaks yang mudah dibaca dan dapat dibuka langsung di browser tanpa server.

## 2.4 MySQL

MySQL adalah sistem manajemen basis data relasional (RDBMS) open-source yang menggunakan SQL (Structured Query Language). MySQL handal, cepat, dan banyak digunakan untuk aplikasi web.

## 2.5 JPA (Java Persistence API) & Hibernate

JPA adalah spesifikasi standar untuk Object-Relational Mapping (ORM) di Java. Hibernate adalah implementasi paling populer dari JPA. Dengan ORM, developer bisa berinteraksi dengan database menggunakan objek Java tanpa menulis SQL manual.

## 2.6 Spring Security

Spring Security adalah framework keamanan untuk aplikasi Spring yang menyediakan:

- **Autentikasi**: Verifikasi identitas pengguna (login/logout).
- **Otorisasi**: Pembatasan akses berdasarkan role pengguna.
- **CSRF Protection**: Perlindungan dari serangan Cross-Site Request Forgery.
- **Password Encoding**: Enkripsi password menggunakan BCrypt.

## 2.7 Konsep OOP (Object-Oriented Programming)

1. **Encapsulation**: Menyembunyikan detail implementasi dengan access modifier (private, public).
2. **Inheritance**: Pewarisan properti dan method dari parent class ke child class.
3. **Polymorphism**: Method yang sama dapat memiliki perilaku berbeda.
4. **Abstraction**: Menyembunyikan kompleksitas dan menampilkan fungsionalitas esensial.

## 2.8 Bank Sampah

Bank sampah adalah sistem pengelolaan sampah berbasis masyarakat yang menerapkan prinsip 3R (Reduce, Reuse, Recycle). Nasabah (warga) menyetorkan sampah yang sudah dipilah ke bank sampah, kemudian sampah ditimbang dan dicatat. Nilai sampah dikonversi menjadi poin yang bisa ditabung atau ditukarkan dengan berbagai reward.
