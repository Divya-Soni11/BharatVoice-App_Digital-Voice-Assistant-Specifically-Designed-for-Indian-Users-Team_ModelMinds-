# 📊 Data Flow Diagrams - Voice Accessibility Assistant

## Overview

This document provides comprehensive data flow diagrams showing how data flows through the
application, from user input to execution of actions.

---

## 🎯 High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER'S PHONE                            │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    FRONTEND LAYER                        │  │
│  │  ┌────────────────┐         ┌───────────────────┐       │  │
│  │  │ AssistantScreen│◄────────│  MainActivity     │       │  │
│  │  │  (Compose UI)  │         │   (Entry Point)   │       │  │
│  │  └───────┬────────┘         └───────────────────┘       │  │
│  │          │                                                │  │
│  │          │ User Taps Microphone                          │  │
│  │          ▼                                                │  │
│  │  ┌──────────────────────────────────────────────┐        │  │
│  │  │        AssistantViewModel                    │        │  │
│  │  │        (Business Logic)                      │        │  │
│  │  └────┬─────────────────┬──────────────┬───────┘        │  │
│  └───────┼─────────────────┼──────────────┼────────────────┘  │
│          │                 │              │                    │
│  ┌───────▼─────────────────▼──────────────▼────────────────┐  │
│  │                  SERVICE LAYER                          │  │
│  │  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  │  │
│  │  │VoiceAssistant│  │AICommandProc │  │AccessibilityServ│ │
│  │  │ (Voice I/O) │  │  (AI Brain)  │  │ (Screen Reader)│  │
│  │  └──────┬──────┘  └──────┬───────┘  └────────┬────────┘  │
│  └─────────┼────────────────┼──────────────────┼───────────┘  │
│            │                │                  │              │
│  ┌─────────▼────────────────▼──────────────────▼───────────┐  │
│  │                  DATA LAYER                             │  │
│  │  ┌──────────────┐  ┌────────────┐  ┌────────────────┐  │  │
│  │  │ScreenState   │  │  LLM Model │  │  Android APIs  │  │  │
│  │  │   Manager    │  │  (Local)   │  │  (System)      │  │  │
│  │  └──────────────┘  └────────────┘  └────────────────┘  │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              TARGET APPLICATIONS                        │  │
│  │   (Instagram, Gmail, Settings, Any App on Phone)        │  │
│  └─────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Complete Data Flow - User Voice Command

### Flow 1: Voice Command Execution

```
┌──────────────────────────────────────────────────────────────────┐
│  STEP 1: USER INTERACTION                                        │
└──────────────────────────────────────────────────────────────────┘
                              ▼
    ┌─────────────────────────────────────────────┐
    │  User taps microphone button                │
    │  Location: AssistantScreen.kt (UI)          │
    └────────────────┬────────────────────────────┘
                     │
                     │ onClick Event
                     ▼
    ┌─────────────────────────────────────────────┐
    │  viewModel.startListening()                 │
    │  Location: AssistantViewModel.kt            │
    │  Data: UiState { isListening = true }       │
    └────────────────┬────────────────────────────┘
                     │
                     │ Calls
                     ▼

┌──────────────────────────────────────────────────────────────────┐
│  STEP 2: VOICE CAPTURE                                           │
└──────────────────────────────────────────────────────────────────┘
                     ▼
    ┌─────────────────────────────────────────────┐
    │  voiceAssistant.startListening()            │
    │  Location: VoiceAssistant.kt                │
    │  Component: Android SpeechRecognizer        │
    └────────────────┬────────────────────────────┘
                     │
                     │ Initializes
                     ▼
    ┌─────────────────────────────────────────────┐
    │  SpeechRecognizer API                       │
    │  - Starts microphone                        │
    │  - Records audio                            │
    │  - Converts to text (Google Speech API)     │
    └────────────────┬────────────────────────────┘
                     │
                     │ Returns: String (voice command)
                     ▼
    ┌─────────────────────────────────────────────┐
    │  onResults(results: Bundle)                 │
    │  Data: command = "Click the WiFi button"    │
    └────────────────┬────────────────────────────┘
                     │
                     │ Callback
                     ▼

┌──────────────────────────────────────────────────────────────────┐
│  STEP 3: SCREEN CONTEXT RETRIEVAL                                │
└──────────────────────────────────────────────────────────────────┘
                     ▼
    ┌─────────────────────────────────────────────┐
    │  ScreenStateManager.getCurrentScreen()      │
    │  Location: ScreenStateManager.kt            │
    │  Returns: ScreenData object                 │
    └────────────────┬────────────────────────────┘
                     │
                     │ Contains:
                     ▼
    ┌─────────────────────────────────────────────┐
    │  ScreenData {                               │
    │    appPackageName: "com.android.settings"   │
    │    elements: [                              │
    │      UIElement {                            │
    │        text: "WiFi",                        │
    │        isClickable: true,                   │
    │        bounds: Rect(10, 100, 500, 200)      │
    │      },                                     │
    │      UIElement {                            │
    │        text: "Bluetooth",                   │
    │        isClickable: true,                   │
    │        ...                                  │
    │      }                                      │
    │    ],                                       │
    │    hierarchy: "Screen hierarchy string",    │
    │    timestamp: 1738012345678                 │
    │  }                                          │
    └────────────────┬────────────────────────────┘
                     │
                     │ Pass to AI
                     ▼

┌──────────────────────────────────────────────────────────────────┐
│  STEP 4: AI COMMAND INTERPRETATION                               │
└──────────────────────────────────────────────────────────────────┘
                     ▼
    ┌─────────────────────────────────────────────┐
    │  aiProcessor.interpretCommand()             │
    │  Location: AICommandProcessor.kt            │
    │  Input:                                     │
    │    - command: "Click the WiFi button"       │
    │    - screenData: ScreenData object          │
    └────────────────┬────────────────────────────┘
                     │
                     │ Builds prompt
                     ▼
    ┌─────────────────────────────────────────────┐
    │  LLM Prompt:                                │
    │  "You are an accessibility assistant.       │
    │   CURRENT SCREEN:                           │
    │   App: com.android.settings                 │
    │   UI Elements:                              │
    │   - Text: 'WiFi' [Clickable]                │
    │   - Text: 'Bluetooth' [Clickable]           │
    │   USER COMMAND: 'Click the WiFi button'     │
    │   Respond in JSON format..."                │
    └────────────────┬────────────────────────────┘
                     │
                     │ Sends to LLM
                     ▼
    ┌─────────────────────────────────────────────┐
    │  On-Device LLM (RunAnywhere SDK)            │
    │  Location: Local model file                 │
    │  Model: SmolLM2 360M Q8_0 (119 MB)          │
    │  Processing: ~1-2 seconds                   │
    └────────────────┬────────────────────────────┘
                     │
                     │ Returns JSON
                     ▼
    ┌─────────────────────────────────────────────┐
    │  AI Response (JSON):                        │
    │  {                                          │
    │    "action": "click",                       │
    │    "targetElement": "WiFi",                 │
    │    "textToRead": "Clicking WiFi",           │
    │    "explanation": "User wants to click WiFi"│
    │  }                                          │
    └────────────────┬────────────────────────────┘
                     │
                     │ Parse JSON
                     ▼
    ┌─────────────────────────────────────────────┐
    │  CommandResponse {                          │
    │    action: CommandAction.CLICK,             │
    │    targetElement: "WiFi",                   │
    │    textToRead: "Clicking WiFi",             │
    │    explanation: "User wants to click WiFi"  │
    │  }                                          │
    └────────────────┬────────────────────────────┘
                     │
                     │ Return to ViewModel
                     ▼

┌──────────────────────────────────────────────────────────────────┐
│  STEP 5: ACTION EXECUTION                                        │
└──────────────────────────────────────────────────────────────────┘
                     ▼
    ┌─────────────────────────────────────────────┐
    │  when (response.action) {                   │
    │    CommandAction.CLICK -> {                 │
    │      service.clickElementByText("WiFi")     │
    │    }                                        │
    │  }                                          │
    │  Location: AssistantViewModel.kt            │
    └────────────────┬────────────────────────────┘
                     │
                     │ Calls accessibility service
                     ▼
    ┌─────────────────────────────────────────────┐
    │  AccessibilityAssistantService              │
    │    .clickElementByText("WiFi")              │
    │  Location: AccessibilityAssistantService.kt │
    └────────────────┬────────────────────────────┘
                     │
                     │ Searches UI tree
                     ▼
    ┌─────────────────────────────────────────────┐
    │  findNodeByText(rootNode, "WiFi")           │
    │  - Traverses accessibility tree             │
    │  - Finds node with text "WiFi"              │
    │  - Returns: AccessibilityNodeInfo           │
    └────────────────┬────────────────────────────┘
                     │
                     │ Found node
                     ▼
    ┌─────────────────────────────────────────────┐
    │  node.performAction(ACTION_CLICK)           │
    │  - Android system performs click            │
    │  - Settings app receives touch event        │
    └────────────────┬────────────────────────────┘
                     │
                     │ Success
                     ▼

┌──────────────────────────────────────────────────────────────────┐
│  STEP 6: USER FEEDBACK                                           │
└──────────────────────────────────────────────────────────────────┘
                     ▼
    ┌─────────────────────────────────────────────┐
    │  voiceAssistant.speak("Clicked WiFi")       │
    │  Location: VoiceAssistant.kt                │
    │  Component: Android TextToSpeech            │
    └────────────────┬────────────────────────────┘
                     │
                     │ Audio output
                     ▼
    ┌─────────────────────────────────────────────┐
    │  User hears: "Clicked WiFi"                 │
    └────────────────┬────────────────────────────┘
                     │
                     │ Update UI
                     ▼
    ┌─────────────────────────────────────────────┐
    │  _uiState.value = UiState(                  │
    │    lastCommand: "Click the WiFi button",    │
    │    lastResponse: "Clicked WiFi",            │
    │    statusMessage: "Clicked WiFi",           │
    │    isListening: false                       │
    │  )                                          │
    └────────────────┬────────────────────────────┘
                     │
                     │ UI recomposes
                     ▼
    ┌─────────────────────────────────────────────┐
    │  Screen updates showing:                    │
    │  - Command: "Click the WiFi button"         │
    │  - Response: "Clicked WiFi"                 │
    │  - Status: Ready for next command           │
    └─────────────────────────────────────────────┘
```

---

## 🔍 Background Screen Monitoring Flow

This runs continuously while other apps are active:

```
┌──────────────────────────────────────────────────────────────────┐
│  BACKGROUND PROCESS (Always Running)                             │
└──────────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────────────────┐
    │  User opens Instagram                       │
    └────────────────┬────────────────────────────┘
                     │
                     │ Android system event
                     ▼
    ┌─────────────────────────────────────────────┐
    │  AccessibilityService.onAccessibilityEvent()│
    │  Event Type: TYPE_WINDOW_STATE_CHANGED      │
    │  Package: com.instagram.android             │
    └────────────────┬────────────────────────────┘
                     │
                     │ Check throttle (1 second)
                     ▼
    ┌─────────────────────────────────────────────┐
    │  if (currentTime - lastAnalysis > 1000ms)   │
    │    analyzeCurrentScreen()                   │
    └────────────────┬────────────────────────────┘
                     │
                     │ Get UI tree
                     ▼
    ┌─────────────────────────────────────────────┐
    │  val rootNode = rootInActiveWindow          │
    │  Component: Android Accessibility API       │
    └────────────────┬────────────────────────────┘
                     │
                     │ Extract elements
                     ▼
    ┌─────────────────────────────────────────────┐
    │  uiAnalyzer.extractScreen(rootNode)         │
    │  Location: UIAnalyzer.kt                    │
    └────────────────┬────────────────────────────┘
                     │
                     │ Recursive traversal
                     ▼
    ┌─────────────────────────────────────────────┐
    │  traverseNode(node, elements)               │
    │  For each node:                             │
    │    - Extract text                           │
    │    - Check if clickable                     │
    │    - Get bounds                             │
    │    - Get content description                │
    │    - Recurse to children                    │
    └────────────────┬────────────────────────────┘
                     │
                     │ Build ScreenData
                     ▼
    ┌─────────────────────────────────────────────┐
    │  ScreenData {                               │
    │    appPackageName: "com.instagram.android", │
    │    elements: [                              │
    │      UIElement("Profile", clickable=true),  │
    │      UIElement("Plus", clickable=true),     │
    │      UIElement("Home", clickable=true),     │
    │      ...                                    │
    │    ],                                       │
    │    timestamp: currentTime                   │
    │  }                                          │
    └────────────────┬────────────────────────────┘
                     │
                     │ Store in memory
                     ▼
    ┌─────────────────────────────────────────────┐
    │  ScreenStateManager.updateScreen(screenData)│
    │  - Stores in AtomicReference (thread-safe)  │
    │  - Keeps history of last 10 screens         │
    │  - Overwrites old data                      │
    └────────────────┬────────────────────────────┘
                     │
                     │ Clean up
                     ▼
    ┌─────────────────────────────────────────────┐
    │  rootNode.recycle()                         │
    │  - Frees memory                             │
    │  - Prevents memory leaks                    │
    └─────────────────────────────────────────────┘

    (Process repeats for every screen change)
```

---

## 🗄️ Data Storage Architecture

### In-Memory Data Store (No Database)

```
┌──────────────────────────────────────────────────────────────────┐
│  DATA LAYER - All data stored in RAM                            │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  ScreenStateManager (Singleton)     │
│  Location: ScreenStateManager.kt    │
│  Storage Type: AtomicReference      │
├─────────────────────────────────────┤
│  currentScreen: ScreenData? = null  │
│  - Package name                     │
│  - List of UIElements               │
│  - Hierarchy string                 │
│  - Timestamp                        │
│                                     │
│  screenHistory: List<ScreenData>    │
│  - Max 10 items                     │
│  - FIFO queue                       │
│  - Previous screens                 │
└─────────────────────────────────────┘
           │
           │ Access methods:
           ├──► getCurrentScreen()
           ├──► updateScreen(data)
           ├──► getScreenHistory()
           └──► clear()

┌─────────────────────────────────────┐
│  AssistantViewModel                 │
│  Location: AssistantViewModel.kt    │
│  Storage Type: StateFlow            │
├─────────────────────────────────────┤
│  uiState: StateFlow<AssistantUiState>│
│    - isVoiceReady: Boolean          │
│    - isListening: Boolean           │
│    - isProcessing: Boolean          │
│    - lastCommand: String            │
│    - lastResponse: String           │
│    - statusMessage: String          │
│    - isError: Boolean               │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  LLM Model (File System)            │
│  Location: Android internal storage │
│  Path: /data/user/0/[package]/files│
├─────────────────────────────────────┤
│  Model files:                       │
│  - SmolLM2-360M-Q8_0.gguf (119 MB)  │
│  - Qwen-2.5-0.5B-Q6_K.gguf (374 MB) │
│                                     │
│  Managed by: RunAnywhere SDK        │
│  Loaded into: RAM when needed       │
└─────────────────────────────────────┘
```

---

## 🎨 Frontend Components Data Flow

```
┌──────────────────────────────────────────────────────────────────┐
│  UI LAYER (Jetpack Compose)                                      │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  MainActivity.kt                                                │
├─────────────────────────────────────────────────────────────────┤
│  @Composable MainScreen()                                       │
│    │                                                            │
│    ├─► TabRow (selectedTab)                                    │
│    │     ├─► Tab 0: "Chat"                                     │
│    │     └─► Tab 1: "Assistant" ← Focus here                   │
│    │                                                            │
│    └─► when (selectedTab) {                                    │
│          0 -> ChatScreen()                                     │
│          1 -> AssistantScreen() ← Our component                │
│        }                                                        │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  AssistantScreen.kt                                             │
├─────────────────────────────────────────────────────────────────┤
│  @Composable AssistantScreen(viewModel)                         │
│    │                                                            │
│    ├─► val uiState by viewModel.uiState.collectAsState()       │
│    │   Data Flow: ViewModel StateFlow → Compose State          │
│    │                                                            │
│    ├─► ServiceStatusCard(                                      │
│    │     isEnabled: Boolean,         ← from viewModel         │
│    │     onEnableClick: () -> Unit   ← calls viewModel        │
│    │   )                                                       │
│    │   Shows: ✓ Enabled or ✗ Not Enabled                      │
│    │                                                            │
│    ├─► MicrophoneButton(                                       │
│    │     isListening: uiState.isListening,                    │
│    │     isProcessing: uiState.isProcessing,                  │
│    │     onStartListening: { viewModel.startListening() },    │
│    │     onStopListening: { viewModel.stopListening() }       │
│    │   )                                                       │
│    │   Visual States:                                          │
│    │     - Blue: Ready                                         │
│    │     - Red: Listening (animated)                           │
│    │     - Yellow: Processing (spinner)                        │
│    │                                                            │
│    ├─► StatusDisplay(                                          │
│    │     statusMessage: uiState.statusMessage,                │
│    │     lastCommand: uiState.lastCommand,                    │
│    │     lastResponse: uiState.lastResponse,                  │
│    │     isError: uiState.isError                             │
│    │   )                                                       │
│    │   Shows: Real-time feedback to user                      │
│    │                                                            │
│    └─► CommandsHelpCard()                                     │
│        Shows: Example commands (expandable)                    │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Data Binding: StateFlow → Compose                              │
├─────────────────────────────────────────────────────────────────┤
│  ViewModel emits:                                               │
│    _uiState.value = AssistantUiState(...)                       │
│              │                                                  │
│              ▼                                                  │
│    val uiState: StateFlow<AssistantUiState>                     │
│              │                                                  │
│              ▼                                                  │
│    Compose collectAsState()                                    │
│              │                                                  │
│              ▼                                                  │
│    UI automatically recomposes                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## ⚙️ Service Layer Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│  SERVICE LAYER - Business Logic & System Integration            │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  AssistantViewModel                 │
│  (Coordinator / Controller)         │
├─────────────────────────────────────┤
│  Dependencies:                      │
│    - VoiceAssistant                 │
│    - AICommandProcessor             │
│    - AccessibilityService (static)  │
│                                     │
│  Public Methods:                    │
│    ├─► startListening()             │
│    ├─► stopListening()              │
│    ├─► openAccessibilitySettings()  │
│    └─► getCurrentScreenSummary()    │
│                                     │
│  Private Methods:                   │
│    ├─► onVoiceCommand(String)       │
│    ├─► speakAndUpdate(String)       │
│    └─► buildScreenDescription()     │
└─────────────────┬───────────────────┘
                  │
                  │ Uses
                  ▼
┌─────────────────────────────────────┐
│  VoiceAssistant.kt                  │
│  (Voice I/O Handler)                │
├─────────────────────────────────────┤
│  System APIs:                       │
│    - SpeechRecognizer              │
│    - TextToSpeech                  │
│                                    │
│  Methods:                          │
│    ├─► initialize()                │
│    ├─► startListening(callback)   │
│    ├─► stopListening()             │
│    ├─► speak(text)                 │
│    └─► destroy()                   │
│                                    │
│  Data Flow:                        │
│    Audio → Speech API → Text       │
│    Text → TTS API → Audio          │
└─────────────────┬───────────────────┘
                  │
                  │ Parallel to
                  ▼
┌─────────────────────────────────────┐
│  AICommandProcessor.kt              │
│  (AI Brain)                         │
├─────────────────────────────────────┤
│  Methods:                           │
│    ├─► interpretCommand()           │
│    ├─► buildPrompt()                │
│    ├─► generateLLMResponse()        │
│    └─► parseResponse()              │
│                                     │
│  Data Types:                        │
│    - Input: String + ScreenData     │
│    - Output: CommandResponse        │
│                                     │
│  LLM Integration:                   │
│    - RunAnywhere SDK                │
│    - On-device inference            │
│    - No network calls               │
└─────────────────┬───────────────────┘
                  │
                  │ Parallel to
                  ▼
┌─────────────────────────────────────┐
│  AccessibilityAssistantService.kt   │
│  (Screen Reader & Actor)            │
├─────────────────────────────────────┤
│  Android Service:                   │
│    - Extends AccessibilityService   │
│    - Runs in background             │
│    - System-level permissions       │
│                                     │
│  Methods:                           │
│    ├─► onAccessibilityEvent()       │
│    ├─► clickElementByText()         │
│    ├─► typeText()                   │
│    ├─► scroll()                     │
│    └─► getCurrentScreenSummary()    │
│                                     │
│  Data Flow:                         │
│    System Events → Extract Data     │
│    Commands → Perform Actions       │
└─────────────────────────────────────┘
```

---

## 🔌 External API Integrations

```
┌──────────────────────────────────────────────────────────────────┐
│  ANDROID SYSTEM APIs                                             │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Accessibility Service API                                       │
├─────────────────────────────────────────────────────────────────┤
│  Package: android.accessibilityservice                          │
│  Class: AccessibilityService                                    │
│                                                                 │
│  Key Methods Used:                                              │
│    - rootInActiveWindow: AccessibilityNodeInfo                  │
│    - onAccessibilityEvent(AccessibilityEvent)                   │
│    - performGlobalAction(int)                                   │
│                                                                 │
│  Data Provided:                                                 │
│    - Complete UI tree of any app                                │
│    - Text content                                               │
│    - Click/focus events                                         │
│    - Window state changes                                       │
│                                                                 │
│  Actions Available:                                             │
│    - ACTION_CLICK                                               │
│    - ACTION_SET_TEXT                                            │
│    - ACTION_SCROLL_FORWARD                                      │
│    - ACTION_SCROLL_BACKWARD                                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Speech Recognition API                                          │
├─────────────────────────────────────────────────────────────────┤
│  Package: android.speech                                        │
│  Class: SpeechRecognizer                                        │
│                                                                 │
│  Setup:                                                         │
│    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)            │
│    - EXTRA_LANGUAGE_MODEL                                       │
│    - EXTRA_PARTIAL_RESULTS                                      │
│                                                                 │
│  Data Flow:                                                     │
│    Audio Input → Google Speech API → Text Output               │
│                                                                 │
│  Events:                                                        │
│    - onReadyForSpeech()                                         │
│    - onResults(Bundle)                                          │
│    - onError(int)                                               │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Text-to-Speech API                                             │
├─────────────────────────────────────────────────────────────────┤
│  Package: android.speech.tts                                    │
│  Class: TextToSpeech                                            │
│                                                                 │
│  Setup:                                                         │
│    TextToSpeech(context, onInitListener)                        │
│    - setLanguage(Locale)                                        │
│                                                                 │
│  Data Flow:                                                     │
│    Text Input → TTS Engine → Audio Output                      │
│                                                                 │
│  Methods:                                                       │
│    - speak(text, queueMode, params, utteranceId)                │
│    - stop()                                                     │
│    - shutdown()                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  RunAnywhere SDK (Local LLM)                                    │
├─────────────────────────────────────────────────────────────────┤
│  Package: com.runanywhere.sdk                                   │
│                                                                 │
│  Components:                                                    │
│    - ModelManager: Download & load models                       │
│    - LlamaCpp Module: Inference engine                          │
│                                                                 │
│  Data Flow:                                                     │
│    Text Prompt → Model Inference → Generated Text               │
│                                                                 │
│  Model Storage:                                                 │
│    Location: /data/data/[package]/files/models/                │
│    Format: GGUF (quantized)                                     │
│    Loading: Into RAM when needed                                │
│                                                                 │
│  No Network: All processing on-device                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔐 Permission Flow

```
┌──────────────────────────────────────────────────────────────────┐
│  PERMISSIONS & SECURITY                                          │
└──────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  1. App Installation                │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  Manifest Permissions Declared:     │
│    - INTERNET                       │
│    - RECORD_AUDIO                   │
│    - FOREGROUND_SERVICE             │
│    - POST_NOTIFICATIONS             │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  2. First Launch                    │
│  MainActivity.onCreate()            │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  Request RECORD_AUDIO               │
│  (Runtime permission)               │
│                                     │
│  User sees: "Allow to record audio?"│
│    - Allow                          │
│    - Deny                           │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  3. User Taps "Enable" Button       │
│  viewModel.openAccessibilitySettings│
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  Intent to Accessibility Settings   │
│  Settings.ACTION_ACCESSIBILITY_SETTINGS│
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  User in System Settings:           │
│  Accessibility → startup_hackathon2.0│
│                                     │
│  Sees Warning:                      │
│  "This app will be able to:         │
│   - Observe your actions            │
│   - Retrieve window content         │
│   - Perform actions for you"        │
│                                     │
│  Toggle: OFF → ON                   │
└────────────────┬────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────┐
│  System Binds Service               │
│  AccessibilityService.onServiceConnected│
│                                     │
│  Service now has:                   │
│    - Read all app UIs               │
│    - Perform clicks                 │
│    - Type text                      │
│    - Scroll                         │
└─────────────────────────────────────┘
```

---

## 📈 Performance & Optimization

```
┌──────────────────────────────────────────────────────────────────┐
│  PERFORMANCE CONSIDERATIONS                                      │
└──────────────────────────────────────────────────────────────────┘

┌────────────���────────────────────────┐
│  Screen Analysis Throttling         │
├─────────────────────────────────────┤
│  Problem: UI changes frequently     │
│  Solution: Analyze max once/second  │
│                                     │
│  Implementation:                    │
│    var lastAnalysisTime = 0L        │
│    if (current - last < 1000ms)     │
│      return // Skip                 │
│                                     │
│  Benefit: Saves CPU & battery       │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Memory Management                  │
├─────────────────────────────────────┤
│  AccessibilityNodeInfo Recycling:   │
│    rootNode.recycle()               │
│    - Prevents memory leaks          │
│    - Frees system resources         │
│                                     │
│  Screen History Limit:              │
│    - Max 10 screens stored          │
│    - FIFO queue                     │
│    - Old screens discarded          │
│                                     │
│  LLM Model:                         │
│    - Loaded on-demand               │
│    - Stays in RAM once loaded       │
│    - Quantized (Q8_0, Q6_K)         │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Async Operations                   │
├─────────────────────────────────────┤
│  Coroutines:                        │
│    - Screen analysis: Dispatchers.Default│
│    - UI updates: Dispatchers.Main   │
│    - LLM inference: Dispatchers.IO  │
│                                     │
│  Non-blocking:                      │
│    - Voice recognition              │
│    - AI processing                  │
│    - Accessibility actions          │
└─────────────────────────────────────┘
```

---

## 🎯 Summary: Key Data Flows

### 1. **Voice Command Flow**

```
User Voice → SpeechRecognizer → Text Command → AI Processor → 
CommandResponse → Accessibility Service → Action Performed → 
TTS Feedback → User
```

### 2. **Background Monitoring Flow**

```
App Screen Change → Accessibility Event → UI Analysis → 
Extract Elements → Store in ScreenStateManager → 
Ready for Voice Commands
```

### 3. **State Management Flow**

```
ViewModel State Change → StateFlow Emission → 
Compose collectAsState() → UI Recomposition → 
User Sees Update
```

### 4. **No Database**

```
All data in RAM:
  - Current screen state (ScreenStateManager)
  - UI state (ViewModel StateFlow)
  - LLM model (Loaded into memory)
  - No persistent storage (privacy feature)
```

---

## 🔗 Component Dependencies

```
MainActivity
    └─► AssistantScreen
            └─► AssistantViewModel
                    ├─► VoiceAssistant
                    │       ├─► SpeechRecognizer (Android)
                    │       └─► TextToSpeech (Android)
                    │
                    ├─► AICommandProcessor
                    │       └─► RunAnywhere SDK
                    │               └─► LLM Model File
                    │
                    └─► AccessibilityAssistantService (singleton)
                            ├─► UIAnalyzer
                            │       └─► AccessibilityNodeInfo (Android)
                            │
                            └─► ScreenStateManager
                                    └─► ScreenData (in memory)
```

---

This data flow diagram shows how your voice accessibility assistant processes commands from start to
finish, with all components, data types, and interactions clearly mapped out! 🎉
