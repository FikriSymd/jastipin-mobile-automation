import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen

Mobile.comment('TC12_Login_Password_Salah: Password salah ditolak walau emailnya benar.')

CatalogScreen.openAdmin()
AdminLoginScreen.verifyVisible()

AdminLoginScreen.submit(GlobalVariable.G_AdminEmail, GlobalVariable.G_WrongPassword)

AdminLoginScreen.verifyError(GlobalVariable.G_ExpectedWrongCredential)
