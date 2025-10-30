package com.runanywhere.startup_hackathon20

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = viewModel(),
    autoStartListening: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val isServiceEnabled = viewModel.isAccessibilityServiceEnabled()
    val context = androidx.compose.ui.platform.LocalContext.current

    var isBackgroundListeningEnabled by remember {
        mutableStateOf(
            context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("background_listening", false)
        )
    }

    // Auto-start listening if triggered by wake word
    LaunchedEffect(autoStartListening) {
        if (autoStartListening && isServiceEnabled && uiState.isVoiceReady) {
            kotlinx.coroutines.delay(500) // Small delay for UI to settle
            viewModel.startListening()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(
                    text = "🎙️ Voice Assistant",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Navigate apps with your voice",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Background Listening Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🌟 Wake Word Detection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBackgroundListeningEnabled)
                                "Say 'Hey Assistant' anytime"
                            else
                                "Enable to use 'Hey Assistant'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Switch(
                        checked = isBackgroundListeningEnabled,
                        onCheckedChange = { enabled ->
                            isBackgroundListeningEnabled = enabled

                            // Save preference
                            context.getSharedPreferences(
                                "app_prefs",
                                android.content.Context.MODE_PRIVATE
                            )
                                .edit()
                                .putBoolean("background_listening", enabled)
                                .apply()

                            // Start/stop background service
                            if (enabled) {
                                com.runanywhere.startup_hackathon20.voice.BackgroundVoiceService.start(
                                    context
                                )
                            } else {
                                com.runanywhere.startup_hackathon20.voice.BackgroundVoiceService.stop(
                                    context
                                )
                            }
                        }
                    )
                }
            }

            // Status Card
            ServiceStatusCard(
                isEnabled = isServiceEnabled,
                onEnableClick = { viewModel.openAccessibilitySettings() }
            )

            // Main Microphone Button
            MicrophoneButton(
                isListening = uiState.isListening,
                isProcessing = uiState.isProcessing,
                isReady = uiState.isVoiceReady && isServiceEnabled,
                onStartListening = { viewModel.startListening() },
                onStopListening = { viewModel.stopListening() }
            )

            // Status Display
            StatusDisplay(
                statusMessage = uiState.statusMessage,
                lastCommand = uiState.lastCommand,
                lastResponse = uiState.lastResponse,
                isError = uiState.isError
            )

            // Commands Help
            CommandsHelpCard()

            // Screen Info Button
            OutlinedButton(
                onClick = {
                    val summary = viewModel.getCurrentScreenSummary()
                    println(summary)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Info, contentDescription = "Info")
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Current Screen")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ServiceStatusCard(
    isEnabled: Boolean,
    onEnableClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Accessibility Service",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnabled) "✓ Enabled" else "✗ Not Enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isEnabled) Color.Green else Color.Red
                )
            }

            if (!isEnabled) {
                Button(onClick = onEnableClick) {
                    Text("Enable")
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Enabled",
                    tint = Color.Green,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun MicrophoneButton(
    isListening: Boolean,
    isProcessing: Boolean,
    isReady: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    // Animated scale for listening effect
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Outer ripple effect when listening
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        )
                )
            }

            // Main microphone button
            FloatingActionButton(
                onClick = {
                    if (isReady && !isProcessing) {
                        if (isListening) onStopListening() else onStartListening()
                    }
                },
                modifier = Modifier.size(120.dp),
                containerColor = when {
                    isProcessing -> MaterialTheme.colorScheme.tertiary
                    isListening -> MaterialTheme.colorScheme.error
                    !isReady -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                }
            ) {
                when {
                    isProcessing -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    isListening -> {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop",
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    else -> {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Start Listening",
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button label
        Text(
            text = when {
                isProcessing -> "Processing..."
                isListening -> "Tap to stop"
                !isReady -> "Setup required"
                else -> "Tap to speak"
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun StatusDisplay(
    statusMessage: String,
    lastCommand: String,
    lastResponse: String,
    isError: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = lastCommand.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "You said:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "\"$lastCommand\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(visible = lastResponse.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Response:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = lastResponse,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CommandsHelpCard() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Example Commands",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide" else "Show")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    CommandExample("What's on this screen?", "Get screen description")
                    CommandExample("Click [button name]", "Tap a button")
                    CommandExample("Scroll down", "Scroll the page")
                    CommandExample("Type hello", "Enter text")
                    CommandExample("Read the screen", "Read all content")
                }
            }
        }
    }
}

@Composable
fun CommandExample(command: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = command,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
    if (command != "Read the screen") {
        Divider(modifier = Modifier.padding(vertical = 4.dp))
    }
}
