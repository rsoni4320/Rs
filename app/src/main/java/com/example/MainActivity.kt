package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.FuturisticNavBar
import com.example.ui.screens.*
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.JarvisTheme
import com.example.ui.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JarvisTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateTelemetry()
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground),
        containerColor = DeepBackground,
        bottomBar = {
            FuturisticNavBar(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.setScreen(screen) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepBackground)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    "HOME" -> HomeScreen(viewModel = viewModel)
                    "COMMANDS" -> CommandCenterScreen(viewModel = viewModel)
                    "CHAT" -> AiChatScreen(viewModel = viewModel)
                    "PRODUCTIVITY" -> ProductivityScreen(viewModel = viewModel)
                    "API" -> ApiCenterScreen(viewModel = viewModel)
                    "HISTORY" -> ActivityHistoryScreen(viewModel = viewModel)
                    "SETTINGS" -> SettingsScreen(viewModel = viewModel)
                    else -> HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}

