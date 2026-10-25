package red.line.instayar.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.R
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.BrandViolet
import red.line.instayar.ui.theme.DarkBackground
import red.line.instayar.ui.theme.DarkSurface
import red.line.instayar.ui.theme.PrimaryGradientBrush
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val badge: String,
    val title: String,
    val description: String,
    val imageResId: Int
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            badge = "✨ قالب‌های آماده فروشگاهی",
            title = "بیش از صدها طرح استوری و پست",
            description = "طراحی اختصاصی برای پیج‌ها و فروشگاه‌های اینستاگرامی ایرانی، با تنوع بی‌نظیر و سبک مدرن.",
            imageResId = R.drawable.onboarding_templates_1792825833428
        ),
        OnboardingPageData(
            badge = "⚡ سریع، آسان، بدون اینترنت",
            title = "طراحی حرفه‌ای در کمتر از ۳۰ ثانیه",
            description = "بدون نیاز به فتوشاپ یا مهارت طراحی؛ فقط متن و عکست رو اضافه کن و خروجی باکیفیت بگیر!",
            imageResId = R.drawable.onboarding_speed_1792825833720
        ),
        OnboardingPageData(
            badge = "🎨 پروفایل برند هوشمند",
            title = "اعمال خودکار هویت برند شما",
            description = "لوگو، آیدی پیج اینستاگرام، شماره تماس و رنگ‌های کسب‌وکارت خودکار روی همه طرح‌ها اعمال می‌شوند.",
            imageResId = R.drawable.onboarding_branding_1792825846016
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle ambient luxury glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x337C3AED),
                                Color(0x00000000),
                                Color(0x22EC4899)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar: Skip button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BrandCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "قالبینو",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (!isLastPage) {
                        TextButton(
                            onClick = onFinished,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "رد کردن",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                // Pager Content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Illustration with glassmorphic frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.25f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(DarkSurface)
                                .border(
                                    width = 1.dp,
                                    color = Color(0x338B5CF6),
                                    shape = RoundedCornerShape(28.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = page.imageResId),
                                contentDescription = page.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x228B5CF6))
                                .border(
                                    width = 1.dp,
                                    color = Color(0x448B5CF6),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = page.badge,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFC084FC),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description
                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Bottom Controls: Page Indicators and Action Button
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Modern Animated Dot Indicators
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val widthAnim by animateDpAsState(
                                targetValue = if (isSelected) 28.dp else 8.dp,
                                animationSpec = spring(),
                                label = "dotWidth"
                            )
                            val dotColor = if (isSelected) BrandPurple else Color(0xFF334155)

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(widthAnim)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    // Main Action Button
                    Button(
                        onClick = {
                            if (isLastPage) {
                                onFinished()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PrimaryGradientBrush),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isLastPage) "شروع به کار در قالبینو 🚀" else "مرحله بعدی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (!isLastPage) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
