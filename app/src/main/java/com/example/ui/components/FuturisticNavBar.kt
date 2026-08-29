package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class NavItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

val navItems = listOf(
    NavItem("HOME", "CORE", Icons.Default.Radar),
    NavItem("COMMANDS", "COMMAND", Icons.Default.Terminal),
    NavItem("CHAT", "AI CHAT", Icons.Default.ChatBubbleOutline),
    NavItem("PRODUCTIVITY", "MEMORY", Icons.Default.Checklist),
    NavItem("API", "API CENTER", Icons.Default.Api),
    NavItem("HISTORY", "LOGS", Icons.Default.History),
    NavItem("SETTINGS", "SETTINGS", Icons.Default.Settings)
)

@Composable
fun FuturisticNavBar(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SecondaryBackground.copy(alpha = 0.98f),
                        DeepBackground
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.horizontalGradient(listOf(BlueBorder.copy(alpha = 0.4f), CyanBorder.copy(alpha = 0.5f), BlueBorder.copy(alpha = 0.4f))),
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
            val isSelected = currentScreen == item.id
            val accentColor = if (isSelected) ElectricCyan else DimText

            Column(
                modifier = Modifier
                    .testTag("nav_item_${item.id.lowercase()}")
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) ElectricCyan.copy(alpha = 0.12f) else DeepBackground.copy(alpha = 0.4f)
                    )
                    .border(
                        width = if (isSelected) 0.8.dp else 0.4.dp,
                        color = if (isSelected) ElectricCyan.copy(alpha = 0.6f) else BlueBorder.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onNavigate(item.id) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top Indicator: Glowing pill if selected, small dot if unselected
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.4f))
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = item.label,
                        color = if (isSelected) ElectricCyan else SecondaryText,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }
    }
}

