package com.singularitech.chronos_hercules.utils

import com.singularitech.chronos_hercules.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial

class TargetAppsAdapter(
    private val apps: MutableList<TargetApp>,
    private val onAppToggled: (TargetApp, Boolean) -> Unit,
    private val onAppRemoved: (TargetApp) -> Unit
) : RecyclerView.Adapter<TargetAppsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appNameText)
        val packageName: TextView = view.findViewById(R.id.packageNameText)
        val toggleSwitch: SwitchMaterial = view.findViewById(R.id.toggleSwitch)
        val removeButton: View = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_target_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.appName.text = app.appName
        holder.packageName.text = app.packageName
        holder.toggleSwitch.isChecked = app.isEnabled

        holder.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            onAppToggled(app, isChecked)
        }

        holder.removeButton.setOnClickListener {
            onAppRemoved(app)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount() = apps.size
}