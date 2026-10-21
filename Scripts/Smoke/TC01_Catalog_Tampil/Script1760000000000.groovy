import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling

import internal.GlobalVariable
import jastipin.MobileActions
import jastipin.CatalogScreen

Mobile.comment('TC01_Catalog_Tampil: Katalog terbaca penuh dari atas sampai bawah.')

CatalogScreen.verifyVisible()

String heroLead = Mobile.getText(findTestObject('Jastipin/Catalog/tvHeroLead'), GlobalVariable.G_Timeout)
Mobile.verifyNotEqual(heroLead, '', FailureHandling.STOP_ON_FAILURE)

// Grid Compose hanya menyusun kartu yang terlihat, jadi tiap barang digulir satu
// per satu. Daftar ini mengikuti urutan sort_order data bawaan aplikasi, supaya
// seluruh delapan barang terbukti terpasang, bukan hanya yang teratas.
List<String> semuaBarang = [
        '945c758c-8859-4407-819d-12dd9fc464a6',
        '682cec96-ec80-4f48-9e12-a9f61ea0e12f',
        '0b899399-15ae-4a31-9ce2-ee6ede624af3',
        'a44019f2-2869-48ef-be45-0d3893dd236f',
        '3e682be1-a482-49e6-af95-47e8cbb09c9c',
        'b28bc7c1-3d25-4b30-93fb-7d3ae4c0eb06',
        'bc0ea786-496c-4c10-b727-15e207fead16',
        '94f7f2fe-1296-4f63-85b5-feb0b8072cee',
]

for (String id : semuaBarang) {
    // Barang yang sedang dicari ditulis ke log. Kalau eksekusi gagal, log
    // langsung menunjukkan barang terakhir yang belum ketemu, tanpa perlu
    // menebak dari urutan.
    Mobile.comment('Mencari kartu barang: ' + id)
    CatalogScreen.scrollToProduct(id)

    // Nama diperiksa hanya untuk barang yang teksnya sudah ada sebagai variabel
    // global, supaya berkas ini tidak menyalin data aplikasi.
    if (id == GlobalVariable.G_ProductIdFirst) {
        CatalogScreen.verifyProductName(id, GlobalVariable.G_ProductNameFirst)
    } else if (id == GlobalVariable.G_ProductIdFifth) {
        CatalogScreen.verifyProductName(id, GlobalVariable.G_ProductNameFifth)
    }
}

// Blok di bawah deretan barang ikut diperiksa, supaya pengguliran terbukti sampai
// dasar halaman dan bukan berhenti di barang terakhir.
MobileActions.scrollToOrFail(findTestObject('Jastipin/Catalog/tvPriceNote'), 12, 'blok cara pesan')
MobileActions.scrollToOrFail(findTestObject('Jastipin/Catalog/tvWhatsappNumber'), 12, 'blok kontak')
MobileActions.scrollToOrFail(findTestObject('Jastipin/Catalog/tvFooterNote'), 12, 'catatan kaki katalog')
MobileActions.scrollToOrFail(findTestObject('Jastipin/Catalog/btnOpenAdmin'), 12, 'tombol Masuk sebagai admin')

MobileActions.takeScreenshot('TC01_katalog')
