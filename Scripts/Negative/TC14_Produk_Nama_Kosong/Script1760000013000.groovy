import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC14_Produk_Nama_Kosong: Barang tanpa nama tidak bisa disimpan.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.addProduct()
ProductEditorScreen.verifyVisible()

ProductEditorScreen.typePrice('150000')
ProductEditorScreen.save()

ProductEditorScreen.verifyError(GlobalVariable.G_ExpectedEmptyName)
ProductEditorScreen.verifyVisible()
