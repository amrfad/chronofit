package com.singularitech.chronofit

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.singularitech.chronofit.utils.AppPickerAdapter
import com.singularitech.chronofit.utils.TargetAppsAdapter
import com.singularitech.chronofit.utils.PreferencesManager
import com.singularitech.chronofit.utils.TargetApp

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: TargetAppsAdapter
    private val targetApps = mutableListOf<TargetApp>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SettingsFragment", "onViewCreated started")

        preferencesManager = PreferencesManager(requireContext())
        setupRecyclerView()
        loadSavedApps()
        loadSettings()

        view.findViewById<MaterialButton>(R.id.addAppButton).setOnClickListener {
            checkAndRequestPackagePermission()
        }

        view.findViewById<MaterialButton>(R.id.saveButton)?.setOnClickListener {
            saveSettings()
            findNavController().navigateUp()
        }

        Log.d("SettingsFragment", "onViewCreated completed")
    }

    private fun checkAndRequestPackagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val hasPermission = requireContext().packageManager.canRequestPackageInstalls()
            if (hasPermission) {
                showAppPicker()
            } else {
                showPermissionExplanationDialog()
            }
        } else {
            // Untuk Android versi di bawah 11, tidak perlu permission khusus
            showAppPicker()
        }
    }


    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app requires permission to view all installed applications so you can select which apps to monitor. To enable this, you must allow the app to request installation of other apps. Please go to your device settings and enable 'Install unknown apps' permission for this app to grant the necessary access.")
            .setPositiveButton("Grant Permission") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(this)
        }
    }

    private fun setupRecyclerView() {
        Log.d("SettingsFragment", "Setting up RecyclerView")

        val recyclerView = view?.findViewById<RecyclerView>(R.id.appsRecyclerView)
        if (recyclerView == null) {
            Log.e("SettingsFragment", "RecyclerView not found!")
            return
        }

        adapter = TargetAppsAdapter(
            targetApps,
            onAppToggled = { app, isEnabled ->
                Log.d("SettingsFragment", "App toggled: ${app.appName}, enabled: $isEnabled")
                app.isEnabled = isEnabled
                preferencesManager.saveTargetApps(targetApps)
            },
            onAppRemoved = { app ->
                Log.d("SettingsFragment", "App removed: ${app.appName}")
                targetApps.remove(app)
                preferencesManager.saveTargetApps(targetApps)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        Log.d("SettingsFragment", "RecyclerView setup completed")
    }

    private fun loadSavedApps() {
        Log.d("SettingsFragment", "Loading saved apps")
        targetApps.clear()
        targetApps.addAll(preferencesManager.getTargetApps())
        adapter.notifyDataSetChanged()
        Log.d("SettingsFragment", "Loaded ${targetApps.size} apps")
    }

    private fun loadSettings() {
        Log.d("SettingsFragment", "Loading settings")
        val minutesPerPushup = preferencesManager.getMinutesPerPushup()
        view?.findViewById<TextInputEditText>(R.id.minutesPerPushupEditText)?.setText(minutesPerPushup.toString())
    }

    private fun saveSettings() {
        Log.d("SettingsFragment", "Saving settings")
        val minutesPerPushup = view?.findViewById<TextInputEditText>(R.id.minutesPerPushupEditText)
            ?.text.toString().toIntOrNull() ?: 2
        preferencesManager.saveMinutesPerPushup(minutesPerPushup)
    }

    private fun showAppPicker() {
        val dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_app_picker)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val searchEditText = dialog.findViewById<TextInputEditText>(R.id.searchEditText)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.appsRecyclerView)

        // Get all installed packages
        val packageManager = requireContext().packageManager
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            .filter { packageInfo ->
                // Filter packages that can be launched
                packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null
            }
            .map { packageInfo ->
                try {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    TargetApp(
                        packageName = packageInfo.packageName,
                        appName = appName
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .filterNotNull()
            .distinctBy { it.packageName }
            .sortedBy { it.appName }

        val adapter = AppPickerAdapter(installedApps.toMutableList()) { selectedApp ->
            if (!targetApps.any { it.packageName == selectedApp.packageName }) {
                targetApps.add(selectedApp)
                preferencesManager.saveTargetApps(targetApps)
                this.adapter.notifyItemInserted(targetApps.size - 1)
            }
            dialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }
        })

        dialog.show()
    }
}