import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.Jastipin

Mobile.comment('TC07_Hapus_Produk: Menghapus barang mengurangi jumlahnya dan barisnya hilang.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyProductCount(GlobalVariable.G_MinProducts)

AdminHomeScreen.deleteProduct(GlobalVariable.G_ProductIdFirst)
Mobile.delay(1)

AdminHomeScreen.verifyProductCount(GlobalVariable.G_MinProducts - 1)
Mobile.verifyElementNotExist(Jastipin.rowProduct(GlobalVariable.G_ProductIdFirst), GlobalVariable.G_ShortTimeOut)
