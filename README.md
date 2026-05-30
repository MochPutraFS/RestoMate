# RestoMate

# Deskripsi Proyek
RestoMate adalah aplikasi desktop manajemen restoran yang kelompok kami kembangkan sebagai proyek akhir mata kuliah Pemrograman Berorientasi Objek (PBO). Aplikasi ini menerapkan konsep-konsep OOP seperti inheritance, abstraction, encapsulation, dan polymorphism dalam konteks sistem kuliner dan restoran yang nyata. Aplikasi dibangun menggunakan JavaFX sebagai framework UI dan SQLite sebagai database lokal, sehingga tidak memerlukan server eksternal dan dapat langsung dijalankan di komputer mana pun. Aplikasi ini membantu pengguna untuk mengorganisasikan segala macam pesanan, reservasi meja atau kegiatan lainnya yang biasa dilakukan di kasir ketika transaksi terjadi.

# Fitur Utama

1.Login & Autentikasi

- Sistem login dengan username dan password
- Password di-hash menggunakan algoritma SHA-256 untuk keamanan

2.Dashboard

Halaman utama setelah login sebagai pusat navigasi antar modul
Tampilan ringkas dan modern dengan navigasi ke seluruh fitur aplikasi

3.Kasir (Point of Sale)

- Tampilan menu makanan dan minuman dengan fitur pencarian dan filter kategori
- Keranjang belanja interaktif untuk menambah/mengurangi item pesanan
- Dukungan tipe pesanan: Dine In dan Take Away
- Pilihan metode pembayaran: Tunai (Cash) dan QRIS
- Fitur Hold Session — menyimpan transaksi sementara dan melayani pelanggan lain
- Generate nomor antrian otomatis untuk setiap transaksi
- Input catatan / instruksi khusus per pesanan
- Struk transaksi digital setelah pembayaran berhasil

4.Manajemen Menu

- Tambah, edit, dan hapus item menu (Makanan & Minuman)
- Pengelolaan stok per item menu
- Atribut khusus per kategori:

Makanan: tingkat kepedasan (Tidak Pedas / Pedas / Sangat Pedas)
Minuman: suhu penyajian (Dingin/Es atau Panas)

Filter dan pencarian menu berdasarkan nama atau kategori
Export daftar menu ke file CSV

5.Reservasi Meja

- Form reservasi dengan input nama pelanggan, nomor meja, tanggal & waktu, jumlah orang
- Pre-order menu saat melakukan reservasi
- Tampilan denah meja visual secara real-time (tersedia / terisi)
- Pengelolaan meja: tambah meja baru dengan kapasitas kustom
- Update status reservasi: Aktif, Selesai, atau Dibatalkan
- Polling otomatis untuk memperbarui status meja secara berkala

6.Laporan & Statistik

- Ringkasan pendapatan harian / per periode dengan filter rentang tanggal
- Rekap per metode pembayaran (Tunai vs QRIS)
- Grafik batang tren pendapatan harian
- Diagram lingkaran (Pie Chart) distribusi pembayaran
- Statistik jumlah transaksi dan rata-rata nilai transaksi (Average Order Value)
- Fitur Tutup Shift untuk menutup sesi kerja dan mereset laporan harian
- Export laporan ke file CSV

# Kontributor

1. Moch.Putra Firmansyah Sultan
2. Achmad TIfli
3. Aliyah Fitraturramadhani