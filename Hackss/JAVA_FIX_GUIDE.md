# 🔧 Java Version Fix Guide

## ❌ The Problem

You're getting this error when building:

```
FAILURE: Build failed with an exception.
* What went wrong:
25
```

**The "25" refers to your Java version!** You have Java 25 installed, but Android Gradle Plugin
requires **Java 17**.

## ✅ Solution Options

### Option 1: Install Java 17 (Recommended)

#### Step 1: Download Java 17

**Oracle JDK 17** (requires account):

- Visit: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
- Download: Windows x64 Installer

**OpenJDK 17** (easier, no account needed):

- Visit: https://adoptium.net/temurin/releases/?version=17
- Select: **Operating System**: Windows, **Architecture**: x64, **Package Type**: JDK
- Download the `.msi` installer
- **Direct link**: https://adoptium.net/temurin/releases/?version=17

#### Step 2: Install Java 17

1. Run the downloaded installer
2. Use default installation path: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot\`
3. Complete the installation

#### Step 3: Configure Gradle to Use Java 17

Edit `Hackss/gradle.properties` and add this line (uncomment and update path):

```properties
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.13.11-hotspot
```

**Note**: Update the version number (`17.0.13.11`) to match what you installed.

#### Step 4: Build Again

```powershell
cd C:\Users\ckaur\OneDrive\Desktop\CGCHackathon\Hackss
.\gradlew.bat clean assembleDebug
```

---

### Option 2: Use Android Studio's JDK

If you have Android Studio installed, it comes with a compatible JDK.

#### Step 1: Find Android Studio's JDK Path

Typical locations:

```
C:\Program Files\Android\Android Studio\jbr
C:\Program Files\Android\Android Studio\jre
```

#### Step 2: Configure Gradle

Edit `Hackss/gradle.properties` and add:

```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

#### Step 3: Build

```powershell
.\gradlew.bat clean assembleDebug
```

---

### Option 3: Set JAVA_HOME Environment Variable (Temporary)

If you can't modify files, set environment variable for this session:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat clean assembleDebug
```

---

## 🔍 Verify Java Installation

After installing Java 17, verify:

```powershell
# Check if Java 17 is accessible
& "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot\bin\java.exe" -version

# Expected output:
# openjdk version "17.0.13" ...
```

---

## 📋 Why This Happened

- Android Gradle Plugin 8.7.x supports Java 17 (and 11)
- Java 25 is too new and not yet supported
- The error message "25" is cryptic but refers to Java version incompatibility

---

## 🎯 Quick Summary

1. **Download**: Java 17 from https://adoptium.net/temurin/releases/?version=17
2. **Install**: Run the installer
3. **Configure**: Add `org.gradle.java.home=` line to `gradle.properties`
4. **Build**: Run `.\gradlew.bat clean assembleDebug`

---

## 🚀 After Fixing

Once Java 17 is configured, the build should complete successfully and you'll see:

```
BUILD SUCCESSFUL in Xs
```

Then you can install the APK:

```powershell
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 💡 Alternative: Downgrade AGP (Not Recommended)

If you really want to keep Java 25, you could try AGP 8.8.0-alpha (experimental):

Edit `Hackss/gradle/libs.versions.toml`:

```toml
agp = "8.8.0-alpha08"
```

**But this is NOT recommended** - use Java 17 instead for stability.

---

## 📞 Still Having Issues?

If you still get errors after installing Java 17:

1. **Verify Java path** in gradle.properties is correct
2. **Restart PowerShell** to clear any cached environment variables
3. **Stop Gradle daemon**: `.\gradlew.bat --stop`
4. **Try build again**: `.\gradlew.bat clean assembleDebug`

---

**Good luck! Once you install Java 17, everything should work perfectly! 🎉**
