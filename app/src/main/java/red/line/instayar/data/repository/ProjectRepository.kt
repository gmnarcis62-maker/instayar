package red.line.instayar.data.repository

import android.content.Context
import red.line.instayar.data.local.AppDatabase
import red.line.instayar.data.local.ProjectEntity
import red.line.instayar.data.model.ElementType
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateBackground
import red.line.instayar.data.model.TemplateElement
import red.line.instayar.data.model.TemplateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ProjectRepository(context: Context) {
    private val dao = AppDatabase.getDatabase(context).projectDao()

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    suspend fun saveProject(template: Template): String = withContext(Dispatchers.IO) {
        val json = serializeTemplateToJson(template)
        val entity = ProjectEntity(
            id = template.id,
            title = template.title,
            templateId = template.id,
            formatName = template.format.name,
            templateJson = json,
            lastModified = System.currentTimeMillis()
        )
        dao.insertProject(entity)
        entity.id
    }

    suspend fun getProjectTemplate(id: String): Template? = withContext(Dispatchers.IO) {
        val entity = dao.getProjectById(id) ?: return@withContext null
        parseTemplateJson(entity.templateJson)
    }

    suspend fun duplicateProject(id: String): String? = withContext(Dispatchers.IO) {
        val existing = dao.getProjectById(id) ?: return@withContext null
        val newId = "proj_${System.currentTimeMillis()}"
        val tpl = parseTemplateJson(existing.templateJson)
        val duplicatedTpl = tpl.copy(id = newId, title = "${tpl.title} (کپی)")
        val json = serializeTemplateToJson(duplicatedTpl)
        val newEntity = existing.copy(
            id = newId,
            title = "${existing.title} (کپی)",
            templateJson = json,
            lastModified = System.currentTimeMillis()
        )
        dao.insertProject(newEntity)
        newId
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        dao.deleteProjectById(id)
    }

    fun serializeTemplateToJson(template: Template): String {
        val root = JSONObject()
        root.put("id", template.id)
        root.put("title", template.title)
        root.put("category", template.category)
        root.put("format", template.format.name)
        root.put("isVip", template.isVip)

        val tagsArray = JSONArray()
        template.tags.forEach { tagsArray.put(it) }
        root.put("tags", tagsArray)

        val bgObj = JSONObject()
        bgObj.put("type", template.background.type)
        bgObj.put("color", template.background.color)
        bgObj.put("angle", template.background.angle)
        val gradArray = JSONArray()
        template.background.gradientColors.forEach { gradArray.put(it) }
        bgObj.put("gradientColors", gradArray)
        root.put("background", bgObj)

        val elArray = JSONArray()
        template.elements.forEach { el ->
            val obj = JSONObject()
            obj.put("id", el.id)
            obj.put("type", el.type.name)
            obj.put("x", el.x)
            obj.put("y", el.y)
            obj.put("width", el.width)
            obj.put("height", el.height)
            obj.put("rotation", el.rotation)
            el.text?.let { obj.put("text", it) }
            obj.put("fontSize", el.fontSize)
            obj.put("fontWeight", el.fontWeight)
            obj.put("textColor", el.textColor)
            obj.put("textAlign", el.textAlign)
            el.backgroundColor?.let { obj.put("backgroundColor", it) }
            obj.put("shapeType", el.shapeType)
            obj.put("cornerRadius", el.cornerRadius)
            el.borderColor?.let { obj.put("borderColor", it) }
            obj.put("borderWidth", el.borderWidth)
            el.badgeIcon?.let { obj.put("badgeIcon", it) }
            el.placeholderLabel?.let { obj.put("placeholderLabel", it) }
            obj.put("hasShadow", el.hasShadow)
            obj.put("shadowColor", el.shadowColor)
            elArray.put(obj)
        }
        root.put("elements", elArray)

        return root.toString()
    }

    fun parseTemplateJson(jsonString: String): Template {
        val json = JSONObject(jsonString)
        val id = json.optString("id", "tpl_${System.currentTimeMillis()}")
        val title = json.optString("title", "پروژه من")
        val category = json.optString("category", "پروژه‌های من")
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
            elements.add(
                TemplateElement(
                    id = elObj.optString("id", "el_$i"),
                    type = ElementType.fromString(elObj.optString("type", "TEXT")),
                    x = elObj.optDouble("x", 0.0).toFloat(),
                    y = elObj.optDouble("y", 0.0).toFloat(),
                    width = elObj.optDouble("width", 0.5).toFloat(),
                    height = elObj.optDouble("height", 0.2).toFloat(),
                    rotation = elObj.optDouble("rotation", 0.0).toFloat(),
                    text = if (elObj.has("text")) elObj.optString("text") else null,
                    fontSize = elObj.optDouble("fontSize", 16.0).toFloat(),
                    fontWeight = elObj.optString("fontWeight", "BOLD"),
                    textColor = elObj.optString("textColor", "#FFFFFF"),
                    textAlign = elObj.optString("textAlign", "CENTER"),
                    backgroundColor = if (elObj.has("backgroundColor")) elObj.optString("backgroundColor") else null,
                    shapeType = elObj.optString("shapeType", "ROUNDED_RECT"),
                    cornerRadius = elObj.optDouble("cornerRadius", 16.0).toFloat(),
                    borderColor = if (elObj.has("borderColor")) elObj.optString("borderColor") else null,
                    borderWidth = elObj.optDouble("borderWidth", 0.0).toFloat(),
                    badgeIcon = if (elObj.has("badgeIcon")) elObj.optString("badgeIcon") else null,
                    placeholderLabel = if (elObj.has("placeholderLabel")) elObj.optString("placeholderLabel") else null,
                    hasShadow = elObj.optBoolean("hasShadow", false),
                    shadowColor = elObj.optString("shadowColor", "#80000000")
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
