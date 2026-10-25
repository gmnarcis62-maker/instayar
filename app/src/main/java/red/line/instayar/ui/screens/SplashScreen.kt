package red.line.instayar.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import red.line.instayar.R
import red.line.instayar.security.AppIntegrityChecker
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.DarkBackground
import red.line.instayar.ui.theme.VipGold
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Animations controllers
    val progressBarAnim = remember { Animatable(0f) }
    val logoScaleAnim = remember { Animatable(0.2f) }
    val logoAlphaAnim = remember { Animatable(0f) }
    val titleAlphaAnim = remember { Animatable(0f) }
    val subtitleAlphaAnim = remember { Animatable(0f) }

    // Cards converging from edges
    val cardConvergenceAnim = remember { Animatable(0f) } // 0f = spread out, 1f = converged to center
    val cardAlphaAnim = remember { Animatable(0f) }

    // Subtle background ambient pulse
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_pulse")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse_float"
    )

    // Particles random positions
    val particles = remember {
        List(18) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 4f + 2f)
        }
    }

    LaunchedEffect(Unit) {
        // Run integrity check in background during splash
        launch {
            AppIntegrityChecker.verifyAppIntegrity(context)
        }

        // Animate floating story/post cards entering and converging
        launch {
            cardAlphaAnim.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            cardConvergenceAnim.animateTo(1f, tween(1600, easing = FastOutSlowInEasing))
            cardAlphaAnim.animateTo(0.2f, tween(500, easing = LinearEasing))
        }

        // Animate Logo with spring scale & glow after cards converge
        launch {
            delay(500)
            launch {
                logoAlphaAnim.animateTo(1f, tween(500))
            }
            logoScaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Animate title and slogan
        launch {
            delay(1100)
            titleAlphaAnim.animateTo(1f, tween(500))
            delay(300)
            subtitleAlphaAnim.animateTo(1f, tween(500))
        }

        // Progress bar fills over precisely 3000ms
        launch {
            progressBarAnim.animateTo(1f, tween(3000, easing = LinearEasing))
        }

        // Exactly 3 seconds total splash duration
        delay(3000)
        onFinished()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            // Deep cosmic purple gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2E124D).copy(alpha = ambientPulse * 0.9f),
                                Color(0xFF1B0A2E),
                                Color(0xFF0F051D)
                            ),
                            radius = 1200f
                        )
                    )
            )

            // Ambient glowing particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                particles.forEach { (xRatio, yRatio, radius) ->
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = 0.25f * ambientPulse),
                        radius = radius * density,
                        center = Offset(canvasWidth * xRatio, canvasHeight * yRatio)
                    )
                }
            }

            // Floating mini story & post cards that converge from edges
            val convergeProgress = cardConvergenceAnim.value
            val cardAlpha = cardAlphaAnim.value

            // Card 1: Top-Right (Story card)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        val startX = (screenWidth.toPx() * 0.45f)
                        val startY = -(screenHeight.toPx() * 0.35f)
                        IntOffset(
                            x = (startX * (1f - convergeProgress)).roundToInt(),
                            y = (startY * (1f - convergeProgress)).roundToInt()
                        )
                    }
                    .rotate(14f * (1f - convergeProgress))
                    .alpha(cardAlpha)
                    .size(width = 64.dp, height = 110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B5CF6).copy(alpha = 0.8f), Color(0xFFEC4899).copy(alpha = 0.8f))
                        )
                    )
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp))
            )

            // Card 2: Top-Left (Square Post card)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        val startX = -(screenWidth.toPx() * 0.42f)
                        val startY = -(screenHeight.toPx() * 0.25f)
                        IntOffset(
                            x = (startX * (1f - convergeProgress)).roundToInt(),
                            y = (startY * (1f - convergeProgress)).roundToInt()
                        )
                    }
                    .rotate(-18f * (1f - convergeProgress))
                    .alpha(cardAlpha)
                    .size(width = 72.dp, height = 72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF06B6D4).copy(alpha = 0.8f), Color(0xFF3B82F6).copy(alpha = 0.8f))
                        )
                    )
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
            )

            // Card 3: Bottom-Left (Story card)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        val startX = -(screenWidth.toPx() * 0.45f)
                        val startY = (screenHeight.toPx() * 0.30f)
                        IntOffset(
                            x = (startX * (1f - convergeProgress)).roundToInt(),
                            y = (startY * (1f - convergeProgress)).roundToInt()
                        )
                    }
                    .rotate(-12f * (1f - convergeProgress))
                    .alpha(cardAlpha)
                    .size(width = 60.dp, height = 105.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFF59E0B).copy(alpha = 0.7f), Color(0xFFEF4444).copy(alpha = 0.7f))
                        )
                    )
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp))
            )

            // Card 4: Bottom-Right (Highlight cover card)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        val startX = (screenWidth.toPx() * 0.40f)
                        val startY = (screenHeight.toPx() * 0.28f)
                        IntOffset(
                            x = (startX * (1f - convergeProgress)).roundToInt(),
                            y = (startY * (1f - convergeProgress)).roundToInt()
                        )
                    }
                    .rotate(16f * (1f - convergeProgress))
                    .alpha(cardAlpha)
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFEC4899).copy(alpha = 0.8f), Color(0xFF8B5CF6).copy(alpha = 0.8f))
                        )
                    )
                    .border(1.5.dp, VipGold.copy(alpha = 0.8f), CircleShape)
            )

            // Central Branding: Glow, Logo, Name & Slogan
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo with Glow & Spring Scale
                Box(
                    modifier = Modifier
                        .scale(logoScaleAnim.value)
                        .alpha(logoAlphaAnim.value)
                        .size(118.dp)
                        .shadow(
                            elevation = 32.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = BrandPink,
                            ambientColor = BrandPurple
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF9333EA), Color(0xFFEC4899))
                            )
                        )
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.8f), BrandPink.copy(alpha = 0.4f))
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo_1792825817726),
                        contentDescription = "لوگوی قالبینو",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // App Name with smooth reveal
                Text(
                    text = "قالبینو",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.alpha(titleAlphaAnim.value),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Slogan
                Text(
                    text = "استوری و پست حرفه‌ای، در چند ثانیه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.alpha(subtitleAlphaAnim.value),
                    textAlign = TextAlign.Center
                )
            }

            // Slim Gradient Progress Bar at Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x338B5CF6))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressBarAnim.value)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(BrandCyan, BrandPurple, BrandPink)
                                )
                            )
                    )
                }
            }
        }
    }
}
