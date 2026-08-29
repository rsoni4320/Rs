package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HudTopBar(
    batteryLevel: Int,
    isCharging: Boolean,
    networkStatus: String,
    isPrivacyMode: Boolean,
    onPrivacyToggle: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now).uppercase(Locale.ROOT)
            delay(1000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SecondaryBackground.copy(alpha = 0.95f),
                        DeepBackground.copy(alpha = 0.9f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(CyanBorder.copy(alpha = 0.4f), BlueBorder.copy(alpha = 0.4f), CyanBorder.copy(alpha = 0.4f))),
                shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: System Status HUD Header (High Density Theme)
        Column {
            Text(
                text = "SYSTEM STATUS",
                color = ElectricCyan.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.8.sp
            )
            Text(
                text = if (isPrivacyMode) "SHIELD ACTIVE // MUTE" else "READY // STABLE",
                color = if (isPrivacyMode) WarningAmber else ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
        }

        // Center: High-Density Pill with Glowing Live Clock
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SecondaryBackground)
                .border(0.5.dp, TechBlue.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(if (isPrivacyMode) WarningAmber else SuccessGreen, CircleShape)
            )
            Text(
                text = currentTimeString.ifEmpty { "--:--:--" },
                color = PrimaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        // Right: Telemetry Badges (Battery, Network, Privacy Mode Button)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Network Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, TechBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (networkStatus == "OFFLINE") Icons.Default.SignalWifiOff else Icons.Default.Wifi,
                    contentDescription = "Network Status",
                    tint = if (networkStatus == "OFFLINE") ErrorRed else ElectricCyan,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = networkStatus,
                    color = PrimaryText,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Battery Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, TechBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = if (batteryLevel < 20) WarningAmber else SuccessGreen,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "$batteryLevel%",
                    color = PrimaryText,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Privacy Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isPrivacyMode) WarningAmber.copy(alpha = 0.2f) else SurfaceCard)
                    .border(
                        0.5.dp,
                        if (isPrivacyMode) WarningAmber else TechBlue.copy(alpha = 0.4f),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onPrivacyToggle() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPrivacyMode) Icons.Default.Shield else Icons.Default.ShieldMoon,
                    contentDescription = "Privacy Mode",
                    tint = if (isPrivacyMode) WarningAmber else ElectricCyan,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
