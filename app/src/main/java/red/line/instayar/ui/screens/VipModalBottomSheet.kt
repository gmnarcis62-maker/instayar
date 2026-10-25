package red.line.instayar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.billing.BillingResult
import red.line.instayar.billing.MyketBillingManager
import red.line.instayar.billing.SkuDetails
import red.line.instayar.ui.theme.DarkSurface
import red.line.instayar.ui.theme.DarkSurfaceCard
import red.line.instayar.ui.theme.DarkSurfaceCardElevated
import red.line.instayar.ui.theme.VipGold
import red.line.instayar.ui.theme.VipGoldDark
import red.line.instayar.ui.theme.VipGoldLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipModalBottomSheet(
    onDismiss: () -> Unit,
    onPurchasePlan: (sku: String, onResult: (BillingResult) -> Unit) -> Unit,
    onRestorePurchases: (onResult: (Boolean, String) -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val plans = listOf(
        SkuDetails(
            sku = MyketBillingManager.SKU_VIP_MONTHLY,
            title = "اشتراک ۱ ماهه",
            period = "۳۰ روز دسترسی کامل به همه امکانات",
            price = "۶۹,۰۰۰ تومان"
        ),
        SkuDetails(
            sku = MyketBillingManager.SKU_VIP_QUARTERLY,
            title = "اشتراک ۳ ماهه",
            period = "۹۰ روز دسترسی کامل (بهترین انتخاب)",
            price = "۱۴۹,۰۰۰ تومان",
            discountPercent = "۳۰٪ تخفیف",
            isPopular = true
        ),
        SkuDetails(
            sku = MyketBillingManager.SKU_VIP_LIFETIME,
            title = "عضویت دائمی",
            period = "دسترسی مادام‌العمر + آپدیت‌های آینده",
            price = "۲۸۹,۰۰۰ تومان",
            discountPercent = "بهترین ارزش"
        )
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedSku by remember { mutableStateOf(MyketBillingManager.SKU_VIP_QUARTERLY) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var marketInstallUrl by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Crown Badge Top
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(VipGoldDark, VipGold, VipGoldLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "VIP Crown",
                    tint = Color(0xFF1F1200),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ارتقا به نسخه طلایی VIP 👑",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "طراحی حرفه‌ای استوری و پست اینستاگرام بدون مرز",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status / Error banner
            statusMessage?.let { msg ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSuccessMessage) Color(0x3310B981) else Color(0x33EF4444))
                        .border(
                            1.dp,
                            if (isSuccessMessage) Color(0xFF10B981) else Color(0xFFEF4444),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccessMessage) Color(0xFF34D399) else Color(0xFFFCA5A5),
                        textAlign = TextAlign.Center
                    )
                    marketInstallUrl?.let { url ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VipGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "دانلود و نصب اپلیکیشن مایکت",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Comparison table (Free vs VIP)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "مقایسه امکانات نسخه رایگان و طلایی",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = VipGold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ComparisonRow(
                    feature = "دسترسی به تمام ۲۰+ قالب فروشگاهی",
                    freeValue = "محدود",
                    vipValue = "نامحدود",
                    isVipExclusive = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                ComparisonRow(
                    feature = "حذف کامل واترمارک روی خروجی",
                    freeValue = "با واترمارک",
                    vipValue = "کاملاً تمیز",
                    isVipExclusive = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                ComparisonRow(
                    feature = "بیش از ۸ فونت فارسی اختصاصی",
                    freeValue = "پایه",
                    vipValue = "تمامی فونت‌ها",
                    isVipExclusive = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                ComparisonRow(
                    feature = "کیفیت تصویر خروجی",
                    freeValue = "استاندارد",
                    vipValue = "Ultra HD 1080p",
                    isVipExclusive = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Plan Options (Myket In-App Billing Products)
            plans.forEach { plan ->
                val isSelected = selectedSku == plan.sku
                val borderModifier = if (isSelected) {
                    Modifier.border(2.dp, VipGold, RoundedCornerShape(18.dp))
                } else {
                    Modifier.border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) DarkSurfaceCardElevated else DarkSurfaceCard)
                        .then(borderModifier)
                        .clickable { selectedSku = plan.sku }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                plan.discountPercent?.let { badge ->
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (plan.isPopular) VipGold else Color(0xFFEC4899))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (plan.isPopular) "پیشنهاد ویژه" else badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = plan.period,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Text(
                            text = plan.price,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) VipGold else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Purchase Button
            Button(
                onClick = {
                    isProcessing = true
                    statusMessage = null
                    marketInstallUrl = null
                    onPurchasePlan(selectedSku) { result ->
                        isProcessing = false
                        when (result) {
                            is BillingResult.Success -> {
                                isSuccessMessage = true
                                statusMessage = result.message
                            }
                            is BillingResult.Error -> {
                                isSuccessMessage = false
                                statusMessage = result.errorMessage
                            }
                            is BillingResult.Cancelled -> {
                                isSuccessMessage = false
                                statusMessage = "خرید لغو شد"
                            }
                            is BillingResult.MarketNotInstalled -> {
                                isSuccessMessage = false
                                statusMessage = "برای خرید، مایکت را نصب کنید."
                                marketInstallUrl = result.installUrl
                            }
                            else -> {}
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(VipGoldDark, VipGold, VipGoldLight)
                        )
                    )
                    .testTag("purchase_vip_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1E1000),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "پرداخت امن مایکت و فعال‌سازی آنی ✨",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1000)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Restore purchases button
            OutlinedButton(
                onClick = {
                    isProcessing = true
                    statusMessage = null
                    onRestorePurchases { success, msg ->
                        isProcessing = false
                        isSuccessMessage = success
                        statusMessage = msg
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("restore_vip_button"),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(Color(0x55FFFFFF), Color(0x33FFFFFF)))
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بازیابی خرید قبلی از مایکت",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "پرداخت ایمن و مطمئن با درگاه پرداخت درون‌برنامه‌ای مایکت",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ComparisonRow(
    feature: String,
    freeValue: String,
    vipValue: String,
    isVipExclusive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = freeValue,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x22FBBF24))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = vipValue,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = VipGold
                )
            }
        }
    }
}
