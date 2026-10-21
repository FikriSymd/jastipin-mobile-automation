package jastipin

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling

import internal.GlobalVariable

/**
 * Layar katalog. Test case memakai aksi pengguna, tidak pernah penanda mentah.
 */
class CatalogScreen {

    @Keyword
    static void verifyVisible() {
        Mobile.verifyElementExist(findTestObject('Jastipin/Catalog/scrCatalog'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void openProduct(String productId) {
        Mobile.tap(Jastipin.productCard(productId), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void openAdmin() {
        // Tombol ini ada di paling bawah halaman, di bawah seluruh katalog,
        // jadi perlu digeser beberapa kali sebelum bisa ditekan.
        MobileActions.scrollToOrFail(findTestObject('Jastipin/Catalog/btnOpenAdmin'),
                12, 'tombol Masuk sebagai admin')
        Mobile.tap(findTestObject('Jastipin/Catalog/btnOpenAdmin'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void scrollToProduct(String productId) {
        MobileActions.scrollToOrFail(Jastipin.productName(productId),
                12, 'kartu barang ' + productId)
    }

    @Keyword
    static void verifyProductName(String productId, String expected) {
        Mobile.verifyElementText(Jastipin.productName(productId), expected)
    }

    @Keyword
    static String whatsappNumber() {
        MobileActions.scrollToOrFail(findTestObject('Jastipin/Catalog/tvWhatsappNumber'),
                12, 'blok kontak')
        return Mobile.getText(findTestObject('Jastipin/Catalog/tvWhatsappNumber'), GlobalVariable.G_Timeout)
    }
}
