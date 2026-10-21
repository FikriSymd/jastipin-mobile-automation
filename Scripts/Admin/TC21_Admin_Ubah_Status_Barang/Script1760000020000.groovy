import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC21_Admin_Ubah_Status_Barang: Mengubah status barang menjadi disembunyikan.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()

AdminHomeScreen.editProduct(GlobalVariable.G_ProductIdFirst)
ProductEditorScreen.verifyVisible()

ProductEditorScreen.chooseStatus('Sembunyikan')
ProductEditorScreen.save()

AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyRowStatus(GlobalVariable.G_ProductIdFirst, 'Disembunyikan')
