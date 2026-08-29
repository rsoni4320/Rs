package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun SettingsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsState()
    val glowIntensity by viewModel.glowIntensity.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()

    var voicePitch by remember { mutableStateOf(viewModel.voiceEngine.voicePitch) }
    var voiceSpeed by remember { mutableStateOf(viewModel.voiceEngine.voiceSpeed) }
    var voiceResponseEnabled by remember { mutableStateOf(viewModel.voiceEngine.isVoiceResponseEnabled) }
    var wakeWordEnabled by remember { mutableStateOf(viewModel.voiceEngine.isWakeWordEnabled) }
    var continuousListening by remember { mutableStateOf(viewModel.voiceEngine.isContinuousListening) }

    var showResetDialog by remember { mutableStateOf(false) }

    val hasMicPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .statusBarsPadding()
            .padding(14.dp)
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SYSTEM SETTINGS",
                color = ElectricCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "NEURAL VOICE, HUD & SECURITY CONTROLS",
                color = SecondaryText,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Voice Synthesis Settings
            item {
                SettingsSectionCard(title = "VOICE ENGINE & SYNTHESIS") {
                    // Voice Response Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Voice Output (TTS)", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("JARVIS speaks answers aloud using neural voice synthesizer", color = SecondaryText, fontSize = 10.sp)
                        }
                        Switch(
                            checked = voiceResponseEnabled,
                            onCheckedChange = {
                                voiceResponseEnabled = it
                                viewModel.setVoiceResponseEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = TechBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Voice Pitch
                    Text("Voice Pitch: ${String.format(java.util.Locale.ROOT, "%.2f", voicePitch)}", color = SecondaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = voicePitch,
                        onValueChange = {
                            voicePitch = it
                            viewModel.setVoicePitch(it)
                        },
                        valueRange = 0.5f..1.8f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    // Voice Speed
                    Text("Speech Rate: ${String.format(java.util.Locale.ROOT, "%.2f", voiceSpeed)}x", color = SecondaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = voiceSpeed,
                        onValueChange = {
                            voiceSpeed = it
                            viewModel.setVoiceSpeed(it)
                        },
                        valueRange = 0.5f..1.6f,
                        colors = SliderDefaults.colors(thumbColor = TechBlue, activeTrackColor = TechBlue)
                    )

                    // Test Voice Button
                    OutlinedButton(
                        onClick = {
                            viewModel.voiceEngine.speak("All systems operational. J.A.R.V.I.S. neural voice online.")
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Test", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TEST VOCAL RESPONSE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // 2. Listening & Wake Word
            item {
                SettingsSectionCard(title = "ACOUSTIC SENSORS & LISTENING") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wake Word 'Jarvis'", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Respond to 'Jarvis' and 'Hey Jarvis' in speech commands", color = SecondaryText, fontSize = 10.sp)
                        }
                        Switch(
                            checked = wakeWordEnabled,
                            onCheckedChange = {
                                wakeWordEnabled = it
                                viewModel.setWakeWordEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = TechBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Continuous Listening", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Keep microphone ready between dialogue turns", color = SecondaryText, fontSize = 10.sp)
                        }
                        Switch(
                            checked = continuousListening,
                            onCheckedChange = {
                                continuousListening = it
                                viewModel.setContinuousListening(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = TechBlue)
                        )
                    }
                }
            }

            // 3. HUD & Visual Aesthetics
            item {
                SettingsSectionCard(title = "HOLOGRAPHIC HUD & AESTHETICS") {
                    Text("Hologram Energy Intensity: ${String.format(java.util.Locale.ROOT, "%.1f", glowIntensity)}x", color = SecondaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = glowIntensity,
                        onValueChange = { viewModel.setGlowIntensity(it) },
                        valueRange = 0.2f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reduce Animation Motion", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Smooth ring rotation and subdued HUD pulse", color = SecondaryText, fontSize = 10.sp)
                        }
                        Switch(
                            checked = reduceMotion,
                            onCheckedChange = { viewModel.setReduceMotion(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = TechBlue)
                        )
                    }
                }
            }

            // 4. Privacy & Telemetry Status
            item {
                SettingsSectionCard(title = "PRIVACY & PERMISSIONS") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacy Shield Mode", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Mutes all acoustic sensors and disables external telemetry", color = SecondaryText, fontSize = 10.sp)
                        }
                        Switch(
                            checked = isPrivacyMode,
                            onCheckedChange = { viewModel.togglePrivacyMode() },
                            colors = SwitchDefaults.colors(checkedThumbColor = WarningAmber, checkedTrackColor = WarningAmber.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mic, contentDescription = "Mic Permission", tint = if (hasMicPermission) SuccessGreen else WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Microphone Permission", color = PrimaryText, fontSize = 12.sp)
                        }
                        Text(if (hasMicPermission) "GRANTED" else "DENIED", color = if (hasMicPermission) SuccessGreen else WarningAmber, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reset & Wipe Button
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, ErrorRed, RoundedCornerShape(8.dp))
                            .testTag("reset_app_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = ErrorRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESET CONVERSATION & CACHE", color = ErrorRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = SurfaceCardElevated,
            title = { Text("CONFIRM SYSTEM RESET", color = ErrorRed, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
            text = {
                Text("This will purge all temporary neural chat transcripts, audit logs, and memories from the local database.", color = PrimaryText, fontSize = 12.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetApplication()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("PURGE CACHES", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = SecondaryText)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(0.5.dp, CyanBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}
