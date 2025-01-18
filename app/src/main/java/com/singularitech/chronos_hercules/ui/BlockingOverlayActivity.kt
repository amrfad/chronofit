package com.singularitech.chronos_hercules.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.singularitech.chronos_hercules.R
import com.google.android.material.button.MaterialButton
import com.singularitech.chronos_hercules.MainActivity

class BlockingOverlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocking_overlay)

        findViewById<MaterialButton>(R.id.startExerciseButton).setOnClickListener {
            // Navigate to exercise screen
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("start_exercise", true)
            })
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Prevent back button from dismissing overlay
        moveTaskToBack(true)
    }
}