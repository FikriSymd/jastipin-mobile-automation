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

    /**
     * Menggulir sampai kartu barangnya terlihat.
     *
     * Yang dicari kartunya, bukan tulisan namanya. Kartu tingginya 693 px,
     * sedangkan tulisan nama ada di bagian bawah kartu dan hanya setinggi
     * sekitar 35 px. Saat kartu baru masuk layar, bagian atasnya yang terlihat
     * lebih dulu, sehingga tulisan namanya masih di luar layar walau kartunya
     * sudah tampak. Menggulir berdasarkan kartu membuat patokannya stabil.
     *
     * Jarak geser 0,25 dipilih karena hasilnya 351 px, lebih pendek dari tinggi
     * satu kartu. Dengan langkah sependek itu tidak ada kartu yang bisa
     * terlewat. Batas geser dinaikkan karena tiap geseran jadi lebih pendek.
     */
    @Keyword
    static void scrollToProduct(String productId) {
        MobileActions.scrollToOrFail(Jastipin.productCard(productId),
                20, 'kartu barang ' + productId, 0.25)
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
