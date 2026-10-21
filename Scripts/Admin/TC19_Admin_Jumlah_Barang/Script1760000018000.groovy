import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen

Mobile.comment('TC19_Admin_Jumlah_Barang: Daftar admin melaporkan jumlah barang yang benar.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()

AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyProductCount(GlobalVariable.G_MinProducts)
