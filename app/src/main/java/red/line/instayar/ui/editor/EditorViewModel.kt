package red.line.instayar.ui.editor

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import red.line.instayar.data.model.BrandProfile
import red.line.instayar.data.model.ElementType
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateBackground
import red.line.instayar.data.model.TemplateElement
import red.line.instayar.data.repository.ProjectRepository
import red.line.instayar.data.repository.TemplateRepository
import red.line.instayar.data.repository.UserPreferences
import red.line.instayar.util.ImageExportUtil
import red.line.instayar.security.AppIntegrityChecker
import red.line.instayar.security.IntegrityResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val currentTemplate: Template? = null,
    val selectedElementId: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val activeToolTab: EditorToolTab = EditorToolTab.TEXT,
    val brandProfile: BrandProfile = BrandProfile(),
    val isVipUser: Boolean = false,
    val showExportDialog: Boolean = false,
    val saveMessage: String? = null,
    val integrityError: String? = null
)

enum class EditorToolTab(val titleFa: String) {
    TEXT("متن"),
    STYLE("استایل و رنگ"),
    BACKGROUND("پس‌زمینه"),
    ELEMENTS("افزودن المان"),
    LAYERS("لایه‌ها")
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TemplateRepository(application)
    private val projectRepository = ProjectRepository(application)
    private val preferences = UserPreferences(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // History stacks for Undo / Redo
    private val history = mutableListOf<Template>()
    private var historyIndex = -1

    init {
        viewModelScope.launch {
            preferences.brandProfile.collect { profile ->
                _uiState.update { it.copy(brandProfile = profile) }
            }
        }
        viewModelScope.launch {
            preferences.isVipUser.collect { isVip ->
                _uiState.update { it.copy(isVipUser = isVip) }
            }
        }
    }

    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // بررسی ضد دستکاری برنامه
            val integrity = AppIntegrityChecker.verifyAppIntegrity(getApplication())
            if (integrity is IntegrityResult.Compromised) {
                _uiState.update { it.copy(isLoading = false, integrityError = integrity.reasonFa) }
                return@launch
            }

            // Check if it's already a saved project in Room
            var template = projectRepository.getProjectTemplate(templateId)
            if (template == null) {
                // If not in Room, load from Assets
                val templates = repository.loadTemplates()
                template = templates.find { it.id == templateId } ?: templates.firstOrNull()
            }

            if (template != null) {
                history.clear()
                history.add(template)
                historyIndex = 0
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentTemplate = template,
                        selectedElementId = template.elements.firstOrNull()?.id,
                        canUndo = false,
                        canRedo = false
                    )
                }
                // Initial auto-save into Room
                projectRepository.saveProject(template)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun pushHistory(newTemplate: Template) {
        while (history.size > historyIndex + 1) {
            history.removeAt(history.size - 1)
        }
        history.add(newTemplate)
        historyIndex = history.size - 1
        _uiState.update {
            it.copy(
                currentTemplate = newTemplate,
                canUndo = historyIndex > 0,
                canRedo = false
            )
        }
        // Auto-save to Room in background
        viewModelScope.launch {
            projectRepository.saveProject(newTemplate)
        }
    }

    fun undo() {
        if (historyIndex > 0) {
            historyIndex--
            val previousTemplate = history[historyIndex]
            _uiState.update {
                it.copy(
                    currentTemplate = previousTemplate,
                    canUndo = historyIndex > 0,
                    canRedo = historyIndex < history.size - 1
                )
            }
            viewModelScope.launch {
                projectRepository.saveProject(previousTemplate)
            }
        }
    }

    fun redo() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            val nextTemplate = history[historyIndex]
            _uiState.update {
                it.copy(
                    currentTemplate = nextTemplate,
                    canUndo = historyIndex > 0,
                    canRedo = historyIndex < history.size - 1
                )
            }
            viewModelScope.launch {
                projectRepository.saveProject(nextTemplate)
            }
        }
    }

    fun selectElement(elementId: String?) {
        _uiState.update { it.copy(selectedElementId = elementId) }
    }

    fun setActiveToolTab(tab: EditorToolTab) {
        _uiState.update { it.copy(activeToolTab = tab) }
    }

    fun updateElementPosition(dxRel: Float, dyRel: Float) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updatedElements = tpl.elements.map { el ->
            if (el.id == id) {
                el.copy(
                    x = (el.x + dxRel).coerceIn(-0.3f, 1.0f),
                    y = (el.y + dyRel).coerceIn(-0.3f, 1.0f)
                )
            } else el
        }
        val newTpl = tpl.copy(elements = updatedElements)
        _uiState.update { it.copy(currentTemplate = newTpl) }
    }

    fun commitTransformGesture() {
        val tpl = _uiState.value.currentTemplate ?: return
        pushHistory(tpl)
    }

    fun updateElementTransform(scaleFactor: Float, deltaRotation: Float) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updatedElements = tpl.elements.map { el ->
            if (el.id == id) {
                val newW = (el.width * scaleFactor).coerceIn(0.08f, 1.2f)
                val newH = (el.height * scaleFactor).coerceIn(0.02f, 1.2f)
                val newRot = (el.rotation + deltaRotation) % 360f
                el.copy(
                    width = newW,
                    height = newH,
                    rotation = newRot
                )
            } else el
        }
        val newTpl = tpl.copy(elements = updatedElements)
        _uiState.update { it.copy(currentTemplate = newTpl) }
    }

    fun updateElementText(newText: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updated = tpl.elements.map { el ->
            if (el.id == id) el.copy(text = newText) else el
        }
        pushHistory(tpl.copy(elements = updated))
    }

    fun updateElementFontSize(newSize: Float) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updated = tpl.elements.map { el ->
            if (el.id == id) el.copy(fontSize = newSize.coerceIn(10f, 60f)) else el
        }
        pushHistory(tpl.copy(elements = updated))
    }

    fun updateElementTextColor(colorHex: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updated = tpl.elements.map { el ->
            if (el.id == id) el.copy(textColor = colorHex) else el
        }
        pushHistory(tpl.copy(elements = updated))
    }

    fun updateElementTextAlign(align: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updated = tpl.elements.map { el ->
            if (el.id == id) el.copy(textAlign = align) else el
        }
        pushHistory(tpl.copy(elements = updated))
    }

    fun updateElementBackgroundColor(colorHex: String?) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updated = tpl.elements.map { el ->
            if (el.id == id) el.copy(backgroundColor = colorHex) else el
        }
        pushHistory(tpl.copy(elements = updated))
    }

    fun updateElementBorder(borderColorHex: String?, borderWidth: Float) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updated = tpl.elements.map { el ->
            if (el.id == id) el.copy(borderColor = borderColorHex, borderWidth = borderWidth) else el
        }
        pushHistory(tpl.copy(elements = updated))
    }

    fun updateElementCornerRadius(radius: Float) {
        val tpl = _uiState.value.currentTemplate ?: return
        val id = _uiState.value.selectedElementId ?: return

        val updated = tpl.elements.map { el ->
            if (el.id == id) el.copy(cornerRadius = radius) else el
        }
        pushHistory(tpl.copy(elements = updated))
    }

    fun updateBackgroundColors(primaryColor: String, secondaryColor: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val newBg = tpl.background.copy(
            gradientColors = listOf(primaryColor, secondaryColor)
        )
        pushHistory(tpl.copy(background = newBg))
    }

    fun bringElementForward(elementId: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val list = tpl.elements.toMutableList()
        val index = list.indexOfFirst { it.id == elementId }
        if (index in 0 until list.size - 1) {
            val el = list.removeAt(index)
            list.add(index + 1, el)
            pushHistory(tpl.copy(elements = list))
        }
    }

    fun sendElementBackward(elementId: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val list = tpl.elements.toMutableList()
        val index = list.indexOfFirst { it.id == elementId }
        if (index > 0) {
            val el = list.removeAt(index)
            list.add(index - 1, el)
            pushHistory(tpl.copy(elements = list))
        }
    }

    fun duplicateElement(elementId: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val el = tpl.elements.find { it.id == elementId } ?: return
        val copy = el.copy(
            id = "el_${System.currentTimeMillis()}",
            x = (el.x + 0.05f).coerceAtMost(0.8f),
            y = (el.y + 0.05f).coerceAtMost(0.8f)
        )
        val list = tpl.elements.toMutableList().apply { add(copy) }
        pushHistory(tpl.copy(elements = list))
        selectElement(copy.id)
    }

    fun deleteElement(elementId: String) {
        val tpl = _uiState.value.currentTemplate ?: return
        val list = tpl.elements.filter { it.id != elementId }
        pushHistory(tpl.copy(elements = list))
        _uiState.update {
            it.copy(selectedElementId = list.lastOrNull()?.id)
        }
    }

    fun addNewElement(type: ElementType, defaultText: String? = null) {
        val tpl = _uiState.value.currentTemplate ?: return
        val newId = "el_${System.currentTimeMillis()}"
        val newEl = when (type) {
            ElementType.TEXT -> TemplateElement(
                id = newId,
                type = ElementType.TEXT,
                x = 0.2f,
                y = 0.45f,
                width = 0.6f,
                height = 0.08f,
                text = defaultText ?: "متن جدید شما",
                fontSize = 20f,
                textColor = "#FFFFFF",
                fontWeight = "BOLD",
                textAlign = "CENTER"
            )
            ElementType.BADGE -> TemplateElement(
                id = newId,
                type = ElementType.BADGE,
                x = 0.3f,
                y = 0.45f,
                width = 0.4f,
                height = 0.05f,
                text = defaultText ?: "🔥 آفر ویژه",
                fontSize = 12f,
                textColor = "#FFFFFF",
                backgroundColor = "#E11D48"
            )
            ElementType.SHAPE -> TemplateElement(
                id = newId,
                type = ElementType.SHAPE,
                x = 0.25f,
                y = 0.45f,
                width = 0.5f,
                height = 0.09f,
                shapeType = "ROUNDED_RECT",
                cornerRadius = 16f,
                backgroundColor = "#7C3AED",
                borderColor = "#C084FC",
                borderWidth = 1.5f
            )
            ElementType.IMAGE_PLACEHOLDER -> TemplateElement(
                id = newId,
                type = ElementType.IMAGE_PLACEHOLDER,
                x = 0.15f,
                y = 0.3f,
                width = 0.7f,
                height = 0.4f,
                cornerRadius = 20f,
                backgroundColor = "#334155",
                borderColor = "#38BDF8",
                borderWidth = 2f,
                placeholderLabel = "تصویر محصول"
            )
            else -> TemplateElement(
                id = newId,
                type = ElementType.TEXT,
                x = 0.2f,
                y = 0.45f,
                width = 0.6f,
                height = 0.08f,
                text = "المان جدید",
                fontSize = 16f,
                textColor = "#FFFFFF"
            )
        }
        val list = tpl.elements.toMutableList().apply { add(newEl) }
        pushHistory(tpl.copy(elements = list))
        selectElement(newId)
    }

    fun exportToGallery(context: Context, onComplete: (Boolean) -> Unit) {
        val integrity = AppIntegrityChecker.verifyAppIntegrity(context)
        if (integrity is IntegrityResult.Compromised) {
            showSaveFeedback(integrity.reasonFa)
            onComplete(false)
            return
        }

        val tpl = _uiState.value.currentTemplate ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val isVip = _uiState.value.isVipUser
            val bitmap = ImageExportUtil.renderTemplateToBitmap(tpl, _uiState.value.brandProfile, isVip, getApplication<Application>().applicationContext)
            val uri = ImageExportUtil.saveBitmapToGallery(context, bitmap, tpl.title)
            _uiState.update { it.copy(isExporting = false) }

            if (uri != null) {
                projectRepository.saveProject(tpl)
                showSaveFeedback("طرح با موفقیت و بالاترین کیفیت در گالری ذخیره شد! 🎉")
                onComplete(true)
            } else {
                showSaveFeedback("خطا در ذخیره‌سازی تصویر!")
                onComplete(false)
            }
        }
    }

    fun shareToInstagram(context: Context) {
        val integrity = AppIntegrityChecker.verifyAppIntegrity(context)
        if (integrity is IntegrityResult.Compromised) {
            showSaveFeedback(integrity.reasonFa)
            return
        }

        val tpl = _uiState.value.currentTemplate ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val isVip = _uiState.value.isVipUser
            val bitmap = ImageExportUtil.renderTemplateToBitmap(tpl, _uiState.value.brandProfile, isVip, getApplication<Application>().applicationContext)
            val intent = ImageExportUtil.shareBitmap(context, bitmap, preferInstagram = true)
            _uiState.update { it.copy(isExporting = false) }

            if (intent != null) {
                context.startActivity(intent)
            } else {
                showSaveFeedback("خطا در آماده‌سازی اشتراک‌گذاری!")
            }
        }
    }

    fun openExportDialog() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun closeExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun showSaveFeedback(message: String) {
        _uiState.update { it.copy(saveMessage = message) }
    }

    fun dismissSaveFeedback() {
        _uiState.update { it.copy(saveMessage = null) }
    }
}
