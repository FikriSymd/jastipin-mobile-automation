import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC18_Produk_Foto_Sebelum_Disimpan: Barang baru harus disimpan dulu sebelum bisa diberi foto.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.addProduct()
ProductEditorScreen.verifyVisible()

ProductEditorScreen.verifyImageHint()
