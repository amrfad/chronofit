package com.singularitech.chronos_hercules.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import androidx.core.app.NotificationCompat
import com.singularitech.chronos_hercules.utils.PreferencesManager
import kotlinx.coroutines.*

class AppMonitoringService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var preferencesManager: PreferencesManager
    private var isMonitoring = false
    private var isTargetAppForeground = false
    private var lastCreditDeduction = 0L

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "AppMonitoring"
        private const val CHECK_INTERVAL = 5000L // 5 seconds
        private const val MINUTE_IN_MILLIS = 60000L // 1 menit dalam milliseconds
    }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        setupNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Get target package from SharedPreferences
        preferencesManager = PreferencesManager(this)

        if (!isMonitoring) {
            startMonitoring()
        }

        return START_STICKY
    }

    private fun startMonitoring() {
        isMonitoring = true
        serviceScope.launch {
            while (isMonitoring) {
                checkAppUsage()
                delay(CHECK_INTERVAL)
            }
        }
    }

    private fun checkAppUsage() {
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(currentTime - CHECK_INTERVAL, currentTime)
        val event = UsageEvents.Event()

        // Check if target app is currently in foreground
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (preferencesManager.isPackageMonitored(event.packageName)) {
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> isTargetAppForeground = true
                    UsageEvents.Event.ACTIVITY_PAUSED -> isTargetAppForeground = false
                }
            }
        }

        if (isTargetAppForeground) {
            val prefs = getSharedPreferences("PushUpTimer", Context.MODE_PRIVATE)
            val credits = prefs.getInt("time_credits", 0)

            // Check if it's time to deduct credit (1 minute passed)
            if (currentTime - lastCreditDeduction >= MINUTE_IN_MILLIS) {
                if (credits <= 0) {
                    // Broadcast intent to block app
                    sendBroadcast(Intent("com.singularitech.chronos_hercules.BLOCK_APP"))
                } else {
                    // Deduct 1 minute credit
                    prefs.edit().putInt("time_credits", credits - 1).apply()
                    lastCreditDeduction = currentTime
                }
            }
        }
    }

    private fun setupNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "App Monitoring Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Time Monitor Active")
            .setContentText("Monitoring app usage")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isMonitoring = false
        serviceScope.cancel()
    }
}