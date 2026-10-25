package red.line.instayar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VipCrownBadge(
    modifier: Modifier = Modifier,
    text: String = "VIP"
) {
    Row(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFFFBBF24))
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD97706),
                        Color(0xFFF59E0B),
                        Color(0xFFFBBF24),
                        Color(0xFFFEF08A)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0x99FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 9.dp, vertical = 3.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "VIP Crown",
            tint = Color(0xFF1E1000),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            color = Color(0xFF1E1000),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun FreeBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x331E293B))
            .border(
                width = 1.dp,
                color = Color(0x2294A3B8),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "رایگان",
            color = Color(0xFF94A3B8),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
