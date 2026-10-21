import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC17_Produk_Melebihi_Batas_Foto: Barang yang fotonya sudah delapan tidak bisa ditambah lagi.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()

AdminHomeScreen.editProduct(GlobalVariable.G_ProductIdMaxImages)
ProductEditorScreen.verifyVisible()

ProductEditorScreen.addSampleImage()

ProductEditorScreen.verifyError(GlobalVariable.G_ExpectedMaxImages)
