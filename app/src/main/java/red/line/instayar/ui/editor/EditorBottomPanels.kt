package red.line.instayar.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.FormatAlignLeft
import androidx.compose.material.icons.rounded.FormatAlignRight
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.data.model.ElementType
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateElement
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.DarkSurface
import red.line.instayar.ui.theme.DarkSurfaceCard
import red.line.instayar.ui.theme.DarkSurfaceCardElevated
import red.line.instayar.ui.theme.PrimaryGradientBrush
import red.line.instayar.ui.theme.VipGold

val LUXURY_PALETTE = listOf(
    "#FFFFFF",
    "#000000",
    "#F59E0B", // Gold
    "#EC4899", // Pink
    "#8B5CF6", // Purple
    "#06B6D4", // Cyan
    "#10B981", // Emerald
    "#EF4444", // Red
    "#3B82F6", // Blue
    "#64748B"  // Slate
)

val PRESET_BACKGROUNDS = listOf(
    Pair("ارغوانی شب", listOf("#2C1252", "#0F0B1E")),
    Pair("سرخابی حراج", listOf("#4C0519", "#1E020D")),
    Pair("اقیانوس لوکس", listOf("#1E40AF", "#0F172A")),
    Pair("زمرد تیره", listOf("#064E3B", "#022C22")),
    Pair("طلای شاهانه", listOf("#78350F", "#291500")),
    Pair("کربن بلک", listOf("#27272A", "#09090B"))
)

@Composable
fun EditorBottomControlPanel(
    template: Template,
    selectedElement: TemplateElement?,
    activeTab: EditorToolTab,
    onTabSelected: (EditorToolTab) -> Unit,
    onUpdateText: (String) -> Unit,
    onUpdateFontSize: (Float) -> Unit,
    onUpdateTextColor: (String) -> Unit,
    onUpdateTextAlign: (String) -> Unit,
    onUpdateBackgroundColor: (String?) -> Unit,
    onUpdateCornerRadius: (Float) -> Unit,
    onUpdateBackground: (primaryColor: String, secondaryColor: String) -> Unit,
    onAddNewElement: (ElementType, String?) -> Unit,
    onBringForward: (String) -> Unit,
    onSendBackward: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSelectElement: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(DarkSurface)
            .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .navigationBarsPadding()
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        // 1. Tool Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EditorToolTab.values().forEach { tab ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BrandPurple else DarkSurfaceCard)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("editor_tab_${tab.name}")
                ) {
                    Text(
                        text = tab.titleFa,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Active Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (activeTab) {
                EditorToolTab.TEXT -> {
                    TextToolPanel(
                        element = selectedElement,
                        onUpdateText = onUpdateText,
                        onUpdateFontSize = onUpdateFontSize,
                        onUpdateTextColor = onUpdateTextColor,
                        onUpdateTextAlign = onUpdateTextAlign
                    )
                }
                EditorToolTab.STYLE -> {
                    StyleToolPanel(
                        element = selectedElement,
                        onUpdateTextColor = onUpdateTextColor,
                        onUpdateBackgroundColor = onUpdateBackgroundColor,
                        onUpdateCornerRadius = onUpdateCornerRadius
                    )
                }
                EditorToolTab.BACKGROUND -> {
                    BackgroundToolPanel(
                        onSelectPreset = onUpdateBackground
                    )
                }
                EditorToolTab.ELEMENTS -> {
                    AddElementsToolPanel(
                        onAddElement = onAddNewElement
                    )
                }
                EditorToolTab.LAYERS -> {
                    LayersToolPanel(
                        template = template,
                        selectedElementId = selectedElement?.id,
                        onBringForward = onBringForward,
                        onSendBackward = onSendBackward,
                        onDuplicate = onDuplicate,
                        onDelete = onDelete,
                        onSelectElement = onSelectElement
                    )
                }
            }
        }
    }
}

@Composable
private fun TextToolPanel(
    element: TemplateElement?,
    onUpdateText: (String) -> Unit,
    onUpdateFontSize: (Float) -> Unit,
    onUpdateTextColor: (String) -> Unit,
    onUpdateTextAlign: (String) -> Unit
) {
    if (element == null || (element.type != ElementType.TEXT && element.type != ElementType.BRAND_NAME && element.type != ElementType.BRAND_CONTACT)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "یک المان متنی را از روی بوم انتخاب کنید تا ویرایش شود.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
        return
    }

    var textInput by remember(element.id, element.text) { mutableStateOf(element.text ?: "") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Text Input Field
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                onUpdateText(it)
            },
            placeholder = { Text("متن خود را اینجا بنویسید...", color = Color(0xFF64748B)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurfaceCard,
                focusedBorderColor = BrandPurple,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Alignment & Size Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Alignment buttons
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceCard)
                    .padding(2.dp)
            ) {
                IconButton(
                    onClick = { onUpdateTextAlign("RIGHT") },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatAlignRight,
                        contentDescription = "راست‌چین",
                        tint = if (element.textAlign == "RIGHT") BrandPurple else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onUpdateTextAlign("CENTER") },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatAlignCenter,
                        contentDescription = "وسط‌چین",
                        tint = if (element.textAlign == "CENTER") BrandPurple else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onUpdateTextAlign("LEFT") },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatAlignLeft,
                        contentDescription = "چپ‌چین",
                        tint = if (element.textAlign == "LEFT") BrandPurple else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Font size slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(180.dp)
            ) {
                Text(
                    text = "${element.fontSize.toInt()}sp",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Slider(
                    value = element.fontSize,
                    onValueChange = onUpdateFontSize,
                    valueRange = 10f..46f,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandPurple,
                        activeTrackColor = BrandPurple
                    )
                )
            }
        }
    }
}

@Composable
private fun StyleToolPanel(
    element: TemplateElement?,
    onUpdateTextColor: (String) -> Unit,
    onUpdateBackgroundColor: (String?) -> Unit,
    onUpdateCornerRadius: (Float) -> Unit
) {
    if (element == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "یک المان را برای تغییر استایل و رنگ انتخاب کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "رنگ متن یا المان:",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Color Swatches
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LUXURY_PALETTE.forEach { colorHex ->
                val isSelected = element.textColor.equals(colorHex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(colorHex)))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) BrandPurple else Color(0x44FFFFFF),
                            shape = CircleShape
                        )
                        .clickable { onUpdateTextColor(colorHex) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Background Color Swatches for Pill / Shape
        Text(
            text = "رنگ پس‌زمینه المان:",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Transparent option
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFFFFF))
                    .border(1.dp, Color(0x44FFFFFF), CircleShape)
                    .clickable { onUpdateBackgroundColor(null) },
                contentAlignment = Alignment.Center
            ) {
                Text("✕", fontSize = 12.sp, color = Color.White)
            }

            LUXURY_PALETTE.forEach { colorHex ->
                val isSelected = element.backgroundColor?.equals(colorHex, ignoreCase = true) == true
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(colorHex)))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) BrandCyan else Color(0x44FFFFFF),
                            shape = CircleShape
                        )
                        .clickable { onUpdateBackgroundColor(colorHex) }
                )
            }
        }
    }
}

@Composable
private fun BackgroundToolPanel(
    onSelectPreset: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "انتخاب گرادیانت پس‌زمینه قالب:",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PRESET_BACKGROUNDS.forEach { preset ->
                val colors = preset.second.map { Color(android.graphics.Color.parseColor(it)) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onSelectPreset(preset.second[0], preset.second[1]) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(colors))
                            .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.first,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AddElementsToolPanel(
    onAddElement: (ElementType, String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AddElementButton("متن جدید", Icons.Rounded.Add) {
            onAddElement(ElementType.TEXT, "متن جدید شما")
        }
        AddElementButton("برچسب حراج", Icons.Rounded.Add) {
            onAddElement(ElementType.BADGE, "🔥 ۵۰٪ تخفیف ویژه")
        }
        AddElementButton("کادر دکوراتیو", Icons.Rounded.Add) {
            onAddElement(ElementType.SHAPE, null)
        }
        AddElementButton("فریم عکس", Icons.Rounded.Add) {
            onAddElement(ElementType.IMAGE_PLACEHOLDER, null)
        }
    }
}

@Composable
private fun AddElementButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCardElevated),
        modifier = Modifier.height(48.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@Composable
private fun LayersToolPanel(
    template: Template,
    selectedElementId: String?,
    onBringForward: (String) -> Unit,
    onSendBackward: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onSelectElement: (String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        Text(
            text = "لایه‌های قالب (کلیک برای انتخاب و مرتب‌سازی):",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(template.elements.reversed()) { _, el ->
                val isSelected = el.id == selectedElementId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0x338B5CF6) else DarkSurfaceCard)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrandPurple else Color(0x15FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectElement(el.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = el.text ?: el.placeholderLabel ?: el.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onBringForward(el.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "جلوتر", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onSendBackward(el.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "عقب‌تر", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onDuplicate(el.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = "تکثیر", tint = BrandCyan, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onDelete(el.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = "حذف", tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
