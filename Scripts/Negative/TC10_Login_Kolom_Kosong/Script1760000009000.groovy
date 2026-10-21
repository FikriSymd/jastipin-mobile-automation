import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen

Mobile.comment('TC10_Login_Kolom_Kosong: Menekan Masuk tanpa mengisi apa pun ditolak.')

CatalogScreen.openAdmin()
AdminLoginScreen.verifyVisible()

AdminLoginScreen.tapLogin()

AdminLoginScreen.verifyError(GlobalVariable.G_ExpectedEmptyLogin)
AdminLoginScreen.verifyVisible()
