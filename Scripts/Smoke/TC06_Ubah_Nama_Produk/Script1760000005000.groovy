import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC06_Ubah_Nama_Produk: Perubahan nama barang tersimpan dan tampil di daftar.')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()

AdminHomeScreen.editProduct(GlobalVariable.G_ProductIdFirst)
ProductEditorScreen.verifyVisible()

String namaBaru = 'Tumbler Owala Freesip (Sudah Diubah)'
ProductEditorScreen.typeName(namaBaru)
ProductEditorScreen.save()

AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyRowName(GlobalVariable.G_ProductIdFirst, namaBaru)
