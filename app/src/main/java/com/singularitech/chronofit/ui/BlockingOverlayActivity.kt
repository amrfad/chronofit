package com.singularitech.chronofit.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.singularitech.chronofit.R
import com.google.android.material.button.MaterialButton
import com.singularitech.chronofit.MainActivity

class BlockingOverlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set window attributes sebelum setContentView
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_blocking_overlay)

        findViewById<MaterialButton>(R.id.startExerciseButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("start_exercise", true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
            finish()
        }
    }
}
