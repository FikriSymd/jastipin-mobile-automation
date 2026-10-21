import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen

Mobile.comment('TC11_Login_Password_Pendek: Password di bawah batas minimal ditolak sebelum diperiksa ke akun.')

CatalogScreen.openAdmin()
AdminLoginScreen.verifyVisible()

AdminLoginScreen.submit(GlobalVariable.G_AdminEmail, GlobalVariable.G_ShortPassword)

AdminLoginScreen.verifyError(GlobalVariable.G_ExpectedShortPassword)
