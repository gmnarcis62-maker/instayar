package red.line.instayar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.BuildConfig
import red.line.instayar.R
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.DarkBackground
import red.line.instayar.ui.theme.DarkSurfaceCard
import red.line.instayar.ui.theme.DarkSurfaceCardElevated
import red.line.instayar.ui.theme.VipGold
import red.line.instayar.util.PersianDateUtil
import red.line.instayar.util.toPersianDigits

// آدرس ایمیل پشتیبانی به صورت محرمانه درون ثابت کد نگهداری شده و در هیچ بخش UI نمایش داده نمی‌شود
private const val SUPPORT_EMAIL_ADDRESS = "gmnarcis@gmail.com"
private const val MYKET_DEV_PAGE_URL = "https://myket.ir/developer/dev-36089"
private const val MYKET_PACKAGE_NAME = "ir.mservices.market"

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Logo gentle floating/breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x228B5CF6))
                        .testTag("about_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "درباره ما",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Scrollable content with staggered entrance
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 1. Logo & App Header
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -40 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .scale(logoScale)
                                .size(96.dp)
                                .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = BrandPink)
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color(0x99FFFFFF), Color(0x33EC4899))
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo_1792825817726),
                                contentDescription = "لوگوی قالبینو",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(23.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "قالبینو",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val versionName = BuildConfig.VERSION_NAME.ifEmpty { "1.0" }
                        Text(
                            text = "نسخه ${versionName.toPersianDigits()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC084FC),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Card: درباره‌ی برنامه
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 150)) + slideInVertically(tween(500, delayMillis = 150)) { 50 }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(DarkSurfaceCard)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(Color(0x338B5CF6), Color(0x22EC4899))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(BrandPink)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "درباره‌ی برنامه",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "قالبینو دستیار طراحی شما برای اینستاگرام است. اگر فروشگاه اینترنتی، پیج کسب‌وکار یا برند شخصی دارید، دیگر نیازی به دانش طراحی یا نرم‌افزارهای سنگین ندارید. کافی است یک قالب آماده انتخاب کنید، متن و عکس محصولتان را بگذارید و در چند ثانیه استوری، پست یا کاور هایلایت حرفه‌ای تحویل بگیرید.\n\n" +
                                        "🎨 قالب‌های آماده برای معرفی محصول، تخفیف و حراج، قیمت‌نامه، اطلاعیه، تبریک، نظر مشتری و سفارش‌گیری\n" +
                                        "✏️ ویرایشگر ساده و روان با فونت‌های فارسی زیبا، رنگ، شکل و استیکر\n" +
                                        "🏷️ پروفایل برند: لوگو، رنگ‌ها و شماره‌ی تماس شما یکبار ثبت می‌شود و روی همه‌ی قالب‌ها اعمال می‌شود\n" +
                                        "💾 ذخیره‌ی پروژه‌ها و ادامه‌ی ویرایش در هر زمان\n" +
                                        "📤 خروجی با کیفیت بالا و اشتراک‌گذاری مستقیم در اینستاگرام\n" +
                                        "📴 کاملاً آفلاین؛ بدون نیاز به اینترنت\n\n" +
                                        "با نسخه‌ی VIP به همه‌ی قالب‌های ویژه دسترسی دارید و خروجیتان بدون واترمارک خواهد بود.",
                                fontSize = 15.sp,
                                lineHeight = 25.5.sp,
                                color = Color(0xFFE2E8F0),
                                textAlign = TextAlign.Right,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Card: اطلاعات توسعه‌دهنده
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 250)) + slideInVertically(tween(500, delayMillis = 250)) { 50 }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(DarkSurfaceCardElevated)
                            .border(
                                width = 1.dp,
                                color = Color(0x228B5CF6),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(BrandCyan)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "اطلاعات توسعه‌دهنده",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Row 1: توسعه‌دهنده
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x228B5CF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Business,
                                        contentDescription = null,
                                        tint = BrandPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "توسعه‌دهنده",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "تیم نرم‌افزاری ردلاین سافت البرز",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 2: مدیریت
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x22EC4899)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = BrandPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "مدیریت",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "مهندس مهدی رضایی",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Three Large Rounded Action Buttons
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, delayMillis = 350)) + slideInVertically(tween(500, delayMillis = 350)) { 50 }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Button 1: پشتیبانی
                        AboutActionButton(
                            title = "پشتیبانی",
                            subtitle = "ارتباط مستقیم با تیم پشتیبانی فنی و پاسخگویی به سوالات",
                            icon = Icons.Rounded.HeadsetMic,
                            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                            iconTint = Color(0xFF818CF8),
                            testTag = "support_action_button",
                            onClick = {
                                openSupportEmail(context)
                            }
                        )

                        // Button 2: ثبت نظر در مایکت
                        AboutActionButton(
                            title = "ثبت نظر در مایکت",
                            subtitle = "ثبت امتیاز و نظر به منظور حمایت و پیشرفت برنامه",
                            icon = Icons.Rounded.Star,
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                            iconTint = VipGold,
                            testTag = "myket_comment_action_button",
                            onClick = {
                                openMyketComment(context)
                            }
                        )

                        // Button 3: دیگر برنامه‌های ما
                        AboutActionButton(
                            title = "دیگر برنامه‌های ما",
                            subtitle = "مشاهده سایر محصولات و اپلیکیشن‌های ردلاین سافت",
                            icon = Icons.Rounded.Apps,
                            gradientColors = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
                            iconTint = BrandPink,
                            testTag = "other_apps_action_button",
                            onClick = {
                                openDeveloperApps(context)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 5. Copyright footer
                val currentYear = PersianDateUtil.getCurrentSolarYear()
                Text(
                    text = "© ردلاین سافت البرز - $currentYear",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun AboutActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurfaceCard)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.4f) }),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(gradientColors.first().copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * ۱. پشتیبانی از طریق ایمیل با مخفی نگه‌داشتن آدرس ایمیل از UI
 */
private fun openSupportEmail(context: Context) {
    try {
        val emailUri = Uri.parse("mailto:$SUPPORT_EMAIL_ADDRESS?subject=" + Uri.encode("پشتیبانی قالبینو"))
        val intent = Intent(Intent.ACTION_SENDTO, emailUri)

        // بررسی وجود اپلیکیشن ایمیل
        val activities = context.packageManager.queryIntentActivities(intent, 0)
        if (activities.isNotEmpty()) {
            context.startActivity(intent)
        } else {
            // تلاش دوم با Intent مستقیم بدون بررسی query (در صورت محدودیت پکیج اندروید ۱۱)
            try {
                context.startActivity(Intent.createChooser(intent, "ارسال پیام به پشتیبانی"))
            } catch (e: Exception) {
                Toast.makeText(context, "برنامه‌ی ایمیل پیدا نشد", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "برنامه‌ی ایمیل پیدا نشد", Toast.LENGTH_LONG).show()
    }
}

/**
 * ۲. ثبت نظر در مایکت با سقوط هوشمند به صفحه مشخصات و مرورگر
 */
private fun openMyketComment(context: Context) {
    val pkg = context.packageName
    try {
        val commentIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("myket://comment?id=$pkg")
            setPackage(MYKET_PACKAGE_NAME)
        }
        context.startActivity(commentIntent)
    } catch (e1: Exception) {
        try {
            val detailsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("myket://details?id=$pkg")
                setPackage(MYKET_PACKAGE_NAME)
            }
            context.startActivity(detailsIntent)
        } catch (e2: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myket.ir/app/$pkg"))
                context.startActivity(webIntent)
            } catch (e3: Exception) {
                Toast.makeText(context, "امکان باز کردن صفحه مایکت وجود ندارد", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * ۳. صفحه برنامه‌های توسعه‌دهنده در مایکت
 */
private fun openDeveloperApps(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MYKET_DEV_PAGE_URL)).apply {
            // در صورت وجود مایکت با مایکت باز شود
            setPackage(MYKET_PACKAGE_NAME)
        }
        context.startActivity(intent)
    } catch (e1: Exception) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(MYKET_DEV_PAGE_URL))
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "امکان باز کردن صفحه برنامه‌ها وجود ندارد", Toast.LENGTH_SHORT).show()
        }
    }
}
