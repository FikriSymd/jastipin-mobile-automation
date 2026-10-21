import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.MobileActions
import jastipin.CatalogScreen
import jastipin.DetailScreen

Mobile.comment('TC03_Galeri_Tombol_Berikutnya: Tombol galeri mengganti foto tanpa meninggalkan layar detail.')

CatalogScreen.openProduct(GlobalVariable.G_ProductIdFirst)
DetailScreen.verifyVisible()
Mobile.verifyElementExist(findTestObject('Jastipin/Detail/imgMain'), GlobalVariable.G_Timeout)

DetailScreen.nextImage()
Mobile.delay(1)
// Mengganti foto tidak boleh mengubah barang yang sedang dibuka.
DetailScreen.verifyName(GlobalVariable.G_ProductNameFirst)

DetailScreen.previousImage()
Mobile.delay(1)
DetailScreen.verifyVisible()

MobileActions.takeScreenshot('TC03_galeri')
