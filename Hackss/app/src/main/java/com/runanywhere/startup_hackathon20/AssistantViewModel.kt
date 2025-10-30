package com.runanywhere.startup_hackathon20

import android.app.Application
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.accessibility.AccessibilityAssistantService
import com.runanywhere.startup_hackathon20.accessibility.ScreenStateManager
import com.runanywhere.startup_hackathon20.ai.AICommandProcessor
import com.runanywhere.startup_hackathon20.ai.CommandAction
import com.runanywhere.startup_hackathon20.voice.VoiceAssistant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that coordinates voice commands, AI processing, and accessibility actions
 */
class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val voiceAssistant = VoiceAssistant(application)
    private val aiProcessor = AICommandProcessor(application)

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AssistantViewModel"
    }

    init {
        // Initialize voice assistant
        voiceAssistant.initialize {
            _uiState.value = _uiState.value.copy(
                isVoiceReady = true,
                statusMessage = "Voice assistant ready"
            )
        }
    }

    /**
     * Check if accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        return AccessibilityAssistantService.getInstance() != null
    }

    /**
     * Open accessibility settings
     */
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }

    /**
     * Start listening for voice commands
     */
    fun startListening() {
        if (!isAccessibilityServiceEnabled()) {
            _uiState.value = _uiState.value.copy(
                statusMessage = "Please enable Accessibility Service first",
                isError = true
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isListening = true,
            statusMessage = "Listening...",
            isError = false
        )

        voiceAssistant.startListening { command ->
            onVoiceCommand(command)
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        voiceAssistant.stopListening()
        _uiState.value = _uiState.value.copy(
            isListening = false,
            statusMessage = "Stopped listening"
        )
    }

    /**
     * Process voice command
     */
    private fun onVoiceCommand(command: String) {
        Log.d(TAG, "Voice command received: $command")

        _uiState.value = _uiState.value.copy(
            lastCommand = command,
            isProcessing = true,
            statusMessage = "Processing: $command"
        )

        viewModelScope.launch {
            try {
                // Get current screen data
                val screenData = ScreenStateManager.getCurrentScreen()

                if (screenData.elements.isEmpty()) {
                    speakAndUpdate("No screen data available. Make sure the accessibility service is running.")
                    return@launch
                }

                // Use AI to interpret command
                val response = aiProcessor.interpretCommand(command, screenData)

                Log.d(TAG, "AI Response: ${response.action}, ${response.explanation}")

                // Execute action based on AI response
                when (response.action) {
                    CommandAction.CLICK -> {
                        response.targetElement?.let { element ->
                            val service = AccessibilityAssistantService.getInstance()
                            val success = service?.clickElementByText(element) ?: false

                            if (success) {
                                speakAndUpdate("Clicked $element")
                            } else {
                                speakAndUpdate("Couldn't find $element on screen")
                            }
                        } ?: speakAndUpdate("I don't know what to click")
                    }

                    CommandAction.SCROLL -> {
                        response.scrollDirection?.let { direction ->
                            val service = AccessibilityAssistantService.getInstance()
                            val success = service?.scroll(direction) ?: false

                            if (success) {
                                speakAndUpdate("Scrolled ${direction.name.lowercase()}")
                            } else {
                                speakAndUpdate("Couldn't scroll")
                            }
                        }
                    }

                    CommandAction.TYPE -> {
                        response.textToType?.let { text ->
                            val service = AccessibilityAssistantService.getInstance()
                            val success = service?.typeText(text) ?: false

                            if (success) {
                                speakAndUpdate("Typed: $text")
                            } else {
                                speakAndUpdate("Couldn't find text field")
                            }
                        }
                    }

                    CommandAction.READ, CommandAction.DESCRIBE -> {
                        val textToRead = response.textToRead ?: buildScreenDescription(screenData)
                        speakAndUpdate(textToRead)
                    }

                    CommandAction.UNKNOWN -> {
                        speakAndUpdate(response.textToRead ?: "I didn't understand that command")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing command", e)
                speakAndUpdate("Sorry, I encountered an error")
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /**
     * Build screen description from current screen data
     */
    private fun buildScreenDescription(screenData: com.runanywhere.startup_hackathon20.accessibility.ScreenData): String {
        val elements = screenData.elements
            .filter { it.text.isNotEmpty() }
            .take(10)

        if (elements.isEmpty()) {
            return "The screen appears to be empty"
        }

        return buildString {
            append("You're in ${screenData.appPackageName}. ")
            append("I can see: ")
            append(elements.joinToString(", ") { it.text })
        }
    }

    /**
     * Speak text and update UI
     */
    private fun speakAndUpdate(text: String) {
        voiceAssistant.speak(text)
        _uiState.value = _uiState.value.copy(
            statusMessage = text,
            lastResponse = text,
            isError = false
        )
    }

    /**
     * Get current screen summary
     */
    fun getCurrentScreenSummary(): String {
        val service = AccessibilityAssistantService.getInstance()
        return service?.getCurrentScreenSummary() ?: "Accessibility service not running"
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.destroy()
    }
}

/**
 * UI State for the assistant
 */
data class AssistantUiState(
    val isVoiceReady: Boolean = false,
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val lastCommand: String = "",
    val lastResponse: String = "",
    val statusMessage: String = "Initializing...",
    val isError: Boolean = false
)
