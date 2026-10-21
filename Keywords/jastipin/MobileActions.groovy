package jastipin

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.time.Duration

import org.openqa.selenium.By

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.util.KeywordUtil

/**
 * Interaksi yang tidak terikat satu layar. Kelas layar dibangun di atas ini.
 */
class MobileActions {

    /**
     * Membuka aplikasi yang sudah terpasang di perangkat.
     *
     * Aplikasi dipasang lebih dulu dengan adb, bukan diunggah tiap eksekusi,
     * supaya data lokal di dalamnya tidak ikut terhapus tiap test case.
     */
    @Keyword
    static void launchApp() {
        Mobile.startExistingApplication(AppConfig.appPackage())
        dismissSystemDialogIfPresent()
    }

    /**
     * Mengosongkan data aplikasi lalu membukanya kembali.
     *
     * Dipakai sebelum tiap test case supaya hasilnya bisa diulang: kasus yang
     * menambah atau menghapus barang tidak boleh mewariskan keadaannya ke kasus
     * berikutnya. Setelah data dikosongkan, aplikasi mengisi sendiri delapan
     * barang bawaan pada peluncuran berikutnya.
     *
     * Penghapusan dilakukan lewat adb, bukan lewat keyword Katalon. Signature
     * Mobile.clearApplicationData(String, FailureHandling) tidak ada di Katalon
     * 11, dan kegagalannya terjadi di dalam listener sehingga aplikasinya tidak
     * pernah sempat dibuka. Perintah adb tidak bergantung pada versi Katalon.
     */
    @Keyword
    static void resetAndLaunch() {
        // Aplikasi harus berhenti dulu, kalau tidak proses penghapusan ditolak.
        closeApp()

        String pkg = AppConfig.appPackage()
        String adb = adbExecutable()
        Process p = new ProcessBuilder([adb, 'shell', 'pm', 'clear', pkg])
                .redirectErrorStream(true).start()
        String keluaran = p.inputStream.text.trim()
        int kode = p.waitFor()
        KeywordUtil.logInfo('pm clear ' + pkg + ' -> ' + keluaran)

        if (kode != 0 || !keluaran.toLowerCase().contains('success')) {
            throw new IllegalStateException('Gagal mengosongkan data aplikasi: ' + keluaran
                    + '. Pastikan emulatornya hidup dan terbaca oleh adb devices.')
        }

        Mobile.startExistingApplication(pkg)
        dismissSystemDialogIfPresent()
        Mobile.delay(2)
    }

    /**
     * Mencari berkas adb, tanpa bergantung pada variabel global.
     *
     * Diperiksa berurutan: ANDROID_HOME, ANDROID_SDK_ROOT, ANDROID_SDK, lalu
     * lokasi bawaan Android SDK di Windows, terakhir mengandalkan PATH. Yang
     * pertama menjawab dipakai.
     *
     * Sengaja tidak memakai variabel global. Katalon menyusun daftar variabel
     * global saat proyek pertama kali dibuka, sehingga variabel yang ditambahkan
     * belakangan tidak ikut terbaca dan gagal dengan MissingPropertyException.
     * Nilai yang bisa dicari sendiri oleh kode tidak perlu didaftarkan.
     */
    @Keyword
    static String adbExecutable() {
        boolean windows = System.getProperty('os.name').toLowerCase().contains('win')
        String nama = windows ? 'adb.exe' : 'adb'

        List<String> kandidat = []
        for (String env : ['ANDROID_HOME', 'ANDROID_SDK_ROOT', 'ANDROID_SDK']) {
            String nilai = System.getenv(env)
            if (nilai) {
                kandidat.add(new File(nilai, 'platform-tools' + File.separator + nama).absolutePath)
            }
        }
        if (windows) {
            kandidat.add(new File(System.getProperty('user.home'),
                    'AppData/Local/Android/Sdk/platform-tools/' + nama).absolutePath)
        }
        kandidat.add(nama)

        for (String kandidatAdb : kandidat) {
            try {
                Process uji = new ProcessBuilder([kandidatAdb, 'version'])
                        .redirectErrorStream(true).start()
                uji.inputStream.text
                if (uji.waitFor() == 0) {
                    KeywordUtil.logInfo('adb dipakai: ' + kandidatAdb)
                    return kandidatAdb
                }
            } catch (Exception ignored) {
            }
        }
        throw new IllegalStateException('adb tidak ditemukan. Sudah diperiksa: '
                + kandidat.join(' | ')
                + '. Pasang Android SDK, atau setel variabel lingkungan ANDROID_HOME '
                + 'ke folder SDK-nya.')
    }

    @Keyword
    static void closeApp() {
        // Sebuah kasus bisa gagal sebelum aplikasi sempat terbuka, jadi mungkin
        // belum ada driver. Diperiksa dulu supaya proses penutupan tidak
        // mengotori laporan dengan galat yang menutupi kegagalan sebenarnya.
        try {
            if (MobileDriverFactory.getDriver() == null) {
                return
            }
            Mobile.closeApplication()
        } catch (Throwable ignored) {
        }
    }

    /**
     * Menekan hanya kalau elemennya muncul dalam batas waktu, bukan menggagalkan
     * eksekusi. Mengembalikan true kalau penekanan terjadi.
     */
    @Keyword
    static boolean tapIfVisible(TestObject to, int timeout) {
        if (Mobile.waitForElementPresent(to, timeout, FailureHandling.OPTIONAL)) {
            Mobile.tap(to, timeout, FailureHandling.STOP_ON_FAILURE)
            return true
        }
        return false
    }

    /**
     * Android kadang menampilkan dialog izin saat pertama kali dibuka. Test case
     * tidak boleh bergantung pada dialog itu tidak ada, jadi ditutup kalau muncul.
     *
     * Pencariannya dijalankan dengan implicit wait dimatikan. Pencarian yang
     * memang mengharapkan "tidak ketemu" akan memakan seluruh batas tunggu, dan
     * Katalon memasang 60 detik sebelum tiap keyword, sehingga tiga dialog yang
     * tidak ada berbiaya tiga menit per pembukaan aplikasi.
     */
    @Keyword
    static void dismissSystemDialogIfPresent() {
        def driver = MobileDriverFactory.getDriver()
        if (driver == null) {
            return
        }
        def timeouts = driver.manage().timeouts()
        Duration previous = timeouts.getImplicitWaitTimeout()
        timeouts.implicitlyWait(Duration.ZERO)
        try {
            List<String> xpaths = [
                "//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button']",
                "//*[@resource-id='com.android.permissioncontroller:id/permission_deny_button']",
                "//*[@text='Allow']"
            ]
            for (String xp : xpaths) {
                try {
                    def found = driver.findElements(By.xpath(xp))
                    if (found != null && !found.isEmpty()) {
                        found.get(0).click()
                        Mobile.delay(1)
                        return
                    }
                } catch (Exception ignored) {
                }
            }
        } finally {
            // Dikembalikan seperti semula, supaya helper ini tidak meninggalkan jejak.
            timeouts.implicitlyWait(previous)
        }
    }

    /**
     * Menggulir layar sampai elemen tujuan benar-benar ada, lalu berhenti.
     *
     * Mobile.scrollToText tidak bisa diandalkan di Compose. Daftar Compose hanya
     * menyusun elemen yang sedang terlihat, jadi teks yang berada di bawah belum
     * ada di pohon elemen sama sekali dan tidak bisa ditemukan dengan pencarian
     * teks.
     *
     * Dua arah dicoba. Arah geser pada perintah Appium bisa berarti arah jari
     * atau arah gulir, tergantung tafsir, dan salah tafsir menghasilkan geseran
     * ke arah sebaliknya tanpa pesan galat sama sekali. Mencoba keduanya membuat
     * helper ini tidak bergantung pada tafsir itu.
     *
     * Mengembalikan true kalau elemennya ketemu.
     */
    @Keyword
    static boolean scrollTo(TestObject to, int maxGeser) {
        return scrollTo(to, maxGeser, 0.9)
    }

    /**
     * Sama seperti di atas, tapi jarak tiap geseran bisa diatur.
     *
     * Jaraknya penting saat sasarannya sebuah kartu di dalam daftar bergrid.
     * Satu geseran tidak boleh lebih panjang dari tinggi satu kartu, kalau tidak
     * kartu di baris tengah bisa terlewat di antara dua pemeriksaan. Bawaannya
     * 0,9 dipertahankan supaya pemanggil lama tidak berubah perilakunya.
     */
    @Keyword
    static boolean scrollTo(TestObject to, int maxGeser, double langkah) {
        // Papan ketik ditutup lebih dulu. Kalau tidak, geseran yang jatuh di
        // area papan ketik tidak menggulir apa pun, dan form yang termampatkan
        // membuat elemen yang sebenarnya ada jadi tidak terlihat.
        sembunyikanPapanKetik()
        if (terlihat(to, 2)) {
            return true
        }
        for (String arah : ['up', 'down']) {
            if (geserSampaiKetemu(to, maxGeser, arah, langkah)) {
                KeywordUtil.logInfo('Elemen terlihat setelah geser ke arah ' + arah)
                return true
            }
        }
        return false
    }

    /**
     * Menutup papan ketik layar kalau sedang tampil.
     *
     * Setelah sebuah kolom diisi, papan ketik menutupi bagian bawah layar.
     * Geseran yang jatuh di area papan ketik tidak menggulir apa pun, sehingga
     * tombol di bawah form tidak pernah tercapai, dan form yang termampatkan
     * membuat elemen yang sebenarnya ada jadi tidak terlihat.
     *
     * Aman dipanggil walau papan ketiknya tidak sedang tampil. Tidak ada jeda
     * sesudahnya: pemeriksaan keterlihatan berikutnya yang menunggu sampai tata
     * letaknya kembali penuh.
     */
    private static void sembunyikanPapanKetik() {
        try {
            // Tanpa jeda. Pemeriksaan keterlihatan sesudahnya sudah menunggu
            // sampai dua detik, jadi tata letak yang kembali penuh terserap di
            // situ. Jeda tetap di sini dulu membuat setiap pengguliran membayar
            // satu detik walau papan ketiknya tidak sedang tampil.
            Mobile.hideKeyboard(FailureHandling.OPTIONAL)
        } catch (Throwable ignored) {
        }
    }

    /**
     * Apakah elemennya benar-benar tampil di layar, bukan sekadar ada di pohon
     * elemen.
     *
     * Pembedaan ini yang menentukan. Pada wadah malas seperti LazyColumn, elemen
     * di bawah belum disusun, jadi belum ada di pohon: pemeriksaan keberadaan
     * saja sudah cukup untuk memutuskan perlu digeser.
     *
     * Pada wadah yang menggulir biasa seperti verticalScroll, seluruh isinya
     * disusun sekaligus, sehingga elemen di bawah sudah ada di pohon walau tidak
     * terlihat. Pemeriksaan keberadaan langsung menjawab "ada", pengguliran tidak
     * pernah terjadi, lalu penekanan atau pengisian kolom gagal karena elemennya
     * berada di luar layar. Karena itu yang diperiksa harus keterlihatan.
     */
    private static boolean terlihat(TestObject to, int timeout) {
        try {
            return Mobile.verifyElementVisible(to, timeout, FailureHandling.OPTIONAL)
        } catch (Throwable ignored) {
            return false
        }
    }

    private static boolean geserSampaiKetemu(TestObject to, int maxGeser, String arah, double langkah) {
        int w = Mobile.getDeviceWidth()
        int h = Mobile.getDeviceHeight()
        for (int i = 0; i < maxGeser; i++) {
            geserSekali(w, h, arah, langkah)
            Mobile.delay(1)
            if (terlihat(to, 2)) {
                return true
            }
        }
        return false
    }

    /**
     * Satu kali geseran.
     *
     * Jalur utama memakai perintah Appium yang hanya menerima kotak batas dan
     * arah, tanpa koordinat akhir, sehingga tidak ada yang bisa ditafsirkan
     * ganda. Dokumentasi Katalon untuk Mobile.swipe menyebut koordinat akhir
     * sebagai posisi absolut di contoh utamanya, lalu menyebutnya relatif
     * terhadap titik awal di catatan berikutnya, jadi nilainya tidak pasti dan
     * hanya dipakai sebagai cadangan.
     */
    private static void geserSekali(int w, int h, String arah, double langkah) {
        try {
            Object[] params = new Object[1]
            params[0] = [
                    left     : (int) (w * 0.1),
                    top      : (int) (h * 0.2),
                    width    : (int) (w * 0.8),
                    height   : (int) (h * 0.6),
                    direction: arah,
                    // Jarak geser = langkah x tinggi kotak batas di atas, yaitu
                    // langkah x 1.404 px di perangkat ini. Pemanggil yang
                    // menghadapi daftar bergrid harus mengirim angka di bawah 0,5
                    // supaya satu geseran tidak melompati lebih dari satu kartu.
                    percent  : langkah,
                    // Kecepatan wajib diisi. Bawaannya 5000 x kerapatan layar, yaitu
                    // sekitar 13.750 px per detik di perangkat ini, sehingga geseran
                    // berubah menjadi lemparan yang meluncur jauh melewati sasaran.
                    speed    : 1000
            ]
            MobileDriverFactory.getDriver().executeScript('mobile: swipeGesture', params)
            return
        } catch (Throwable t) {
            KeywordUtil.logInfo('Perintah swipeGesture tidak bisa dipakai ('
                    + t.getMessage() + '), memakai Mobile.swipe.')
        }
        int x = (int) (w / 2)
        if ('up'.equals(arah)) {
            Mobile.swipe(x, (int) (h * 0.75), x, (int) (h * 0.25))
        } else {
            Mobile.swipe(x, (int) (h * 0.25), x, (int) (h * 0.75))
        }
    }

    /**
     * Sama seperti scrollTo, tapi gagal dengan pesan yang jelas kalau elemennya
     * tidak ketemu. Dipakai sebelum menekan sesuatu: menekan elemen yang tidak
     * ada menghasilkan pesan locator yang menyesatkan, sedangkan pesan di sini
     * langsung menyebut apa yang dicari.
     */
    @Keyword
    static void scrollToOrFail(TestObject to, int maxGeser, String keterangan) {
        scrollToOrFail(to, maxGeser, keterangan, 0.9)
    }

    @Keyword
    static void scrollToOrFail(TestObject to, int maxGeser, String keterangan, double langkah) {
        if (scrollTo(to, maxGeser, langkah)) {
            return
        }
        boolean adaDiPohon = Mobile.waitForElementPresent(to, 1, FailureHandling.OPTIONAL)
        throw new IllegalStateException('Tidak menemukan ' + keterangan
                + ' setelah ' + maxGeser + ' kali geser layar ke dua arah. '
                + (adaDiPohon
                        ? 'Elemennya ada di pohon elemen tapi tidak pernah terlihat, '
                          + 'jadi penggulirannya yang tidak mengena.'
                        : 'Elemennya tidak ada di pohon elemen sama sekali, jadi '
                          + 'penandanya yang tidak cocok.'))
    }

    /**
     * Menuliskan hierarki UI yang sedang tampil ke folder laporan. Ini alat yang
     * dipakai untuk memeriksa dan memperbaiki locator setelah aplikasi berubah.
     */
    @Keyword
    static void dumpPageSource(String tag) {
        def driver = MobileDriverFactory.getDriver()
        String xml = driver.getPageSource()
        File f = new File(AppConfig.artifactDir(), 'ui-dump-' + tag + '.xml')
        f.setText(xml, 'UTF-8')
        KeywordUtil.logInfo('Hierarki UI ditulis ke ' + f.getAbsolutePath())
    }

    @Keyword
    static void takeScreenshot(String tag) {
        File f = new File(AppConfig.artifactDir(), 'shot-' + tag + '.png')
        Mobile.takeScreenshot(f.getAbsolutePath(), FailureHandling.OPTIONAL)
    }

    @Keyword
    static void log(String message) {
        KeywordUtil.logInfo(message)
    }
}
