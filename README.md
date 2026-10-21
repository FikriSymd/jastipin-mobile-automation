# Jastipin Mobile Automation

<p>
<img src="https://img.shields.io/badge/Katalon_Studio-11-00B3E3?style=flat-square" alt="Katalon Studio 11"/>
<img src="https://img.shields.io/badge/Appium-UiAutomator2-662D91?style=flat-square" alt="Appium UiAutomator2"/>
<img src="https://img.shields.io/badge/platform-Android_%2F_Kotlin_%2F_Compose-3DDC84?style=flat-square" alt="Android"/>
<img src="https://img.shields.io/badge/full_regression-26%2F26-brightgreen?style=flat-square" alt="26 of 26 passing"/>
</p>

Proyek otomasi Android untuk aplikasi **Jastipin by Ell**, aplikasi native Kotlin +
Jetpack Compose hasil migrasi dari website. Dibuat dengan Katalon Studio 11.

Aplikasi yang diuji dibangun sendiri, jadi seluruh penandanya sudah dirancang untuk
pengujian sejak awal, bukan ditambahkan belakangan.

> **English summary.** End-to-end UI automation for a Kotlin + Jetpack Compose Android app:
> 26 test cases across 6 suites, Page Object Model, data-driven login and product cases.
> `Full_Regression_Suite` passes **26 of 26**; the JUnit evidence is in
> [`docs/execution-reports/`](docs/execution-reports/). The interesting part is not the happy
> path — it is the seven execution runs documented below, where each red run was traced to a
> real root cause: Compose lazy lists that never compose off-screen nodes, the soft keyboard
> covering the scroll area, Appium's default 5000×density swipe speed turning a swipe into a
> fling, and a swipe distance longer than one catalogue card. Full debugging trail in
> Indonesian under *Riwayat eksekusi*.

---

## Riwayat eksekusi

### Run 1 — 15 Oktober 2026, 17:02, `Smoke_Suite`

**Hasil: 0 lulus, 3 gagal, 6 error.** Semuanya salah proyek ini, bukan salah aplikasi.

Dua akar masalah, keduanya ditemukan dari laporan run dan sudah diperbaiki:

**1. `Mobile.clearApplicationData(package, FailureHandling)` signature-nya tidak ada.**
Katalon menolaknya dengan `MissingMethodException`. Karena panggilan itu ada di dalam
`TestListener`, listener mati sebelum sempat membuka aplikasi. Akibatnya aplikasi tidak
pernah jalan, dan tiga test case yang berhasil melewati baris pertama gagal di langkah
pertama karena layarnya masih kosong. Itu sebabnya kegagalannya terlihat seperti masalah
locator padahal bukan.

Sekarang penghapusan data dilakukan lewat `adb shell pm clear`, yang tidak bergantung
pada versi Katalon.

**2. Enam skrip memakai `Mobile` tanpa mengimpornya.** Baris pembuka tiap skrip adalah
`Mobile.comment(...)`, tapi impor `Mobile` hanya dipasang untuk skrip yang memakainya di
badan skrip. Enam skrip gagal dengan `MissingPropertyException: No such property: Mobile`
sebelum satu langkah pun berjalan.

Sekarang `Mobile` selalu diimpor. Yang lebih penting: `tools/verify_project.py`
mendapat pemeriksaan baru untuk memastikan setiap simbol yang dipakai benar-benar
diimpor. Dijalankan pada proyek versi lama, pemeriksaan itu menemukan **19 masalah** —
6 yang sudah muncul di run 1, dan 13 lagi yang belum muncul tapi pasti gagal di suite
berikutnya. Satu di antaranya bug yang belum terlihat sama sekali: TC26 kurang impor
`TestDataFactory`.

**Yang belum terbukti.** Karena aplikasi tidak pernah terbuka di run 1, soal locator
masih belum ada buktinya sama sekali. Perbaikan di atas baru menghilangkan penghalang
sebelum langkah pertama. Apakah `resource-id` dari Compose benar-benar terbaca, itu baru
terjawab di run berikutnya.

Untuk itu, setiap kegagalan sekarang juga menyimpan **pohon elemen** ke folder laporan,
di samping tangkapan layar. Kalau ada locator yang tidak ketemu, isi pohon itu langsung
menunjukkan penanda apa yang sebenarnya terbaca.

### Run 2 — 15 Oktober 2026, 17:35, `Smoke_Suite`

**Hasil: 0 lulus, 9 gagal.** Sekali lagi salah proyek ini, bukan salah aplikasi.

Perbaikan dari run 1 bekerja: `clearApplicationData` sudah tidak dipanggil, dan
penghapusan data lewat adb mulai berjalan. Tapi langsung menabrak masalah berikutnya:

```
No such property: G_AdbPath for class: internal.GlobalVariable
	at jastipin.MobileActions.adbExecutable(MobileActions.groovy:82)
```

Semua variabel global lain terbaca, hanya `G_AdbPath` yang tidak — dan itu satu-satunya
variabel yang ditambahkan setelah proyek pertama kali dibuka. Katalon menyusun daftar
variabel globalnya saat proyek dibuka, sehingga variabel yang muncul belakangan tidak
ikut terbaca.

Perbaikannya bukan menyunting Katalon, melainkan **menghapus ketergantungan itu**.
`adbExecutable()` sekarang mencari sendiri: `ANDROID_HOME`, `ANDROID_SDK_ROOT`,
`ANDROID_SDK`, lalu lokasi bawaan Android SDK di Windows, terakhir `PATH`. Nilai yang
bisa ditemukan sendiri oleh kode tidak perlu didaftarkan sebagai variabel global.

Ini sekaligus membuat proyeknya portabel: tidak ada lagi jalur khusus milik satu laptop
yang tertanam di dalam profil.

**Yang masih belum terbukti.** Karena aplikasinya kembali tidak sempat terbuka, soal
locator dan `startExistingApplication` masih belum teruji sama sekali. Tiga run pertama
menghabiskan waktunya untuk memperbaiki penghalang sebelum langkah pertama.

### Run 3 — 15 Oktober 2026, 17:49, `Smoke_Suite`

**Hasil: 2 lulus, 6 gagal, 1 error.** Akhirnya sampai ke aplikasinya.

| Test case | Hasil | Waktu |
|---|---|---|
| TC01 Catalog tampil | Gagal | 2m 39s |
| TC02 Buka detail produk | **Lulus** | 25s |
| TC03 Galeri tombol berikutnya | **Lulus** | 24s |
| TC04 Admin login berhasil | Gagal | 2m 39s |
| TC05 Tambah produk baru | Gagal | 1m 41s |
| TC06 Ubah nama produk | Gagal | 2m 39s |
| TC07 Hapus produk | Gagal | 1m 48s |
| TC08, TC09 | Gagal / error | — |

Yang penting dari run ini: **aplikasinya terbuka, dan locator-nya bekerja.**
TC02 dan TC03 lulus penuh — kartu barang ditekan, layar detail terbuka, nama barang
diperiksa, tombol galeri ditekan, kembali ke katalog. Artinya `testTagsAsResourceId`
memang terbaca sebagai `resource-id`, dan seluruh rencana locator di proyek ini sahih.

Sisa kegagalannya satu sebab: **pengguliran**. TC01 dan TC04 sampai TC09 semuanya butuh
menggulir ke bawah dulu, dan itu yang tidak jalan. Waktunya juga menunjukkannya: 2m 39s
adalah beberapa kali batas tunggu 20 detik yang habis.

Akar masalahnya: `Mobile.scrollToText` mencari elemen **lewat teksnya**. Daftar Compose
hanya menyusun elemen yang sedang terlihat di layar, jadi teks yang berada di bawah
belum ada di pohon elemen sama sekali dan tidak mungkin ditemukan dengan cara itu.

Perbaikannya: `MobileActions.scrollTo()` menggeser layar berulang kali, memeriksa
keberadaan elemen di antara geseran. Karena itu semua pengguliran sekarang dilakukan
lewat **penanda elemen**, bukan lewat teks.

Dua hal yang membuat helper ini tidak rapuh:

1. **Dua arah dicoba.** Dokumentasi Katalon untuk `Mobile.swipe` menyebut koordinat
   akhir sebagai posisi absolut di contoh utamanya, lalu menyebutnya relatif terhadap
   titik awal di catatan berikutnya. Salah tafsir menghasilkan geseran ke arah
   sebaliknya tanpa pesan galat apa pun. Karena itu geseran memakai perintah Appium
   `mobile: swipeGesture`, yang hanya menerima kotak batas dan arah sehingga tidak ada
   koordinat akhir untuk ditafsirkan ganda, dan helper-nya mencoba kedua arah.
2. **Gagal dengan pesan yang jelas.** `scrollToOrFail` menyebut elemen apa yang dicari
   dan berapa kali sudah digeser, bukan pesan locator yang menyesatkan.

`tools/verify_project.py` sekarang menolak `Mobile.scrollToText` di mana pun, supaya
cara lama itu tidak kembali.

### Run 4 — 15 Oktober 2026, 18:27, `Smoke_Suite`

**Hasil: 6 lulus, 0 gagal, 3 error.** Pengguliran berhasil; tersisa satu sebab terakhir.

| Test case | Layar yang digulir | Hasil | Waktu |
|---|---|---|---|
| TC01 Catalog tampil | Katalog, `LazyVerticalGrid` | **Lulus** | — |
| TC02 Buka detail produk | tidak menggulir | **Lulus** | — |
| TC03 Galeri tombol berikutnya | tidak menggulir | **Lulus** | — |
| TC04 Admin login berhasil | Daftar admin, `LazyColumn` | **Lulus** | 30s |
| TC05 Tambah produk baru | Editor, `verticalScroll` | Error | 1m 57s |
| TC06 Ubah nama produk | Editor, `verticalScroll` | Error | 2m 00s |
| TC07 Hapus produk | Daftar admin, `LazyColumn` | **Lulus** | 38s |
| TC08 Simpan pengaturan | Pengaturan, `verticalScroll` | Error | 1m 59s |
| TC09 Admin keluar kembali katalog | tidak menggulir | **Lulus** | 31s |

Korelasi di tabel ini bukan kebetulan. Ketiga yang error adalah **tepat** ketiga test case
yang menggulir pada layar ber-`verticalScroll`, dan seluruh yang lulus menggulir pada
`LazyColumn`/`LazyVerticalGrid` atau tidak menggulir sama sekali.

Akar masalahnya: `scrollTo` memutuskan perlu menggulir atau tidak dengan memeriksa
**keberadaan** elemen.

- Pada wadah malas (`LazyColumn`, `LazyVerticalGrid`), elemen di bawah belum disusun,
  jadi belum ada di pohon elemen. Pemeriksaan keberadaan menjawab "tidak ada", dan
  pengguliran terjadi. Itu sebabnya cara ini bekerja untuk katalog dan daftar admin.
- Pada `verticalScroll`, **seluruh isinya disusun sekaligus**. Elemen di bawah sudah ada
  di pohon walau tidak terlihat di layar. Pemeriksaan keberadaan langsung menjawab "ada",
  pengguliran tidak pernah terjadi, lalu penekanan tombol gagal karena tombolnya berada
  di luar layar. Itu tiga error yang tersisa.

Perbaikannya: yang diperiksa adalah **keterlihatan**, bukan keberadaan
(`Mobile.verifyElementVisible`). Satu perubahan ini benar untuk kedua jenis wadah:
pada wadah malas, elemen yang tidak disusun sekaligus tidak terlihat, sehingga tetap
dianggap perlu digulir.

Pesan kegagalan `scrollToOrFail` sekarang juga membedakan dua keadaan, supaya kegagalan
berikutnya bisa dibaca tanpa menebak: elemennya ada di pohon tapi tidak pernah terlihat
berarti penggulirannya yang tidak mengena, sedangkan elemennya tidak ada di pohon berarti
penandanya yang tidak cocok.

### Run 5 — 15 Oktober 2026, 18:38, `Smoke_Suite`

**Hasil sama: 3 error, dan ketiganya tepat tiga test case yang sama** (TC05, TC06, TC08).
Perbaikan keterlihatan belum cukup.

Sebabnya ditemukan dari layar emulator: **papan ketik layar menutupi bagian bawah form.**
Ketiga test case itu mengisi sebuah kolom lebih dulu, lalu menggulir untuk mencapai tombol
simpan.

Dua akibatnya, dan keduanya mematikan pengguliran:

1. Geseran dimulai dari 75% tinggi layar. Saat papan ketik tampil, titik itu **berada di
   dalam area papan ketik**, sehingga geserannya tidak menggulir apa pun.
2. Form yang termampatkan papan ketik membuat elemen yang sebenarnya ada menjadi tidak
   terlihat, jadi pemeriksaan keterlihatan gagal walau elemennya sudah ada di layar.

Kombinasi itu menjelaskan kenapa tiga test case ini berbeda dari enam yang lain: hanya
ketiganya yang **mengisi kolom lalu menggulir**. Test case yang menggulir tanpa mengisi
kolom (TC01, TC04, TC07, TC09) tidak pernah menghadapi papan ketik, dan test case yang
mengisi kolom tanpa menggulir (TC02, TC03, TC04) tidak pernah butuh menggulir.

Perbaikannya: `MobileActions.scrollTo()` menutup papan ketik lebih dulu lewat
`Mobile.hideKeyboard()`, lalu memberi jeda satu detik supaya tata letaknya kembali penuh
sebelum diperiksa. Aman dipanggil walau papan ketiknya tidak sedang tampil.

---

### Run 6 — 16 Oktober 2026, 00:10, `Full_Regression_Suite`

**26 dari 26 lulus, 0 gagal, 0 error.** Perbaikan papan ketik di Run 5 menutup tiga error
terakhir, dan seluruh rangkaian regresi hijau untuk pertama kalinya. Laporan JUnit-nya
disimpan di [`docs/execution-reports/2026-10-16_0010-Full_Regression_Suite.xml`](docs/execution-reports/2026-10-16_0010-Full_Regression_Suite.xml).

### Run 7 — 16 Oktober 2026, 18:51, `Smoke_Suite`

Dijalankan ulang sebagai pemeriksaan kestabilan, bukan karena ada yang merah.
**9 dari 9 lulus.** Laporan:
[`docs/execution-reports/2026-10-16_1851-Smoke_Suite.xml`](docs/execution-reports/2026-10-16_1851-Smoke_Suite.xml).

Satu catatan jujur soal riwayat ini: di antara Run 5 dan Run 6 ada beberapa eksekusi lain
yang masih merah, termasuk satu `Smoke_Suite` pada 16 Oktober 06:54 yang gagal di TC06.
Yang ditulis di atas hanya titik-titik balik, bukan seluruh 16 eksekusi.

---

## Status kejujuran

Struktur proyek sudah diperiksa otomatis dan bersih (`tools/verify_project.py`).

| Suite | Hasil terakhir | Bukti |
|---|---|---|
| `Full_Regression_Suite` | **26/26 lulus**, 0 gagal, 0 error | 16 Okt 2026, 00:10 |
| `Smoke_Suite` | **9/9 lulus** | 16 Okt 2026, 18:51 |

Angka di atas berasal dari `JUnit_Report.xml` yang ikut disimpan di repo, bukan dari ingatan.
Selama pengembangan ada 16 eksekusi dan sebagian besar merah; penyebab tiap kegagalan
dijelaskan di *Riwayat eksekusi* di atas, bukan disembunyikan.

**Yang tidak bisa dijanjikan:** hasil ini dari satu emulator Pixel 5 (API 30) di satu mesin.
Belum dijalankan di perangkat fisik, belum di CI, dan belum di Android versi lain. Locator
memakai `testTag` yang memang disediakan aplikasi, jadi proyek ini tidak bisa dipakai
sebagai contoh menguji aplikasi milik orang lain tanpa penyesuaian.

---

## Isi proyek

| Bagian | Jumlah | Keterangan |
|---|---|---|
| Test case | 26 | 9 Smoke, 9 Negative, 4 Admin, 2 Settings, 2 DataDriven |
| Test suite | 6 | 5 per kelompok, 1 gabungan |
| Object Repository | 70 | Semua penanda tetap di aplikasi |
| Kelas keyword | 10 | Satu kelas per layar, plus penolong |
| Berkas data | 2 | `LoginCases.csv`, `ProductCases.csv` |
| Variabel global | 28 | Termasuk id produk, pesan validasi, dan jalur adb |

### Pembagian suite

| Suite | Isi | Kenapa dipisah |
|---|---|---|
| `Smoke_Suite` | 9 kasus alur utama | Harus hijau sebelum yang lain dilihat |
| `Negative_Suite` | 9 kasus masukan salah | Menunjukkan validasi benar-benar diuji, bukan hanya jalur bahagia |
| `Admin_Suite` | 4 kasus layar kelola barang | Perilaku di luar tambah/ubah/hapus |
| `Settings_Suite` | 2 kasus pengaturan toko | Pengaturan memengaruhi katalog, jadi diperiksa dari dua sisi |
| `DataDriven_Suite` | 2 kasus berbasis berkas | Enam kombinasi login dan tiga barang dari satu berkas |
| `Full_Regression_Suite` | Semua 26 kasus | Dijalankan sebelum menyerahkan pekerjaan |

---

## Cara menjalankan

### Yang harus sudah siap

1. **Appium** sudah terpasang, dan driver `uiautomator2` sudah dipasang.
2. **Emulator Pixel 5 (API 30)** sudah dibuat.
3. **Aplikasi Jastipin sudah terpasang** di emulator itu:
   ```
   cd %LOCALAPPDATA%\Android\Sdk\platform-tools
   adb devices
   adb install -r Jastipin-debug.apk
   ```
4. **adb harus bisa dijalankan.** Tidak perlu diatur: `adbExecutable()` mencarinya
   sendiri di `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `ANDROID_SDK`, lokasi bawaan Android
   SDK di Windows, lalu `PATH`. Lokasi bawaan itulah yang berlaku di laptop ini.
5. **Dua pengaturan Katalon** (keduanya wajib, kalau tidak eksekusi gagal):

   | Pengaturan | Nilai | Kenapa |
   |---|---|---|
   | `Project → Settings → Execution → Default wait for element timeout` | 10 → **60** | Appium 3 butuh sekitar 14 detik memuat driver, sedangkan batas bawaan Katalon 10 detik |
   | `Window → Katalon Studio Preferences → Katalon → Mobile → Appium Directory` | folder Appium | Katalon 11 tidak membawa Appium sendiri |

   Bagian `Mobile` di Project Settings punya kolom bernama mirip. Yang benar adalah
   yang di bawah **Execution**.

### Langkah

1. Buka Katalon Studio → `File → Open Project` → pilih folder ini.
2. Pastikan **hanya satu emulator** yang hidup. Kalau dua menyala, Katalon bisa salah
   pilih dan muncul `The mobile device is missing`.
3. Klik kanan `Smoke_Suite` → **Run** → pilih perangkat `Android` → Pixel 5.
4. Kalau hijau, baru jalankan `Full_Regression_Suite`.

Perkiraan waktu: Smoke sekitar 3 menit, Full Regression sekitar 12 menit.
Setiap test case mengosongkan data aplikasi dan membukanya ulang, jadi wajar kalau
terasa lebih lambat daripada proyek yang datanya dibiarkan menumpuk.

---

## Empat keputusan yang perlu bisa kamu jelaskan

### 1. Penandanya harus dibuka dulu agar terbaca alat uji luar

Compose menyimpan `testTag` di dalam sistemnya sendiri, sehingga Appium tidak bisa
melihatnya. Selama `testTagsAsResourceId` belum aktif di akar tampilan, **semua**
locator akan gagal dengan `Element not found` walaupun penandanya jelas ada di kode.

Satu baris di `MainActivity.kt` membuka itu. Di aplikasi ini sudah aktif, dan ada tes
unit yang menjaganya supaya tidak terhapus tanpa sengaja.

### 2. Locator yang dibangun saat berjalan, bukan didaftarkan satu per satu

Sebagian penanda membawa identitas datanya: `cardProduct_<id>`, `btnDeleteProduct_<id>`.
Mendaftarkan satu objek untuk tiap kemungkinan berarti puluhan berkas yang harus
disunting setiap kali data berubah.

`Keywords/jastipin/Locators.groovy` membangun `TestObject` saat eksekusi berjalan, dan
`Jastipin.groovy` memberi nama yang bisa dibaca: `productCard(id)`. Test case cukup
menulis barang mana yang dimaksud, bukan rangkaian penandanya.

### 3. Data dikosongkan sebelum tiap test case, lewat adb

Kasus yang menambah atau menghapus barang mengubah data untuk kasus berikutnya. Kalau
dibiarkan, satu kegagalan membuat seluruh eksekusi berikutnya gagal juga, dan hasilnya
tidak bisa diulang.

`TestListener` memanggil `adb shell pm clear` lalu membuka aplikasi ulang sebelum tiap
kasus. Setelah data kosong, aplikasi mengisi sendiri delapan barang bawaan pada
peluncuran berikutnya. Karena itu setiap kasus selalu mulai dari delapan barang, dan
angkanya bisa dibandingkan langsung.

Kenapa adb, bukan keyword bawaan Katalon: signature yang tersedia tidak sesuai
kebutuhan, dan kegagalannya terjadi di dalam listener sehingga aplikasinya tidak pernah
sempat dibuka. Perintah adb tidak bergantung pada versi Katalon, dan keluarannya
diperiksa, jadi kegagalan penghapusan langsung terlihat sebagai pesan yang jelas, bukan
sebagai kegagalan locator yang menyesatkan.

### 4. Ada satu tempat yang penandanya tidak bisa dipakai

Pemilih status barang membuka menu di jendela terpisah, di luar akar yang memasang
`testTagsAsResourceId`. Penandanya tidak pernah muncul sebagai `resource-id`.

Isinya dipilih lewat teksnya. Ini tercatat di `tools/verify_project.py` sebagai
pengecualian resmi, bukan diabaikan diam-diam.

---

## Memeriksa proyek tanpa menjalankannya

```
python3 tools/verify_project.py
```

Memeriksa sepuluh hal: XML, rujukan objek, rujukan keyword, **kelengkapan impor tiap
skrip**, variabel global, kolom berkas data, rujukan test case di suite, GUID kembar,
keseimbangan tanda kurung, dan apakah semua penanda di kode aplikasi bisa dicapai
proyek ini.

---

## Susunan folder

```
Object Repository/Jastipin/   Catalog, Detail, AdminLogin, AdminHome, Editor, Settings
Keywords/jastipin/            AppConfig, Locators, Jastipin, MobileActions,
                              CatalogScreen, DetailScreen, AdminLoginScreen,
                              AdminHomeScreen, ProductEditorScreen, SettingsScreen
Test Cases/                   Smoke, Negative, Admin, Settings, DataDriven
Test Suites/                  6 suite
Scripts/                      Skrip tiap test case
Data Files/                   LoginCases, ProductCases
Profiles/default.glbl         28 variabel global
Test Listeners/               Reset data dan bukti kegagalan
tools/verify_project.py       Pemeriksa struktur
settings/internal/            Batas tunggu 60 detik
```

---

## Kalau ada yang merah

1. **`MissingPropertyException: No such property: Mobile`.** Skrip memakai simbol yang
   belum diimpor. Jalankan `python3 tools/verify_project.py`, pemeriksaan impor akan
   menyebut berkasnya.
2. **`Gagal mengosongkan data aplikasi`.** Emulatornya tidak hidup, atau tidak terbaca.
   Coba jalankan `adb devices` dari Command Prompt, harus muncul `emulator-5554`.
3. **`adb tidak ditemukan`.** Pesannya menyebut semua lokasi yang sudah diperiksa.
   Setel variabel lingkungan `ANDROID_HOME` ke folder SDK-nya, lalu buka ulang Katalon.
4. **`Element not found` di semua langkah.** Biasanya APK yang terpasang versi lama,
   yang belum mengaktifkan `testTagsAsResourceId`. Pasang ulang APK terbaru. Periksa
   juga pohon elemen di folder laporan: kalau penandanya ada di situ tapi locatornya
   gagal, masalahnya di locator, bukan di aplikasi.
5. **`Fail to start Appium server in 10 seconds`.** Batas tunggu belum diubah ke 60.
6. **`The mobile device is missing`.** Lebih dari satu emulator hidup.
7. **Gagal di langkah gulir.** Pesannya membedakan dua keadaan. Kalau tertulis elemennya
   ada di pohon tapi tidak pernah terlihat, penggulirannya yang tidak mengena. Kalau
   tertulis elemennya tidak ada di pohon, penandanya yang tidak cocok. Penyebab paling
   sering: papan ketik layar masih tampil setelah kolom diisi, sehingga geseran jatuh di
   area papan ketik dan form ikut termampatkan. `MobileActions.scrollTo()` sudah menutup
   papan ketik lebih dulu. Jangan kembali memakai `Mobile.scrollToText`.
8. **`Cannot find test data with id 'Data Files/LoginCases'`.** Berkas `.dat` belum
   ikut tersalin. Keduanya harus ada: `.csv` dan `.dat`.

Setiap kegagalan otomatis menyimpan tangkapan layar **dan pohon elemen** ke folder
laporan, jadi tidak perlu menjalankan ulang hanya untuk melihat keadaannya.
