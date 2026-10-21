import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen

Mobile.comment('TC13_Login_Email_Tidak_Terdaftar: Email yang tidak terdaftar ditolak.')

CatalogScreen.openAdmin()
AdminLoginScreen.verifyVisible()

AdminLoginScreen.submit(GlobalVariable.G_UnknownEmail, GlobalVariable.G_AdminPassword)

AdminLoginScreen.verifyError(GlobalVariable.G_ExpectedWrongCredential)
