package com.runanywhere.startup_hackathon20.accessibility

import java.util.concurrent.atomic.AtomicReference

/**
 * Singleton manager to store and retrieve current screen state
 * Thread-safe storage of the current screen data
 */
object ScreenStateManager {

    private val currentScreen = AtomicReference<ScreenData?>(null)
    private val screenHistory = mutableListOf<ScreenData>()
    private const val MAX_HISTORY_SIZE = 10

    /**
     * Update the current screen state
     */
    fun updateScreen(screenData: ScreenData) {
        val previous = currentScreen.getAndSet(screenData)

        // Add to history if different from previous
        if (previous != null && previous.appPackageName != screenData.appPackageName) {
            synchronized(screenHistory) {
                screenHistory.add(previous)
                if (screenHistory.size > MAX_HISTORY_SIZE) {
                    screenHistory.removeAt(0)
                }
            }
        }
    }

    /**
     * Get the current screen state
     */
    fun getCurrentScreen(): ScreenData {
        return currentScreen.get() ?: ScreenData(
            appPackageName = "none",
            elements = emptyList(),
            hierarchy = "No screen data available",
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Get screen history
     */
    fun getScreenHistory(): List<ScreenData> {
        return synchronized(screenHistory) {
            screenHistory.toList()
        }
    }

    /**
     * Clear all data
     */
    fun clear() {
        currentScreen.set(null)
        synchronized(screenHistory) {
            screenHistory.clear()
        }
    }

    /**
     * Check if screen data is available
     */
    fun hasScreenData(): Boolean {
        return currentScreen.get() != null
    }
}
