# TickerTide 🌊📈

[![Download APK](https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android)](https://github.com/Faderu/TickerTide/releases/tag/v1.0.0)

**TickerTide** adalah aplikasi pemantauan saham dan simulasi *trading* (Mock Trading) interaktif yang memberikan kemudahan dalam menganalisis pasar saham secara *real-time*. Pengguna dapat mencari saham, menambahkannya ke dalam *Watchlist*, mengatur *Price Alerts*, serta melakukan simulasi beli/jual saham menggunakan uang virtual sebesar $10,000.

Aplikasi ini dikembangkan untuk memenuhi **Tugas Final Lab Mobile 2026**.

## 📥 Unduh Aplikasi
Anda dapat mengunduh versi terbaru TickerTide melalui tombol di atas, atau melalui halaman **[Releases](https://github.com/Faderu/TickerTide/releases/tag/v1.0.0)** repositori ini.

## 👨‍💻 Identitas Pengembang
- **Nama:** Muhammad Fadel Aryasatya Makkulau
- **NIM:** H071241071

## ✨ Fitur Utama
1. **Real-time Market Data:** Melacak harga saham secara langsung menggunakan Financial Modeling Prep (FMP) API via Retrofit.
2. **Mock Trading Portfolio:** Simulasi *trading* tanpa risiko dengan uang virtual. Pengguna bisa membeli/menjual saham dan melihat keuntungan/kerugian (Profit/Loss). Terdapat fitur **Reset** untuk mengembalikan saldo ke awal.
3. **Smart Watchlist:** Menyimpan saham favorit pengguna untuk dipantau secara khusus. Mendukung *Swipe-to-Delete*.
4. **Price Alerts:** Memasang notifikasi target harga saham idaman.
5. **Offline Mode & Caching:** Data tetap bisa dilihat meski tanpa koneksi internet (menggunakan SQLite cache).
6. **Dark & Light UI Optimization:** Desain material modern yang elegan, dinamis, dan responsif.

## 🛠 Spesifikasi Teknis (Sesuai Ketentuan Tugas)

### 1. Activity
Aplikasi menggunakan lebih dari satu Activity:
- `SplashActivity`: Berfungsi sebagai *Launcher* (Entry Point) yang menampilkan animasi pembuka.
- `MainActivity`: Berfungsi sebagai *Host* utama yang menampung *Navigation Component* dan *BottomNavigationView*.

### 2. Intent
Mengimplementasikan **Explicit Intent** di dalam `SplashActivity` untuk berpindah ke `MainActivity` setelah proses inisialisasi awal selesai.

### 3. RecyclerView
Digunakan di seluruh bagian aplikasi untuk menampilkan *list* (daftar) data secara efisien dengan pola *Adapter-ViewHolder*, di antaranya:
- Menampilkan daftar saham di *Dashboard*
- Menampilkan koleksi saham di *Watchlist*
- Menampilkan aset yang dimiliki di *Portfolio*
- Menampilkan daftar target harga di *Price Alerts*

### 4. Fragment & Navigation
Aplikasi menerapkan arsitektur modern **Single-Activity, Multiple-Fragments**. 
Menggunakan **Jetpack Navigation Component** (`nav_graph.xml`) untuk mengelola navigasi antar *Fragment* (`DashboardFragment`, `WatchlistFragment`, `PortfolioFragment`, `AlertsFragment`, dan `StockDetailFragment`). Perpindahan halaman dilengkapi dengan animasi transisi serta pengiriman data antar-fragment melalui *Safe Args*.

### 5. Background Thread
Semua operasi berat—seperti proses I/O membaca dan menyimpan data ke *Database SQLite* lokal—tidak berjalan di *Main Thread* (UI Thread). Aplikasi ini secara khusus menggunakan kelas pembantu `AppExecutors` (`ExecutorService`) untuk mengeksekusi *background process* agar antarmuka tidak *lagging*.

### 6. Networking & API
- **Retrofit:** Digunakan untuk melakukan *HTTP request* data saham (Harga, Volume, Perubahan, dll).
- **Endpoint:** Menggunakan API dari **Financial Modeling Prep**.
- **Error Handling & Refresh Button:** Jika gagal memuat data karena tidak ada jaringan, aplikasi akan memunculkan peringatan "Mode Offline" dan menampilkan data *cache* terakhir. Pengguna bisa memanfaatkan fitur **Swipe-to-Refresh** atau tombol **Refresh** *error state* untuk mengambil data kembali setelah internet menyala.

### 7. Local Data Persistent
- **SQLite (SQLiteOpenHelper):** Digunakan melalui kelas `DatabaseHelper` untuk menyimpan secara luring (offline):
  - *Cache* seluruh data saham
  - Status *Watchlist* pengguna
  - Item *Portfolio* (Aset Trading)
  - *Price Alerts*
- **SharedPreferences:** Digunakan untuk menyimpan konfigurasi ringan seperti jumlah **Cash Balance** saat ini.
- Aplikasi dirancang agar tetap bisa dibuka dan menampilkan data lokal saat mode *offline*.

### 8. Dua Tema (Dark Theme / Light Theme)
Aplikasi mendukung pewarnaan ganda berbasis sistem (*DayNight*). Pada `themes.xml`, telah dikonfigurasi atribut dasar untuk menunjang tampilan yang memanjakan mata, menyesuaikan warna teks, latar belakang, dan komponen aplikasi sesuai dengan *Dark* maupun *Light Theme* yang aktif di perangkat Android pengguna.

## 🚀 Cara Penggunaan Aplikasi
1. **Membuka Aplikasi:** Tunggu *Splash Screen* memuat komponen awal.
2. **Dashboard Utama:** Anda dapat mengeksplorasi daftar saham atau menggunakan bilah pencarian (*Search Bar*) untuk mencari *ticker* spesifik (Contoh: "AAPL", "TSLA").
3. **Detail Saham:** Ketuk salah satu saham untuk melihat statistiknya. Di sini Anda bisa:
   - Mengetuk ikon **Bintang** untuk memasukkannya ke *Watchlist*.
   - Mengetuk ikon **Lonceng** untuk mengatur *Price Alert*.
4. **Beli/Jual (Trading):** Di dalam halaman Detail, klik tombol panah **Buy/Sell** di ujung kanan barisan aksi untuk memulai simulasi. Atur jumlah lot yang ingin dibeli/dijual dan selesaikan transaksi.
5. **Cek Portofolio:** Buka tab *Portfolio* di bar navigasi bawah untuk memantau performa aset Anda. Anda bisa mereset uang kembali ke $10,000 kapan saja menggunakan tombol panah putar di sudut kanan atas.
6. **Kelola Daftar:** Pada tab *Watchlist* maupun *Alerts*, Anda dapat menghapus data secara praktis dengan sekadar menggeser ( *swipe* ) item ke arah kiri atau kanan.

---
*Dibuat untuk Tugas Akhir Lab Mobile 2026*