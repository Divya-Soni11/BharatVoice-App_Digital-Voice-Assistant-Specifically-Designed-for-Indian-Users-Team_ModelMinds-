package com.runanywhere.startup_hackathon20.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.app.NotificationCompat
import com.runanywhere.startup_hackathon20.R
import com.runanywhere.startup_hackathon20.managers.AppConfigManager
import com.runanywhere.startup_hackathon20.models.AssistanceMode
import kotlinx.coroutines.*
import java.util.Locale

/**
 * Core Accessibility Service that reads UI from other applications
 * This runs in the background and has access to screen content of all apps
 */
class AccessibilityAssistantService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var uiAnalyzer: UIAnalyzer
    private lateinit var appConfigManager: AppConfigManager
    private var textToSpeech: TextToSpeech? = null

    // Audio focus management
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    // Event management
    private var lastAnalysisTime = 0L
    private val analysisThrottle = 1000L
    private var lastEventTime = 0L
    private val eventDebounceMs = 500L // Debounce rapid events

    // App state tracking
    private var currentActivePackage: String? = null
    private var isReadingScreen = false
    private var lastAutoReadTime = 0L
    private val autoReadCooldown = 3000L
    private var lastReadPackage: String? = null

    // System packages to ignore
    private val systemPackages = setOf(
        "com.android.systemui",        // System UI
        "com.google.android.gms",      // Google Play Services
        "com.android.launcher3",       // Launcher
        "com.android.inputmethod",     // Keyboard
        "com.google.android.inputmethod", // Gboard
        "com.sec.android.inputmethod", // Samsung keyboard
        "android"                      // Core Android
    )

    companion object {
        private const val TAG = "AccessibilityAssistant"
        private var instance: AccessibilityAssistantService? = null

        fun getInstance(): AccessibilityAssistantService? = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        uiAnalyzer = UIAnalyzer()
        appConfigManager = AppConfigManager(this)

        // Get AudioManager for audio focus
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Initialize Text-to-Speech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                Log.d(TAG, "Text-to-Speech initialized successfully")
            }
        }

        // Build audio focus request for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    Log.d(TAG, "Audio focus changed: $focusChange")
                }
                .build()
        }

        Log.d(TAG, "Accessibility Assistant Service Created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo().apply {
            // Listen to all UI events
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED

            // Can read window content
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY

            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100

            // null means monitor ALL apps
            packageNames = null
        }

        serviceInfo = info
        Log.d(TAG, "Accessibility Service Connected and Configured")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Get the package name of the current app
        val packageName = event.packageName?.toString() ?: return

        // Filter out system packages and our own app
        if (packageName in systemPackages || packageName == this.packageName) {
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // App switched - this is the most reliable event for app switches
                Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED for: $packageName")

                val now = System.currentTimeMillis()

                // Debounce: Ignore if too soon after last event
                if (now - lastEventTime < eventDebounceMs) {
                    Log.d(TAG, "Debouncing event for: $packageName")
                    return
                }

                lastEventTime = now

                // Verify this is a real app window (not dialog/overlay)
                if (!isRealAppWindow(packageName)) {
                    Log.d(TAG, "Not a real app window, ignoring: $packageName")
                    return
                }

                handleAppSwitch(packageName)

                // Analyze screen immediately
                val currentTime = System.currentTimeMillis()
                lastAnalysisTime = currentTime
                analyzeCurrentScreen(packageName)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Screen content changed within the app
                // Only analyze if we should provide assistance
                if (shouldProvideAssistance(packageName)) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastAnalysisTime >= analysisThrottle) {
                        lastAnalysisTime = currentTime
                        analyzeCurrentScreen(packageName)
                    }
                }
            }
        }
    }

    /**
     * Verify this is an actual app window, not a system dialog or overlay
     */
    private fun isRealAppWindow(packageName: String): Boolean {
        try {
            // Check if actual visible application window
            val windows = windows
            if (windows == null || windows.isEmpty()) {
                Log.d(TAG, "No windows available")
                return false
            }

            val activeWindow = windows.find {
                it.type == AccessibilityWindowInfo.TYPE_APPLICATION && it.isActive
            }

            if (activeWindow == null) {
                Log.d(TAG, "No active application window found")
                return false
            }

            // Check window size (dialogs are usually small)
            val rootNode = activeWindow.root
            if (rootNode != null) {
                val bounds = Rect()
                rootNode.getBoundsInScreen(bounds)

                if (bounds.width() < 100 || bounds.height() < 100) {
                    Log.d(
                        TAG,
                        "Window too small (${bounds.width()}x${bounds.height()}), likely a dialog"
                    )
                    return false
                }
            }

            // Check if it's a user app (not pure system app)
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            if (!isUserApp) {
                Log.d(TAG, "Not a user app: $packageName")
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if real app window: ${e.message}")
            return false
        }
    }

    /**
     * Handle app switch - check if new app needs Always-On assistance
     */
    private fun handleAppSwitch(packageName: String) {
        Log.d(TAG, "handleAppSwitch called for: $packageName")

        // Don't process if it's our own app
        if (packageName == this.packageName) {
            Log.d(TAG, "Ignoring our own app")
            return
        }

        // Check if this is actually a new app
        val isNewApp = currentActivePackage != packageName

        if (isNewApp) {
            Log.d(TAG, "New app detected. Previous: $currentActivePackage, New: $packageName")
            currentActivePackage = packageName

            // Always reset the last read package when switching apps
            lastReadPackage = null
            lastAutoReadTime = 0L

            // Check if this app is enabled
            val isEnabled = appConfigManager.isAppEnabled(packageName)
            Log.d(TAG, "App $packageName enabled: $isEnabled")

            if (isEnabled) {
                val mode = appConfigManager.getAssistanceMode(packageName)
                Log.d(TAG, "App $packageName mode: $mode")

                when (mode) {
                    AssistanceMode.ALWAYS_ON -> {
                        Log.d(TAG, "ALWAYS_ON mode for $packageName - scheduling auto-read")
                        serviceScope.launch {
                            // Longer delay to let the app fully load - 1500ms
                            delay(1500)
                            Log.d(TAG, "Executing auto-read for $packageName")
                            autoReadScreen(packageName)
                        }
                    }

                    AssistanceMode.ON_DEMAND -> {
                        Log.d(TAG, "ON_DEMAND mode for $packageName - waiting for user activation")
                        // Could show floating button here (will implement later)
                    }

                    else -> {
                        Log.d(TAG, "App $packageName mode is DISABLED")
                    }
                }
            } else {
                Log.d(TAG, "App $packageName is not enabled")
            }
        }
    }

    /**
     * Check if we should provide assistance for this app
     */
    private fun shouldProvideAssistance(packageName: String): Boolean {
        // Don't process our own app
        if (packageName == this.packageName) return false

        // Check if app is enabled
        return appConfigManager.isAppEnabled(packageName)
    }

    /**
     * Auto-read screen content (for ALWAYS_ON mode)
     */
    private fun autoReadScreen(packageName: String) {
        Log.d(TAG, "=================================================")
        Log.d(TAG, "🔊 AUTO-READ SCREEN STARTED")
        Log.d(TAG, "Package: $packageName")
        Log.d(TAG, "=================================================")

        if (isReadingScreen) {
            Log.d(TAG, "Already reading screen, skipping")
            return
        }

        // Check cooldown only for the same app
        val now = System.currentTimeMillis()
        if (packageName == lastReadPackage && (now - lastAutoReadTime) < autoReadCooldown) {
            Log.d(
                TAG,
                "Cooldown active for $packageName, skipping. Time since last: ${now - lastAutoReadTime}ms"
            )
            return
        }

        try {
            isReadingScreen = true
            Log.d(TAG, "✅ Starting screen read for $packageName")

            // Get screen data
            Log.d(TAG, "📊 Getting current screen data...")
            val screenData = ScreenStateManager.getCurrentScreen()
            Log.d(TAG, "📊 Screen data retrieved: ${screenData.elements.size} elements")

            if (screenData.elements.isEmpty()) {
                Log.w(TAG, "⚠️ No screen elements found, waiting and retrying...")
                // Try one more time after a short delay
                Thread.sleep(1000)
                ScreenStateManager.getCurrentScreen().let { retryData ->
                    if (retryData.elements.isEmpty()) {
                        Log.w(TAG, "❌ Still no elements found after retry")
                        return
                    }
                }
            }

            // Get app name
            Log.d(TAG, "📱 Getting app config...")
            val appConfig = runBlocking {
                appConfigManager.getAppConfig(packageName)
            }
            val appName = appConfig?.appName ?: packageName.split(".").lastOrNull() ?: "App"

            Log.d(TAG, "📱 App name: $appName, Screen elements: ${screenData.elements.size}")

            // Build summary with key elements
            val keyElements = screenData.elements
                .filter { it.text.isNotEmpty() && it.isClickable }
                .take(5)

            Log.d(TAG, "🔑 Key clickable elements found: ${keyElements.size}")
            keyElements.forEachIndexed { index, element ->
                Log.d(TAG, "  $index: ${element.text}")
            }

            val summary = if (keyElements.isNotEmpty()) {
                buildString {
                    append("$appName opened. ")
                    append("Available options: ")
                    keyElements.forEachIndexed { index, element ->
                        append(element.text)
                        if (index < keyElements.size - 1) append(", ")
                    }
                }
            } else {
                "$appName opened"
            }

            Log.d(TAG, "💬 Summary to speak: \"$summary\"")
            Log.d(TAG, "🔊 Calling speak() method...")

            // Speak the summary
            speak(summary)

            // Update tracking
            lastReadPackage = packageName
            lastAutoReadTime = now

            Log.d(TAG, "✅ Successfully completed auto-read for $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error auto-reading screen for $packageName", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            e.printStackTrace()
        } finally {
            isReadingScreen = false
            Log.d(TAG, "=================================================")
        }
    }

    /**
     * Speak text using TTS with proper audio focus handling
     */
    private fun speak(text: String) {
        Log.d(TAG, "=== SPEAK METHOD CALLED ===")
        Log.d(TAG, "Text to speak: $text")
        Log.d(TAG, "TTS initialized: ${textToSpeech != null}")
        Log.d(TAG, "AudioManager initialized: ${audioManager != null}")

        if (textToSpeech == null) {
            Log.e(TAG, "TextToSpeech is null! Cannot speak.")
            return
        }

        // Try to request audio focus, but speak anyway if it fails
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
                ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            ) ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        Log.d(TAG, "Audio focus request result: $result")

        // Build params for notification stream
        val params = Bundle().apply {
            putInt(
                TextToSpeech.Engine.KEY_PARAM_STREAM,
                AudioManager.STREAM_NOTIFICATION
            )
        }

        // Speak regardless of audio focus result (be more lenient)
        val utteranceId = "assistantTTS_${System.currentTimeMillis()}"

        try {
            val speakResult = textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                params,
                utteranceId
            )

            Log.d(TAG, "TTS speak() called, result: $speakResult")

            when (speakResult) {
                TextToSpeech.SUCCESS -> Log.d(TAG, " TTS speak SUCCESS")
                TextToSpeech.ERROR -> Log.e(TAG, " TTS speak ERROR")
                else -> Log.w(TAG, " TTS speak returned: $speakResult")
            }

            // Also try without params as fallback
            if (speakResult == TextToSpeech.ERROR) {
                Log.d(TAG, "Retrying without params...")
                textToSpeech?.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    utteranceId
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while speaking: ${e.message}", e)

            // Last resort - try basic speak without any extras
            try {
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            } catch (e2: Exception) {
                Log.e(TAG, "Even basic speak failed: ${e2.message}", e2)
            }
        }

        Log.d(TAG, "=== SPEAK METHOD END ===")
    }

    /**
     * Release audio focus
     */
    private fun releaseAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
            Log.d(TAG, "Audio focus released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio focus: ${e.message}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
        textToSpeech?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()

        // Release audio focus
        releaseAudioFocus()

        textToSpeech?.shutdown()
        textToSpeech = null
        Log.d(TAG, "Accessibility Service Destroyed")
    }

    /**
     * Analyze the current screen and extract UI elements
     */
    private fun analyzeCurrentScreen(packageName: String? = null) {
        serviceScope.launch {
            try {
                val rootNode = rootInActiveWindow ?: return@launch
                val screenData = uiAnalyzer.extractScreen(rootNode)

                // Store current screen state for voice queries
                ScreenStateManager.updateScreen(screenData)

                Log.d(
                    TAG, "Screen analyzed: ${screenData.appPackageName}, " +
                            "${screenData.elements.size} elements found"
                )

                // Clean up
                rootNode.recycle()
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing screen", e)
            }
        }
    }

    /**
     * Programmatically click an element by text
     */
    fun clickElementByText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val node = findNodeByText(rootNode, text)

        return if (node != null && node.isClickable) {
            val result = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Clicked element: $text, success: $result")
            node.recycle()
            rootNode.recycle()
            result
        } else {
            rootNode.recycle()
            false
        }
    }

    /**
     * Type text into an editable field
     */
    fun typeText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val node = findEditableNode(rootNode)

        return if (node != null && node.isEditable) {
            val args = android.os.Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
                )
            }
            val result = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            Log.d(TAG, "Typed text: $text, success: $result")
            node.recycle()
            rootNode.recycle()
            result
        } else {
            rootNode.recycle()
            false
        }
    }

    /**
     * Scroll the screen
     */
    fun scroll(direction: ScrollDirection): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val action = when (direction) {
            ScrollDirection.UP -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            ScrollDirection.DOWN -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        }

        val result = rootNode.performAction(action)
        Log.d(TAG, "Scrolled ${direction.name}, success: $result")
        rootNode.recycle()
        return result
    }

    /**
     * Find node by text content (recursive search)
     */
    private fun findNodeByText(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        node ?: return null

        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeByText(child, text)
            if (result != null) {
                return result
            }
            child?.recycle()
        }

        return null
    }

    /**
     * Find first editable node (text field)
     */
    private fun findEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        node ?: return null

        if (node.isEditable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findEditableNode(child)
            if (result != null) {
                return result
            }
            child?.recycle()
        }

        return null
    }

    /**
     * Get current screen content as text summary
     */
    fun getCurrentScreenSummary(): String {
        val screenData = ScreenStateManager.getCurrentScreen()
        return buildString {
            appendLine("App: ${screenData.appPackageName}")
            appendLine("Elements on screen:")
            screenData.elements.take(20).forEach { element ->
                if (element.text.isNotEmpty()) {
                    appendLine("- ${element.text} [${if (element.isClickable) "clickable" else ""}]")
                }
            }
        }
    }
}

enum class ScrollDirection {
    UP, DOWN
}
