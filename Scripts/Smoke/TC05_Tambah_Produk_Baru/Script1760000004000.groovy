import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.MobileActions
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC05_Tambah_Produk_Baru: Barang baru tersimpan dan jumlah barang bertambah satu.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyProductCount(GlobalVariable.G_MinProducts)

AdminHomeScreen.addProduct()
ProductEditorScreen.verifyVisible()

ProductEditorScreen.typeName('Barang Uji Otomasi')
ProductEditorScreen.typePrice('150000')
ProductEditorScreen.typeDescription('Ditambahkan oleh test case TC05.')
ProductEditorScreen.save()

AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyProductCount(GlobalVariable.G_MinProducts + 1)

MobileActions.takeScreenshot('TC05_setelah_tambah')
