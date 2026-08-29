package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.voice.JarvisState

@Composable
fun JarvisStateBanner(
    state: JarvisState,
    modifier: Modifier = Modifier
) {
    val (statusColor, icon) = when (state) {
        JarvisState.IDLE -> Pair(ElectricCyan, Icons.Default.Radar)
        JarvisState.LISTENING -> Pair(ElectricCyan, Icons.Default.Mic)
        JarvisState.THINKING -> Pair(TechBlue, Icons.Default.Memory)
        JarvisState.SPEAKING -> Pair(PurpleAccent, Icons.Default.GraphicEq)
        JarvisState.EXECUTING -> Pair(ElectricCyan, Icons.Default.PlayArrow)
        JarvisState.SUCCESS -> Pair(SuccessGreen, Icons.Default.CheckCircle)
        JarvisState.ERROR -> Pair(ErrorRed, Icons.Default.Warning)
        JarvisState.SLEEP -> Pair(DimText, Icons.Default.Bedtime)
        JarvisState.PRIVACY_MODE -> Pair(WarningAmber, Icons.Default.Lock)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard.copy(alpha = 0.9f))
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = state.label,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = state.label,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = state.description,
                    color = SecondaryText,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
