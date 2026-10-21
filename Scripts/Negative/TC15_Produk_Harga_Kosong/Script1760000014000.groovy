import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC15_Produk_Harga_Kosong: Barang tanpa harga tidak bisa disimpan.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.addProduct()
ProductEditorScreen.verifyVisible()

ProductEditorScreen.typeName('Barang Tanpa Harga')
ProductEditorScreen.save()

ProductEditorScreen.verifyError(GlobalVariable.G_ExpectedEmptyPrice)
