package com.runanywhere.startup_hackathon20.managers

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.runanywhere.startup_hackathon20.models.AppConfig
import com.runanywhere.startup_hackathon20.models.AssistanceMode
import com.runanywhere.startup_hackathon20.models.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppConfigManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_config_prefs",
        Context.MODE_PRIVATE
    )

    private val packageManager: PackageManager = context.packageManager

    companion object {
        private const val TAG = "AppConfigManager"
        private const val KEY_ENABLED_APPS = "enabled_apps"
        private const val KEY_APP_MODES = "app_modes"
        private const val KEY_FLOATING_BUTTON = "floating_button_enabled"
        private const val KEY_AUTO_READ = "auto_read_enabled"

        // Popular apps to show by default
        val POPULAR_APPS = listOf(
            // Messaging
            "com.whatsapp",
            "com.whatsapp.w4b", // WhatsApp Business

            // Social Media
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.linkedin.android",
            "com.snapchat.android",

            // Google Apps
            "com.google.android.youtube",
            "com.google.android.gm", // Gmail
            "com.google.android.apps.maps",
            "com.google.android.googlequicksearchbox", // Google app
            "com.android.chrome",

            // Payment Apps
            "com.phonepe.app",
            "com.google.android.apps.nbu.paisa.user", // Google Pay
            "net.one97.paytm", // Paytm
            "in.org.npci.upiapp", // BHIM

            // Shopping
            "com.amazon.mShop.android.shopping",
            "in.amazon.mShop.android.shopping",
            "com.flipkart.android",
            "com.myntra.android",

            // Entertainment
            "com.spotify.music",
            "com.netflix.mediaclient",
            "in.startv.hotstar", // Disney+ Hotstar
            "com.jio.media.jiobeats", // JioSaavn

            // Utilities
            "com.android.settings",
            "com.android.camera2",
            "com.google.android.apps.photos",

            // Food Delivery
            "com.application.zomato",
            "in.swiggy.android",

            // Transportation
            "com.olacabs.customer",
            "com.ubercab",

            // Education
            "com.duolingo",
            "org.khanacademy.android"
        )
    }

    // Get all installed apps using Intent query (most reliable method)
    suspend fun getInstalledApps(includeSystemApps: Boolean = true): List<InstalledAppInfo> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "=== Starting App Detection ===")

                // Use Intent to find all launchable apps (most reliable)
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val launchableApps = packageManager.queryIntentActivities(intent, 0)

                Log.d(TAG, "Found ${launchableApps.size} launchable apps from Intent query")

                val appList = launchableApps.mapNotNull { resolveInfo ->
                    try {
                        val packageName = resolveInfo.activityInfo.packageName

                        // Skip our own app
                        if (packageName == context.packageName) {
                            return@mapNotNull null
                        }

                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val isUpdatedSystemApp =
                            (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                        // Skip pure system apps if not including them
                        if (!includeSystemApps && isSystemApp && !isUpdatedSystemApp) {
                            return@mapNotNull null
                        }

                        val appName = appInfo.loadLabel(packageManager).toString()
                        val icon = appInfo.loadIcon(packageManager)

                        // Log popular apps specifically
                        if (packageName in POPULAR_APPS) {
                            Log.d(TAG, "★ Found popular app from query: $appName ($packageName)")
                        }

                        InstalledAppInfo(
                            packageName = packageName,
                            appName = appName,
                            icon = icon,
                            isSystemApp = isSystemApp && !isUpdatedSystemApp
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing app: ${e.message}")
                        null
                    }
                }.toMutableList()

                Log.d(TAG, "Processed ${appList.size} apps from initial query")

                // FORCE CHECK: Explicitly check for popular apps even if not in query
                Log.d(TAG, "=== Force Checking Popular Apps ===")
                POPULAR_APPS.forEach { packageName ->
                    // Check if already in list
                    if (appList.none { it.packageName == packageName }) {
                        try {
                            // First check if package is installed at all
                            val appInfo = packageManager.getApplicationInfo(packageName, 0)
                            val appName = appInfo.loadLabel(packageManager).toString()
                            val icon = appInfo.loadIcon(packageManager)

                            // Successfully got app info, so it's installed
                            appList.add(
                                InstalledAppInfo(
                                    packageName = packageName,
                                    appName = appName,
                                    icon = icon,
                                    isSystemApp = false
                                )
                            )
                            Log.d(
                                TAG,
                                "★★ FORCE ADDED missing popular app: $appName ($packageName)"
                            )
                        } catch (e: Exception) {
                            // App truly not installed
                            Log.d(TAG, "✗ Popular app not installed: $packageName")
                        }
                    }
                }

                val sortedList = appList.sortedBy { it.appName.lowercase() }
                Log.d(TAG, "=== FINAL: Returning ${sortedList.size} total apps ===")

                // Log all found apps for debugging
                sortedList.forEach { app ->
                    if (app.packageName in POPULAR_APPS) {
                        Log.d(TAG, "  ★ ${app.appName} (${app.packageName})")
                    }
                }

                sortedList
            } catch (e: Exception) {
                Log.e(TAG, "Error getting installed apps: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Get popular apps that are actually installed
    suspend fun getPopularInstalledApps(): List<InstalledAppInfo> {
        return withContext(Dispatchers.IO) {
            val popularList = POPULAR_APPS.mapNotNull { packageName ->
                try {
                    // Check if app exists and has launcher intent
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        val appName = appInfo.loadLabel(packageManager).toString()
                        val icon = appInfo.loadIcon(packageManager)

                        Log.d(TAG, "Popular app found: $appName ($packageName)")

                        InstalledAppInfo(
                            packageName = packageName,
                            appName = appName,
                            icon = icon,
                            isSystemApp = false
                        )
                    } else {
                        Log.d(TAG, "Popular app not installed: $packageName")
                        null
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.d(TAG, "Popular app not found: $packageName")
                    null
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking popular app $packageName: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Found ${popularList.size} popular apps installed")
            popularList
        }
    }

    // Check if app is enabled
    fun isAppEnabled(packageName: String): Boolean {
        val enabledApps = getEnabledApps()
        return packageName in enabledApps
    }

    // Get enabled apps
    fun getEnabledApps(): Set<String> {
        return prefs.getStringSet(KEY_ENABLED_APPS, emptySet()) ?: emptySet()
    }

    // Toggle app enabled status
    fun setAppEnabled(packageName: String, enabled: Boolean) {
        val enabledApps = getEnabledApps().toMutableSet()
        if (enabled) {
            enabledApps.add(packageName)
            Log.d(TAG, "Enabled app: $packageName")
        } else {
            enabledApps.remove(packageName)
            Log.d(TAG, "Disabled app: $packageName")
        }
        prefs.edit().putStringSet(KEY_ENABLED_APPS, enabledApps).apply()
    }

    // Get assistance mode for an app
    fun getAssistanceMode(packageName: String): AssistanceMode {
        val modeString = prefs.getString("mode_$packageName", AssistanceMode.ON_DEMAND.name)
        return try {
            AssistanceMode.valueOf(modeString ?: AssistanceMode.ON_DEMAND.name)
        } catch (e: IllegalArgumentException) {
            AssistanceMode.ON_DEMAND
        }
    }

    // Set assistance mode for an app
    fun setAssistanceMode(packageName: String, mode: AssistanceMode) {
        prefs.edit().putString("mode_$packageName", mode.name).apply()
        Log.d(TAG, "Set mode for $packageName: $mode")
    }

    // Get floating button preference
    fun isFloatingButtonEnabled(): Boolean {
        return prefs.getBoolean(KEY_FLOATING_BUTTON, true)
    }

    // Set floating button preference
    fun setFloatingButtonEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_BUTTON, enabled).apply()
    }

    // Get auto-read preference
    fun isAutoReadEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_READ, false)
    }

    // Set auto-read preference
    fun setAutoReadEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_READ, enabled).apply()
    }

    // Get app config
    suspend fun getAppConfig(packageName: String): AppConfig? {
        return withContext(Dispatchers.IO) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                AppConfig(
                    packageName = packageName,
                    appName = appInfo.loadLabel(packageManager).toString(),
                    appIcon = appInfo.loadIcon(packageManager),
                    isEnabled = isAppEnabled(packageName),
                    assistanceMode = getAssistanceMode(packageName)
                )
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "App not found: $packageName")
                null
            }
        }
    }

    // Get all configured apps
    suspend fun getConfiguredApps(): List<AppConfig> {
        val enabledPackages = getEnabledApps()
        return enabledPackages.mapNotNull { packageName ->
            getAppConfig(packageName)
        }
    }
}
