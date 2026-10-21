# Jastipin Mobile Automation

<p>
<img src="https://img.shields.io/badge/Katalon_Studio-11-00B3E3?style=flat-square" alt="Katalon Studio 11"/>
<img src="https://img.shields.io/badge/Appium-UiAutomator2-662D91?style=flat-square" alt="Appium UiAutomator2"/>
<img src="https://img.shields.io/badge/platform-Android_%2F_Kotlin_%2F_Compose-3DDC84?style=flat-square" alt="Android"/>
<img src="https://img.shields.io/badge/test_cases-26-brightgreen?style=flat-square" alt="26 test cases"/>
<img src="https://img.shields.io/badge/suites-6-blue?style=flat-square" alt="6 suites"/>
</p>

Proyek otomasi Android untuk aplikasi **Jastipin by Ell**, aplikasi native Kotlin +
Jetpack Compose hasil migrasi dari website. Dibuat dengan Katalon Studio 11.

Aplikasi yang diuji dibangun sendiri, jadi seluruh penandanya sudah dirancang untuk
pengujian sejak awal, bukan ditambahkan belakangan.

> **English summary.** End-to-end UI automation for a Kotlin + Jetpack Compose Android app:
> 26 test cases across 6 suites, built on the Page Object Model with data-driven login and
> product cases. The interesting part is not the happy path — it is the six execution runs
> documented below, where each red run was traced to a real root cause (Compose lazy lists
> that never compose off-screen nodes, the soft keyboard covering the scroll area, Appium's
> default 5000×density swipe speed turning a swipe into a fling). Full debugging trail in
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

### Run 6 — 15 Oktober 2026, 19:51, `Full_Regression_Suite`

**Hasil: 25 lulus, 1 error — hanya TC01.** Seluruh 26 test case sisanya hijau, termasuk
tiga yang sebelumnya gagal karena papan ketik. Perbaikan papan ketik berhasil.

Satu yang tersisa bukan soal locator dan bukan soal papan ketik. Buktinya ada di
`appium.log`: selama TC01, **hanya satu perintah geser yang pernah dikirim** ke perangkat,
dan setelah itu berhenti bertanya. Padahal 12 kali geser seharusnya menghasilkan 12
perintah.

Sebabnya parameter `speed` pada `mobile: swipeGesture`. Kalau tidak disebut, Appium
memakai bawaan **5000 x kerapatan layar**. Di Pixel 5 kerapatannya 2,75, jadi kecepatannya
**13.750 px per detik**. Geseran sepanjang 1.264 px selesai dalam **0,09 detik** — itu
bukan geseran, itu **lemparan**. Momentumnya membuat daftar meluncur jauh melewati sasaran,
dan luncuran itu masih berjalan saat pemeriksaan berikutnya dilakukan, sehingga elemen di
tengah daftar terlewat di antara dua pemeriksaan.

Ini juga menjelaskan kenapa elemen di ujung bawah (tombol admin, tombol simpan) selalu
ketemu sementara elemen di tengah tidak: luncuran yang dilempar ke bawah berhenti di batas
akhir gulir, yaitu tepat di ujung. Di tengah tidak ada yang menghentikannya.

Perbaikannya: `speed: 1000` disebut eksplisit di `geserSekali()`. Geseran berhenti tepat di
tempatnya, jadi tidak ada elemen yang bisa terlewat. Validator sekarang menolak
`swipeGesture` yang tidak menyebut kecepatan.

**Biaya dari perbaikan ini: eksekusi jadi lebih lambat.** Geseran yang tadinya 0,09 detik
sekarang 1,26 detik, dan itu memang harga yang dibayar untuk menghilangkan lemparan. Yang
tidak perlu adalah jeda tetap satu detik di `sembunyikanPapanKetik()`, yang ikut dibayar di
setiap pengguliran walau papan ketiknya tidak sedang tampil. Jeda itu sudah dibuang:
pemeriksaan keterlihatan yang menyusul sudah menunggu sampai dua detik, jadi tata letak yang
kembali penuh terserap di situ.

### TC22 setelah kecepatan geser diperbaiki

TC22 lulus di run 6 (43,5 detik) dan error di versi berikutnya. Penyebabnya bukan
kecepatannya sendiri, melainkan posisi berhenti pengguliran yang jadi berbeda.

`deleteFirstImage()` menggulir sampai tombol `btnDeleteImage_0` baru terlihat, lalu berhenti.
Tombol itu ada di deretan foto, dan tulisan `tvProductNotice` berada **di bawahnya**, di bawah
baris "Tambah foto contoh". Kalau pengguliran berhenti tepat saat tombol hapus baru muncul di
tepi bawah layar, tulisannya berada di luar layar. Pemeriksaan tulisan itu sebelumnya tidak
menggulir lebih dulu, jadi hasilnya bergantung pada posisi berhenti pengguliran, yang berubah
begitu kecepatan geser diubah.

Perbaikannya: `ProductEditorScreen.verifyNotice()` menggulir ke `tvProductNotice` lebih dulu.
Pemeriksaannya tidak dilonggarkan, hanya dipastikan elemennya ada di layar sebelum dibaca.

Satu perbaikan sampingan ikut dibawa: `TestListener` hanya menyimpan bukti kegagalan untuk
status `FAILED`, padahal kegagalan karena exception dilaporkan Katalon sebagai `ERROR`.
Akibatnya run 6, satu-satunya run yang benar-benar sampai ke aplikasi dan gagal, justru tidak
punya tangkapan layar maupun pohon elemen. Sekarang semua status selain `PASSED` disimpan.

### TC01 kembali error: jarak geser, bukan kecepatan

Setelah `speed: 1000` dipasang, TC01 masih error, dan **barang yang gagal berbeda tiap
eksekusi** (`3e682be1` di satu run, `bc0ea786` di run lain). Itu tanda masalahnya bukan satu
barang tertentu.

Yang pertama dibersihkan: data dan urutannya. Kedelapan ID ada di `seed.json`, semuanya
berstatus `active`, `sort_order` 0 sampai 7 tanpa celah, dan query katalog memakai
`ORDER BY sort_order ASC, name ASC`. Jadi urutan render sudah pasti dan cocok dengan urutan
yang diuji. Dugaan "data tidak sesuai seed" dan "urutan berbeda dari asumsi" tidak berlaku.

Sebabnya ada di **jarak** geser, bukan kecepatannya. Ukurannya diambil bukan dari perkiraan,
tapi dari pengukuran sungguhan: tes Robolectric membaca kotak tiap penanda di pohon elemen
aplikasi.

- Layar: 2.338 px
- Tinggi satu kartu katalog: **693 px**, jarak antar baris 32 px
- Tulisan nama barang: tinggi **35 px**, letaknya **di bagian bawah kartu**

Kotak batas geser tingginya 0,6 x 2.340 px = 1.404 px, dan `percent: 0.9` berarti satu
geseran menempuh **1.264 px**. Itu **hampir dua kali tinggi satu kartu**. Satu geseran bisa
melompati satu baris penuh tanpa pernah berhenti di atasnya. Posisi awal gulir bergeser
sedikit antar eksekusi, sehingga baris yang terlewat pun berbeda: kadang baris 3
(`3e682be1`, barang ke-5), kadang baris 4 (`bc0ea786`, barang ke-7).

Ada sebab kedua yang memperbesar peluangnya: yang dicari TC01 adalah **tulisan nama**,
sedangkan tulisan nama ada di bawah gambar, di dalam kartu. Saat kartu baru masuk layar,
yang terlihat lebih dulu bagian atasnya, dan tulisan namanya masih di luar layar. Kartu
baris 3 misalnya hanya terlihat 74% (y 1.822 sampai 2.338), sedangkan tulisan namanya ada di
y 2.437, di luar layar. Jadi penanda itu bisa saja sudah ada di pohon, tapi belum terlihat.

Dua perbaikan:

1. **Jarak geser diperpendek.** `percent` turun dari 0,9 ke **0,25**, jadi satu geseran
   menempuh 0,25 x 1.404 px = **351 px**. Angka itu dipilih karena **351 px lebih pendek dari
   tinggi satu kartu (693 px)**. Kalau langkahnya lebih pendek dari tinggi elemen, elemen itu
   tidak bisa terlewat: ia menempati rentang 693 px, dan setiap langkah hanya 351 px, jadi
   minimal satu pemeriksaan jatuh di dalam rentang itu. Kegagalan satu pemeriksaan tidak lagi
   berakibat, karena geseran berikutnya masih menangkap elemen yang sama.
2. **Yang dicari kartunya, bukan tulisannya.** `scrollToProduct()` sekarang memakai
   `cardProduct_<id>`, bukan `tvProductName_<id>`. Kartu tingginya 693 px dan menempati
   seluruh area kartu, sedangkan tulisan nama hanya 35 px di bagian bawah. Patokan yang lebih
   besar lebih jarang salah. Ini juga terbukti dari pengukuran: di pohon elemen gabungan,
   yang bentuknya paling dekat dengan yang dibaca Appium, `cardProduct_<id>` **selalu ada**,
   sedangkan `tvProductName_<id>` tidak muncul di pohon gabungan sama sekali karena tertelan
   penggabungan `Modifier.clickable` pada kartu.

Batas geser dinaikkan dari 12 ke 20 karena tiap geseran sekarang lebih pendek. Jeda tetap
satu detik di `geserSampaiKetemu()` dibuang: dengan langkah yang lebih pendek dari tinggi
kartu, penantian panjang tidak lagi diperlukan.

Validator sekarang juga menolak `percent` di atas 0,5, supaya jarak geser yang melompati
lebih dari satu kartu tertangkap otomatis.

## Kalau aplikasi berubah: cara mencari penanda yang baru

Ada dua jenis perubahan, dan cuma satu yang butuh dicari ulang.

**Data berubah** (barang ditambah, harga diganti, barang dihapus): **tidak ada yang perlu
dicari.** Locator per baris dibangun saat eksekusi dari ID barangnya
(`Locators.resourceId('cardProduct_' + productId)`), jadi barang baru otomatis terjangkau
selama ID-nya dikirim dari data uji. Ini alasan `Locators.groovy` ada.

**Struktur berubah** (penanda diganti nama, elemen dipindah, layar ditata ulang): locator
lama meleset dan harus dicari ulang. Tiga cara, dari yang paling murah:

**1. Aplikasi sendiri: baca kode sumbernya.** Penanda di aplikasi ini berasal dari
`testTag("...")` di Kotlin, jadi sumber kebenarannya ada di source, bukan hasil menebak:

```bash
grep -rn 'testTag("' app/src/main/java | grep -o 'testTag("[^"]*")' | sort -u
```

`tools/verify_project.py` sudah melakukan ini otomatis: ia membaca seluruh `testTag` di
aplikasi, mencocokkannya dengan Object Repository, dan **gagal** kalau ada penanda yang tidak
terjangkau proyek. Jadi langkah pertama setelah aplikasi diubah adalah menjalankan validator,
bukan menebak dari log eksekusi.

**2. Aplikasi orang lain: periksa pohon elemen yang hidup.** Kalau yang diuji APK pihak lain,
tidak ada source untuk dibaca. Yang dibaca adalah pohon elemen di perangkat.

Di Katalon: **Spy Mobile Element** (toolbar), lalu klik elemen di layar. Jendela spy
menampilkan atribut elemen yang diklik, termasuk `resource-id`. Ini cara tercepat untuk satu
elemen.

Untuk banyak elemen sekaligus, ada dua jalur:

```bash
# Jalur adb, tidak butuh Katalon. Simpan struktur layar ke XML.
bash tools/dump-hierarchy.sh ui-dump.xml

# Periksa SELURUH locator sekaligus terhadap dump itu.
python3 tools/check_locators.py ui-dump.xml
```

`check_locators.py` mencetak `FOUND` atau `MISS` untuk tiap objek di Object Repository, dan
untuk yang meleset ia menyebut nilai yang **mirip** di dalam dump. Jadi kalau
`btnAddProduct` berganti jadi `btnAddProduct_baru`, yang tercetak adalah baris `MISS` diikuti
`mirip: btnAddProduct_baru` — nama barunya langsung kelihatan, tanpa menebak.

Batasan yang perlu diketahui: alat ini memeriksa 70 objek statis di Object Repository.
Locator dinamis (`cardProduct_<id>`) dibangun di kode, jadi tidak ikut diperiksa. Dan locator
yang menunjuk elemen di dalam daftar hanya cocok kalau layarnya **sedang terbuka** saat dump
dibuat: buka layarnya dulu, baru dump.

**3. Yang sudah jalan sendiri: pohon elemen disimpan setiap kali gagal.** `TestListener`
memanggil `dumpPageSource('GAGAL_<nama case>')` untuk setiap status selain `PASSED`, jadi
setiap kegagalan sudah meninggalkan `ui-dump-GAGAL_*.xml` di folder `artifacts`. Setelah
aplikasi diperbarui dan sebuah test case mulai gagal, jalankan saja: buka XML itu di folder
laporan, dan penanda yang sebenarnya ada di situ. Tidak perlu menulis kode apa pun.

---

## Status kejujuran

Struktur proyek sudah diperiksa otomatis dan bersih. Eksekusi sudah enam kali dijalankan.
**Full_Regression_Suite: 25 dari 26 test case lulus, 1 error.** Seluruh Smoke_Suite hijau,
termasuk tiga yang sebelumnya gagal karena papan ketik. Satu error tersisa sudah diketahui
sebabnya, kecepatan geser bawaan Appium yang mengubah geseran menjadi lemparan, dan sudah
diperbaiki. Angka hijau penuh baru ada setelah kamu menjalankannya lagi.

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

> **Catatan data.** `G_WhatsappNumber` di `Profiles/default.glbl` berisi nomor contoh
> (`6281234567890`), bukan nomor toko yang sebenarnya. Ganti nilainya dengan nomor yang
> benar-benar tersimpan di pengaturan aplikasi sebelum menjalankan TC24. Nomor asli sengaja
> tidak ikut di repositori ini.

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
   tertulis elemennya tidak ada di pohon, penandanya yang tidak cocok. Dua penyebab yang
   sudah pernah terjadi: papan ketik layar masih tampil setelah kolom diisi, dan kecepatan
   geser bawaan Appium yang membuat geseran meluncur melewati sasaran. Keduanya sudah
   ditangani di `MobileActions`. Jangan kembali memakai `Mobile.scrollToText`, dan jangan
   memanggil `mobile: swipeGesture` tanpa menyebut `speed`.
8. **`Cannot find test data with id 'Data Files/LoginCases'`.** Berkas `.dat` belum
   ikut tersalin. Keduanya harus ada: `.csv` dan `.dat`.

Setiap kegagalan otomatis menyimpan tangkapan layar **dan pohon elemen** ke folder
laporan, jadi tidak perlu menjalankan ulang hanya untuk melihat keadaannya.
