package red.line.instayar.ui.home

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateFormat
import red.line.instayar.data.repository.TemplateRepository
import red.line.instayar.data.repository.UserPreferences
import red.line.instayar.billing.BillingResult
import red.line.instayar.billing.MyketBillingManager
import red.line.instayar.security.AppIntegrityChecker
import red.line.instayar.security.IntegrityResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val allTemplates: List<Template> = emptyList(),
    val filteredTemplates: List<Template> = emptyList(),
    val selectedFormatFilter: FormatFilter = FormatFilter.ALL,
    val selectedCategory: String = "همه",
    val searchQuery: String = "",
    val isVipUser: Boolean = false,
    val showVipModal: Boolean = false,
    val selectedTemplateForDetail: Template? = null,
    val billingMessage: String? = null,
    val integrityWarning: String? = null
)

enum class FormatFilter(val titleFa: String) {
    ALL("همه"),
    STORY("استوری"),
    POST("پست"),
    HIGHLIGHT("هایلایت")
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TemplateRepository(application)
    private val preferences = UserPreferences(application)
    val billingManager = MyketBillingManager(application, preferences)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // App integrity verification on initialization
        when (val result = AppIntegrityChecker.verifyAppIntegrity(application)) {
            is IntegrityResult.Compromised -> {
                _uiState.update { it.copy(integrityWarning = result.reasonFa) }
            }
            IntegrityResult.Valid -> {}
        }

        viewModelScope.launch {
            preferences.isVipUser.collect { isVip ->
                _uiState.update { it.copy(isVipUser = isVip) }
            }
        }

        // Verify genuine purchases with Myket on startup
        billingManager.checkAndRestorePurchasesOnStartup { isPurchased ->
            _uiState.update { it.copy(isVipUser = isPurchased) }
        }

        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val loaded = repository.loadTemplates()
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    allTemplates = loaded,
                    filteredTemplates = filterTemplates(
                        templates = loaded,
                        format = current.selectedFormatFilter,
                        category = current.selectedCategory,
                        query = current.searchQuery
                    )
                )
            }
        }
    }

    fun onFormatFilterSelected(filter: FormatFilter) {
        _uiState.update { current ->
            current.copy(
                selectedFormatFilter = filter,
                filteredTemplates = filterTemplates(
                    templates = current.allTemplates,
                    format = filter,
                    category = current.selectedCategory,
                    query = current.searchQuery
                )
            )
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                filteredTemplates = filterTemplates(
                    templates = current.allTemplates,
                    format = current.selectedFormatFilter,
                    category = category,
                    query = current.searchQuery
                )
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredTemplates = filterTemplates(
                    templates = current.allTemplates,
                    format = current.selectedFormatFilter,
                    category = current.selectedCategory,
                    query = query
                )
            )
        }
    }

    fun openVipModal() {
        val integrity = AppIntegrityChecker.verifyAppIntegrity(getApplication())
        if (integrity is IntegrityResult.Compromised) {
            _uiState.update { it.copy(integrityWarning = integrity.reasonFa, showVipModal = false) }
            return
        }
        _uiState.update { it.copy(showVipModal = true) }
    }

    fun closeVipModal() {
        _uiState.update { it.copy(showVipModal = false) }
    }

    fun onTemplateClicked(template: Template) {
        if (template.isVip) {
            val integrity = AppIntegrityChecker.verifyAppIntegrity(getApplication())
            if (integrity is IntegrityResult.Compromised) {
                _uiState.update { it.copy(integrityWarning = integrity.reasonFa) }
                return
            }
        }
        _uiState.update { it.copy(selectedTemplateForDetail = template) }
    }

    fun closeTemplateDetail() {
        _uiState.update { it.copy(selectedTemplateForDetail = null) }
    }

    fun purchasePlan(activity: Activity?, sku: String, onResult: (BillingResult) -> Unit) {
        val integrity = AppIntegrityChecker.verifyAppIntegrity(getApplication())
        if (integrity is IntegrityResult.Compromised) {
            val err = BillingResult.Error(integrity.reasonFa)
            _uiState.update { it.copy(billingMessage = integrity.reasonFa) }
            onResult(err)
            return
        }

        billingManager.purchaseProduct(activity, sku) { result ->
            when (result) {
                is BillingResult.Success -> {
                    _uiState.update { it.copy(isVipUser = true, billingMessage = result.message) }
                }
                is BillingResult.Error -> {
                    _uiState.update { it.copy(billingMessage = result.errorMessage) }
                }
                is BillingResult.MarketNotInstalled -> {
                    _uiState.update { it.copy(billingMessage = "برای خرید، لطفاً اپلیکیشن مایکت را نصب کنید.") }
                }
                else -> {}
            }
            onResult(result)
        }
    }

    fun restorePurchases(onResult: (Boolean, String) -> Unit) {
        billingManager.restorePurchases { success, message ->
            if (success) {
                _uiState.update { it.copy(isVipUser = true, billingMessage = message) }
            } else {
                _uiState.update { it.copy(billingMessage = message) }
            }
            onResult(success, message)
        }
    }

    fun dismissIntegrityWarning() {
        _uiState.update { it.copy(integrityWarning = null) }
    }

    fun clearBillingMessage() {
        _uiState.update { it.copy(billingMessage = null) }
    }

    private fun filterTemplates(
        templates: List<Template>,
        format: FormatFilter,
        category: String,
        query: String
    ): List<Template> {
        val trimmed = query.trim()
        val filtered = templates.filter { tpl ->
            val formatMatch = when (format) {
                FormatFilter.ALL -> true
                FormatFilter.STORY -> tpl.format == TemplateFormat.STORY
                FormatFilter.POST -> tpl.format == TemplateFormat.POST_PORTRAIT || tpl.format == TemplateFormat.POST_SQUARE
                FormatFilter.HIGHLIGHT -> tpl.format == TemplateFormat.HIGHLIGHT
            }

            val categoryMatch = if (category == "همه") true else tpl.category == category

            val queryMatch = if (trimmed.isEmpty()) true else {
                tpl.title.contains(trimmed, ignoreCase = true) ||
                        tpl.category.contains(trimmed, ignoreCase = true) ||
                        tpl.tags.any { it.contains(trimmed, ignoreCase = true) }
            }

            formatMatch && categoryMatch && queryMatch
        }

        // When viewing "ALL", place Story and Post templates first and Highlight covers last
        return if (format == FormatFilter.ALL) {
            filtered.sortedBy { tpl ->
                when (tpl.format) {
                    TemplateFormat.STORY -> 0
                    TemplateFormat.POST_PORTRAIT -> 1
                    TemplateFormat.POST_SQUARE -> 2
                    TemplateFormat.HIGHLIGHT -> 3
                }
            }
        } else {
            filtered
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.destroy()
    }
}
