package jastipin

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling

import internal.GlobalVariable

/**
 * Layar daftar barang versi admin.
 */
class AdminHomeScreen {

    @Keyword
    static void verifyVisible() {
        Mobile.verifyElementExist(findTestObject('Jastipin/AdminHome/scrAdminHome'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void verifyLoggedInAs(String email) {
        Mobile.verifyElementText(findTestObject('Jastipin/AdminHome/tvWhoami'), 'Masuk sebagai ' + email)
    }

    @Keyword
    static String productCount() {
        return Mobile.getText(findTestObject('Jastipin/AdminHome/tvProductCount'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void verifyProductCount(int expected) {
        Mobile.verifyElementText(findTestObject('Jastipin/AdminHome/tvProductCount'), expected + ' barang')
    }

    @Keyword
    static void addProduct() {
        Mobile.tap(findTestObject('Jastipin/AdminHome/btnAddProduct'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void refresh() {
        Mobile.tap(findTestObject('Jastipin/AdminHome/btnRefresh'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void openSettings() {
        Mobile.tap(findTestObject('Jastipin/AdminHome/btnSettings'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void logout() {
        Mobile.tap(findTestObject('Jastipin/AdminHome/btnLogout'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void editProduct(String productId) {
        MobileActions.scrollToOrFail(Jastipin.editButton(productId),
                12, 'tombol Ubah barang ' + productId)
        Mobile.tap(Jastipin.editButton(productId), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void deleteProduct(String productId) {
        MobileActions.scrollToOrFail(Jastipin.deleteButton(productId),
                12, 'tombol Hapus barang ' + productId)
        Mobile.tap(Jastipin.deleteButton(productId), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void verifyRowName(String productId, String expected) {
        Mobile.verifyElementText(Jastipin.rowName(productId), expected)
    }

    @Keyword
    static void verifyRowStatus(String productId, String expected) {
        Mobile.verifyElementText(Jastipin.rowStatus(productId), expected)
    }

    @Keyword
    static String rowImageCount(String productId) {
        return Mobile.getText(Jastipin.rowImages(productId), GlobalVariable.G_Timeout)
    }
}
