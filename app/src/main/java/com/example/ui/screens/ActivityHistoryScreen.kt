package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ActivityLogEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityHistoryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val activityLogs by viewModel.activityLogs.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "VOICE_COMMAND", "AI_RESPONSE", "SYSTEM_EVENT", "ERROR")

    val filteredLogs = remember(activityLogs, selectedFilter) {
        if (selectedFilter == "ALL") activityLogs
        else activityLogs.filter { it.type == selectedFilter }
    }

    val timeFormat = remember { SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .statusBarsPadding()
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEURAL ACTIVITY LOG",
                    color = ElectricCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "AUDIT TRAIL & SYSTEM TELEMETRY",
                    color = SecondaryText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = { viewModel.clearLogs() },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = ErrorRed, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) TechBlue else SurfaceCard)
                        .border(0.5.dp, if (isSelected) ElectricCyan else CyanBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = filter.replace("_", " "),
                        color = if (isSelected) PrimaryText else SecondaryText,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No logs recorded in this filter view.", color = SecondaryText, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            items(filteredLogs) { log ->
                val (badgeColor, typeIcon) = when (log.type) {
                    "VOICE_COMMAND" -> Pair(ElectricCyan, Icons.Default.Mic)
                    "AI_RESPONSE" -> Pair(PurpleAccent, Icons.Default.AutoAwesome)
                    "ERROR" -> Pair(ErrorRed, Icons.Default.Warning)
                    else -> Pair(TechBlue, Icons.Default.Info)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(0.5.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(0.5.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeIcon, contentDescription = log.type, tint = badgeColor, modifier = Modifier.size(14.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = log.type.replace("_", " "),
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (log.understoodIntent.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "[${log.understoodIntent}]",
                                        color = SecondaryText,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = timeFormat.format(Date(log.timestamp)),
                                color = DimText,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (log.spokenText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Prompt: \"${log.spokenText}\"",
                                color = PrimaryText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.resultSummary,
                            color = SecondaryText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
