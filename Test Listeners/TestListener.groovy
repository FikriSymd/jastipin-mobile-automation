import com.kms.katalon.core.annotation.AfterTestCase
import com.kms.katalon.core.annotation.BeforeTestCase
import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.context.TestCaseContext
import com.kms.katalon.core.context.TestSuiteContext
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

import internal.GlobalVariable
import jastipin.MobileActions

/**
 * Kepentingan lintas test case.
 *
 * Setiap kasus dimulai dari keadaan yang sama: data aplikasi dikosongkan lalu
 * aplikasi dibuka ulang. Tanpa ini, kasus yang menambah atau menghapus barang
 * akan mengubah data untuk kasus berikutnya, sehingga satu kali gagal membuat
 * seluruh eksekusi berikutnya gagal juga dan hasilnya tidak bisa diulang.
 *
 * Setelah data dikosongkan, aplikasi mengisi sendiri delapan barang bawaan pada
 * peluncuran berikutnya.
 */
class TestListener {

    @BeforeTestSuite
    def beforeTestSuite(TestSuiteContext testSuiteContext) {
        KeywordUtil.logInfo('Suite dimulai: ' + testSuiteContext.getTestSuiteId())
    }

    @BeforeTestCase
    def beforeTestCase(TestCaseContext testCaseContext) {
        KeywordUtil.logInfo('Test case dimulai: ' + testCaseContext.getTestCaseId())
        if (GlobalVariable.G_ResetDataBeforeEachCase) {
            MobileActions.resetAndLaunch()
        } else {
            MobileActions.launchApp()
        }
    }

    @AfterTestCase
    def afterTestCase(TestCaseContext testCaseContext) {
        String status = testCaseContext.getTestCaseStatus()
        KeywordUtil.logInfo('Test case selesai: ' + testCaseContext.getTestCaseId() + ' status=' + status)

        if ('FAILED'.equals(status) && GlobalVariable.G_ScreenshotOnFailure) {
            try {
                String safeName = testCaseContext.getTestCaseId().replace('/', '_').replace(' ', '_')
                File f = new File(RunConfiguration.getReportFolder(),
                        'failure_' + safeName + '_' + System.currentTimeMillis() + '.png')
                f.getParentFile().mkdirs()
                Mobile.takeScreenshot(f.getAbsolutePath(), FailureHandling.OPTIONAL)
                KeywordUtil.logInfo('Tangkapan layar kegagalan disimpan di ' + f.getAbsolutePath())

                // Pohon elemen ikut disimpan. Saat sebuah locator tidak ketemu,
                // inilah satu-satunya cara melihat penanda apa yang sebenarnya
                // terbaca, tanpa harus menjalankan ulang.
                MobileActions.dumpPageSource('GAGAL_' + safeName)
            } catch (Exception e) {
                KeywordUtil.logInfo('Tidak bisa menyimpan bukti kegagalan: ' + e.getMessage())
            }
        }
    }
}
