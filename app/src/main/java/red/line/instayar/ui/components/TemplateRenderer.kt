package red.line.instayar.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import red.line.instayar.data.model.BrandProfile
import red.line.instayar.data.model.ElementType
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateBackground
import red.line.instayar.data.model.TemplateElement
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TemplateThumbnailPreview(
    template: Template,
    modifier: Modifier = Modifier,
    brandProfile: BrandProfile = BrandProfile(),
    cornerRadius: Float = 20f
) {
    val appContext = LocalContext.current
    Box(
        modifier = modifier
            .aspectRatio(template.aspectRatioFloat)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(Color(0xFF1E1935))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTemplate(
                template = template,
                canvasSize = size,
                brandProfile = brandProfile,
                context = appContext
            )
        }
    }
}

fun DrawScope.drawTemplate(
    template: Template,
    canvasSize: Size,
    brandProfile: BrandProfile = BrandProfile(),
    context: Context? = null
) {
    val w = canvasSize.width
    val h = canvasSize.height

    // 1. Draw Background
    drawTemplateBackground(template.background, w, h)

    // 2. Draw Elements in order
    for (element in template.elements) {
        drawTemplateElement(element, w, h, brandProfile, context)
    }
}

private fun DrawScope.drawTemplateBackground(bg: TemplateBackground, w: Float, h: Float) {
    if (bg.type.uppercase() == "GRADIENT" && bg.gradientColors.isNotEmpty()) {
        val colors = bg.gradientColors.map { parseColorHex(it) }
        val rad = (bg.angle * PI / 180f).toFloat()
        val startOffset = Offset(0f, 0f)
        val endOffset = Offset(
            x = (w * cos(rad)).coerceIn(0f, w),
            y = (h * sin(rad)).coerceIn(0f, h)
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = if (colors.size > 1) colors else listOf(colors.first(), colors.first()),
                start = startOffset,
                end = if (endOffset == Offset.Zero) Offset(w, h) else endOffset
            ),
            size = Size(w, h)
        )
    } else {
        drawRect(
            color = parseColorHex(bg.color),
            size = Size(w, h)
        )
    }

    // Add subtle ambient glowing circles for depth and polish
    drawCircle(
        color = Color(0x18FFFFFF),
        radius = w * 0.45f,
        center = Offset(w * 0.85f, h * 0.15f)
    )
    drawCircle(
        color = Color(0x12FFFFFF),
        radius = w * 0.35f,
        center = Offset(w * 0.15f, h * 0.85f)
    )
}

private fun DrawScope.drawTemplateElement(
    element: TemplateElement,
    canvasW: Float,
    canvasH: Float,
    brandProfile: BrandProfile,
    context: Context? = null
) {
    val elX = element.x * canvasW
    val elY = element.y * canvasH
    val elW = element.width * canvasW
    val elH = element.height * canvasH
    val centerX = elX + elW / 2f
    val centerY = elY + elH / 2f

    rotate(degrees = element.rotation, pivot = Offset(centerX, centerY)) {
        when (element.type) {
            ElementType.SHAPE -> {
                drawShapeElement(element, elX, elY, elW, elH)
            }
            ElementType.IMAGE_PLACEHOLDER -> {
                drawImagePlaceholder(element, elX, elY, elW, elH, context)
            }
            ElementType.TEXT -> {
                drawTextElement(element, element.text, elX, elY, elW, elH, canvasW)
            }
            ElementType.BRAND_NAME -> {
                val brandText = if (brandProfile.brandName.isNotBlank()) brandProfile.brandName else (element.text ?: "")
                drawTextElement(element, brandText, elX, elY, elW, elH, canvasW)
            }
            ElementType.BRAND_CONTACT -> {
                val contactText = when {
                    brandProfile.phoneNumber.isNotBlank() && brandProfile.instagramHandle.isNotBlank() ->
                        "${brandProfile.instagramHandle} | ${brandProfile.phoneNumber}"
                    brandProfile.phoneNumber.isNotBlank() -> brandProfile.phoneNumber
                    brandProfile.instagramHandle.isNotBlank() -> brandProfile.instagramHandle
                    else -> element.text ?: ""
                }
                drawTextElement(element, contactText, elX, elY, elW, elH, canvasW)
            }
            ElementType.BADGE, ElementType.STICKER -> {
                drawBadgeElement(element, elX, elY, elW, elH, canvasW)
            }
            ElementType.BRAND_LOGO -> {
                drawBrandLogoElement(element, elX, elY, elW, elH, brandProfile)
            }
        }
    }
}

private fun DrawScope.drawShapeElement(element: TemplateElement, x: Float, y: Float, w: Float, h: Float) {
    val bgColor = element.backgroundColor?.let { parseColorHex(it) } ?: Color(0x33FFFFFF)
    val corner = element.cornerRadius * (w / 300f).coerceAtLeast(0.5f)

    when (element.shapeType.uppercase()) {
        "CIRCLE" -> {
            drawCircle(
                color = bgColor,
                radius = minOf(w, h) / 2f,
                center = Offset(x + w / 2f, y + h / 2f)
            )
        }
        "TAG" -> {
            val path = Path().apply {
                moveTo(x + 16f, y)
                lineTo(x + w, y)
                lineTo(x + w, y + h)
                lineTo(x + 16f, y + h)
                lineTo(x, y + h / 2f)
                close()
            }
            drawPath(path = path, color = bgColor, style = Fill)
        }
        "RIBBON" -> {
            val path = Path().apply {
                moveTo(x, y)
                lineTo(x + w, y)
                lineTo(x + w - 12f, y + h / 2f)
                lineTo(x + w, y + h)
                lineTo(x, y + h)
                lineTo(x + 12f, y + h / 2f)
                close()
            }
            drawPath(path = path, color = bgColor, style = Fill)
        }
        else -> {
            drawRoundRect(
                color = bgColor,
                topLeft = Offset(x, y),
                size = Size(w, h),
                cornerRadius = CornerRadius(corner, corner)
            )
        }
    }

    if (element.borderWidth > 0f && element.borderColor != null) {
        val bColor = parseColorHex(element.borderColor)
        if (element.shapeType.uppercase() == "CIRCLE") {
            drawCircle(
                color = bColor,
                radius = minOf(w, h) / 2f,
                center = Offset(x + w / 2f, y + h / 2f),
                style = Stroke(width = element.borderWidth)
            )
        } else {
            drawRoundRect(
                color = bColor,
                topLeft = Offset(x, y),
                size = Size(w, h),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = element.borderWidth)
            )
        }
    }
}

private fun DrawScope.drawImagePlaceholder(element: TemplateElement, x: Float, y: Float, w: Float, h: Float, context: Context? = null) {
    val userImagePath = element.imagePath
    if (!userImagePath.isNullOrBlank()) {
        try {
            val bitmap = BitmapFactory.decodeFile(userImagePath)
            if (bitmap != null) {
                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    val save = native.save()
                    val radius = element.cornerRadius * (w / 300f).coerceAtLeast(0.5f)
                    val clip = android.graphics.Path().apply { addRoundRect(RectF(x, y, x + w, y + h), radius, radius, android.graphics.Path.Direction.CW) }
                    native.clipPath(clip)
                    native.drawBitmap(bitmap, null, RectF(x, y, x + w, y + h), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                    native.restoreToCount(save)
                    bitmap.recycle()
                }
                return
            }
        } catch (_: Exception) { }
    }
    val assetName = element.placeholderLabel
    if (context != null && !assetName.isNullOrBlank() && assetName.startsWith("images/")) {
        try {
            val bitmap = context.assets.open(assetName).use { BitmapFactory.decodeStream(it) }
            if (bitmap != null) {
                drawIntoCanvas { canvas ->
                    val native = canvas.nativeCanvas
                    val save = native.save()
                    val clip = android.graphics.Path().apply {
                        addRoundRect(RectF(x, y, x + w, y + h), element.cornerRadius * (w / 300f).coerceAtLeast(0.5f), element.cornerRadius * (w / 300f).coerceAtLeast(0.5f), android.graphics.Path.Direction.CW)
                    }
                    native.clipPath(clip)
                    val src = Rect(0, 0, bitmap.width, bitmap.height)
                    val dst = RectF(x, y, x + w, y + h)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    native.drawBitmap(bitmap, src, dst, paint)
                    native.restoreToCount(save)
                    bitmap.recycle()
                }
                return
            }
        } catch (_: Exception) { }
    }
    val corner = (element.cornerRadius * (w / 300f)).coerceAtLeast(8f)
    val cx = x + w / 2f
    val cy = y + h / 2f

    // 1. Rich studio photo backdrop gradient
    val baseColor = element.backgroundColor?.let { parseColorHex(it) } ?: Color(0xFF2E2452)
    val lightTop = Color(0xFF4C3882)
    val darkBottom = Color(0xFF191330)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(lightTop, baseColor, darkBottom),
            startY = y,
            endY = y + h
        ),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(corner, corner)
    )

    // 2. Soft studio spotlight effect in center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x38FFFFFF), Color(0x00FFFFFF)),
            center = Offset(cx, cy - h * 0.08f),
            radius = minOf(w, h) * 0.45f
        ),
        radius = minOf(w, h) * 0.45f,
        center = Offset(cx, cy - h * 0.08f)
    )

    // 3. Crisp border with high contrast
    val strokeColor = element.borderColor?.let { parseColorHex(it) } ?: Color(0x88A78BFA)
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(corner, corner),
        style = Stroke(width = 2.5f)
    )

    // 4. Sample product artwork simulation across 5 shop categories:
    // 0: کیف (Handbag / luxury shopping bag)
    // 1: لباس (T-shirt / fashion apparel)
    // 2: شیرینی (Cupcake / bakery dessert)
    // 3: لوازم دیجیتال (Modern smartphone)
    // 4: لوازم آرایشی (Cosmetic perfume bottle)
    val categoryIndex = Math.abs((element.id + (element.placeholderLabel ?: "")).hashCode()) % 5

    val defaultLabel = when (categoryIndex) {
        0 -> "کیف و اکسسوری"
        1 -> "پوشاک و مد"
        2 -> "کافه و قنادی"
        3 -> "دیجیتال و گجت"
        else -> "آرایشی و عطر"
    }
    val label = element.placeholderLabel ?: defaultLabel

    when (categoryIndex) {
        0 -> {
            // --- Category 0: کیف (Luxury Handbag) ---
            val artW = (w * 0.38f).coerceIn(28f, 100f)
            val artH = artW * 1.05f
            val artX = cx - artW / 2f
            val artY = cy - artH / 2f - h * 0.05f

            // Shadow
            drawOval(
                color = Color(0x55000000),
                topLeft = Offset(cx - artW * 0.42f, artY + artH - 2f),
                size = Size(artW * 0.84f, artH * 0.18f)
            )
            // Bag Body
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF472B6), Color(0xFF8B5CF6)),
                    start = Offset(artX, artY),
                    end = Offset(artX + artW, artY + artH)
                ),
                topLeft = Offset(artX, artY),
                size = Size(artW, artH),
                cornerRadius = CornerRadius(artW * 0.16f, artW * 0.16f)
            )
            // Highlight reflection
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x66FFFFFF), Color(0x05FFFFFF)),
                    startY = artY,
                    endY = artY + artH * 0.45f
                ),
                topLeft = Offset(artX + 3f, artY + 3f),
                size = Size(artW - 6f, artH * 0.42f),
                cornerRadius = CornerRadius(artW * 0.12f, artW * 0.12f)
            )
            // Handle arch
            val handleW = artW * 0.45f
            val handleH = artH * 0.32f
            drawRoundRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(cx - handleW / 2f, artY - handleH * 0.55f),
                size = Size(handleW, handleH),
                cornerRadius = CornerRadius(handleW / 2f, handleW / 2f),
                style = Stroke(width = 2.5f)
            )
            // Golden lock clasp
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = (artW * 0.08f).coerceAtLeast(3f),
                center = Offset(cx, artY + artH * 0.45f)
            )
        }
        1 -> {
            // --- Category 1: لباس (T-shirt / Fashion Apparel) ---
            val artW = (w * 0.42f).coerceIn(30f, 105f)
            val artH = artW * 0.95f
            val artX = cx - artW / 2f
            val artY = cy - artH / 2f - h * 0.05f

            // Shadow
            drawOval(
                color = Color(0x55000000),
                topLeft = Offset(cx - artW * 0.38f, artY + artH - 2f),
                size = Size(artW * 0.76f, artH * 0.18f)
            )
            // T-shirt Path
            val shirtPath = Path().apply {
                moveTo(cx - artW * 0.18f, artY)
                lineTo(artX, artY + artH * 0.28f)
                lineTo(artX + artW * 0.16f, artY + artH * 0.40f)
                lineTo(artX + artW * 0.20f, artY + artH * 0.32f)
                lineTo(artX + artW * 0.20f, artY + artH)
                lineTo(artX + artW * 0.80f, artY + artH)
                lineTo(artX + artW * 0.80f, artY + artH * 0.32f)
                lineTo(artX + artW * 0.84f, artY + artH * 0.40f)
                lineTo(artX + artW, artY + artH * 0.28f)
                lineTo(cx + artW * 0.18f, artY)
                close()
            }
            drawPath(
                path = shirtPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
                    start = Offset(artX, artY),
                    end = Offset(artX + artW, artY + artH)
                )
            )
            // Collar cutout
            drawArc(
                color = Color(0xFF1E1935),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - artW * 0.15f, artY - artH * 0.08f),
                size = Size(artW * 0.30f, artH * 0.22f)
            )
            // Chest fashion stripes
            drawRoundRect(
                color = Color(0x88FFFFFF),
                topLeft = Offset(cx - artW * 0.15f, artY + artH * 0.45f),
                size = Size(artW * 0.30f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = Color(0x66FEF08A),
                topLeft = Offset(cx - artW * 0.10f, artY + artH * 0.55f),
                size = Size(artW * 0.20f, 3f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
        }
        2 -> {
            // --- Category 2: شیرینی (Bakery Cupcake / Dessert) ---
            val artW = (w * 0.38f).coerceIn(28f, 100f)
            val artH = artW * 1.05f
            val artX = cx - artW / 2f
            val artY = cy - artH / 2f - h * 0.05f

            // Shadow
            drawOval(
                color = Color(0x55000000),
                topLeft = Offset(cx - artW * 0.35f, artY + artH - 2f),
                size = Size(artW * 0.70f, artH * 0.18f)
            )
            // Cupcake trapezoid cup
            val cupPath = Path().apply {
                moveTo(artX + artW * 0.15f, artY + artH * 0.45f)
                lineTo(artX + artW * 0.25f, artY + artH)
                lineTo(artX + artW * 0.75f, artY + artH)
                lineTo(artX + artW * 0.85f, artY + artH * 0.45f)
                close()
            }
            drawPath(
                path = cupPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFDE68A), Color(0xFFD97706)),
                    startY = artY + artH * 0.45f,
                    endY = artY + artH
                )
            )
            // Vertical pleat lines on cup
            for (p in 1..4) {
                val frac = p / 5f
                val topPx = artX + artW * 0.15f + frac * (artW * 0.70f)
                val botPx = artX + artW * 0.25f + frac * (artW * 0.50f)
                drawLine(
                    color = Color(0x6692400E),
                    start = Offset(topPx, artY + artH * 0.45f),
                    end = Offset(botPx, artY + artH),
                    strokeWidth = 1.5f
                )
            }
            // Swirled strawberry frosting mound
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDA4AF), Color(0xFFF43F5E)),
                    center = Offset(cx, artY + artH * 0.38f),
                    radius = artW * 0.42f
                ),
                topLeft = Offset(artX + artW * 0.08f, artY + artH * 0.22f),
                size = Size(artW * 0.84f, artH * 0.35f)
            )
            drawCircle(
                color = Color(0xFFFB7185),
                radius = artW * 0.22f,
                center = Offset(cx, artY + artH * 0.25f)
            )
            // Shiny cherry on top
            drawCircle(
                color = Color(0xFF991B1B),
                radius = (artW * 0.11f).coerceAtLeast(4f),
                center = Offset(cx, artY + artH * 0.12f)
            )
            // Cherry stem
            drawArc(
                color = Color(0xFF15803D),
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx, artY),
                size = Size(artW * 0.18f, artH * 0.15f),
                style = Stroke(width = 2f)
            )
        }
        3 -> {
            // --- Category 3: لوازم دیجیتال (Modern Smartphone) ---
            val artW = (w * 0.32f).coerceIn(26f, 85f)
            val artH = artW * 1.55f
            val artX = cx - artW / 2f
            val artY = cy - artH / 2f - h * 0.05f

            // Shadow
            drawOval(
                color = Color(0x55000000),
                topLeft = Offset(cx - artW * 0.42f, artY + artH - 2f),
                size = Size(artW * 0.84f, artH * 0.16f)
            )
            // Phone Chassis
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF334155), Color(0xFF0F172A)),
                    startY = artY,
                    endY = artY + artH
                ),
                topLeft = Offset(artX, artY),
                size = Size(artW, artH),
                cornerRadius = CornerRadius(artW * 0.16f, artW * 0.16f)
            )
            // Glowing Screen
            val pad = 3f
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF818CF8), Color(0xFFC084FC)),
                    start = Offset(artX + pad, artY + pad),
                    end = Offset(artX + artW - pad, artY + artH - pad)
                ),
                topLeft = Offset(artX + pad, artY + pad),
                size = Size(artW - pad * 2f, artH - pad * 2f),
                cornerRadius = CornerRadius(artW * 0.13f, artW * 0.13f)
            )
            // Camera dynamic island pill
            drawRoundRect(
                color = Color(0xFF020617),
                topLeft = Offset(cx - artW * 0.20f, artY + pad + 2f),
                size = Size(artW * 0.40f, 5f),
                cornerRadius = CornerRadius(2.5f, 2.5f)
            )
            // Home indicator bar
            drawRoundRect(
                color = Color(0xCCFFFFFF),
                topLeft = Offset(cx - artW * 0.18f, artY + artH - pad - 4f),
                size = Size(artW * 0.36f, 2.5f),
                cornerRadius = CornerRadius(1.25f, 1.25f)
            )
        }
        else -> {
            // --- Category 4: لوازم آرایشی (Cosmetic / Perfume Bottle) ---
            val artW = (w * 0.34f).coerceIn(26f, 90f)
            val artH = artW * 1.30f
            val artX = cx - artW / 2f
            val artY = cy - artH / 2f - h * 0.05f

            // Shadow
            drawOval(
                color = Color(0x55000000),
                topLeft = Offset(cx - artW * 0.40f, artY + artH - 2f),
                size = Size(artW * 0.80f, artH * 0.18f)
            )
            // Glass Bottle Body
            val bodyH = artH * 0.65f
            val bodyY = artY + artH * 0.35f
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFEC4899)),
                    start = Offset(artX, bodyY),
                    end = Offset(artX + artW, bodyY + bodyH)
                ),
                topLeft = Offset(artX, bodyY),
                size = Size(artW, bodyH),
                cornerRadius = CornerRadius(artW * 0.15f, artW * 0.15f)
            )
            // Glass bottle highlight facet
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x88FFFFFF), Color(0x05FFFFFF)),
                    startY = bodyY,
                    endY = bodyY + bodyH
                ),
                topLeft = Offset(artX + 3f, bodyY + 3f),
                size = Size(artW * 0.25f, bodyH - 6f),
                cornerRadius = CornerRadius(artW * 0.10f, artW * 0.10f)
            )
            // Golden atomizer neck
            drawRoundRect(
                color = Color(0xFFFDE047),
                topLeft = Offset(cx - artW * 0.22f, artY + artH * 0.22f),
                size = Size(artW * 0.44f, artH * 0.14f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Golden square cap
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFFEF08A), Color(0xFFD97706)),
                    startX = cx - artW * 0.16f,
                    endX = cx + artW * 0.16f
                ),
                topLeft = Offset(cx - artW * 0.16f, artY),
                size = Size(artW * 0.32f, artH * 0.24f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Perfume diamond emblem
            drawCircle(
                color = Color(0xCCFFFFFF),
                radius = (artW * 0.08f).coerceAtLeast(3f),
                center = Offset(cx, bodyY + bodyH * 0.45f)
            )
        }
    }

    // 5. Clean Persian sample label pill
    val pillH = (h * 0.11f).coerceIn(16f, 28f)
    val pillW = (w * 0.65f).coerceIn(60f, 150f)
    val pillY = cy + h * 0.36f

    drawRoundRect(
        color = Color(0xD91E1935),
        topLeft = Offset(cx - pillW / 2f, pillY),
        size = Size(pillW, pillH),
        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
    )
    drawRoundRect(
        color = Color(0x44FFFFFF),
        topLeft = Offset(cx - pillW / 2f, pillY),
        size = Size(pillW, pillH),
        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
        style = Stroke(width = 1f)
    )

    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            color = AndroidColor.WHITE
            textSize = (pillH * 0.55f).coerceIn(9f, 14f)
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val textY = pillY + pillH / 2f - (paint.descent() + paint.ascent()) / 2f
        drawText(label, cx, textY, paint)
    }
}

private fun DrawScope.drawTextElement(
    element: TemplateElement,
    rawText: String?,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    canvasW: Float
) {
    val text = rawText ?: return
    if (text.isBlank()) return

    if (element.backgroundColor != null) {
        val pillColor = parseColorHex(element.backgroundColor)
        val corner = element.cornerRadius * (w / 300f).coerceAtLeast(0.5f)
        drawRoundRect(
            color = pillColor,
            topLeft = Offset(x, y),
            size = Size(w, h),
            cornerRadius = CornerRadius(corner, corner)
        )
    }

    drawIntoCanvas { canvas ->
        val textPaint = TextPaint().apply {
            color = AndroidColor.parseColor(sanitizeHex(element.textColor))
            val scale = canvasW / 360f
            textSize = (element.fontSize * scale).coerceAtLeast(9f)
            typeface = if (element.fontWeight.uppercase() in listOf("BOLD", "BLACK", "EXTRA_BOLD")) {
                Typeface.DEFAULT_BOLD
            } else {
                Typeface.DEFAULT
            }
            isAntiAlias = true
            if (element.hasShadow) {
                setShadowLayer(4f, 2f, 2f, AndroidColor.parseColor(sanitizeHex(element.shadowColor)))
            }
        }

        val alignment = when (element.textAlign.uppercase()) {
            "RIGHT" -> Layout.Alignment.ALIGN_OPPOSITE
            "LEFT" -> Layout.Alignment.ALIGN_NORMAL
            else -> Layout.Alignment.ALIGN_CENTER
        }

        val availableWidth = (w - 8f).toInt().coerceAtLeast(10)
        
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, availableWidth)
                .setAlignment(alignment)
                .setLineSpacing(2f, 1f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, textPaint, availableWidth, alignment, 1.0f, 2.0f, false)
        }

        val layoutH = staticLayout.height
        val startY = y + (h - layoutH) / 2f
        val startX = x + 4f

        canvas.nativeCanvas.save()
        canvas.nativeCanvas.translate(startX, startY)
        staticLayout.draw(canvas.nativeCanvas)
        canvas.nativeCanvas.restore()
    }
}

private fun DrawScope.drawBadgeElement(
    element: TemplateElement,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    canvasW: Float
) {
    val bgColor = element.backgroundColor?.let { parseColorHex(it) } ?: Color(0xFFE11D48)
    val corner = (h / 2f).coerceAtLeast(8f)

    drawRoundRect(
        color = bgColor,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(corner, corner)
    )

    element.text?.let { text ->
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                color = AndroidColor.parseColor(sanitizeHex(element.textColor))
                val scale = canvasW / 360f
                textSize = (element.fontSize * scale).coerceIn(9f, 24f)
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val textBounds = Rect()
            paint.getTextBounds(text, 0, text.length, textBounds)
            val cx = x + w / 2f
            val cy = y + h / 2f + textBounds.height() / 2f - 1f
            drawText(text, cx, cy, paint)
        }
    }
}

private fun DrawScope.drawBrandLogoElement(
    element: TemplateElement,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    brandProfile: BrandProfile
) {
    val radius = minOf(w, h) / 2f
    val cx = x + w / 2f
    val cy = y + h / 2f

    val brandInitial = if (brandProfile.brandName.isNotBlank()) {
        brandProfile.brandName.take(1)
    } else {
        "ب"
    }

    drawCircle(
        color = Color(0x33FFFFFF),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = parseColorHex(brandProfile.primaryColorHex),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 2.5f)
    )

    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            color = AndroidColor.WHITE
            textSize = radius * 0.9f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText(brandInitial, cx, cy + radius * 0.35f, paint)
    }
}

private fun parseColorHex(hex: String): Color {
    return try {
        Color(AndroidColor.parseColor(sanitizeHex(hex)))
    } catch (e: Exception) {
        Color(0xFF7C3AED)
    }
}

private fun sanitizeHex(hex: String): String {
    val clean = hex.trim()
    return if (clean.startsWith("#")) {
        if (clean.length == 7 || clean.length == 9) clean
        else if (clean.length == 4) {
            "#" + clean[1] + clean[1] + clean[2] + clean[2] + clean[3] + clean[3]
        } else "#7C3AED"
    } else {
        "#$clean"
    }
}
