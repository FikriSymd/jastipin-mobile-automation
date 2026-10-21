import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen

Mobile.comment('TC20_Admin_Muat_Ulang: Muat ulang tidak mengubah data yang sudah ada.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()

String sebelum = AdminHomeScreen.productCount()

AdminHomeScreen.refresh()
Mobile.delay(1)

String sesudah = AdminHomeScreen.productCount()
Mobile.verifyEqual(sesudah, sebelum)
