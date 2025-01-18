package com.singularitech.chronos_hercules.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BlockAppReceiver(private val onBlockApp: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.singularitech.chronos_hercules.BLOCK_APP") {
            onBlockApp()
        }
    }
}