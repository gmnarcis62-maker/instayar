package red.line.instayar.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import red.line.instayar.data.model.BrandProfile
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateFormat
import red.line.instayar.ui.components.drawTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExportUtil {

    suspend fun renderTemplateToBitmap(
        template: Template,
        brandProfile: BrandProfile,
        isVip: Boolean,
        context: Context? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        val (targetW, targetH) = when (template.format) {
            TemplateFormat.STORY -> Pair(1080, 1920)
            TemplateFormat.POST_PORTRAIT -> Pair(1080, 1350)
            TemplateFormat.POST_SQUARE -> Pair(1080, 1080)
            TemplateFormat.HIGHLIGHT -> Pair(1080, 1080)
        }

        val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val androidCanvas = android.graphics.Canvas(bitmap)
        val composeCanvas = Canvas(androidCanvas)

        val drawScope = CanvasDrawScope()
        drawScope.draw(
            density = Density(3f),
            layoutDirection = LayoutDirection.Rtl,
            canvas = composeCanvas,
            size = Size(targetW.toFloat(), targetH.toFloat())
        ) {
            drawTemplate(
                template = template,
                canvasSize = size,
                brandProfile = brandProfile,
                context = context
            )
        }

        // Draw discreet luxury watermark for Free users
        if (!isVip) {
            drawWatermark(androidCanvas, targetW, targetH)
        }

        bitmap
    }

    private fun drawWatermark(canvas: android.graphics.Canvas, w: Int, h: Int) {
        val watermarkText = "ساخته شده با قالبینو ✨"
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val textWidth = paint.measureText(watermarkText)
        val pillWidth = textWidth + 60f
        val pillHeight = 56f
        val pillX = (w - pillWidth) / 2f
        val pillY = h - 90f

        // Draw translucent dark background pill
        val bgPaint = Paint().apply {
            color = android.graphics.Color.argb(190, 18, 14, 34)
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = android.graphics.Color.argb(90, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        val rect = RectF(pillX, pillY, pillX + pillWidth, pillY + pillHeight)
        canvas.drawRoundRect(rect, 28f, 28f, bgPaint)
        canvas.drawRoundRect(rect, 28f, 28f, borderPaint)

        // Draw Text inside pill
        val textY = pillY + pillHeight / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(watermarkText, w / 2f, textY, paint)
    }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String
    ): Uri? = withContext(Dispatchers.IO) {
        val safeTitle = title.trim().replace(Regex("[^\\p{L}\\p{N}._ -]"), "_").take(60).ifBlank { "طرح_قالبینو" }
        val fileName = "${safeTitle}_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver
        var uri: Uri? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Ghalibino")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("امکان ساخت فایل در گالری وجود ندارد")
                val stream = resolver.openOutputStream(uri, "w")
                    ?: throw IllegalStateException("امکان نوشتن فایل وجود ندارد")
                stream.use { out ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        throw IllegalStateException("تبدیل تصویر به فایل ناموفق بود")
                    }
                    out.flush()
                }
                val ready = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                if (resolver.update(uri, ready, null, null) <= 0) throw IllegalStateException("ثبت نهایی تصویر ناموفق بود")
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "Ghalibino")
                if (!appDir.exists() && !appDir.mkdirs()) throw IllegalStateException("ساخت پوشه تصاویر ممکن نشد")
                val file = File(appDir, fileName)
                FileOutputStream(file).use { out ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) throw IllegalStateException("تبدیل تصویر ناموفق بود")
                    out.flush()
                }
                android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
                uri = Uri.fromFile(file)
            }
            uri
        } catch (e: Exception) {
            uri?.let { try { resolver.delete(it, null, null) } catch (_: Exception) {} }
            null
        }
    }

    suspend fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        preferInstagram: Boolean = true
    ): Intent? = withContext(Dispatchers.IO) {
        try {
            val cacheImagesDir = File(context.cacheDir, "images").apply {
                if (!exists() && !mkdirs()) throw IllegalStateException("ساخت پوشه موقت اشتراک‌گذاری ناموفق بود")
            }
            val sharedFile = File(cacheImagesDir, "shared_story_${System.currentTimeMillis()}.png")
            FileOutputStream(sharedFile).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    throw IllegalStateException("ساخت فایل تصویر برای اشتراک‌گذاری ناموفق بود")
                }
                out.flush()
            }

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sharedFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (preferInstagram) {
                // Check if Instagram is installed
                val pm = context.packageManager
                val isInstagramInstalled = try {
                    pm.getPackageInfo("com.instagram.android", 0)
                    true
                } catch (e: Exception) {
                    false
                }

                if (isInstagramInstalled) {
                    shareIntent.setPackage("com.instagram.android")
                    return@withContext shareIntent
                }
            }

            Intent.createChooser(shareIntent, "اشتراک‌گذاری طرح با:")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
