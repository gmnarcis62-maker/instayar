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
        val fileName = "Ghalibino_${System.currentTimeMillis()}.png"
        var uri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Ghalibino")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "Ghalibino").apply { if (!exists()) mkdirs() }
                val file = File(appDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                uri = Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        uri
    }

    suspend fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        preferInstagram: Boolean = true
    ): Intent? = withContext(Dispatchers.IO) {
        try {
            val cacheImagesDir = File(context.cacheDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val sharedFile = File(cacheImagesDir, "shared_story_${System.currentTimeMillis()}.png")
            FileOutputStream(sharedFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
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
