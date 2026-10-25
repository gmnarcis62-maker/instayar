package red.line.instayar.data.repository

import android.content.Context
import android.content.SharedPreferences
import red.line.instayar.billing.MyketBillingManager
import red.line.instayar.data.model.BrandProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * مدیریت تنظیمات کاربر و وضعیت عضویت VIP با اعتبارسنجی رمزنگاری‌شده امضای RSA مایکت
 * و صحت محتوای بسته (packageName و productId)
 */
class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ghalibino_prefs", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_DONE, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _vipPlanType = MutableStateFlow<String?>(null)
    val vipPlanType: StateFlow<String?> = _vipPlanType.asStateFlow()

    private val _vipPurchaseToken = MutableStateFlow<String?>(null)
    val vipPurchaseToken: StateFlow<String?> = _vipPurchaseToken.asStateFlow()

    private val _vipPurchaseTime = MutableStateFlow(0L)
    val vipPurchaseTime: StateFlow<Long> = _vipPurchaseTime.asStateFlow()

    // وضعیت VIP بر اساس اعتبارسنجی امضای RSA مایکت، صحت بسته و انقضای پلن محاسبه می‌شود
    private val _isVipUser = MutableStateFlow(false)
    val isVipUser: StateFlow<Boolean> = _isVipUser.asStateFlow()

    private val _brandProfile = MutableStateFlow(loadBrandProfile())
    val brandProfile: StateFlow<BrandProfile> = _brandProfile.asStateFlow()

    init {
        // حذف کلید منسوخ key_is_vip از حافظه کش به طوری که دیگر ملاک تصمیم نباشد
        if (prefs.contains("key_is_vip")) {
            prefs.edit().remove("key_is_vip").apply()
        }
        // محاسبه و اعتبارسنجی وضعیت VIP بر اساس JSON و امضا
        _isVipUser.value = verifyAndCalculateVipStatus()
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
        _onboardingCompleted.value = completed
    }

    /**
     * اعتبارسنجی کامل امضای RSA مایکت، اعتبارسنجی محتوای خرید (packageName و productId) و محاسبه انقضا
     */
    fun verifyAndCalculateVipStatus(): Boolean {
        val originalJson = prefs.getString(KEY_PURCHASE_ORIGINAL_JSON, null)
        val signature = prefs.getString(KEY_PURCHASE_SIGNATURE, null)

        if (originalJson.isNullOrBlank() || signature.isNullOrBlank()) {
            _isVipUser.value = false
            _vipPlanType.value = null
            _vipPurchaseToken.value = null
            _vipPurchaseTime.value = 0L
            return false
        }

        // ۱. بررسی امضای دیجیتال با کلید RSA عمومی مایکت
        val isSignatureValid = MyketBillingManager.verifyRsaSignature(originalJson, signature)
        if (!isSignatureValid) {
            clearVipStatus()
            return false
        }

        return try {
            val json = JSONObject(originalJson)

            // ۲. اعتبارسنجی محتوای خرید: بررسی packageName و مجاز بودن productId
            if (!MyketBillingManager.isPurchaseContentValid(json)) {
                clearVipStatus()
                return false
            }

            val productId = json.optString("productId")
            val token = json.optString("purchaseToken", json.optString("token"))
            val purchaseTime = json.optLong("purchaseTime", 0L)

            if (purchaseTime <= 0L) {
                clearVipStatus()
                return false
            }

            val expired = isPlanExpired(productId, purchaseTime)
            _vipPlanType.value = productId
            _vipPurchaseToken.value = token
            _vipPurchaseTime.value = purchaseTime

            if (expired) {
                _isVipUser.value = false
                false
            } else {
                _isVipUser.value = true
                true
            }
        } catch (e: Exception) {
            clearVipStatus()
            false
        }
    }

    /**
     * ذخیره متن کامل JSON خرید و امضای دیجیتال آن پس از بررسی دقیق امضا و محتوا
     */
    fun recordValidPurchase(originalJson: String, signature: String): Boolean {
        // ۱. بررسی امضای دیجیتال
        val isSignatureValid = MyketBillingManager.verifyRsaSignature(originalJson, signature)
        if (!isSignatureValid) {
            clearVipStatus()
            return false
        }

        return try {
            val json = JSONObject(originalJson)

            // ۲. بررسی صحت packageName و مجاز بودن productId
            if (!MyketBillingManager.isPurchaseContentValid(json)) {
                clearVipStatus()
                return false
            }

            val productId = json.optString("productId")
            val token = json.optString("purchaseToken", json.optString("token"))
            val purchaseTime = json.optLong("purchaseTime", System.currentTimeMillis())

            val expired = isPlanExpired(productId, purchaseTime)

            prefs.edit()
                .putString(KEY_PURCHASE_ORIGINAL_JSON, originalJson)
                .putString(KEY_PURCHASE_SIGNATURE, signature)
                .putString(KEY_VIP_PLAN_TYPE, productId)
                .putString(KEY_VIP_PURCHASE_TOKEN, token)
                .putLong(KEY_VIP_PURCHASE_TIME, purchaseTime)
                .remove("key_is_vip")
                .apply()

            _vipPlanType.value = productId
            _vipPurchaseToken.value = token
            _vipPurchaseTime.value = purchaseTime
            _isVipUser.value = !expired
            !expired
        } catch (e: Exception) {
            clearVipStatus()
            false
        }
    }

    /**
     * بررسی مجدد وضعیت VIP و به‌روزرسانی StateFlow
     */
    fun revalidateVipStatus(): Boolean {
        val isValid = verifyAndCalculateVipStatus()
        _isVipUser.value = isValid
        return isValid
    }

    /**
     * پاکسازی کامل اطلاعات خرید و لغو عضویت VIP
     */
    fun clearVipStatus() {
        prefs.edit()
            .remove(KEY_PURCHASE_ORIGINAL_JSON)
            .remove(KEY_PURCHASE_SIGNATURE)
            .remove(KEY_VIP_PLAN_TYPE)
            .remove(KEY_VIP_PURCHASE_TOKEN)
            .remove(KEY_VIP_PURCHASE_TIME)
            .remove("key_is_vip")
            .apply()

        _isVipUser.value = false
        _vipPlanType.value = null
        _vipPurchaseToken.value = null
        _vipPurchaseTime.value = 0L
    }

    fun getStoredOriginalJson(): String? = prefs.getString(KEY_PURCHASE_ORIGINAL_JSON, null)
    fun getStoredSignature(): String? = prefs.getString(KEY_PURCHASE_SIGNATURE, null)

    /**
     * بررسی انقضای پلن‌های زمان‌دار (۳۰ روز، ۹۰ روز، یا نامحدود)
     */
    fun isPlanExpired(sku: String, purchaseTime: Long, currentTime: Long = System.currentTimeMillis()): Boolean {
        if (sku == MyketBillingManager.SKU_VIP_LIFETIME) return false
        val durationMs = when (sku) {
            MyketBillingManager.SKU_VIP_MONTHLY -> 30L * 24 * 60 * 60 * 1000
            MyketBillingManager.SKU_VIP_QUARTERLY -> 90L * 24 * 60 * 60 * 1000
            else -> 30L * 24 * 60 * 60 * 1000
        }
        return (currentTime - purchaseTime) > durationMs
    }

    fun saveBrandProfile(profile: BrandProfile) {
        prefs.edit()
            .putString(KEY_BRAND_NAME, profile.brandName)
            .putString(KEY_BRAND_HANDLE, profile.instagramHandle)
            .putString(KEY_BRAND_PHONE, profile.phoneNumber)
            .putString(KEY_BRAND_ADDRESS, profile.websiteOrAddress)
            .putString(KEY_BRAND_COLOR_PRIMARY, profile.primaryColorHex)
            .putString(KEY_BRAND_COLOR_SECONDARY, profile.secondaryColorHex)
            .putString(KEY_BRAND_LOGO, profile.logoUri)
            .apply()
        _brandProfile.value = profile
    }

    private fun loadBrandProfile(): BrandProfile {
        return BrandProfile(
            brandName = prefs.getString(KEY_BRAND_NAME, "فروشگاه آنلاین من") ?: "فروشگاه آنلاین من",
            instagramHandle = prefs.getString(KEY_BRAND_HANDLE, "@my_shop_page") ?: "@my_shop_page",
            phoneNumber = prefs.getString(KEY_BRAND_PHONE, "۰۹۱۲۳۴۵۶۷۸۹") ?: "۰۹۱۲۳۴۵۶۷۸۹",
            websiteOrAddress = prefs.getString(KEY_BRAND_ADDRESS, "تهران، بازار بزرگ") ?: "تهران، بازار بزرگ",
            primaryColorHex = prefs.getString(KEY_BRAND_COLOR_PRIMARY, "#7C3AED") ?: "#7C3AED",
            secondaryColorHex = prefs.getString(KEY_BRAND_COLOR_SECONDARY, "#EC4899") ?: "#EC4899",
            logoUri = prefs.getString(KEY_BRAND_LOGO, null)
        )
    }

    companion object {
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
        private const val KEY_PURCHASE_ORIGINAL_JSON = "key_purchase_original_json"
        private const val KEY_PURCHASE_SIGNATURE = "key_purchase_signature"
        private const val KEY_VIP_PLAN_TYPE = "key_vip_plan_type"
        private const val KEY_VIP_PURCHASE_TOKEN = "key_vip_purchase_token"
        private const val KEY_VIP_PURCHASE_TIME = "key_vip_purchase_time"
        private const val KEY_BRAND_NAME = "key_brand_name"
        private const val KEY_BRAND_HANDLE = "key_brand_handle"
        private const val KEY_BRAND_PHONE = "key_brand_phone"
        private const val KEY_BRAND_ADDRESS = "key_brand_address"
        private const val KEY_BRAND_COLOR_PRIMARY = "key_brand_color_primary"
        private const val KEY_BRAND_COLOR_SECONDARY = "key_brand_color_secondary"
        private const val KEY_BRAND_LOGO = "key_brand_logo"
    }
}
