import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen

Mobile.comment('TC09_Admin_Keluar_Kembali_Katalog: Keluar dari sesi admin mengembalikan ke katalog.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()

AdminHomeScreen.logout()

CatalogScreen.verifyVisible()
