import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.testdata.TestDataFactory

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen
import jastipin.ProductEditorScreen

Mobile.comment('TC26_Tambah_Produk_Data_Driven: Tiga barang ditambahkan dari satu berkas data dan jumlahnya naik bertahap.')

def data = TestDataFactory.findTestData('Data Files/ProductCases')
Mobile.comment('TC26: ' + data.getRowNumbers() + ' barang ditambahkan dari berkas data')

CatalogScreen.openAdmin()
AdminLoginScreen.loginAsAdmin()
AdminHomeScreen.verifyVisible()
AdminHomeScreen.verifyProductCount(GlobalVariable.G_MinProducts)

for (int row = 1; row <= data.getRowNumbers(); row++) {
    String nama = data.getValue('nama', row)
    String harga = data.getValue('harga', row)

    Mobile.comment('Baris ' + row + ' -> ' + nama + ' seharga ' + harga)

    AdminHomeScreen.addProduct()
    ProductEditorScreen.verifyVisible()
    ProductEditorScreen.typeName(nama)
    ProductEditorScreen.typePrice(harga)
    ProductEditorScreen.save()

    AdminHomeScreen.verifyVisible()
    AdminHomeScreen.verifyProductCount(GlobalVariable.G_MinProducts + row)
}
