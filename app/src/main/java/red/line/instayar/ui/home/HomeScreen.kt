package red.line.instayar.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.delay
import red.line.instayar.data.model.BrandProfile
import red.line.instayar.util.PersianDateUtil
import red.line.instayar.util.toPersianDigits
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.data.model.Template
import red.line.instayar.ui.components.FreeBadge
import red.line.instayar.ui.components.TemplateThumbnailPreview
import red.line.instayar.ui.components.VipCrownBadge
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.BrandViolet
import red.line.instayar.ui.theme.DarkBackground
import red.line.instayar.ui.theme.DarkSurface
import red.line.instayar.ui.theme.DarkSurfaceCard
import red.line.instayar.ui.theme.DarkSurfaceCardElevated
import red.line.instayar.ui.theme.PrimaryGradientBrush
import red.line.instayar.ui.theme.VipGold
import red.line.instayar.ui.theme.VipGoldDark
import red.line.instayar.ui.theme.VipGoldLight

val CATEGORIES = listOf(
    "همه",
    "معرفی محصول",
    "تخفیف و حراج",
    "قیمت‌نامه",
    "اطلاعیه",
    "مناسبتی",
    "نظر مشتری",
    "تبریک",
    "سفارش‌گیری"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    brandProfile: BrandProfile = BrandProfile(),
    onNavigateToEditor: (templateId: String) -> Unit = {},
    onOpenAbout: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? Activity
    var lastBackPressTime by remember { mutableStateOf(0L) }

    // خروج با دو بار کلیک دکمه بازگشت (BackHandler)
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 2000L) {
            activity?.finish()
        } else {
            lastBackPressTime = now
            Toast.makeText(context, "برای خروج، یک‌بار دیگر بازگشت را بزنید", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top Header with Greeting & VIP action & Info
            HomeHeader(
                isVip = state.isVipUser,
                onVipClick = { viewModel.openVipModal() },
                onOpenAbout = onOpenAbout
            )

            // 2. Search Field
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onClear = {
                    viewModel.onSearchQueryChanged("")
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Format Tabs (همه | استوری | پست | هایلایت)
            FormatFilterTabs(
                selectedFilter = state.selectedFormatFilter,
                onFilterSelect = { viewModel.onFormatFilterSelected(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Horizontal Category Chips
            CategoryChipsRow(
                selectedCategory = state.selectedCategory,
                onCategorySelect = { viewModel.onCategorySelected(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Staggered Grid of Templates
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BrandPurple,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (state.filteredTemplates.isEmpty()) {
                    EmptyTemplatesView(
                        onResetFilters = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onCategorySelected("همه")
                            viewModel.onFormatFilterSelected(FormatFilter.ALL)
                        }
                    )
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 120.dp // clearance for floating nav bar (navbar height + 16dp)
                        ),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalItemSpacing = 14.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("templates_staggered_grid")
                    ) {
                        items(
                            items = state.filteredTemplates,
                            key = { it.id }
                        ) { template ->
                            TemplateCardItem(
                                template = template,
                                brandProfile = brandProfile,
                                onClick = {
                                    viewModel.onTemplateClicked(template)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Template Detail Bottom Sheet
    state.selectedTemplateForDetail?.let { template ->
        TemplateDetailBottomSheet(
            template = template,
            brandProfile = brandProfile,
            isVipUser = state.isVipUser,
            onDismiss = { viewModel.closeTemplateDetail() },
            onSelectTemplate = {
                viewModel.closeTemplateDetail()
                if (template.isVip && !state.isVipUser) {
                    viewModel.openVipModal()
                } else {
                    onNavigateToEditor(template.id)
                }
            }
        )
    }
}

@Composable
private fun HomeHeader(
    isVip: Boolean,
    onVipClick: () -> Unit,
    onOpenAbout: () -> Unit
) {
    var liveDateText by remember { mutableStateOf(PersianDateUtil.getPersianDate().fullFormattedText) }
    var liveTimeText by remember { mutableStateOf(PersianDateUtil.getCurrentLiveTime()) }
    var greetingText by remember { mutableStateOf(PersianDateUtil.getTimeOfDayGreeting()) }

    LaunchedEffect(Unit) {
        while (true) {
            liveDateText = PersianDateUtil.getPersianDate().fullFormattedText
            liveTimeText = PersianDateUtil.getCurrentLiveTime()
            greetingText = PersianDateUtil.getTimeOfDayGreeting()
            delay(60_000L) // به‌روزرسانی هر دقیقه
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "سلام، $greetingText",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = BrandPink,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = liveDateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "•",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = BrandCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = liveTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Action Buttons Row: Info Icon + VIP CTA
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Info Icon Button (درباره ما)
            IconButton(
                onClick = onOpenAbout,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x228B5CF6))
                    .border(1.dp, Color(0x338B5CF6), CircleShape)
                    .testTag("home_info_icon_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "درباره ما",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // VIP CTA Button with glowing badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = if (isVip) {
                            Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                        } else {
                            Brush.linearGradient(listOf(VipGoldDark, VipGold, VipGoldLight))
                        }
                    )
                    .clickable { onVipClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("vip_header_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "VIP",
                    tint = if (isVip) Color.White else Color(0xFF1F1200),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isVip) "حساب VIP فعال" else "نسخه VIP 👑",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isVip) Color.White else Color(0xFF1F1200)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "جستجوی قالب (محصول، تخفیف، قیمت، تبریک...)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "جستجو",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "پاک کردن",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkSurfaceCard,
            unfocusedContainerColor = DarkSurfaceCard,
            focusedBorderColor = BrandPurple,
            unfocusedBorderColor = Color(0x228B5CF6),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = BrandPurple
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
            .testTag("search_text_field")
    )
}

@Composable
private fun FormatFilterTabs(
    selectedFilter: FormatFilter,
    onFilterSelect: (FormatFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FormatFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            val bgModifier = if (isSelected) {
                Modifier.background(PrimaryGradientBrush, RoundedCornerShape(12.dp))
            } else {
                Modifier.background(Color.Transparent)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .then(bgModifier)
                    .clickable { onFilterSelect(filter) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.titleFa,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(CATEGORIES) { category ->
            val isSelected = selectedCategory == category
            val chipBgColor by animateColorAsState(
                targetValue = if (isSelected) BrandPurple else Color(0xFF262045),
                animationSpec = spring(),
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFFCBD5E1),
                animationSpec = spring(),
                label = "chipText"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(chipBgColor)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0x99FFFFFF) else Color(0x338B5CF6),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategorySelect(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun TemplateCardItem(
    template: Template,
    brandProfile: BrandProfile,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF262045))
            .border(
                width = 1.5.dp,
                color = Color(0x448B5CF6),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
            .testTag("template_card_${template.id}")
    ) {
        // Preview area with Badges
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            TemplateThumbnailPreview(
                template = template,
                brandProfile = brandProfile,
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16f
            )

            // VIP / Free Badge Top-Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                if (template.isVip) {
                    VipCrownBadge(text = "VIP")
                } else {
                    FreeBadge()
                }
            }

            // Format Pill Bottom-Left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC0B0916))
                    .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = template.format.titleFa,
                    fontSize = 10.sp,
                    color = Color(0xFFF1F5F9),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Details Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = template.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1)
                )

                Text(
                    text = "${template.elements.size} المان",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyTemplatesView(
    onResetFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0x228B5CF6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "قالبی با این مشخصات پیدا نشد!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "لطفاً عبارت جستجو یا فیلتر دسته‌بندی را تغییر دهید.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onResetFilters,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "مشاهده همه قالب‌ها",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateDetailBottomSheet(
    template: Template,
    brandProfile: BrandProfile,
    isVipUser: Boolean,
    onDismiss: () -> Unit,
    onSelectTemplate: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurfaceCardElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview card in bottom sheet (Large Preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, if (template.isVip && !isVipUser) VipGold else Color(0x668B5CF6), RoundedCornerShape(24.dp))
            ) {
                TemplateThumbnailPreview(
                    template = template,
                    brandProfile = brandProfile,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24f
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Title & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                if (template.isVip) {
                    Spacer(modifier = Modifier.width(8.dp))
                    VipCrownBadge()
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "دسته‌بندی: ${template.category} • قالب ${template.format.titleFa}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1)
            )

            if (template.isVip && !isVipUser) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FBBF24))
                        .border(1.dp, VipGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🔒 این قالب مخصوص اعضای طلایی VIP است",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = VipGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Editing CTA Button
            Button(
                onClick = onSelectTemplate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (template.isVip && !isVipUser) {
                            Brush.linearGradient(listOf(VipGoldDark, VipGold))
                        } else {
                            PrimaryGradientBrush
                        }
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (template.isVip && !isVipUser) {
                        "باز کردن با VIP 👑"
                    } else {
                        "شروع ویرایش این قالب ✨"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (template.isVip && !isVipUser) Color(0xFF1E1000) else Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
