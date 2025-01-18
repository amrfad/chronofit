package com.singularitech.chronos_hercules.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("PushUpTimer", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTargetApps(apps: List<TargetApp>) {
        val json = gson.toJson(apps)
        prefs.edit().putString("target_apps", json).apply()
    }

    fun getTargetApps(): List<TargetApp> {
        val json = prefs.getString("target_apps", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<TargetApp>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun saveMinutesPerPushup(minutes: Int) {
        prefs.edit().putInt("minutes_per_pushup", minutes).apply()
    }

    fun getMinutesPerPushup(): Int {
        return prefs.getInt("minutes_per_pushup", 2) // default 2 minutes
    }

    fun isPackageMonitored(packageName: String): Boolean {
        return getTargetApps().any { it.packageName == packageName && it.isEnabled }
    }
}