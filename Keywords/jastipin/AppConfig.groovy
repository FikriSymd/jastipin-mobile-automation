package jastipin

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.util.internal.PathUtil

import internal.GlobalVariable

/**
 * Satu sumber kebenaran untuk semua yang menggambarkan aplikasi yang diuji.
 * Naik versi APK hanya mengubah profil, tidak pernah menyentuh test case.
 */
class AppConfig {

    @Keyword
    static String appPath() {
        return PathUtil.relativeToAbsolutePath(GlobalVariable.G_AppPath, RunConfiguration.getProjectDir())
    }

    @Keyword
    static String appPackage() {
        return GlobalVariable.G_AppPackage
    }

    /**
     * Folder tempat tangkapan layar dan dump hierarki ditulis untuk tiap eksekusi.
     */
    @Keyword
    static String artifactDir() {
        String dir = RunConfiguration.getReportFolder() + File.separator + 'artifacts'
        new File(dir).mkdirs()
        return dir
    }
}
