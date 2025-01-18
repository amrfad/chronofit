package com.singularitech.chronofit.utils

import android.content.Context

class TimeCreditsManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("PushUpTimer", Context.MODE_PRIVATE)

    fun getCredits(): Int {
        return sharedPreferences.getInt("time_credits", 0)
    }

    fun addCredits(pushups: Int) {
        val minutesPerPushup = sharedPreferences.getInt("minutes_per_pushup", 2)
        val currentCredits = getCredits()
        val newCredits = currentCredits + (pushups * minutesPerPushup)

        sharedPreferences.edit().putInt("time_credits", newCredits).apply()
    }

    fun useCredits(minutes: Int): Boolean {
        val currentCredits = getCredits()
        if (currentCredits >= minutes) {
            sharedPreferences.edit().putInt("time_credits", currentCredits - minutes).apply()
            return true
        }
        return false
    }
}