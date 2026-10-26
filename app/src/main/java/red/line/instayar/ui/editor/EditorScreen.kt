package red.line.instayar.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.DarkBackground
import red.line.instayar.ui.theme.DarkSurface
import red.line.instayar.ui.theme.DarkSurfaceCard
import red.line.instayar.ui.theme.DarkSurfaceCardElevated
import red.line.instayar.ui.theme.PrimaryGradientBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    templateId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    editorViewModel: EditorViewModel = viewModel()
) {
    val state by editorViewModel.uiState.collectAsState()
    val screenContext = androidx.compose.ui.platform.LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) editorViewModel.applyUserImage(screenContext, uri)
    }

    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val handleBackPress: () -> Unit = {
        if (state.canUndo) {
            showExitConfirmDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler {
        handleBackPress()
    }

    LaunchedEffect(templateId) {
        editorViewModel.loadTemplate(templateId)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        if (state.integrityError != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "خطای امنیتی اصالت برنامه ⚠️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.integrityError ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("بازگشت به صفحه اصلی", color = Color.White)
                }
            }
        } else if (state.isLoading || state.currentTemplate == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPurple)
            }
        } else {
            val template = state.currentTemplate!!
            val selectedElement = template.elements.find { it.id == state.selectedElementId }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Quick, real gallery import action; user image is copied into private app storage.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCardElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = BrandCyan)
                        Spacer(Modifier.width(8.dp))
                        Text("افزودن عکس دلخواه", color = Color.White)
                    }
                }

                // 1. Top Action Bar
                EditorTopBar(
                    canUndo = state.canUndo,
                    canRedo = state.canRedo,
                    formatTitle = template.format.titleFa,
                    onBackClick = handleBackPress,
                    onUndoClick = { editorViewModel.undo() },
                    onRedoClick = { editorViewModel.redo() },
                    onExportClick = { editorViewModel.openExportDialog() }
                )

                // 2. Interactive Canvas Viewport (Center)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EditorInteractiveCanvas(
                        template = template,
                        selectedElementId = state.selectedElementId,
                        brandProfile = state.brandProfile,
                        onSelectElement = { editorViewModel.selectElement(it) },
                        onDragElement = { dx, dy -> editorViewModel.updateElementPosition(dx, dy) },
                        onTransformElement = { scale, rot -> editorViewModel.updateElementTransform(scale, rot) },
                        onGestureEnd = { editorViewModel.commitTransformGesture() }
                    )

                    // Auto-save feedback toast
                    if (state.saveMessage != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xE610B981))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = state.saveMessage ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. Bottom Tool Panels
                EditorBottomControlPanel(
                    template = template,
                    selectedElement = selectedElement,
                    activeTab = state.activeToolTab,
                    onTabSelected = { editorViewModel.setActiveToolTab(it) },
                    onUpdateText = { editorViewModel.updateElementText(it) },
                    onUpdateFontSize = { editorViewModel.updateElementFontSize(it) },
                    onUpdateTextColor = { editorViewModel.updateElementTextColor(it) },
                    onUpdateTextAlign = { editorViewModel.updateElementTextAlign(it) },
                    onUpdateBackgroundColor = { editorViewModel.updateElementBackgroundColor(it) },
                    onUpdateCornerRadius = { editorViewModel.updateElementCornerRadius(it) },
                    onUpdateBackground = { c1, c2 -> editorViewModel.updateBackgroundColors(c1, c2) },
                    onAddNewElement = { type, text -> editorViewModel.addNewElement(type, text) },
                    onBringForward = { editorViewModel.bringElementForward(it) },
                    onSendBackward = { editorViewModel.sendElementBackward(it) },
                    onDuplicate = { editorViewModel.duplicateElement(it) },
                    onDelete = { editorViewModel.deleteElement(it) },
                    onSelectElement = { editorViewModel.selectElement(it) }
                )
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Export Bottom Sheet
    if (state.showExportDialog && state.currentTemplate != null) {
        ExportBottomSheet(
            template = state.currentTemplate!!,
            isVipUser = state.isVipUser,
            isExporting = state.isExporting,
            onDismiss = { editorViewModel.closeExportDialog() },
            onSaveToGallery = {
                editorViewModel.exportToGallery(context) { success ->
                    if (success) {
                        editorViewModel.closeExportDialog()
                    }
                }
            },
            onShareInstagram = {
                editorViewModel.shareToInstagram(context)
                editorViewModel.closeExportDialog()
            }
        )
    }

    // Unsaved Changes Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "خروج از ویرایشگر ⚠️",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "تغییراتی در این قالب اعمال کرده‌اید. پروژه به طور خودکار ذخیره شده است. آیا مایل به خروج هستید؟",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("خروج", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmDialog = false }
                ) {
                    Text("ادامه ویرایش", color = Color.White)
                }
            },
            containerColor = DarkSurfaceCardElevated,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun EditorTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    formatTitle: String,
    onBackClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceCard)
                .clickable { onBackClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("editor_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "بازگشت",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "خروج",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }

        // Center Format & Undo/Redo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onUndoClick,
                enabled = canUndo,
                modifier = Modifier.size(36.dp).testTag("editor_undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) Color.White else Color(0xFF475569)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x228B5CF6))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = formatTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onRedoClick,
                enabled = canRedo,
                modifier = Modifier.size(36.dp).testTag("editor_redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) Color.White else Color(0xFF475569)
                )
            }
        }

        // Export Button
        Button(
            onClick = onExportClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(PrimaryGradientBrush)
                .height(38.dp)
                .testTag("editor_export_button")
        ) {
            Text(
                text = "خروجی 📥",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportBottomSheet(
    template: red.line.instayar.data.model.Template,
    isVipUser: Boolean,
    isExporting: Boolean,
    onDismiss: () -> Unit,
    onSaveToGallery: () -> Unit,
    onShareInstagram: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val resolution = when (template.format) {
        red.line.instayar.data.model.TemplateFormat.STORY -> "۱۰۸۰ × ۱۹۲۰ پیکسل (Full HD)"
        red.line.instayar.data.model.TemplateFormat.POST_PORTRAIT -> "۱۰۸۰ × ۱۳۵۰ پیکسل"
        red.line.instayar.data.model.TemplateFormat.POST_SQUARE -> "۱۰۸۰ × ۱۰۸۰ پیکسل"
        red.line.instayar.data.model.TemplateFormat.HIGHLIGHT -> "۱۰۸۰ × ۱۰۸۰ پیکسل"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0x2210B981)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "آماده‌سازی خروجی باکیفیت 🖼️",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ابعاد خروجی: $resolution\nفرمت: PNG شفاف و بهینه‌سازی شده",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Watermark notification badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isVipUser) Color(0x22F59E0B) else Color(0x228B5CF6))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isVipUser) "👑 نسخه VIP: خروجی بدون واترمارک" else "✨ کاربر عادی: شامل واترمارک شیک قالبینو",
                    fontSize = 11.sp,
                    color = if (isVipUser) Color(0xFFFDE68A) else Color(0xFFC084FC),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isExporting) {
                CircularProgressIndicator(
                    color = BrandPurple,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "در حال پردازش و رندرینگ عکس...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            } else {
                // Save to Gallery CTA
                Button(
                    onClick = onSaveToGallery,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PrimaryGradientBrush)
                        .testTag("save_to_gallery_button")
                ) {
                    Text(
                        text = "ذخیره در گالری گوشی 💾",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Share to Instagram
                Button(
                    onClick = onShareInstagram,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCardElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, Color(0x338B5CF6), RoundedCornerShape(18.dp))
                        .testTag("share_instagram_button")
                ) {
                    Icon(imageVector = Icons.Rounded.Share, contentDescription = null, tint = BrandPink)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اشتراک‌گذاری مستقیم در اینستاگرام 📲",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
