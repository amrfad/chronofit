package com.singularitech.chronos_hercules.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.singularitech.chronos_hercules.R

class AppPickerAdapter(
    private val apps: MutableList<TargetApp>,
    private val onAppSelected: (TargetApp) -> Unit
) : RecyclerView.Adapter<AppPickerAdapter.ViewHolder>() {
    private var filteredApps = mutableListOf<TargetApp>()

    init {
        filteredApps.addAll(apps)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
        val packageName: TextView = view.findViewById(R.id.packageName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_picker, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filteredApps[position]
        val packageManager = holder.itemView.context.packageManager

        try {
            holder.appIcon.setImageDrawable(packageManager.getApplicationIcon(app.packageName))
            holder.appName.text = app.appName
            holder.packageName.text = app.packageName

            holder.itemView.setOnClickListener {
                onAppSelected(app)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = filteredApps.size

    fun filter(query: String) {
        filteredApps.clear()
        if (query.isEmpty()) {
            filteredApps.addAll(apps)
        } else {
            val lowerCaseQuery = query.lowercase()
            filteredApps.addAll(apps.filter { app ->
                app.appName.lowercase().contains(lowerCaseQuery) ||
                        app.packageName.lowercase().contains(lowerCaseQuery)
            })
        }
        notifyDataSetChanged()
    }
}