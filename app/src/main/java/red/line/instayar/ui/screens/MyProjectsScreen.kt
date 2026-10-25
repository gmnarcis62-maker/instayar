package red.line.instayar.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.data.local.ProjectEntity
import red.line.instayar.data.model.BrandProfile
import red.line.instayar.data.repository.ProjectRepository
import red.line.instayar.ui.components.TemplateThumbnailPreview
import red.line.instayar.ui.theme.BrandCyan
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.DarkBackground
import red.line.instayar.ui.theme.DarkSurfaceCard
import red.line.instayar.ui.theme.DarkSurfaceCardElevated
import red.line.instayar.ui.theme.PrimaryGradientBrush
import kotlinx.coroutines.launch
import red.line.instayar.util.PersianDateUtil
import red.line.instayar.util.toPersianDigits

@Composable
fun MyProjectsScreen(
    brandProfile: BrandProfile = BrandProfile(),
    onEditProject: (String) -> Unit,
    onNavigateHome: () -> Unit
) {
    BackHandler { onNavigateHome() }

    val context = LocalContext.current
    val repository = remember { ProjectRepository(context) }
    val projects by repository.allProjects.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "پروژه‌های من 📂",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${projects.size.toPersianDigits()} طرح ذخیره‌شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Button(
                    onClick = onNavigateHome,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "طرح جدید", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (projects.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color(0x228B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FolderSpecial,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "هنوز پروژه‌ای ذخیره نشده است",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "هر قالبی که در ویرایشگر تغییر دهید خودکار در این بخش ذخیره می‌شود تا بعداً بتوانید ادامه دهید یا خروجی بگیرید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onNavigateHome,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(PrimaryGradientBrush),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = "شروع ساخت اولین قالب 🚀",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // Projects Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectCardItem(
                            project = project,
                            repository = repository,
                            brandProfile = brandProfile,
                            onEdit = { onEditProject(project.id) },
                            onDuplicate = {
                                coroutineScope.launch {
                                    repository.duplicateProject(project.id)
                                }
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    repository.deleteProject(project.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCardItem(
    project: ProjectEntity,
    repository: ProjectRepository,
    brandProfile: BrandProfile,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val template = remember(project.templateJson) {
        try {
            repository.parseTemplateJson(project.templateJson)
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, Color(0x228B5CF6), RoundedCornerShape(20.dp))
            .clickable { onEdit() }
            .testTag("project_item_${project.id}")
    ) {
        // Thumbnail preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            if (template != null) {
                TemplateThumbnailPreview(
                    template = template,
                    brandProfile = brandProfile,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 14f
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(DarkSurfaceCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text("پروژه ذخیره‌شده", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }

            // Format badge bottom-left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = project.formatName,
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Details & Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val formattedDate = PersianDateUtil.formatShortPersianDate(project.lastModified)
            Text(
                text = "ویرایش: $formattedDate",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = "ویرایش", tint = BrandPurple, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "تکثیر", tint = BrandCyan, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Delete, contentDescription = "حذف", tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
