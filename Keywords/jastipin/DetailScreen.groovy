package jastipin

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable

/**
 * Layar detail barang.
 */
class DetailScreen {

    @Keyword
    static void verifyVisible() {
        Mobile.verifyElementExist(findTestObject('Jastipin/Detail/scrDetail'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void verifyName(String expected) {
        Mobile.verifyElementText(findTestObject('Jastipin/Detail/tvDetailName'), expected)
    }

    @Keyword
    static void nextImage() {
        Mobile.tap(findTestObject('Jastipin/Detail/btnNext'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void previousImage() {
        Mobile.tap(findTestObject('Jastipin/Detail/btnPrev'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void backToCatalog() {
        Mobile.tap(findTestObject('Jastipin/Detail/btnBack'), GlobalVariable.G_Timeout)
    }
}
