import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.DetailScreen

Mobile.comment('TC02_Buka_Detail_Produk: Menekan kartu barang membuka detail dengan nama yang sesuai.')

CatalogScreen.verifyVisible()
CatalogScreen.openProduct(GlobalVariable.G_ProductIdFirst)

DetailScreen.verifyVisible()
DetailScreen.verifyName(GlobalVariable.G_ProductNameFirst)

DetailScreen.backToCatalog()
CatalogScreen.verifyVisible()
