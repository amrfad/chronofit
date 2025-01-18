package com.singularitech.chronofit

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.singularitech.chronofit.services.AppBlockerService
import com.singularitech.chronofit.services.AppMonitoringService
import com.singularitech.chronofit.services.UsageStatsUtil
import com.singularitech.chronofit.utils.TimeCreditsManager

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var timeCreditsManager: TimeCreditsManager
    private lateinit var creditTextView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("PushUpTimer", Context.MODE_PRIVATE)
        timeCreditsManager = TimeCreditsManager(requireContext())
        creditTextView = view.findViewById(R.id.creditTextView)

        // Setup navigation to exercise
        view.findViewById<MaterialButton>(R.id.startExerciseButton).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_exercise)
            updateCreditsDisplay()
        }

        // Setup navigation to settings
        view.findViewById<MaterialButton>(R.id.toSettingButton).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        checkRequiredPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateCreditsDisplay()
    }

    private fun updateCreditsDisplay() {
        val credits = timeCreditsManager.getCredits()
        creditTextView.text = "$credits minutes"
    }

    private fun checkRequiredPermissions() {
        // Check Usage Stats Permission
        if (!UsageStatsUtil.hasUsageStatsPermission(requireContext())) {
            showUsagePermissionDialog()
            return
        }

        // Check Accessibility Service
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityDialog()
            return
        }

        // Start monitoring if all permissions granted
        startMonitoring()
    }

    private fun startMonitoring() {
        val serviceIntent = Intent(requireContext(), AppMonitoringService::class.java)
        requireContext().startService(serviceIntent)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val serviceComponentName = ComponentName(requireContext(), AppBlockerService::class.java)

        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )

        return enabledServices.any { it.id.contains(serviceComponentName.flattenToShortString()) }
    }

    private fun showUsagePermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app needs Usage Access permission to monitor app usage time. Please enable it in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                UsageStatsUtil.openUsageSettings(requireContext())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Accessibility Permission Required")
            .setMessage("Please enable accessibility service for app blocking feature to work")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
