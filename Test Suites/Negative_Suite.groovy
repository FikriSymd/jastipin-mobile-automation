import com.kms.katalon.core.annotation.SetUp
import com.kms.katalon.core.annotation.TearDown
import com.kms.katalon.core.util.KeywordUtil

import jastipin.MobileActions

/**
 * Setiap test case membuka sendiri aplikasinya, jadi persiapan suite hanya
 * menulis satu baris log. Penutupan aplikasi dilakukan defensif, untuk keadaan
 * ketika sebuah kasus gagal di tengah jalan.
 */
@SetUp(skipped = false)
def setUp() {
    KeywordUtil.logInfo('Persiapan suite: tiap test case membuka aplikasinya sendiri.')
}

@TearDown(skipped = false)
def tearDown() {
    MobileActions.closeApp()
}
