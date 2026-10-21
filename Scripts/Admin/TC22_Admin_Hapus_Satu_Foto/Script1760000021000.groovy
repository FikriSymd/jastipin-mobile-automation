import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC22_Admin_Hapus_Satu_Foto: Menghapus satu foto memberi konfirmasi di editor.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()

AdminHomeScreen.editProduct(GlobalVariable.G_ProductIdFirst)
ProductEditorScreen.verifyVisible()

ProductEditorScreen.deleteFirstImage()

ProductEditorScreen.verifyNotice('Foto dihapus.')
