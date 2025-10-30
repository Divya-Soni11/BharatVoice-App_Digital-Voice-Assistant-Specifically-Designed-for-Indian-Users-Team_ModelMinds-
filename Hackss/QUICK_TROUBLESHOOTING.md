# 🔧 Quick Troubleshooting - "Not Working At All"

## 📋 Let's Check Everything Step by Step

### ✅ Step 1: Is the App Installed?

**On your phone:**

- Look for the app icon: **"startup_hackathon2.0"**
- Can you see it?
    - ✅ YES → Go to Step 2
    - ❌ NO → Reinstall the app

### ✅ Step 2: Is Accessibility Service Enabled?

**On your phone:**

1. Go to **Settings**
2. **Accessibility**
3. Find **"startup_hackathon2.0"** in the list
4. Is the toggle **ON**?
    - ✅ YES → Go to Step 3
    - ❌ NO → Turn it ON, accept the warning, then go to Step 3

### ✅ Step 3: Do You See the Notification?

**Swipe down notification shade:**

- Do you see: **"Voice Assistant Active"**?
    - ✅ YES → Good! Service is running. Go to Step 4
    - ❌ NO → Service isn't starting. Go to "Service Not Starting" section below

### ✅ Step 4: Is Gallery Enabled in Apps Tab?

**In your app:**

1. Open the app
2. Go to **"Apps" tab** (third tab)
3. Is **Gallery** visible in the list?
    - ✅ YES → Go to Step 5
    - ❌ NO → Gallery might not be detected. Try another app like **Settings**

### ✅ Step 5: Is Gallery Set to "Always On"?

**In Apps tab:**

1. Tap **Gallery** icon (should show checkmark)
2. Tap it **again** → Bottom sheet opens
3. Is **"Always On"** selected with a checkmark?
    - ✅ YES → Go to Step 6
    - ❌ NO → Select "Always On", close sheet

### ✅ Step 6: Test with Gallery

1. **Press Home button**
2. **Open Gallery app** from home screen
3. **Wait 2 seconds**
4. **Do you hear anything?**
    - ✅ YES → IT WORKS! 🎉
    - ❌ NO → Go to "No Audio" section below

---

## 🚨 Service Not Starting (No Notification)

If you don't see the "Voice Assistant Active" notification:

### Fix 1: Force Stop and Restart

1. **Settings** → **Apps** → **startup_hackathon2.0**
2. Tap **"Force Stop"**
3. Go to **Settings** → **Accessibility**
4. Find your app, toggle **OFF** then **ON** again
5. Check notification shade - should appear now

### Fix 2: Check Permissions

1. **Settings** → **Apps** → **startup_hackathon2.0**
2. **Permissions** → Make sure **Microphone** is **Allowed**

### Fix 3: MIUI Battery Settings (Xiaomi phones)

1. **Settings** → **Apps** → **Manage apps**
2. Find **startup_hackathon2.0**
3. **Battery saver** → Select **"No restrictions"**
4. **Autostart** → **Enable**
5. Restart accessibility service

---

## 🔇 No Audio (Service Running But Silent)

If notification is visible but no audio:

### Check 1: Volume

1. **Press volume UP button**
2. **Swipe down the volume slider**
3. Check **ALL volume channels**:
    - Media: 50%+
    - Call: 50%+
    - Ring: 50%+
    - **Notification: MUST BE 50%+** ← Most important!
    - Alarm: 50%+

### Check 2: Do Not Disturb

1. **Swipe down notification shade twice**
2. Is **DND** icon active?
3. If YES → Tap to **disable it**

### Check 3: Silent Mode

- Make sure phone isn't on **silent mode**
- Should be on **ring** or **vibrate**

### Check 4: Test TTS Manually

1. **Settings** → **System** → **Languages & input**
2. **Text-to-speech output**
3. Tap **gear icon** next to engine
4. Tap **"Play"** or **"Listen to example"**
5. **Do you hear it?**
    - ✅ YES → TTS works, issue is with our app
    - ❌ NO → Install "Google Text-to-Speech" from Play Store

---

## 🔍 Get Debug Logs

If still not working, get the logs:

```powershell
$env:Path += ";C:\Users\ckaur\Downloads\platform-tools-latest-windows\platform-tools"
adb logcat -c
adb logcat | Select-String "AccessibilityAssistant"
```

**Leave this running, then:**

1. Open Gallery
2. Wait 5 seconds
3. Copy **ALL** the log output
4. Share it with me

---

## 🎯 Most Common Issues

### Issue: App crashes on launch

**Fix:** Uninstall completely, reinstall fresh

### Issue: Accessibility service won't enable

**Fix:**

1. Uninstall app
2. Restart phone
3. Reinstall app
4. Enable accessibility service

### Issue: Notification appears then disappears

**Fix:** MIUI is killing it

1. Settings → Apps → Battery saver → No restrictions
2. Settings → Apps → Autostart → Enable

### Issue: Audio was working before, now it's not

**Fix:**

1. Check notification volume (most common!)
2. Restart phone
3. Re-enable accessibility service

---

## ✅ Quick Checklist

Copy this and check each:

- [ ] App installed
- [ ] Accessibility service enabled
- [ ] Notification "Voice Assistant Active" visible
- [ ] Gallery enabled in Apps tab
- [ ] Gallery set to "Always On"
- [ ] Notification volume > 50%
- [ ] Do Not Disturb OFF
- [ ] Phone not on silent
- [ ] TTS manual test works
- [ ] MIUI battery restrictions disabled

---

## 🆘 Still Not Working?

Share with me:

1. **Which step failed?** (1-6 above)
2. **Do you see the notification?** (YES/NO)
3. **Logcat output** (use command above)
4. **Phone model and Android version**

I'll help you debug further! 🚀