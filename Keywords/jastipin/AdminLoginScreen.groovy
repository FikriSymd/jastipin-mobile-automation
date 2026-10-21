package jastipin

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling

import internal.GlobalVariable

/**
 * Layar masuk admin.
 *
 * Kolom yang dibiarkan kosong tidak disentuh sama sekali, sehingga tiap cabang
 * validasi bisa dicapai tanpa harus menghapus isi kolom lebih dulu.
 */
class AdminLoginScreen {

    @Keyword
    static void verifyVisible() {
        Mobile.verifyElementExist(findTestObject('Jastipin/AdminLogin/scrAdminLogin'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void typeEmail(String value) {
        Mobile.setText(findTestObject('Jastipin/AdminLogin/etAdminEmail'), value, GlobalVariable.G_Timeout)
    }

    @Keyword
    static void typePassword(String value) {
        Mobile.setText(findTestObject('Jastipin/AdminLogin/etAdminPassword'), value, GlobalVariable.G_Timeout)
    }

    @Keyword
    static void tapLogin() {
        Mobile.tap(findTestObject('Jastipin/AdminLogin/btnLogin'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void submit(String email, String password) {
        if (!email.isEmpty()) {
            typeEmail(email)
        }
        if (!password.isEmpty()) {
            typePassword(password)
        }
        tapLogin()
    }

    @Keyword
    static void loginAsAdmin() {
        submit(GlobalVariable.G_AdminEmail, GlobalVariable.G_AdminPassword)
    }

    /**
     * Pesan galat selalu ada di pohon elemen walau kosong, jadi keberadaannya
     * tidak membuktikan apa pun. Yang diperiksa isinya.
     */
    @Keyword
    static String errorText() {
        return Mobile.getText(findTestObject('Jastipin/AdminLogin/tvLoginError'), GlobalVariable.G_ShortTimeOut, FailureHandling.OPTIONAL)
    }

    @Keyword
    static void verifyError(String expected) {
        Mobile.verifyElementText(findTestObject('Jastipin/AdminLogin/tvLoginError'), expected, FailureHandling.STOP_ON_FAILURE)
    }
}
