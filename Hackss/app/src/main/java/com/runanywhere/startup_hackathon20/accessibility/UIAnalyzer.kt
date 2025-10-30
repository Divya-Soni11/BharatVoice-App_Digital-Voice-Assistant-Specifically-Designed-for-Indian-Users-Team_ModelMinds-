package com.runanywhere.startup_hackathon20.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Represents a UI element extracted from the screen
 */
data class UIElement(
    val text: String,
    val className: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isFocusable: Boolean,
    val bounds: Rect,
    val viewId: String?,
    val contentDescription: String?
)

/**
 * Complete screen data snapshot
 */
data class ScreenData(
    val appPackageName: String,
    val elements: List<UIElement>,
    val hierarchy: String,
    val timestamp: Long
)

/**
 * Analyzes and extracts UI elements from accessibility tree
 */
class UIAnalyzer {

    /**
     * Extract complete screen information
     */
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

    /**
     * Recursively traverse the accessibility tree
     */
    private fun traverseNode(
        node: AccessibilityNodeInfo?,
        elements: MutableList<UIElement>,
        depth: Int = 0
    ) {
        node ?: return

        // Only extract meaningful elements
        if (shouldExtractNode(node)) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            elements.add(
                UIElement(
                    text = node.text?.toString() ?: "",
                    className = node.className?.toString() ?: "",
                    isClickable = node.isClickable,
                    isEditable = node.isEditable,
                    isFocusable = node.isFocusable,
                    bounds = bounds,
                    viewId = node.viewIdResourceName,
                    contentDescription = node.contentDescription?.toString()
                )
            )
        }

        // Recursively traverse children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            traverseNode(child, elements, depth + 1)
            child?.recycle()
        }
    }

    /**
     * Determine if a node should be extracted
     */
    private fun shouldExtractNode(node: AccessibilityNodeInfo): Boolean {
        return node.text != null ||
                node.contentDescription != null ||
                node.isClickable ||
                node.isEditable ||
                node.isCheckable
    }

    /**
     * Build a human-readable hierarchy string
     */
    private fun buildHierarchyString(elements: List<UIElement>): String {
        return elements.joinToString("\n") { element ->
            buildString {
                if (element.text.isNotEmpty()) {
                    append("Text: \"${element.text}\" ")
                }
                if (element.contentDescription != null && element.contentDescription.isNotEmpty()) {
                    append("Desc: \"${element.contentDescription}\" ")
                }
                if (element.isClickable) append("[Clickable] ")
                if (element.isEditable) append("[Editable] ")
                if (element.isFocusable) append("[Focusable] ")
                append("(${element.className.substringAfterLast('.')})")
            }.trim()
        }
    }

    /**
     * Find elements by text (fuzzy match)
     */
    fun findElementsByText(screenData: ScreenData, query: String): List<UIElement> {
        return screenData.elements.filter { element ->
            element.text.contains(query, ignoreCase = true) ||
                    element.contentDescription?.contains(query, ignoreCase = true) == true
        }
    }

    /**
     * Get all clickable elements
     */
    fun getClickableElements(screenData: ScreenData): List<UIElement> {
        return screenData.elements.filter { it.isClickable }
    }

    /**
     * Get all editable elements (text fields)
     */
    fun getEditableElements(screenData: ScreenData): List<UIElement> {
        return screenData.elements.filter { it.isEditable }
    }
}
