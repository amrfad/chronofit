package com.singularitech.chronofit.utils

data class TargetApp(
    val packageName: String,
    val appName: String,
    var isEnabled: Boolean = true
)