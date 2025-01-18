package com.singularitech.chronos_hercules.utils

data class TargetApp(
    val packageName: String,
    val appName: String,
    var isEnabled: Boolean = true
)