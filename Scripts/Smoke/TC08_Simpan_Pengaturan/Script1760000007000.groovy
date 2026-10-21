import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import jastipin.MobileActions
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.SettingsScreen

Mobile.comment('TC08_Simpan_Pengaturan: Pengaturan toko tersimpan dan aplikasi memberi konfirmasi.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()

AdminHomeScreen.openSettings()
SettingsScreen.verifyVisible()

SettingsScreen.typeField('heroLead', 'Belanja titipan dari luar negeri, dikurasi manual.')
SettingsScreen.save()
SettingsScreen.verifySaved()

MobileActions.takeScreenshot('TC08_pengaturan')
