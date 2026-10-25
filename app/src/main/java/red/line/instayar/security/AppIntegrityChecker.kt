package red.line.instayar.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import red.line.instayar.BuildConfig
import java.security.MessageDigest

sealed class IntegrityResult {
    object Valid : IntegrityResult()
    data class Compromised(val reasonFa: String) : IntegrityResult()
}

object AppIntegrityChecker {

    const val EXPECTED_PACKAGE_NAME = "red.line.instayar"

    /**
     * اثر انگشت SHA-256 گواهی امضای رسمی ریلیز.
     * برای استخراج مقدار واقعی از فایل keystore خود، دستور زیر را در ترمینال اجرا کنید:
     *
     * keytool -list -v -keystore <path_to_keystore.jks> -alias <your_alias>
     *
     * سپس مقدار بخش "SHA256:" را بدون دونقطه و با حروف بزرگ در این متغیر قرار دهید.
     * مثال: "A1B2C3D4E5F6..."
     */
    const val EXPECTED_SIGNATURE_SHA256 = "RELEASE_KEYSTORE_SHA256_FINGERPRINT_PLACEHOLDER"

    fun verifyAppIntegrity(context: Context): IntegrityResult {
        // 1. بررسی نام پکیج رسمی
        val actualPackageName = context.packageName
        if (actualPackageName != EXPECTED_PACKAGE_NAME) {
            return IntegrityResult.Compromised(
                "نام پکیج برنامه معتبر نیست ($actualPackageName). لطفاً نسخه اصلی قالبینو را از مایکت دریافت کنید."
            )
        }

        // 2. بررسی وضعیت دیباگ در نسخه ریلیز (ضد دستکاری و هک)
        if (!BuildConfig.DEBUG) {
            val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable) {
                return IntegrityResult.Compromised(
                    "برنامه در حالت دستکاری‌شده (Debuggable) اجرا شده است. دسترسی به امکانات امن مسدود شد."
                )
            }
        }

        // 3. بررسی اثر انگشت امضای برنامه (SHA-256 Signature Certificate Check)
        val currentFingerprint = getAppSignatureSha256(context)
        if (EXPECTED_SIGNATURE_SHA256 != "RELEASE_KEYSTORE_SHA256_FINGERPRINT_PLACEHOLDER") {
            if (currentFingerprint == null || !currentFingerprint.equals(EXPECTED_SIGNATURE_SHA256, ignoreCase = true)) {
                return IntegrityResult.Compromised(
                    "امضای دیجیتال برنامه معتبر نیست و دستکاری شده است. برای امنیت حساب کاربری، دسترسی مسدود گردید."
                )
            }
        }

        return IntegrityResult.Valid
    }

    fun isIntegrityValid(context: Context): Boolean {
        return verifyAppIntegrity(context) is IntegrityResult.Valid
    }

    private fun getAppSignatureSha256(context: Context): String? {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = packageInfo.signingInfo ?: return null
                val signatures = if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
                signatures.firstOrNull()?.let { sha256(it.toByteArray()) }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures?.firstOrNull()?.let { sha256(it.toByteArray()) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun sha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02X".format(it) }
    }
}
