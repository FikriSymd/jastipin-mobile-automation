package jastipin

import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling

import internal.GlobalVariable

/**
 * Layar pengaturan toko.
 */
class SettingsScreen {

    /** Label yang tampil di layar untuk tiap kunci pengaturan. */
    private static final Map<String, String> LABELS = [
        'heroLead'     : 'Kalimat hero',
        'galleryIntro' : 'Deskripsi galeri',
        'whatsapp'     : 'Nomor WhatsApp',
        'whatsappIntro': 'Intro WhatsApp',
        'priceNote'    : 'Catatan harga',
        'shipping'     : 'Ongkos kirim',
        'payment'      : 'Cara bayar',
        'estimatedTime': 'Perkiraan waktu',
        'orderingNote' : 'Catatan pemesanan',
        'footerNote'   : 'Catatan footer',
    ]

    @Keyword
    static void verifyVisible() {
        Mobile.verifyElementExist(findTestObject('Jastipin/Settings/scrSettings'), GlobalVariable.G_Timeout)
    }

    /**
     * Menggulir ke kolom lewat labelnya, bukan lewat kuncinya. Yang tampil di
     * layar adalah label, sedangkan kunci hanya dipakai di dalam kode.
     */
    @Keyword
    static void typeField(String key, String value) {
        String label = LABELS.get(key)
        if (label == null) {
            throw new IllegalArgumentException('Kunci pengaturan tidak dikenal: ' + key)
        }
        MobileActions.scrollToOrFail(Jastipin.settingsField(key),
                12, 'kolom pengaturan ' + label)
        Mobile.setText(Jastipin.settingsField(key), value, GlobalVariable.G_Timeout)
    }

    @Keyword
    static String fieldValue(String key) {
        return Mobile.getText(Jastipin.settingsField(key), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void save() {
        MobileActions.scrollToOrFail(findTestObject('Jastipin/Settings/btnSaveSettings'),
                12, 'tombol Simpan pengaturan')
        Mobile.tap(findTestObject('Jastipin/Settings/btnSaveSettings'), GlobalVariable.G_Timeout)
    }

    @Keyword
    static void verifySaved() {
        Mobile.verifyElementText(findTestObject('Jastipin/Settings/tvSettingsStatus'), 'Pengaturan disimpan.')
    }
}
