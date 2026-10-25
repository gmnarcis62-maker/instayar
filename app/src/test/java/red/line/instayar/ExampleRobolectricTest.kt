package red.line.instayar

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.farasource.billing.util.TableCodes
import org.json.JSONObject
import red.line.instayar.billing.BillingResult
import red.line.instayar.billing.MyketBillingManager
import red.line.instayar.data.repository.TemplateRepository
import red.line.instayar.data.repository.UserPreferences
import red.line.instayar.security.AppIntegrityChecker
import red.line.instayar.security.IntegrityResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("قالبینو", appName)
  }

  @Test
  fun `load templates from assets`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = TemplateRepository(context)
    val templates = repo.loadTemplates()
    assertTrue(templates.isNotEmpty())
    assertTrue(templates.size >= 20)
  }

  @Test
  fun `verify user preferences vip persistence and rsa validation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = UserPreferences(context)

    // Clear state
    prefs.clearVipStatus()
    assertFalse(prefs.isVipUser.value)

    // Generate test RSA KeyPair
    val keyGen = java.security.KeyPairGenerator.getInstance("RSA").apply {
      initialize(1024)
    }
    val keyPair = keyGen.generateKeyPair()
    val testPubKeyBase64 = android.util.Base64.encodeToString(keyPair.public.encoded, android.util.Base64.NO_WRAP)
    MyketBillingManager.testPublicKeyOverride = testPubKeyBase64

    try {
      // 1. Test Valid Lifetime purchase JSON & RSA signature
      val lifetimeJson = """{"productId":"vip_lifetime","purchaseToken":"token_life","purchaseTime":${System.currentTimeMillis()}}"""
      val signer = java.security.Signature.getInstance("SHA1withRSA").apply {
        initSign(keyPair.private)
        update(lifetimeJson.toByteArray(Charsets.UTF_8))
      }
      val validLifetimeSig = android.util.Base64.encodeToString(signer.sign(), android.util.Base64.NO_WRAP)

      val recordedLife = prefs.recordValidPurchase(lifetimeJson, validLifetimeSig)
      assertTrue(recordedLife)
      assertTrue(prefs.isVipUser.value)
      assertEquals("vip_lifetime", prefs.vipPlanType.value)

      // 2. Test Expired Monthly purchase JSON (35 days ago) with valid RSA signature
      val expiredMonthlyTime = System.currentTimeMillis() - (35L * 24 * 60 * 60 * 1000)
      val expiredMonthlyJson = """{"productId":"vip_monthly","purchaseToken":"token_month","purchaseTime":$expiredMonthlyTime}"""
      val signerMonth = java.security.Signature.getInstance("SHA1withRSA").apply {
        initSign(keyPair.private)
        update(expiredMonthlyJson.toByteArray(Charsets.UTF_8))
      }
      val expiredMonthSig = android.util.Base64.encodeToString(signerMonth.sign(), android.util.Base64.NO_WRAP)

      prefs.recordValidPurchase(expiredMonthlyJson, expiredMonthSig)
      // Because it's expired, isVipUser must be false!
      assertFalse(prefs.isVipUser.value)
      assertTrue(prefs.isPlanExpired("vip_monthly", expiredMonthlyTime))

      // 3. Test Invalid/Tampered signature rejection
      val fakeSig = "invalidsignature12345"
      val recordFake = prefs.recordValidPurchase(lifetimeJson, fakeSig)
      assertFalse(recordFake)
      assertFalse(prefs.isVipUser.value)

      // 4. Test revalidate on startup without key_is_vip
      val sharedPrefs = context.getSharedPreferences("ghalibino_prefs", Context.MODE_PRIVATE)
      assertFalse(sharedPrefs.contains("key_is_vip"))
    } finally {
      MyketBillingManager.testPublicKeyOverride = null
    }
  }

  @Test
  fun `verify myket billing manager skus and obfuscated public key`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = UserPreferences(context)
    val billingManager = MyketBillingManager(context, prefs)

    assertEquals(3, billingManager.availableSkus.value.size)

    val key = MyketBillingManager.getMyketPublicKey()
    assertNotNull(key)
    assertTrue(key.startsWith("MIGfMA0GCSqGSIb3DQEBAQU"))
    assertTrue(key.endsWith("IDAQAB"))

    // Invalid signature rejection test
    val isFakeValid = MyketBillingManager.verifyRsaSignature("fake_json", "fake_signature")
    assertFalse(isFakeValid)
  }

  @Test
  fun `verify app integrity checker package verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val integrity = AppIntegrityChecker.verifyAppIntegrity(context)
    assertNotNull(integrity)
  }

  @Test
  fun `verify persian date util digits and calendar conversion`() {
    // Digits conversion test
    val original = "Story 1405 - Price 69000 Tomans"
    val converted = red.line.instayar.util.PersianDateUtil.toPersianDigits(original)
    assertEquals("Story ۱۴۰۵ - Price ۶۹۰۰۰ Tomans", converted)

    // Gregorian to Persian calendar conversion test
    val (jy, jm, jd) = red.line.instayar.util.PersianDateUtil.gregorianToPersian(2026, 10, 25)
    assertEquals(1405, jy)
    assertEquals(8, jm) // Aban

    // Current solar year
    val solarYear = red.line.instayar.util.PersianDateUtil.getCurrentSolarYear()
    assertTrue(solarYear.isNotEmpty())

    // Live time check
    val liveTime = red.line.instayar.util.PersianDateUtil.getCurrentLiveTime()
    assertTrue(liveTime.contains(":"))

    // Greeting check
    val greeting = red.line.instayar.util.PersianDateUtil.getTimeOfDayGreeting()
    assertTrue(greeting.isNotEmpty())
  }

  @Test
  fun `verify isPurchaseContentValid rejects mismatched packageName and unknown productId`() {
    // 1. Valid cases
    val validMonthly = JSONObject("""{"packageName":"red.line.instayar","productId":"vip_monthly"}""")
    val validQuarterly = JSONObject("""{"packageName":"red.line.instayar","productId":"vip_quarterly"}""")
    val validLifetime = JSONObject("""{"packageName":"red.line.instayar","productId":"vip_lifetime"}""")
    val validWithoutPackage = JSONObject("""{"productId":"vip_lifetime"}""")

    assertTrue(MyketBillingManager.isPurchaseContentValid(validMonthly))
    assertTrue(MyketBillingManager.isPurchaseContentValid(validQuarterly))
    assertTrue(MyketBillingManager.isPurchaseContentValid(validLifetime))
    assertTrue(MyketBillingManager.isPurchaseContentValid(validWithoutPackage))

    // 2. Invalid packageName cases
    val wrongPackage = JSONObject("""{"packageName":"com.other.app","productId":"vip_monthly"}""")
    val emptyPackage = JSONObject("""{"packageName":"","productId":"vip_monthly"}""")
    assertFalse(MyketBillingManager.isPurchaseContentValid(wrongPackage))
    assertFalse(MyketBillingManager.isPurchaseContentValid(emptyPackage))

    // 3. Invalid productId cases
    val wrongProduct = JSONObject("""{"packageName":"red.line.instayar","productId":"coins_100"}""")
    val emptyProduct = JSONObject("""{"packageName":"red.line.instayar","productId":""}""")
    val missingProduct = JSONObject("""{"packageName":"red.line.instayar"}""")
    assertFalse(MyketBillingManager.isPurchaseContentValid(wrongProduct))
    assertFalse(MyketBillingManager.isPurchaseContentValid(emptyProduct))
    assertFalse(MyketBillingManager.isPurchaseContentValid(missingProduct))
  }

  @Test
  fun `verify recordValidPurchase rejects bad packageName and bad productId even with valid signature`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = UserPreferences(context)
    prefs.clearVipStatus()

    val keyGen = java.security.KeyPairGenerator.getInstance("RSA").apply {
      initialize(1024)
    }
    val keyPair = keyGen.generateKeyPair()
    val testPubKeyBase64 = android.util.Base64.encodeToString(keyPair.public.encoded, android.util.Base64.NO_WRAP)
    MyketBillingManager.testPublicKeyOverride = testPubKeyBase64

    try {
      // Bad packageName with valid RSA signature
      val badPkgJson = """{"packageName":"ir.fake.app","productId":"vip_lifetime","purchaseTime":${System.currentTimeMillis()}}"""
      val signer = java.security.Signature.getInstance("SHA1withRSA").apply {
        initSign(keyPair.private)
        update(badPkgJson.toByteArray(Charsets.UTF_8))
      }
      val badPkgSig = android.util.Base64.encodeToString(signer.sign(), android.util.Base64.NO_WRAP)
      val recordedBadPkg = prefs.recordValidPurchase(badPkgJson, badPkgSig)
      assertFalse(recordedBadPkg)
      assertFalse(prefs.isVipUser.value)

      // Bad productId with valid RSA signature
      val badPidJson = """{"packageName":"red.line.instayar","productId":"fake_vip_plan","purchaseTime":${System.currentTimeMillis()}}"""
      val signer2 = java.security.Signature.getInstance("SHA1withRSA").apply {
        initSign(keyPair.private)
        update(badPidJson.toByteArray(Charsets.UTF_8))
      }
      val badPidSig = android.util.Base64.encodeToString(signer2.sign(), android.util.Base64.NO_WRAP)
      val recordedBadPid = prefs.recordValidPurchase(badPidJson, badPidSig)
      assertFalse(recordedBadPid)
      assertFalse(prefs.isVipUser.value)
    } finally {
      MyketBillingManager.testPublicKeyOverride = null
    }
  }

  @Test
  fun `verify handleBillingStatus prevents purchase loop on error and cancellation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = UserPreferences(context)
    val billingManager = MyketBillingManager(context, prefs)

    // Simulate pending purchase and user cancellation (code 1)
    var resultReceived: BillingResult? = null
    billingManager.setPendingCallbackForTesting {
      resultReceived = it
    }

    billingManager.handleBillingStatus(1)
    assertEquals(BillingResult.Cancelled, resultReceived)

    // Verify callback was cleared and further calls do not trigger it again
    resultReceived = null
    billingManager.handleBillingStatus(TableCodes.BILLING_FAILED)
    // Because pending callback was cleared, resultReceived remains null
    assertEquals(null, resultReceived)

    // Test BILLING_FAILED terminates with Error and clears callback without loop
    billingManager.setPendingCallbackForTesting {
      resultReceived = it
    }
    billingManager.handleBillingStatus(TableCodes.BILLING_FAILED)
    assertTrue(resultReceived is BillingResult.Error)

    // Verify that after BILLING_FAILED, callback is cleared and no loop occurs
    resultReceived = null
    billingManager.handleBillingStatus(TableCodes.BILLING_FAILED)
    assertEquals(null, resultReceived)
  }
}
