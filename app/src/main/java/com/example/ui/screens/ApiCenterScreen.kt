package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ApiConfigEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun ApiCenterScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val apiConfigs by viewModel.apiConfigs.collectAsState()
    val testingId by viewModel.testingApiConfigId.collectAsState()
    val testResult by viewModel.apiTestResult.collectAsState()

    var editingConfig by remember { mutableStateOf<ApiConfigEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

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
                    text = "API & NEURAL PROVIDERS",
                    color = ElectricCyan,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "OPENROUTER / GEMINI / OPENAI / DEEPSEEK",
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
                modifier = Modifier.testTag("add_provider_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Provider", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD PROVIDER", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Security Notice Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .border(0.5.dp, CyanBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = "Security", tint = ElectricCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "API Keys are encrypted and stored locally in on-device secure database. Never transmitted to third-party telemetry.",
                color = SecondaryText,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(apiConfigs) { config ->
                ApiConfigCard(
                    config = config,
                    isTesting = testingId == config.id,
                    testResult = if (testResult?.first == config.id) testResult?.second else null,
                    onSelect = { viewModel.selectApiConfig(config.id) },
                    onEdit = { editingConfig = config },
                    onTest = { viewModel.testApiConnection(config) },
                    onDelete = { viewModel.deleteApiConfig(config) }
                )
            }
        }
    }

    // Edit or Create Config Dialog
    val activeDialogConfig = editingConfig ?: if (showCreateDialog) {
        ApiConfigEntity(
            name = "Custom Provider",
            provider = "OPENROUTER",
            apiKey = "",
            model = "google/gemini-2.0-flash-001",
            baseUrl = "https://openrouter.ai/api/v1"
        )
    } else null


    if (activeDialogConfig != null) {
        ApiConfigEditDialog(
            initialConfig = activeDialogConfig,
            isNew = editingConfig == null,
            onDismiss = {
                editingConfig = null
                showCreateDialog = false
            },
            onSave = { saved ->
                viewModel.saveApiConfig(saved)
                editingConfig = null
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ApiConfigCard(
    config: ApiConfigEntity,
    isTesting: Boolean,
    testResult: String?,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit
) {
    val isSelected = config.isSelected
    val hasKey = config.apiKey.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SurfaceCardElevated else SurfaceCard)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) ElectricCyan else CyanBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Radio select indicator
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ElectricCyan else Color.Transparent)
                        .border(1.5.dp, if (isSelected) ElectricCyan else DimText, CircleShape)
                        .clickable { onSelect() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DeepBackground)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = config.name,
                            color = PrimaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(ElectricCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE", color = ElectricCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    Text(
                        text = "Model: ${config.model}",
                        color = SecondaryText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Connection Status Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (!hasKey) WarningAmber.copy(alpha = 0.2f)
                        else if (config.isConnected) SuccessGreen.copy(alpha = 0.2f)
                        else SurfaceCard
                    )
                    .border(
                        0.5.dp,
                        if (!hasKey) WarningAmber
                        else if (config.isConnected) SuccessGreen
                        else CyanBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (!hasKey) WarningAmber else if (config.isConnected) SuccessGreen else DimText)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (!hasKey) "NO KEY" else if (config.isConnected) "ONLINE" else "UNTESTED",
                    color = if (!hasKey) WarningAmber else if (config.isConnected) SuccessGreen else SecondaryText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Endpoint & Parameters summary
        Text(
            text = "Endpoint: ${config.baseUrl}",
            color = DimText,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
        Text(
            text = "Temp: ${config.temperature} | Max Tokens: ${config.maxTokens} | Key: ${if (hasKey) "••••••••${config.apiKey.takeLast(4)}" else "None"}",
            color = DimText,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        if (testResult != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = testResult,
                color = if (testResult.startsWith("SUCCESS")) SuccessGreen else ErrorRed,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onTest,
                enabled = !isTesting && hasKey,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = ElectricCyan, strokeWidth = 1.5.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(if (isTesting) "TESTING..." else "TEST CONNECTION", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryText),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("EDIT", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ApiConfigEditDialog(
    initialConfig: ApiConfigEntity,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (ApiConfigEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialConfig.name) }
    var providerType by remember { mutableStateOf(initialConfig.provider) }
    var apiKey by remember { mutableStateOf(initialConfig.apiKey) }
    var model by remember { mutableStateOf(initialConfig.model) }
    var baseUrl by remember { mutableStateOf(initialConfig.baseUrl) }
    var temperature by remember { mutableStateOf(initialConfig.temperature) }
    var maxTokens by remember { mutableStateOf(initialConfig.maxTokens) }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCardElevated,
        title = {
            Text(
                text = if (isNew) "ADD AI PROVIDER" else "EDIT AI CONFIGURATION",
                color = ElectricCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Provider Type preset pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("GEMINI", "OPENROUTER", "OPENAI", "DEEPSEEK").forEach { type: String ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (providerType == type) TechBlue else SurfaceCard)
                                .clickable {
                                    providerType = type
                                    when (type) {
                                        "GEMINI" -> {
                                            name = "Google Gemini"
                                            model = "gemini-2.5-flash"
                                            baseUrl = "https://generativelanguage.googleapis.com/v1beta"
                                        }
                                        "OPENROUTER" -> {
                                            name = "OpenRouter"
                                            model = "google/gemini-2.0-flash-001"
                                            baseUrl = "https://openrouter.ai/api/v1"
                                        }
                                        "OPENAI" -> {
                                            name = "OpenAI"
                                            model = "gpt-4o-mini"
                                            baseUrl = "https://api.openai.com/v1"
                                        }
                                        "DEEPSEEK" -> {
                                            name = "DeepSeek"
                                            model = "deepseek-chat"
                                            baseUrl = "https://api.deepseek.com/v1"
                                        }
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(type, color = PrimaryText, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider Name") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = SecondaryText
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model ID") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    singleLine = true
                )

                Text("Temperature: ${String.format(java.util.Locale.ROOT, "%.2f", temperature)}", color = SecondaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                )

                Text("Max Tokens: $maxTokens", color = SecondaryText, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = maxTokens.toFloat(),
                    onValueChange = { maxTokens = it.toInt() },
                    valueRange = 256f..4096f,
                    steps = 15,
                    colors = SliderDefaults.colors(thumbColor = TechBlue, activeTrackColor = TechBlue)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialConfig.copy(
                            name = name,
                            provider = providerType,
                            apiKey = apiKey,
                            model = model,
                            baseUrl = baseUrl,
                            temperature = temperature,
                            maxTokens = maxTokens
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("SAVE", color = DeepBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = SecondaryText)
            }
        }
    )
}
