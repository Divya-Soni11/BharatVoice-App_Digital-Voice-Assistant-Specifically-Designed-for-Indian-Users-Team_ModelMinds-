# 🚀 Quick Start Guide - Voice Accessibility Assistant

Get up and running in 5 minutes!

## ✅ Prerequisites Checklist

- [ ] Android device (API 24+, Android 7.0+)
- [ ] USB cable (for installation)
- [ ] ~500 MB free storage
- [ ] ADB installed (or use Android Studio)

## 📝 Step-by-Step Setup

### Step 1: Build the App (2 minutes)

```bash
cd Hackss
./gradlew assembleDebug
```

**Windows users:**

```powershell
cd Hackss
.\gradlew.bat assembleDebug
```

### Step 2: Install on Device (1 minute)

**Via ADB:**

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Via Android Studio:**

- Open project in Android Studio
- Click "Run" button (▶️)
- Select your device

### Step 3: Enable Accessibility Service (1 minute)

1. Open the installed app
2. You'll see "Accessibility Service ✗ Not Enabled"
3. Tap "Enable" button
4. This opens Settings → Accessibility
5. Find "startup_hackathon2.0" in the list
6. Toggle it ON
7. Confirm the permission dialog
8. Return to app

**Alternative path:**

```
Settings → Accessibility → Downloaded apps → [Your App] → Use service (ON)
```

### Step 4: Grant Microphone Permission (30 seconds)

- App will automatically request this
- Tap "Allow" when prompted
- Or go to: Settings → Apps → [App Name] → Permissions → Microphone → Allow

### Step 5: Download AI Model (1 minute)

1. In the app, go to "Chat" tab
2. Tap "Models" button
3. Choose "SmolLM2 360M Q8_0" (119 MB - smallest)
4. Tap "Download"
5. Wait for download to complete
6. Tap "Load"
7. Wait for "Model loaded!" message

### Step 6: Test the Assistant! (30 seconds)

1. Go to "Assistant" tab
2. Verify green checkmark ✓ shows "Enabled"
3. Tap the large microphone button
4. Say: **"What's on this screen?"**
5. Listen to response!

## 🎉 You're Done!

Now open any app and try these commands:

- "What's on this screen?"
- "Read the screen"
- "Click [button name]"
- "Scroll down"

## 🐛 Quick Troubleshooting

### Problem: Accessibility won't enable

**Fix:** Some devices need manual activation:

```
Settings → Accessibility → [App Name] → Toggle ON manually
```

### Problem: Voice recognition not working

**Fix:** Check microphone permission:

```
Settings → Apps → [App Name] → Permissions → Microphone
```

### Problem: "No screen data available"

**Fix:** Accessibility service needs restart:

1. Disable service in Settings
2. Re-enable it
3. Return to app

### Problem: Model download fails

**Fix:**

- Check internet connection
- Ensure 200+ MB free space
- Try smaller model first

## 📱 Testing on Your Own Apps

### Good Apps to Start With:

1. **Settings app** - Simple UI, lots of buttons
2. **Calculator** - Easy to test clicks
3. **Notes app** - Test typing commands
4. **Browser** - Test scrolling

### Example Testing Flow:

**Open Settings App:**

```
You: "What's on this screen?"
AI: "You're in Settings. I see: WiFi, Bluetooth, Apps..."

You: "Click WiFi"
AI: *Clicks WiFi setting*

You: "Go back"
You: "Scroll down"
AI: *Scrolls the list*
```

**Open Calculator:**

```
You: "Click the number 5"
AI: *Taps 5 button*

You: "Click plus"
You: "Click 3"
You: "Click equals"
```

## 🎯 Next Steps

### Explore More Commands:

- "Type hello world" (in text field)
- "Read the price" (finds price on shopping apps)
- "Click the first button"
- "What buttons are there?"

### Customize:

- Try different AI models (Chat tab)
- Check example commands (Assistant tab → expand help)
- Test on different apps

### Learn More:

- Read `ACCESSIBILITY_ASSISTANT_README.md` for full documentation
- Read `ACCESSIBILITY_ASSISTANT_GUIDE.md` for implementation details

## 💡 Pro Tips

1. **Be Specific**: Instead of "click button", say "click submit button"
2. **Use Exact Text**: Say the exact button text you see
3. **One Action at a Time**: Don't chain multiple commands
4. **Background Operation**: Assistant monitors screen even when you switch apps
5. **Battery Saving**: Stop listening when not in use

## 🔗 Useful Commands Reference

| Command                       | Action                     | Example             |
|-------------------------------|----------------------------|---------------------|
| "What's on this screen?"      | Describes visible elements | Lists buttons, text |
| "Click [element]"             | Taps specified element     | "Click login"       |
| "Scroll down/up"              | Scrolls page               | "Scroll down"       |
| "Type [text]"                 | Types into text field      | "Type password123"  |
| "Read the screen"             | Reads all content          | Speaks everything   |

## 🎬 Demo Video Script

Want to show someone? Follow this script:

1. **Show app launch** → "Assistant" tab
2. **Point out green checkmark** → "Service enabled"
3. **Tap microphone** → "Now listening"
4. **Say command** → "What's on this screen?"
5. **Show response** → App speaks back
6. **Switch to another app** → e.g., Settings
7. **Return to assistant**
8. **Try click command** → "Click WiFi"
9. **Show it working** → WiFi setting opens!

## ⚡ Fast Track (For Experienced Developers)

```bash
# Build and install
./gradlew installDebug

# Enable accessibility via ADB (requires root/special setup)
# Usually must be done manually

# Download model via UI
# Load model via UI

# Test
adb logcat | grep "AccessibilityAssistant"
```

## 📞 Need Help?

- Check `ACCESSIBILITY_ASSISTANT_README.md` → Troubleshooting section
- Review logcat: `adb logcat | grep Accessibility`
- Open GitHub issue with logs

---

**Enjoy your voice-controlled Android experience! 🎙️📱**
