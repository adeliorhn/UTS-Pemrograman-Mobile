# Selera Nusantara 🍽️

Aplikasi profil restoran modern yang dibangun menggunakan Jetpack Compose untuk Android. Aplikasi ini merupakan proyek UTS Pemrograman Mobile yang menampilkan daftar menu, detail hidangan, dan informasi profil restoran.

## ✨ Fitur Utama

-   **Splash Screen**: Animasi pembuka yang menarik saat aplikasi dijalankan.
-   **Beranda (Home)**: Menampilkan carousel promosi dan akses cepat ke menu dan profil.
-   **Daftar Menu**: Menampilkan berbagai hidangan lezat dengan harga dan ikon yang representatif.
-   **Detail Menu**: Informasi lengkap setiap hidangan termasuk deskripsi dan fitur pemberian rating bintang.
-   **Profil Restoran**: Menampilkan informasi alamat, deskripsi, dan jam operasional.
-   **Edit Profil**: Fitur untuk memperbarui informasi restoran yang tersimpan secara lokal.
-   **Mode Gelap (Dark Mode)**: Dukungan tema gelap untuk kenyamanan mata pengguna.
-   **Penyimpanan Lokal**: Menggunakan `SharedPreferences` untuk menyimpan pengaturan tema dan data profil restoran.

## 🛠️ Teknologi yang Digunakan

-   **Bahasa**: [Kotlin](https://kotlinlang.org/)
-   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
-   **Material Design**: Material 3
-   **Navigation**: Navigation Compose (Single Activity Architecture)
-   **Data Storage**: SharedPreferences
-   **Icons**: Material Icons Extended

## 🚀 Cara Menjalankan Proyek

1.  **Clone repositori ini**:
    ```bash
    git clone https://github.com/username/uts-pemrograman-mobile.git
    ```
2.  **Buka di Android Studio**:
    Buka Android Studio, pilih `File > Open`, lalu arahkan ke folder proyek ini.
3.  **Sync Gradle**:
    Tunggu hingga proses sinkronisasi Gradle selesai.
4.  **Jalankan Aplikasi**:
    Klik tombol `Run` atau tekan `Shift + F10` untuk menjalankan aplikasi pada Emulator atau Perangkat Fisik.

## 📱 Struktur Navigasi

-   `splash`: Layar pembuka.
-   `home`: Layar utama.
-   `menu`: Daftar hidangan.
-   `detail_menu/{menuId}`: Informasi rinci makanan/minuman.
-   `profile`: Informasi restoran.
-   `edit_profile`: Formulir pembaruan data restoran.

## 📝 Lisensi

Proyek ini dibuat untuk tujuan akademis (UTS Pemrograman Mobile).

---
Dibuat dengan ❤️ oleh [Nama Anda]
