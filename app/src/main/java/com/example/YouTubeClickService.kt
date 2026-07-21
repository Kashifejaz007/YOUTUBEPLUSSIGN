package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class YouTubeClickService : AccessibilityService() {

    companion object {
        const val TAG = "YouTubeClickService"
        
        // State variables to ensure we only auto-click when launched from our app
        var shouldAutoClick = false
        var lastLaunchTime = 0L
        
        fun markAppLaunched() {
            shouldAutoClick = true
            lastLaunchTime = System.currentTimeMillis()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isClickInProgress = false

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Only run if the launcher app initiated the redirection within the last 15 seconds
        if (!shouldAutoClick || System.currentTimeMillis() - lastLaunchTime > 15000) {
            return
        }

        val packageName = event.packageName?.toString()
        if (packageName == "com.google.android.youtube") {
            if (isClickInProgress) return
            
            isClickInProgress = true
            // Debounce/delay slightly to let YouTube UI settle and layout completely
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                performAutoClickSequence()
            }, 2500) // Wait 2.5 seconds for YouTube to render
        }
    }

    private fun performAutoClickSequence() {
        Log.d(TAG, "Attempting to locate and click Create button...")
        val rootNode = rootInActiveWindow
        
        if (rootNode != null) {
            val clicked = findAndClickCreateNode(rootNode)
            if (clicked) {
                Log.d(TAG, "Successfully clicked Create button using accessibility node action.")
                shouldAutoClick = false
                isClickInProgress = false
                rootNode.recycle()
                return
            }
            rootNode.recycle()
        }

        // Fallback: Dispatch a precise gesture touch at the bottom-center of the screen
        Log.d(TAG, "Node search failed or returned false. Dispatching fallback gesture tap...")
        dispatchBottomCenterGesture()
    }

    private fun findAndClickCreateNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        // Check content description for "Create", "Plus", "Add", or language equivalents
        val desc = node.contentDescription?.toString()?.lowercase()
        if (desc != null) {
            if (desc.contains("create") || desc.contains("plus") || desc.contains("add") || desc.contains("post")) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                } else {
                    // If the specific node isn't marked clickable, try clicking its parent
                    var parent = node.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            parent.recycle()
                            return true
                        }
                        val nextParent = parent.parent
                        parent.recycle()
                        parent = nextParent
                    }
                }
            }
        }

        // Recursively inspect child nodes
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (findAndClickCreateNode(child)) {
                child?.recycle()
                return true
            }
            child?.recycle()
        }

        return false
    }

    private fun dispatchBottomCenterGesture() {
        try {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // The "+" button is located at the exact bottom center of the YouTube app.
            // Calculate screen coordinates dynamically: X is center, Y is slightly above navigation bar/bottom edge.
            val targetX = (screenWidth / 2).toFloat()
            val targetY = (screenHeight - (displayMetrics.density * 36)) // Dynamic offset from the bottom edge

            val path = Path().apply {
                moveTo(targetX, targetY)
            }

            val stroke = GestureDescription.StrokeDescription(path, 0, 100)
            val gestureBuilder = GestureDescription.Builder().apply {
                addStroke(stroke)
            }

            dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Log.d(TAG, "Gesture completed successfully at coordinate ($targetX, $targetY)")
                    shouldAutoClick = false
                    isClickInProgress = false
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Log.d(TAG, "Gesture execution cancelled.")
                    isClickInProgress = false
                }
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing fallback gesture: ${e.message}", e)
            isClickInProgress = false
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted.")
        isClickInProgress = false
    }
}
