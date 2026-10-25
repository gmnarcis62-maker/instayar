package red.line.instayar.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.data.model.BrandProfile
import red.line.instayar.ui.editor.LUXURY_PALETTE
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.DarkBackground
import red.line.instayar.ui.theme.DarkSurfaceCard
import red.line.instayar.ui.theme.DarkSurfaceCardElevated
import red.line.instayar.ui.theme.PrimaryGradientBrush

@Composable
fun BrandProfileScreen(
    brandProfile: BrandProfile,
    onSaveProfile: (BrandProfile) -> Unit,
    onNavigateHome: () -> Unit
) {
    BackHandler { onNavigateHome() }

    var brandName by remember(brandProfile) { mutableStateOf(brandProfile.brandName) }
    var handle by remember(brandProfile) { mutableStateOf(brandProfile.instagramHandle) }
    var phone by remember(brandProfile) { mutableStateOf(brandProfile.phoneNumber) }
    var address by remember(brandProfile) { mutableStateOf(brandProfile.websiteOrAddress) }
    var primaryColor by remember(brandProfile) { mutableStateOf(brandProfile.primaryColorHex) }
    var secondaryColor by remember(brandProfile) { mutableStateOf(brandProfile.secondaryColorHex) }

    var showSuccessToast by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "پروفایل برند هوشمند 🎨",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "این اطلاعات یک‌بار وارد شده و خودکار روی تمام قالب‌های انتخابی شما اعمال می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Live Mockup Card Preview
            Text(
                text = "پیش‌نمایش زنده هویت برند شما روی استوری:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                parseColor(primaryColor),
                                parseColor(secondaryColor).copy(alpha = 0.6f),
                                Color(0xFF0F0B1E)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFFFFF))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = brandName.take(1).ifBlank { "ب" },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = brandName.ifBlank { "فروشگاه آنلاین من" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = handle.ifBlank { "@my_shop" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandCyan
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x44000000))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("پیش‌نمایش", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x22000000))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📞 ${phone.ifBlank { "۰۹۱۲۳۴۵۶۷۸۹" }}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = "📍 ${address.ifBlank { "تهران، بازار بزرگ" }}",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Inputs
            BrandInputField(
                label = "نام پیج یا برند تجاری",
                value = brandName,
                onValueChange = { brandName = it },
                placeholder = "مثال: بوتیک نیلوفر"
            )

            Spacer(modifier = Modifier.height(12.dp))

            BrandInputField(
                label = "آیدی اینستاگرام (یا لینک پیج)",
                value = handle,
                onValueChange = { handle = it },
                placeholder = "مثال: @niloufar_boutique"
            )

            Spacer(modifier = Modifier.height(12.dp))

            BrandInputField(
                label = "شماره تماس یا واتساپ سفارشات",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "مثال: ۰۹۱۲۳۴۵۶۷۸۹"
            )

            Spacer(modifier = Modifier.height(12.dp))

            BrandInputField(
                label = "آدرس فروشگاه یا نشانی وب‌سایت",
                value = address,
                onValueChange = { address = it },
                placeholder = "مثال: تهران، ونک، برج آفتاب یا سایت"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Colors
            Text(
                text = "رنگ سازمانی و اصلی برند:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LUXURY_PALETTE.forEach { colorHex ->
                    val isSelected = primaryColor.equals(colorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) BrandCyan else Color(0x44FFFFFF),
                                shape = CircleShape
                            )
                            .clickable { primaryColor = colorHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "رنگ دوم / مکمل برند:",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LUXURY_PALETTE.forEach { colorHex ->
                    val isSelected = secondaryColor.equals(colorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) BrandPink else Color(0x44FFFFFF),
                                shape = CircleShape
                            )
                            .clickable { secondaryColor = colorHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save CTA Button
            Button(
                onClick = {
                    onSaveProfile(
                        BrandProfile(
                            brandName = brandName,
                            instagramHandle = handle,
                            phoneNumber = phone,
                            websiteOrAddress = address,
                            primaryColorHex = primaryColor,
                            secondaryColorHex = secondaryColor
                        )
                    )
                    showSuccessToast = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryGradientBrush)
                    .testTag("save_brand_profile_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ذخیره و اعمال روی همه قالب‌ها ✨",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            AnimatedVisibility(visible = showSuccessToast) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x3310B981))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "اطلاعات برند با موفقیت ذخیره شد و روی قالب‌ها اعمال می‌شود! ✅",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6EE7B7),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun BrandInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF64748B), fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurfaceCard,
                focusedBorderColor = BrandPurple,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        BrandPurple
    }
}
