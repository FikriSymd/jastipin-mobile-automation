package jastipin

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.testobject.TestObject

/**
 * Penanda yang menyertakan identitas data, dibangun dari Locators.
 * Test case berbicara dalam istilah barang, bukan dalam rangkaian penanda.
 */
class Jastipin {

    @Keyword
    static TestObject productCard(String productId) {
        return Locators.resourceId('cardProduct_' + productId)
    }

    @Keyword
    static TestObject productName(String productId) {
        return Locators.resourceId('tvProductName_' + productId)
    }

    @Keyword
    static TestObject productPrice(String productId) {
        return Locators.resourceId('tvProductPrice_' + productId)
    }

    @Keyword
    static TestObject rowProduct(String productId) {
        return Locators.resourceId('rowProduct_' + productId)
    }

    @Keyword
    static TestObject rowName(String productId) {
        return Locators.resourceId('tvRowName_' + productId)
    }

    @Keyword
    static TestObject rowStatus(String productId) {
        return Locators.resourceId('tvRowStatus_' + productId)
    }

    @Keyword
    static TestObject rowImages(String productId) {
        return Locators.resourceId('tvRowImages_' + productId)
    }

    @Keyword
    static TestObject editButton(String productId) {
        return Locators.resourceId('btnEditProduct_' + productId)
    }

    @Keyword
    static TestObject deleteButton(String productId) {
        return Locators.resourceId('btnDeleteProduct_' + productId)
    }

    @Keyword
    static TestObject existingImage(int order) {
        return Locators.resourceId('imgExisting_' + order)
    }

    @Keyword
    static TestObject deleteImageButton(int order) {
        return Locators.resourceId('btnDeleteImage_' + order)
    }

    @Keyword
    static TestObject thumbnail(int index) {
        return Locators.resourceId('thumb_' + index)
    }

    @Keyword
    static TestObject settingsField(String key) {
        return Locators.resourceId('et_' + key)
    }
}
