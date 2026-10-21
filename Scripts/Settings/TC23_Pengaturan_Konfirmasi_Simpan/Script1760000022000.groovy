import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.SettingsScreen

Mobile.comment('TC23_Pengaturan_Konfirmasi_Simpan: Menyimpan pengaturan memberi pesan konfirmasi.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.openSettings()
SettingsScreen.verifyVisible()

SettingsScreen.typeField('footerNote', 'Dibuat untuk pengujian otomasi.')
SettingsScreen.save()
SettingsScreen.verifySaved()
