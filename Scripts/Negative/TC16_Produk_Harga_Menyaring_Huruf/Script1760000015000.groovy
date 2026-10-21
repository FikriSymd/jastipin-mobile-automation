import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC16_Produk_Harga_Menyaring_Huruf: Kolom harga membuang karakter selain angka.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.addProduct()
ProductEditorScreen.verifyVisible()

ProductEditorScreen.typePrice('abc123')
Mobile.verifyElementText(findTestObject('Jastipin/Editor/etProductPrice'), '123')

ProductEditorScreen.typePrice('12x34y5')
Mobile.verifyElementText(findTestObject('Jastipin/Editor/etProductPrice'), '12345')
