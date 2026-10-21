import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.testdata.TestDataFactory

import internal.GlobalVariable
import jastipin.CatalogScreen
import jastipin.AdminLoginScreen
import jastipin.AdminHomeScreen

Mobile.comment('TC25_Login_Data_Driven: Enam kombinasi login diuji dari satu berkas data.')

def data = TestDataFactory.findTestData('Data Files/LoginCases')
Mobile.comment('TC25: ' + data.getRowNumbers() + ' kombinasi login diuji')

for (int row = 1; row <= data.getRowNumbers(); row++) {
    String email = data.getValue('email', row)
    String password = data.getValue('password', row)
    String expected = data.getValue('expectedError', row)

    Mobile.comment('Baris ' + row + ' -> email=[' + email + '] password=[' + password + '] harapan=[' + expected + ']')

    CatalogScreen.openAdmin()
    AdminLoginScreen.verifyVisible()
    AdminLoginScreen.submit(email, password)

    if (expected.isEmpty()) {
        // Baris terakhir: kredensial benar, jadi harus masuk lalu keluar lagi.
        AdminHomeScreen.verifyVisible()
        AdminHomeScreen.logout()
    } else {
        AdminLoginScreen.verifyError(expected)
        Mobile.tap(findTestObject('Jastipin/AdminLogin/btnBackFromLogin'), GlobalVariable.G_Timeout)
    }

    CatalogScreen.verifyVisible()
}
