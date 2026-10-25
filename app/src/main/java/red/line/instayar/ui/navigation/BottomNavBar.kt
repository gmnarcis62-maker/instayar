package red.line.instayar.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.instayar.ui.theme.BrandPink
import red.line.instayar.ui.theme.BrandPurple
import red.line.instayar.ui.theme.PrimaryGradientBrush

enum class NavDestination(
    val titleFa: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("قالب‌ها", Icons.Rounded.GridView, "nav_home"),
    PROJECTS("پروژه‌های من", Icons.Rounded.FolderSpecial, "nav_projects"),
    BRAND("برند من", Icons.Rounded.Person, "nav_brand"),
    SETTINGS("تنظیمات", Icons.Rounded.Settings, "nav_settings")
}

@Composable
fun FloatingBottomNavBar(
    currentDestination: NavDestination,
    onDestinationSelect: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Floating pill bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x667C3AED)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xF0151226))
                .border(
                    width = 1.dp,
                    color = Color(0x338B5CF6),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFF64748B),
                    animationSpec = spring(),
                    label = "iconColor"
                )

                val pillBgModifier = if (isSelected) {
                    Modifier.background(PrimaryGradientBrush, RoundedCornerShape(18.dp))
                } else {
                    Modifier.background(Color.Transparent)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .then(pillBgModifier)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDestinationSelect(destination)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag(destination.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.titleFa,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = destination.titleFa,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
