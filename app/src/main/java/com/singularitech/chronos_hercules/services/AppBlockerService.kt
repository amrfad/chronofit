package com.singularitech.chronos_hercules.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.content.IntentFilter
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Handler
import android.os.Looper
import com.singularitech.chronos_hercules.ui.BlockingOverlayActivity

class AppBlockerService : AccessibilityService() {
    private lateinit var targetPackage: String
    private var blockReceiver: BlockAppReceiver? = null
    private var isBlocking = false

    override fun onCreate() {
        super.onCreate()
        setupBlockReceiver()
        loadTargetPackage()
    }

    private fun setupBlockReceiver() {
        blockReceiver = BlockAppReceiver {
            performGlobalAction(GLOBAL_ACTION_HOME)
            showBlockingOverlay()
        }
        registerReceiver(
            blockReceiver,
            IntentFilter("com.singularitech.chronos_hercules.BLOCK_APP")
        )
    }

    private fun loadTargetPackage() {
        val prefs = getSharedPreferences("PushUpTimer", Context.MODE_PRIVATE)
        targetPackage = prefs.getString("target_app", "") ?: ""
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            packageNames = arrayOf(targetPackage)
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val prefs = getSharedPreferences("PushUpTimer", Context.MODE_PRIVATE)
            val credits = prefs.getInt("time_credits", 0)
            val targetPackage = prefs.getString("target_app", "") ?: ""

            if (credits <= 0 && event.packageName == targetPackage) {
                if (!isBlocking) {
                    isBlocking = true
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    showBlockingOverlay()

                    // Reset blocking flag after delay
                    Handler(Looper.getMainLooper()).postDelayed({
                        isBlocking = false
                    }, 1000)
                }
            }
        }
    }

    private fun showBlockingOverlay() {
        val intent = Intent(this, BlockingOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Handle interruption if needed
    }

    override fun onDestroy() {
        super.onDestroy()
        blockReceiver?.let { unregisterReceiver(it) }
    }
}