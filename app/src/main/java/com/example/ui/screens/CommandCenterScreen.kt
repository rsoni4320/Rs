package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.local.entities.CustomCommandEntity
import com.example.ui.components.VoiceStatusCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.JarvisViewModel

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val command: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val liveSpokenText by viewModel.liveSpokenText.collectAsState()
    val understoodIntent by viewModel.lastUnderstoodIntent.collectAsState()
    val actionSummary by viewModel.lastActionSummary.collectAsState()
    val spokenResponse by viewModel.lastSpokenResponse.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val customCommands by viewModel.customCommands.collectAsState()

    var testInputText by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val quickGrid = listOf(
        QuickActionItem("Time Telemetry", "Inquire current time", "what time is it", Icons.Default.AccessTime),
        QuickActionItem("Calendar Date", "Inquire date & day", "what is today's date", Icons.Default.CalendarToday),
        QuickActionItem("Power Level", "Inspect battery charge", "check battery status", Icons.Default.BatteryChargingFull),
        QuickActionItem("Launch YouTube", "Open video stream", "open youtube", Icons.Default.SmartDisplay),
        QuickActionItem("Search Web", "AI search query", "search how to make a game", Icons.Default.Search),
        QuickActionItem("Take Note", "Create quick note", "create a note Project JARVIS deployment ready", Icons.Default.NoteAdd),
        QuickActionItem("Add Task", "Create to-do item", "create a task Review security protocols", Icons.Default.AddTask),
        QuickActionItem("Set Reminder", "Schedule reminder", "create a reminder Sync with headquarters", Icons.Default.Alarm),
        QuickActionItem("Halt Voice", "Interrupt voice stream", "stop speaking", Icons.Default.VolumeOff),
        QuickActionItem("Privacy Shield", "Toggle sensors", "privacy mode", Icons.Default.Shield)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .statusBarsPadding()
            .padding(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMMAND CENTER",
                    color = ElectricCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "VOICE PARSER & ACTION MATRIX",
                    color = SecondaryText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("add_custom_command_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Command", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW COMMAND", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Manual Text Command Input & Execute Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = testInputText,
                onValueChange = { testInputText = it },
                placeholder = { Text("Enter command (e.g. 'open spotify' or 'remember fact')", fontSize = 12.sp, color = DimText) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = CyanBorder,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                )
            )

            IconButton(
                onClick = {
                    if (testInputText.isNotBlank()) {
                        viewModel.processVoiceCommand(testInputText)
                        testInputText = ""
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElectricCyan)
                    .size(50.dp)
                    .testTag("execute_command_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Execute Command",
                    tint = DeepBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real-time Voice & Action status
        VoiceStatusCard(
            liveSpokenText = liveSpokenText,
            understoodIntent = understoodIntent,
            actionSummary = actionSummary,
            spokenResponse = spokenResponse,
            isSpeaking = isSpeaking,
            onStopSpeaking = { viewModel.stopSpeaking() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "PRESET SYSTEM COMMANDS",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickGrid) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(0.5.dp, CyanBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { viewModel.processVoiceCommand(item.command) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TechBlue.copy(alpha = 0.2f))
                                .border(0.5.dp, TechBlue, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = item.title, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(item.title, color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("\"${item.command}\"", color = SecondaryText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = "Execute", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                }
            }

            if (customCommands.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CUSTOM USER COMMANDS",
                        color = PurpleAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                items(customCommands) { cmd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCardElevated)
                            .border(0.5.dp, PurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\"${cmd.phrase}\"", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(PurpleAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(cmd.actionType, color = PurpleAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Text(
                                text = "Payload: ${cmd.targetPayload}",
                                color = SecondaryText,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        Row {
                            IconButton(onClick = { viewModel.processVoiceCommand(cmd.phrase) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = SuccessGreen)
                            }
                            IconButton(onClick = { viewModel.deleteCustomCommand(cmd) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Command Create Dialog
    if (showCreateDialog) {
        var phrase by remember { mutableStateOf("") }
        var actionType by remember { mutableStateOf("OPEN_URL") } // OPEN_URL, SEARCH, SAY_TEXT
        var payload by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceCardElevated,
            title = {
                Text("ADD CUSTOM COMMAND", color = ElectricCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = phrase,
                        onValueChange = { phrase = it },
                        label = { Text("Trigger Phrase (e.g. 'matrix mode')", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CyanBorder, focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                    )

                    Text("ACTION TYPE", color = SecondaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("OPEN_URL", "SEARCH", "SAY_TEXT").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (actionType == type) TechBlue else SurfaceCard)
                                    .clickable { actionType = type }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(type, color = PrimaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = payload,
                        onValueChange = { payload = it },
                        label = { Text(if (actionType == "OPEN_URL") "URL (e.g. https://github.com)" else if (actionType == "SEARCH") "Search Query" else "Spoken Message", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CyanBorder, focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricCyan, unfocusedBorderColor = CyanBorder, focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phrase.isNotBlank() && payload.isNotBlank()) {
                            viewModel.addCustomCommand(phrase, actionType, payload, description)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text("SAVE COMMAND", color = DeepBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = SecondaryText)
                }
            }
        )
    }
}
