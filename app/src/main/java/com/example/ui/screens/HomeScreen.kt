package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.JarvisViewModel
import com.example.voice.JarvisState

@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val jarvisState by viewModel.jarvisState.collectAsState()
    val liveSpokenText by viewModel.liveSpokenText.collectAsState()
    val understoodIntent by viewModel.lastUnderstoodIntent.collectAsState()
    val actionSummary by viewModel.lastActionSummary.collectAsState()
    val spokenResponse by viewModel.lastSpokenResponse.collectAsState()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val rmsLevel by viewModel.voiceEngine.rmsLevel.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val glowIntensity by viewModel.glowIntensity.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val quickPrompts = listOf(
        "What time is it?",
        "Open YouTube",
        "Search how to make a game",
        "Remember I like concise answers",
        "Create a note Review JARVIS core",
        "Create a task Deploy build",
        "Battery status",
        "Privacy mode"
    )

    // Glowing Pulse for Mic Button
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    // Non-scrolling fixed HUD screen layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .drawBehind {
                // Background Decorative Grid (High Density Theme)
                val step = 24.dp.toPx()
                val dotRadius = 1.dp.toPx()
                val gridColor = TechBlue.copy(alpha = 0.08f)
                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) {
                        drawCircle(
                            color = gridColor,
                            radius = dotRadius,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        y += step
                    }
                    x += step
                }
            }
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP HUD: System Top Bar & State Banner
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HudTopBar(
                batteryLevel = batteryLevel,
                isCharging = isCharging,
                networkStatus = networkStatus,
                isPrivacyMode = isPrivacyMode,
                onPrivacyToggle = { viewModel.togglePrivacyMode() },
                onNavigateSettings = { viewModel.setScreen("SETTINGS") }
            )

            JarvisStateBanner(state = jarvisState)
        }

        // 2. CENTER: Holographic Core Avatar & High Density Subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HolographicCore(
                state = jarvisState,
                rmsLevel = rmsLevel,
                glowIntensity = glowIntensity,
                reduceMotion = reduceMotion
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isSpeaking && spokenResponse.isNotBlank()) {
                    "\"${spokenResponse.take(60)}...\""
                } else if (isListening) {
                    "\"Listening to vocal telemetry...\""
                } else if (isPrivacyMode) {
                    "\"Acoustic sensors silenced. Privacy protocol active.\""
                } else {
                    "\"Standing by for voice command, Sir.\""
                },
                color = SecondaryText,
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                letterSpacing = 0.4.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // 3. BOTTOM: Telemetry Card, High Density Control Console & Quick Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // High Density Recognized Text Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SecondaryBackground)
                    .border(0.5.dp, BlueBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isListening) ElectricCyan else if (isPrivacyMode) WarningAmber else SuccessGreen)
                    )
                    Text(
                        text = if (liveSpokenText.isNotEmpty()) liveSpokenText else if (actionSummary.isNotEmpty()) actionSummary else "Waiting for input...",
                        color = if (liveSpokenText.isNotEmpty()) PrimaryText else SecondaryText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Prompts Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard.copy(alpha = 0.8f))
                            .border(0.5.dp, BlueBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.processVoiceCommand(prompt)
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = prompt,
                            color = PrimaryText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Main Interface Controls Console (High Density Layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SecondaryBackground.copy(alpha = 0.6f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Button: Commands / Settings
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepBackground)
                        .border(0.8.dp, BlueBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { viewModel.setScreen("COMMANDS") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Commands",
                        tint = TechBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Center: Glowing High-Density Mic Trigger Core
                Box(
                    modifier = Modifier
                        .testTag("main_mic_button")
                        .size(68.dp)
                        .scale(if (isListening) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isPrivacyMode) {
                                    listOf(WarningAmber.copy(alpha = 0.3f), DeepBackground)
                                } else if (isListening) {
                                    listOf(ElectricCyan.copy(alpha = 0.4f), PurpleAccent.copy(alpha = 0.2f), DeepBackground)
                                } else {
                                    listOf(TechBlue.copy(alpha = 0.25f), DeepBackground)
                                }
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = if (isPrivacyMode) {
                                    listOf(WarningAmber, ErrorRed)
                                } else if (isListening) {
                                    listOf(ElectricCyan, PurpleAccent, ElectricCyan)
                                } else {
                                    listOf(ElectricCyan, TechBlue)
                                }
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            if (isPrivacyMode) {
                                viewModel.togglePrivacyMode()
                            } else if (isListening) {
                                viewModel.stopListening()
                            } else {
                                if (hasAudioPermission) {
                                    viewModel.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isPrivacyMode) WarningAmber else ElectricCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPrivacyMode) Icons.Default.MicOff else if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Microphone Trigger",
                            tint = DeepBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Right Button: Privacy Shield Toggle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepBackground)
                        .border(
                            0.8.dp,
                            if (isPrivacyMode) WarningAmber.copy(alpha = 0.7f) else BlueBorder.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.togglePrivacyMode() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPrivacyMode) Icons.Default.Shield else Icons.Default.ShieldMoon,
                        contentDescription = "Privacy Shield",
                        tint = if (isPrivacyMode) WarningAmber else TechBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
