package jastipin

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling

import internal.GlobalVariable

/**
 * Layar editor barang.
 */
class ProductEditorScreen {

    @Keyword
    static void verifyVisible() {
        Mobile.verifyElementExist(findTestObject('Jastipin/Editor/scrProductEditor'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void typeName(String value) {
        Mobile.setText(findTestObject('Jastipin/Editor/etProductName'), value, GlobalVariable.G_Timeout)
    }

    @Keyword
    static void typePrice(String value) {
        Mobile.setText(findTestObject('Jastipin/Editor/etProductPrice'), value, GlobalVariable.G_Timeout)
    }

    @Keyword
    static void typeDescription(String value) {
        Mobile.setText(findTestObject('Jastipin/Editor/etProductDescription'), value, GlobalVariable.G_Timeout)
    }

    @Keyword
    static String priceValue() {
        return Mobile.getText(findTestObject('Jastipin/Editor/etProductPrice'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void save() {
        MobileActions.scrollToOrFail(findTestObject('Jastipin/Editor/btnSaveProduct'),
                12, 'tombol Simpan produk')
        Mobile.tap(findTestObject('Jastipin/Editor/btnSaveProduct'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void addSampleImage() {
        MobileActions.scrollToOrFail(findTestObject('Jastipin/Editor/btnPickImage'),
                12, 'tombol Tambah foto contoh')
        Mobile.tap(findTestObject('Jastipin/Editor/btnPickImage'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void deleteFirstImage() {
        MobileActions.scrollToOrFail(Jastipin.deleteImageButton(0),
                12, 'tombol Hapus foto pertama')
        Mobile.tap(Jastipin.deleteImageButton(0), GlobalVariable.G_Timeout)
    }

    /**
     * Pemilih status membuka jendela terpisah, sehingga penandanya tidak ikut
     * terbaca sebagai resource-id. Isinya dipilih lewat teks.
     */
    @Keyword
    static void chooseStatus(String label) {
        Mobile.tap(findTestObject('Jastipin/Editor/spProductStatus'), GlobalVariable.G_Timeout)
        Mobile.tap(Locators.byText(label), GlobalVariable.G_Timeout)
    }

    @Keyword
    static String errorText() {
        return Mobile.getText(findTestObject('Jastipin/Editor/tvProductFormError'), GlobalVariable.G_ShortTimeOut, FailureHandling.OPTIONAL)
    }

    @Keyword
    static void verifyError(String expected) {
        Mobile.verifyElementText(findTestObject('Jastipin/Editor/tvProductFormError'), expected)
    }

    @Keyword
    static void verifyNotice(String expected) {
        Mobile.verifyElementText(findTestObject('Jastipin/Editor/tvProductNotice'), expected)
    }

    @Keyword
    static void verifyImageHint() {
        Mobile.verifyElementText(findTestObject('Jastipin/Editor/tvImageHint'),
                'Simpan produk dulu sebelum menambah foto.')
    }
}
