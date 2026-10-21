import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen

Mobile.comment('TC04_Admin_Login_Berhasil: Kredensial admin yang benar membuka layar kelola barang.')

CatalogScreen.openAdmin()
AdminLoginScreen.verifyVisible()

AdminLoginScreen.loginAsAdmin()

AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyLoggedInAs(GlobalVariable.G_AdminEmail)
