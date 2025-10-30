# Voice-Controlled Accessibility Assistant App - Implementation Guide

## Overview

This guide explains how to build an accessibility assistant that can:

- Read UI elements from other applications
- Respond to voice commands
- Provide navigation assistance
- Run in the background
- Use on-device AI for privacy

## Architecture Components

### 1. **Screen Reading & UI Access**

#### Android Approach (AccessibilityService)

**Key Technology**: Android Accessibility Service API

```kotlin
// This service runs in the background and has access to UI of all apps
class AppNavigatorAccessibilityService : AccessibilityService() {
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Receives UI events from other apps
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // UI changed, extract new elements
                analyzeScreen(event.source)
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                // User focused on an element
            }
        }
    }
    
    private fun analyzeScreen(rootNode: AccessibilityNodeInfo?) {
        // Extract all UI elements recursively
        val uiElements = extractUIHierarchy(rootNode)
        // Send to AI for understanding
        processWithAI(uiElements)
    }
}
```

**Capabilities**:

- ✅ Read text, buttons, labels from ANY app
- ✅ Detect clickable elements, text fields, etc.
- ✅ Programmatically click/tap elements
- ✅ Fill text fields
- ✅ Scroll, swipe, navigate
- ✅ Run in background continuously
- ✅ Works across all apps (with user permission)

**Permissions Required**:

```xml
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### 2. **Voice Command Processing**

#### Option A: On-Device Speech Recognition (Privacy-First)

```kotlin
class VoiceCommandProcessor {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    
    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }
    
    private val recognitionListener = object : RecognitionListener {
        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            )
            matches?.firstOrNull()?.let { command ->
                processCommand(command)
            }
        }
    }
}
```

#### Option B: Advanced Voice AI (Using Your RunAnywhere SDK)

You can use your existing LLM to process natural language commands:

```kotlin
// Convert speech to text, then process with LLM
val userCommand = "What's the price on this screen?"
val screenContext = getCurrentScreenContent()
val prompt = """
You are an accessibility assistant. 
Current screen shows: $screenContext
User asked: "$userCommand"
Provide helpful response or action.
"""
```

### 3. **Background Execution**

```kotlin
class AccessibilityBackgroundService : Service() {
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create persistent notification (required for foreground service)
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Keep listening for voice commands
        voiceListener.startContinuousListening()
        
        return START_STICKY // Restart if killed
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Accessibility Assistant Active")
            .setContentText("Tap to open • Say 'Hey Assistant' to activate")
            .setSmallIcon(R.drawable.ic_accessibility)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
```

## Complete Implementation Plan

### Phase 1: Core Accessibility Service

**File**: `app/src/main/java/com/your/app/AccessibilityService.kt`

```kotlin
class AccessibilityAssistantService : AccessibilityService() {
    
    private lateinit var uiAnalyzer: UIAnalyzer
    private lateinit var aiProcessor: AIProcessor
    
    override fun onCreate() {
        super.onCreate()
        uiAnalyzer = UIAnalyzer()
        aiProcessor = AIProcessor(this)
    }
    
    override fun onServiceConnected() {
        val config = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = config
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        val screenData = uiAnalyzer.extractScreen(rootNode)
        
        // Store current screen state for voice queries
        ScreenStateManager.updateScreen(screenData)
    }
    
    fun performAction(action: AssistantAction) {
        when (action) {
            is AssistantAction.Click -> {
                val node = findNodeByText(action.elementText)
                node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            is AssistantAction.TypeText -> {
                val node = findEditableNode()
                val args = Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        action.text
                    )
                }
                node?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            }
            is AssistantAction.Scroll -> {
                rootInActiveWindow?.performAction(
                    if (action.direction == "up") 
                        AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    else 
                        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                )
            }
        }
    }
}
```

### Phase 2: UI Analysis & Element Extraction

**File**: `app/src/main/java/com/your/app/UIAnalyzer.kt`

```kotlin
data class UIElement(
    val text: String,
    val className: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val bounds: Rect,
    val viewId: String?,
    val contentDescription: String?
)

data class ScreenData(
    val appPackageName: String,
    val elements: List<UIElement>,
    val hierarchy: String,
    val timestamp: Long
)

class UIAnalyzer {
    
    fun extractScreen(rootNode: AccessibilityNodeInfo): ScreenData {
        val elements = mutableListOf<UIElement>()
        traverseNode(rootNode, elements)
        
        return ScreenData(
            appPackageName = rootNode.packageName?.toString() ?: "unknown",
            elements = elements,
            hierarchy = buildHierarchyString(elements),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun traverseNode(
        node: AccessibilityNodeInfo?,
        elements: MutableList<UIElement>
    ) {
        node ?: return
        
        // Extract meaningful elements
        if (node.text != null || node.contentDescription != null || 
            node.isClickable || node.isEditable) {
            
            elements.add(UIElement(
                text = node.text?.toString() ?: "",
                className = node.className?.toString() ?: "",
                isClickable = node.isClickable,
                isEditable = node.isEditable,
                bounds = Rect().apply { node.getBoundsInScreen(this) },
                viewId = node.viewIdResourceName,
                contentDescription = node.contentDescription?.toString()
            ))
        }
        
        // Recursively traverse children
        for (i in 0 until node.childCount) {
            traverseNode(node.getChild(i), elements)
        }
    }
    
    private fun buildHierarchyString(elements: List<UIElement>): String {
        return elements.joinToString("\n") { element ->
            buildString {
                if (element.text.isNotEmpty()) append("Text: ${element.text} ")
                if (element.contentDescription != null) 
                    append("Description: ${element.contentDescription} ")
                if (element.isClickable) append("[Clickable] ")
                if (element.isEditable) append("[Editable] ")
                append("(${element.className})")
            }
        }
    }
}
```

### Phase 3: Voice Command Integration

**File**: `app/src/main/java/com/your/app/VoiceAssistant.kt`

```kotlin
class VoiceAssistant(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    fun initialize() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(recognitionListener)
    }
    
    fun startListening() {
        if (isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        
        speechRecognizer?.startListening(intent)
        isListening = true
    }
    
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            // Show listening indicator
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            )
            matches?.firstOrNull()?.let { command ->
                processVoiceCommand(command)
            }
            isListening = false
            
            // Restart listening if continuous mode
            if (continuousListeningEnabled) {
                Handler(Looper.getMainLooper()).postDelayed({
                    startListening()
                }, 500)
            }
        }
        
        override fun onError(error: Int) {
            isListening = false
            // Handle errors and retry if needed
        }
        
        // Other required overrides...
    }
    
    private fun processVoiceCommand(command: String) {
        val screenData = ScreenStateManager.getCurrentScreen()
        
        // Use AI to interpret command in context
        val response = interpretCommand(command, screenData)
        
        when (response.action) {
            "click" -> performClick(response.targetElement)
            "read" -> speakText(response.textToRead)
            "scroll" -> performScroll(response.direction)
            "type" -> performType(response.textToType)
            "describe" -> describeScreen(screenData)
        }
    }
}
```

### Phase 4: AI Integration (Using Your RunAnywhere SDK)

**File**: `app/src/main/java/com/your/app/AIProcessor.kt`

```kotlin
class AIProcessor(private val context: Context) {
    
    private val modelManager = ModelManager.getInstance()
    
    suspend fun interpretCommand(
        userCommand: String, 
        screenData: ScreenData
    ): CommandResponse {
        
        val prompt = buildPrompt(userCommand, screenData)
        
        val response = StringBuilder()
        modelManager.generateText(
            prompt = prompt,
            onToken = { token -> response.append(token) },
            onComplete = { /* done */ }
        )
        
        return parseResponse(response.toString())
    }
    
    private fun buildPrompt(command: String, screenData: ScreenData): String {
        return """
You are an accessibility assistant helping users navigate apps.

CURRENT SCREEN CONTENT:
App: ${screenData.appPackageName}
${screenData.hierarchy}

USER COMMAND: "$command"

Analyze the command and respond in JSON format:
{
  "action": "click|read|scroll|type|describe",
  "targetElement": "text of element to interact with",
  "textToRead": "text to speak to user",
  "textToType": "text to type if action is type",
  "direction": "up|down if scrolling",
  "explanation": "brief explanation of what you're doing"
}
        """.trimIndent()
    }
    
    private fun parseResponse(aiResponse: String): CommandResponse {
        // Parse JSON response from AI
        return try {
            val json = JSONObject(aiResponse)
            CommandResponse(
                action = json.getString("action"),
                targetElement = json.optString("targetElement"),
                textToRead = json.optString("textToRead"),
                textToType = json.optString("textToType"),
                direction = json.optString("direction"),
                explanation = json.getString("explanation")
            )
        } catch (e: Exception) {
            // Fallback parsing
            CommandResponse(action = "describe", explanation = aiResponse)
        }
    }
}

data class CommandResponse(
    val action: String,
    val targetElement: String? = null,
    val textToRead: String? = null,
    val textToType: String? = null,
    val direction: String? = null,
    val explanation: String
)
```

### Phase 5: Manifest Configuration

**File**: `app/src/main/AndroidManifest.xml`

```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <application>
        <!-- Accessibility Service -->
        <service
            android:name=".AccessibilityAssistantService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
        
        <!-- Background Service -->
        <service
            android:name=".AccessibilityBackgroundService"
            android:foregroundServiceType="microphone"
            android:exported="false" />
    </application>
</manifest>
```

**File**: `app/src/main/res/xml/accessibility_service_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackSpoken"
    android:accessibilityFlags="flagReportViewIds|flagRetrieveInteractiveWindows|flagRequestEnhancedWebAccessibility"
    android:canRetrieveWindowContent="true"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100"
    android:packageNames="@null"
    android:settingsActivity=".SettingsActivity" />
```

## User Flow

### 1. **Initial Setup**

1. User installs app
2. App requests Accessibility Service permission
3. User goes to Settings → Accessibility → Your App → Enable
4. User grants microphone permission
5. App downloads small AI model (e.g., SmolLM2 360M)

### 2. **Background Operation**

1. Service starts automatically on boot
2. Shows persistent notification "Assistant Active"
3. Listens for wake word or button press
4. Continuously monitors current screen state

### 3. **Voice Interaction**

```
User: "What's on this screen?"
→ App reads screen content
→ AI summarizes: "You're on Instagram feed with 5 posts visible..."

User: "Click the first post"
→ App finds first clickable post
→ Performs click action

User: "Read the price"
→ App scans for price-related text
→ Speaks: "The price is $29.99"

User: "Scroll down"
→ App performs scroll action
```

## Privacy Considerations

✅ **All processing on-device** (using RunAnywhere SDK)
✅ **No screen data sent to servers**
✅ **User controls which apps to monitor**
✅ **Transparent about data access**

## Technical Challenges & Solutions

### Challenge 1: Battery Usage

**Solution**:

- Only activate voice when wake word detected
- Throttle screen analysis (only on user request)
- Use efficient AI model (360M parameters)

### Challenge 2: Performance

**Solution**:

- Cache screen state (don't re-analyze constantly)
- Use quantized models (Q8_0, Q6_K)
- Lazy load AI model

### Challenge 3: Accuracy

**Solution**:

- Combine rule-based + AI approach
- Use confidence thresholds
- Allow user to correct and learn

## Alternative Approaches

### Windows Desktop Version

For screen reading on Windows:

- **UI Automation API** (Microsoft.Windows.Automation)
- **OCR** (Windows.Media.Ocr)
- **Screen capture** + Vision AI

### Web Extension Version

For browser-based assistance:

- **Chrome Extension** with content scripts
- **DOM access** to read page structure
- **Web Speech API** for voice

## Next Steps

1. **Start with Android AccessibilityService** (most feasible)
2. **Integrate with your existing RunAnywhere SDK**
3. **Build minimal prototype**:
    - Enable accessibility
    - Read current screen
    - Respond to one voice command
4. **Iterate and expand**

## Example Use Cases

- **Visually impaired users**: Navigate apps with voice
- **Hands-free operation**: While cooking, driving, etc.
- **Elderly users**: Simplify complex interfaces
- **Power users**: Automation and shortcuts
- **Learning**: "What does this button do?"

## Legal & Ethical Notes

⚠️ **Important**:

- Must comply with accessibility guidelines
- Clear privacy policy required
- Can't intercept sensitive data (passwords, banking)
- Must respect app developers' terms of service
- Some apps may block accessibility services

## Resources

- [Android Accessibility Service Guide](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Speech Recognition API](https://developer.android.com/reference/android/speech/SpeechRecognizer)
- [AccessibilityNodeInfo](https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo)

---

**Ready to build?** Start with the AccessibilityService implementation!
