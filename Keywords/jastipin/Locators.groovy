package jastipin

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject

/**
 * Membuat TestObject saat eksekusi berjalan.
 *
 * Sebagian besar elemen di aplikasi ini membawa penanda yang menyertakan
 * identitas datanya, misalnya cardProduct_<id> atau btnDeleteProduct_<id>.
 * Menyimpan satu objek statis untuk tiap kemungkinan berarti puluhan berkas
 * yang harus disunting setiap kali data berubah. Membangunnya di kode membuat
 * test case cukup menulis productCard(id).
 *
 * Compose mengekspos testTag sebagai resource-id apa adanya, jadi nilainya
 * dicocokkan sama persis. Prefix paket tidak ikut disertakan.
 */
class Locators {

    @Keyword
    static TestObject resourceId(String value) {
        TestObject to = new TestObject(value)
        to.addProperty('resource-id', ConditionType.EQUALS, value)
        return to
    }

    /** Mencari elemen lewat teksnya. Dipakai untuk isi menu yang muncul di jendela terpisah. */
    @Keyword
    static TestObject byText(String value) {
        TestObject to = new TestObject('text:' + value)
        to.addProperty('text', ConditionType.EQUALS, value)
        return to
    }
}
