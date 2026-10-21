import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen

Mobile.comment('TC24_Nomor_WhatsApp_Tampil_Di_Katalog: Nomor WhatsApp dari pengaturan tampil utuh di katalog.')

CatalogScreen.verifyVisible()

String tampil = CatalogScreen.whatsappNumber()
// Nomor bisa ditulis dengan spasi atau tanda hubung, jadi hanya angkanya yang dibandingkan.
String angkaSaja = tampil.replaceAll('[^0-9]', '')
Mobile.verifyEqual(angkaSaja, GlobalVariable.G_WhatsappNumber)
