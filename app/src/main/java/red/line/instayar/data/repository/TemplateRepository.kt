package red.line.instayar.data.repository

import android.content.Context
import red.line.instayar.data.model.ElementType
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateBackground
import red.line.instayar.data.model.TemplateElement
import red.line.instayar.data.model.TemplateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class TemplateRepository(private val context: Context) {

    private val _templates = MutableStateFlow<List<Template>>(emptyList())
    val templates: StateFlow<List<Template>> = _templates.asStateFlow()

    suspend fun loadTemplates(): List<Template> = withContext(Dispatchers.IO) {
        val loadedList = mutableListOf<Template>()
        try {
            val assetManager = context.assets
            val files = assetManager.list("templates") ?: emptyArray()
            
            for (fileName in files) {
                if (fileName.endsWith(".json")) {
                    try {
                        val inputStream = assetManager.open("templates/$fileName")
                        val content = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                        val template = parseTemplateJson(content)
                        loadedList.add(template)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort: Free and VIP nicely mixed, or by ID
        val result = loadedList.sortedBy { it.id }
        _templates.value = result
        result
    }

    private fun parseTemplateJson(jsonString: String): Template {
        val json = JSONObject(jsonString)
        val id = json.optString("id", "tpl_${System.currentTimeMillis()}")
        val title = json.optString("title", "قالب بدون نام")
        val category = json.optString("category", "عمومی")
        val formatStr = json.optString("format", "STORY")
        val format = TemplateFormat.fromString(formatStr)
        val isVip = json.optBoolean("isVip", false)

        val tags = mutableListOf<String>()
        val tagsArray = json.optJSONArray("tags")
        if (tagsArray != null) {
            for (i in 0 until tagsArray.length()) {
                tags.add(tagsArray.getString(i))
            }
        }

        val bgJson = json.optJSONObject("background")
        val bgType = bgJson?.optString("type", "GRADIENT") ?: "GRADIENT"
        val bgColor = bgJson?.optString("color", "#1A102F") ?: "#1A102F"
        val bgAngle = bgJson?.optDouble("angle", 45.0)?.toFloat() ?: 45f

        val gradientColors = mutableListOf<String>()
        val gradientArray = bgJson?.optJSONArray("gradientColors")
        if (gradientArray != null && gradientArray.length() > 0) {
            for (i in 0 until gradientArray.length()) {
                gradientColors.add(gradientArray.getString(i))
            }
        } else {
            gradientColors.add("#2E0854")
            gradientColors.add("#0C071E")
        }

        val background = TemplateBackground(
            type = bgType,
            color = bgColor,
            gradientColors = gradientColors,
            angle = bgAngle
        )

        val elements = mutableListOf<TemplateElement>()
        val elementsArray = json.optJSONArray("elements") ?: JSONArray()
        for (i in 0 until elementsArray.length()) {
            val elObj = elementsArray.getJSONObject(i)
            val elId = elObj.optString("id", "el_$i")
            val typeStr = elObj.optString("type", "TEXT")
            val type = ElementType.fromString(typeStr)

            val x = elObj.optDouble("x", 0.0).toFloat()
            val y = elObj.optDouble("y", 0.0).toFloat()
            val width = elObj.optDouble("width", 0.5).toFloat()
            val height = elObj.optDouble("height", 0.2).toFloat()
            val rotation = elObj.optDouble("rotation", 0.0).toFloat()

            val text = if (elObj.has("text")) elObj.optString("text") else null
            val fontSize = elObj.optDouble("fontSize", 16.0).toFloat()
            val fontWeight = elObj.optString("fontWeight", "BOLD")
            val textColor = elObj.optString("textColor", "#FFFFFF")
            val textAlign = elObj.optString("textAlign", "CENTER")
            val backgroundColor = if (elObj.has("backgroundColor")) elObj.optString("backgroundColor") else null
            val shapeType = elObj.optString("shapeType", "ROUNDED_RECT")
            val cornerRadius = elObj.optDouble("cornerRadius", 16.0).toFloat()
            val borderColor = if (elObj.has("borderColor")) elObj.optString("borderColor") else null
            val borderWidth = elObj.optDouble("borderWidth", 0.0).toFloat()
            val badgeIcon = if (elObj.has("badgeIcon")) elObj.optString("badgeIcon") else null
            val placeholderLabel = if (elObj.has("placeholderLabel")) elObj.optString("placeholderLabel") else null
            val hasShadow = elObj.optBoolean("hasShadow", false)
            val shadowColor = elObj.optString("shadowColor", "#80000000")

            elements.add(
                TemplateElement(
                    id = elId,
                    type = type,
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    rotation = rotation,
                    text = text,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    textColor = textColor,
                    textAlign = textAlign,
                    backgroundColor = backgroundColor,
                    shapeType = shapeType,
                    cornerRadius = cornerRadius,
                    borderColor = borderColor,
                    borderWidth = borderWidth,
                    badgeIcon = badgeIcon,
                    placeholderLabel = placeholderLabel,
                    hasShadow = hasShadow,
                    shadowColor = shadowColor
                )
            )
        }

        return Template(
            id = id,
            title = title,
            category = category,
            format = format,
            isVip = isVip,
            tags = tags,
            background = background,
            elements = elements
        )
    }
}
