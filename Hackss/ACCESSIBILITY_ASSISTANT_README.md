# 🎙️ Voice-Controlled Accessibility Assistant

A powerful Android app that helps users navigate other applications using voice commands. Built with
on-device AI for complete privacy.

## 🌟 Features

### Core Capabilities

- **📱 Screen Reading**: Access and understand UI elements from any app
- **🗣️ Voice Commands**: Navigate apps hands-free with natural language
- **🤖 AI-Powered**: Intelligent command interpretation using on-device LLM
- **🔒 Privacy-First**: All processing happens locally on your device
- **🎯 Accessibility**: Helps visually impaired and hands-free users

### Supported Actions

- **Click** buttons and interactive elements
- **Scroll** up and down through content
- **Read** screen content aloud
- **Type** text into fields
- **Describe** what's currently on screen
- **Navigate** between apps

## 🏗️ Architecture

```
┌─────────────────┐
│  Voice Input    │ ← User speaks
└────────┬────────┘
         ↓
┌─────────────────┐
│ Speech Recognition│ (Android SpeechRecognizer)
└────────┬────────┘
         ↓
┌─────────────────┐
│  AI Processor   │ ← Interprets command + screen context
│ (RunAnywhere SDK)│
└────────┬────────┘
         ↓
┌─────────────────────────────────┐
│  Accessibility Service          │
│  • Reads UI from other apps     │
│  • Performs actions (click, etc)│
│  • Monitors screen changes      │
└─────────────────────────────────┘
         ↓
┌─────────────────┐
│  Target App     │ ← User's destination app
└─────────────────┘
```

## 📦 Technology Stack

- **Android Accessibility Service API**: Screen reading and interaction
- **Android Speech Recognition**: Voice-to-text conversion
- **Text-to-Speech (TTS)**: Voice feedback to user
- **RunAnywhere SDK**: On-device LLM for command interpretation
- **Jetpack Compose**: Modern UI
- **Kotlin Coroutines**: Asynchronous operations
- **MVVM Architecture**: Clean separation of concerns

## 🚀 Getting Started

### Prerequisites

- Android device with API 24+ (Android 7.0+)
- ~500 MB free storage (for AI model)
- Microphone access
- Accessibility service permissions

### Installation

1. **Build and Install**
   ```bash
   cd Hackss
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Enable Accessibility Service**
    - Open the app
    - Tap "Enable" on the Accessibility Service card
    - Or: Settings → Accessibility → [App Name] → Toggle ON
    - Grant permission

3. **Grant Microphone Permission**
    - The app will request this automatically
    - Or: Settings → Apps → [App Name] → Permissions → Microphone

4. **Download AI Model**
    - Go to "Chat" tab
    - Tap "Models"
    - Download "SmolLM2 360M Q8_0" (recommended, 119 MB)
    - Tap "Load" to activate

## 🎯 Usage

### Basic Workflow

1. **Launch the App**
    - Open the app
    - Switch to "Assistant" tab
    - Verify Accessibility Service is enabled (green checkmark)

2. **Open Target App**
    - Navigate to any app you want to control
    - The assistant monitors screen in background

3. **Give Voice Commands**
    - Return to assistant app (or use from notification)
    - Tap microphone button
    - Speak your command
    - Wait for confirmation

### Example Commands

#### Reading Content

```
"What's on this screen?"
"Read the screen"
"What do I see here?"
```

**Response**: AI describes visible elements

#### Clicking Elements

```
"Click the submit button"
"Tap on login"
"Press the menu icon"
```

**Action**: Finds and clicks the specified element

#### Scrolling

```
"Scroll down"
"Scroll up"
"Go down"
```

**Action**: Scrolls the current view

#### Typing Text

```
"Type hello world"
"Enter my email"
"Type search query"
```

**Action**: Types into focused text field

## 🏛️ Project Structure

```
Hackss/app/src/main/java/com/runanywhere/startup_hackathon20/
│
├── accessibility/
│   ├── AccessibilityAssistantService.kt  # Core service for screen reading
│   ├── UIAnalyzer.kt                     # Extracts UI elements
│   ├── ScreenStateManager.kt             # Stores current screen state
│   └── [Data classes]
│
├── voice/
│   └── VoiceAssistant.kt                 # Speech recognition + TTS
│
├── ai/
│   └── AICommandProcessor.kt             # LLM-powered command interpretation
│
├── AssistantViewModel.kt                 # Coordinates all components
├── AssistantScreen.kt                    # Main UI for voice assistant
├── MainActivity.kt                       # App entry point
└── [Other files...]
```

## 🔧 Configuration

### Accessibility Service Config

`app/src/main/res/xml/accessibility_service_config.xml`

```xml
<accessibility-service
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:canRetrieveWindowContent="true"
    android:packageNames="@null"  <!-- null = all apps -->
    ... />
```

### Permissions Required

`app/src/main/AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 🎨 UI Components

### Main Screen

- **Service Status Card**: Shows if accessibility is enabled
- **Microphone Button**: Large FAB with animation when listening
- **Status Display**: Shows current command and response
- **Commands Help**: Expandable list of example commands

### States

- 🔴 **Disabled**: Accessibility service not enabled
- 🟢 **Ready**: All permissions granted, ready to listen
- 🔵 **Listening**: Actively recording voice
- 🟡 **Processing**: AI interpreting command
- ⚫ **Executing**: Performing action

## 🧠 How It Works

### 1. Screen Analysis

```kotlin
// Accessibility service captures UI hierarchy
val rootNode = rootInActiveWindow
val screenData = uiAnalyzer.extractScreen(rootNode)

// Extract elements
screenData.elements.forEach { element ->
    println("${element.text} [${element.isClickable}]")
}
```

### 2. Voice Command Processing

```kotlin
// User speaks → Speech recognizer converts to text
voiceAssistant.startListening { command ->
    // "Click the submit button"
    processCommand(command)
}
```

### 3. AI Interpretation

```kotlin
// AI analyzes command + screen context
val response = aiProcessor.interpretCommand(
    userCommand = "Click the submit button",
    screenData = currentScreen
)

// Response: 
// { action: "click", targetElement: "Submit" }
```

### 4. Action Execution

```kotlin
// Perform the action via Accessibility Service
when (response.action) {
    CLICK -> {
        service.clickElementByText(response.targetElement)
    }
    SCROLL -> {
        service.scroll(response.direction)
    }
    // ... etc
}
```

## 🔐 Privacy & Security

### ✅ Privacy Features

- **No data collection**: Nothing sent to servers
- **On-device AI**: All processing local
- **User control**: Explicit permission required
- **Transparent**: User sees all actions

### ⚠️ Important Notes

- **Cannot read passwords**: Input fields marked as sensitive are hidden
- **Banking apps**: Some apps block accessibility for security
- **App restrictions**: Developers can prevent accessibility access

## 🎯 Use Cases

### Primary Use Cases

1. **Visually Impaired Users**: Navigate apps without seeing screen
2. **Motor Impairments**: Control apps without touch
3. **Hands-Free Operation**: Use phone while cooking, driving, etc.
4. **Elderly Users**: Simplify complex interfaces
5. **Power Users**: Automate repetitive tasks

### Example Scenarios

**Scenario 1: Cooking**

- User follows recipe on phone
- Hands are messy/wet
- Says "Scroll down" to continue reading

**Scenario 2: Accessibility**

- Visually impaired user opens Instagram
- Says "What's on this screen?"
- AI: "You're on Instagram feed. I see 5 posts..."
- User: "Click the first post"

**Scenario 3: Multitasking**

- User working on laptop
- Says "Type meeting notes" into phone
- Continues working without touching phone

## 🚧 Known Limitations

1. **Some apps block accessibility**: Banking, secure apps
2. **Accuracy depends on UI quality**: Poorly labeled UIs are harder
3. **Battery usage**: Voice recognition and AI use power
4. **Language**: Currently optimized for English
5. **Complex gestures**: Can't do pinch-to-zoom, double-tap, etc.

## 🔮 Future Enhancements

### Planned Features

- [ ] **Wake word detection** ("Hey Assistant...")
- [ ] **Continuous listening mode**
- [ ] **Custom voice shortcuts**
- [ ] **Multi-language support**
- [ ] **OCR for images** (read text from images)
- [ ] **Gesture support** (swipe, pinch)
- [ ] **Learning mode** (train on your vocabulary)
- [ ] **App-specific profiles** (custom commands per app)

### Advanced Ideas

- **Screen recording + playback**: Automate workflows
- **Voice-controlled phone settings**: "Turn on WiFi"
- **Integration with smart home**: "Show security camera"
- **Collaborative features**: Share voice shortcuts

## 🐛 Troubleshooting

### Accessibility Service Won't Enable

- **Solution**: Check Settings → Accessibility, enable manually
- **Cause**: Some Android versions require manual activation

### Voice Recognition Not Working

- **Check**: Microphone permission granted?
- **Check**: Is microphone physically blocked?
- **Try**: Speak more clearly, reduce background noise

### Commands Not Executing

- **Check**: Is accessibility service still enabled?
- **Check**: Is target app blocking accessibility?
- **Try**: Re-enable accessibility service
- **Debug**: Check logcat for errors (`adb logcat | grep Accessibility`)

### AI Responses Are Wrong

- **Solution**: Use more specific commands
- **Solution**: Describe elements by their exact text
- **Note**: Fallback to rule-based if LLM not loaded

### App Crashes

- **Check**: Sufficient memory? Try smaller AI model
- **Check**: Device API level 24+?
- **Report**: Share logcat output

## 📚 Technical Deep Dive

### Accessibility Service Lifecycle

```
onCreate() → onServiceConnected() → onAccessibilityEvent() → onDestroy()
```

### Event Flow

```
User opens app → TYPE_WINDOW_STATE_CHANGED
User scrolls → TYPE_VIEW_SCROLLED
Button appears → TYPE_WINDOW_CONTENT_CHANGED
```

### Memory Management

- UI nodes are recycled after use
- Screen history limited to 10 items
- LLM loads on-demand

## 🤝 Contributing

This is a hackathon project, but contributions welcome!

### Areas to Improve

- Better AI prompt engineering
- More robust element matching
- Additional languages
- UI enhancements
- Performance optimization

## 📄 License

See main project LICENSE file.

## 🙏 Acknowledgments

- **RunAnywhere SDK**: On-device LLM inference
- **Android Accessibility API**: Core functionality
- **Google Speech Services**: Voice recognition

## 📞 Support

For issues:

1. Check troubleshooting section
2. Review logcat logs
3. Open GitHub issue with details

---

**Built with ❤️ for CGC Hackathon**

*Making technology accessible to everyone, everywhere.*
