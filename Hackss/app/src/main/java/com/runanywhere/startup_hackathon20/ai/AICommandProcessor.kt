package com.runanywhere.startup_hackathon20.ai

import android.content.Context
import android.util.Log
import com.runanywhere.startup_hackathon20.accessibility.ScreenData
import com.runanywhere.startup_hackathon20.accessibility.ScrollDirection
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * AI-powered command processor that uses RunAnywhere LLM
 * to interpret user voice commands in context of current screen
 */
class AICommandProcessor(private val context: Context) {

    companion object {
        private const val TAG = "AICommandProcessor"
    }

    /**
     * Interpret a voice command in the context of current screen
     */
    suspend fun interpretCommand(
        userCommand: String,
        screenData: ScreenData
    ): CommandResponse {

        val prompt = buildPrompt(userCommand, screenData)

        Log.d(TAG, "Processing command: $userCommand")

        // Generate response using LLM
        val aiResponse = generateLLMResponse(prompt)

        // Parse and return structured response
        return parseResponse(aiResponse, userCommand)
    }

    /**
     * Build prompt for LLM with screen context
     */
    private fun buildPrompt(command: String, screenData: ScreenData): String {
        // Limit elements to prevent token overflow
        val limitedElements = screenData.elements
            .filter { it.text.isNotEmpty() || it.contentDescription?.isNotEmpty() == true }
            .take(30)

        val screenContent = limitedElements.joinToString("\n") { element ->
            buildString {
                if (element.text.isNotEmpty()) {
                    append("- Text: \"${element.text}\"")
                }
                if (element.contentDescription != null && element.contentDescription.isNotEmpty()) {
                    append(" Description: \"${element.contentDescription}\"")
                }
                if (element.isClickable) append(" [Clickable]")
                if (element.isEditable) append(" [Editable]")
            }.trim()
        }

        return """You are an accessibility assistant helping users navigate mobile apps.

CURRENT SCREEN:
App: ${screenData.appPackageName}
UI Elements:
$screenContent

USER COMMAND: "$command"

Analyze the command and respond in VALID JSON format:
{
  "action": "click|read|scroll|type|describe|unknown",
  "targetElement": "exact text of element to interact with",
  "textToRead": "text to speak to user",
  "textToType": "text to type if action is type",
  "direction": "up or down if scrolling",
  "explanation": "brief explanation"
}

Rules:
- Use "click" action if user wants to tap/press/select something
- Use "read" action if user asks what's on screen or to read something
- Use "scroll" action if user wants to scroll up/down
- Use "type" action if user wants to enter text
- Use "describe" action to explain what's on screen
- For "click", targetElement must match text from UI Elements list EXACTLY
- Keep textToRead concise and helpful
- Respond ONLY with valid JSON, no additional text"""
    }

    /**
     * Generate LLM response (placeholder - integrate with your RunAnywhere SDK)
     */
    private suspend fun generateLLMResponse(prompt: String): String =
        suspendCancellableCoroutine { continuation ->
            // TODO: Integrate with your existing RunAnywhere SDK
            // For now, using rule-based fallback

            try {
                // This is where you would call your LLM:
                // val modelManager = ModelManager.getInstance()
                // val response = StringBuilder()
                // modelManager.generateText(
                //     prompt = prompt,
                //     onToken = { token -> response.append(token) },
                //     onComplete = { continuation.resume(response.toString()) }
                // )

                // Fallback: rule-based interpretation
                val fallbackResponse = generateFallbackResponse(prompt)
                continuation.resume(fallbackResponse)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating LLM response", e)
                continuation.resume("""{"action":"unknown","explanation":"Error processing command"}""")
            }
        }

    /**
     * Fallback rule-based response when LLM is not available
     */
    private fun generateFallbackResponse(prompt: String): String {
        val command = prompt.substringAfter("USER COMMAND: \"").substringBefore("\"").lowercase()

        return when {
            command.contains("what") && (command.contains("screen") || command.contains("see")) -> {
                """{"action":"describe","textToRead":"Let me describe what's on screen","explanation":"Describing screen"}"""
            }

            command.contains("click") || command.contains("tap") || command.contains("press") -> {
                val element = extractElement(prompt, command)
                """{"action":"click","targetElement":"$element","explanation":"Clicking element"}"""
            }

            command.contains("scroll down") -> {
                """{"action":"scroll","direction":"down","explanation":"Scrolling down"}"""
            }

            command.contains("scroll up") -> {
                """{"action":"scroll","direction":"up","explanation":"Scrolling up"}"""
            }

            command.contains("type") || command.contains("enter") -> {
                val text = extractTextToType(command)
                """{"action":"type","textToType":"$text","explanation":"Typing text"}"""
            }

            command.contains("read") -> {
                """{"action":"read","textToRead":"Reading screen content","explanation":"Reading content"}"""
            }

            else -> {
                """{"action":"describe","textToRead":"I can help you click, scroll, read, or type. What would you like to do?","explanation":"Unknown command"}"""
            }
        }
    }

    private fun extractElement(prompt: String, command: String): String {
        // Try to extract element from command
        val elements = prompt.substringAfter("UI Elements:").substringBefore("USER COMMAND:")
            .lines()
            .filter { it.contains("Text:") }
            .map { it.substringAfter("Text: \"").substringBefore("\"") }
            .filter { it.isNotEmpty() }

        // Find best matching element
        for (element in elements) {
            if (command.contains(element.lowercase())) {
                return element
            }
        }

        return elements.firstOrNull() ?: ""
    }

    private fun extractTextToType(command: String): String {
        // Extract text after "type" or "enter"
        val afterType = command.substringAfter("type ", "")
        val afterEnter = command.substringAfter("enter ", "")
        return when {
            afterType.isNotEmpty() -> afterType.trim()
            afterEnter.isNotEmpty() -> afterEnter.trim()
            else -> ""
        }
    }

    /**
     * Parse AI response into structured command
     */
    private fun parseResponse(aiResponse: String, originalCommand: String): CommandResponse {
        return try {
            // Find JSON in response (handle cases where LLM adds extra text)
            val jsonStart = aiResponse.indexOf('{')
            val jsonEnd = aiResponse.lastIndexOf('}') + 1

            if (jsonStart == -1 || jsonEnd == 0) {
                throw IllegalArgumentException("No JSON found in response")
            }

            val jsonString = aiResponse.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonString)

            CommandResponse(
                action = CommandAction.fromString(json.optString("action", "unknown")),
                targetElement = json.optString("targetElement", null),
                textToRead = json.optString("textToRead", null),
                textToType = json.optString("textToType", null),
                scrollDirection = when (json.optString("direction", "").lowercase()) {
                    "up" -> ScrollDirection.UP
                    "down" -> ScrollDirection.DOWN
                    else -> null
                },
                explanation = json.optString("explanation", "Processing command")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing AI response: $aiResponse", e)
            // Fallback response
            CommandResponse(
                action = CommandAction.DESCRIBE,
                textToRead = "I'm having trouble understanding. Could you rephrase that?",
                explanation = "Parse error"
            )
        }
    }
}

/**
 * Available command actions
 */
enum class CommandAction {
    CLICK,      // Click an element
    READ,       // Read screen content
    SCROLL,     // Scroll up/down
    TYPE,       // Type text
    DESCRIBE,   // Describe what's on screen
    UNKNOWN;    // Unknown command

    companion object {
        fun fromString(value: String): CommandAction {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}

/**
 * Structured command response from AI
 */
data class CommandResponse(
    val action: CommandAction,
    val targetElement: String? = null,
    val textToRead: String? = null,
    val textToType: String? = null,
    val scrollDirection: ScrollDirection? = null,
    val explanation: String
)
