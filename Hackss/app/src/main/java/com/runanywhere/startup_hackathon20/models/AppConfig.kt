package com.runanywhere.startup_hackathon20.models

import android.graphics.drawable.Drawable

// App Configuration Model
data class AppConfig(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val isEnabled: Boolean = false,
    val assistanceMode: AssistanceMode = AssistanceMode.ON_DEMAND
)

// Assistance Mode Options
enum class AssistanceMode {
    ALWAYS_ON,      // Auto-starts reading when app opens
    ON_DEMAND,      // User activates via floating button/gesture
    DISABLED        // No assistance for this app
}

// App Info Model (for display)
data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean = false
)

// User Preferences
data class AssistantPreferences(
    val enabledApps: Set<String> = emptySet(),
    val appModes: Map<String, AssistanceMode> = emptyMap(),
    val floatingButtonEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val autoReadOnOpen: Boolean = false
)
