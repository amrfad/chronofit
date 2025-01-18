package com.singularitech.chronofit.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.abs
import kotlin.math.atan2

class PushUpCounter(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var onPushUpCompleted: ((Int) -> Unit)? = null
    private var totalPushUps = 0

    private var pushUpCount = 0
    private var positionState = "UP"
    private var lastResult: PoseLandmarkerResult? = null

    // Threshold
    private val minAngle = 70f
    private val maxAngle = 160f
    private val hipThreshold = 30f

    // Paint untuk menggambar
    private val skeletonPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private val landmarkPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.GREEN
        textSize = 60f
        textAlign = Paint.Align.LEFT
    }

    // Koneksi untuk skeleton
    private val poseConnections = listOf(
        0 to 1, 1 to 4, 4 to 7, 7 to 8, 8 to 5, 5 to 2, 2 to 0,  // Wajah
        11 to 12, 11 to 13, 13 to 15, 12 to 14, 14 to 16,        // Lengan
        11 to 23, 12 to 24,                                       // Tubuh
        23 to 25, 25 to 27, 27 to 29, 29 to 31,                  // Kaki kiri
        24 to 26, 26 to 28, 28 to 30, 30 to 32,                  // Kaki kanan
        23 to 24                                                  // Pinggul
    )

    private fun calculateAngle(a: NormalizedLandmark, b: NormalizedLandmark, c: NormalizedLandmark): Float {
        val radians = atan2(c.y() - b.y(), c.x() - b.x()) - atan2(a.y() - b.y(), a.x() - b.x())
        var angle = abs(radians * 180f / Math.PI.toFloat())

        if (angle > 180f) {
            angle = 360f - angle
        }
        return angle
    }

    private fun checkPosition(landmarks: List<NormalizedLandmark>): Boolean {
        val shoulder = landmarks[11]
        val hip = landmarks[23]
        val wrists = listOf(landmarks[15], landmarks[16])

        val handsOnGround = wrists.all { it.x() < 0.3f }
        val bodyAligned = abs(hip.x() - shoulder.x()) < hipThreshold/100f

        return handsOnGround && bodyAligned
    }

    fun processFrame(poseLandmarkerResult: PoseLandmarkerResult) {
        lastResult = poseLandmarkerResult

        poseLandmarkerResult.landmarks().firstOrNull()?.let { landmarks ->
            val shoulder = landmarks[11]
            val elbow = landmarks[13]
            val wrist = landmarks[15]

            val angle = calculateAngle(shoulder, elbow, wrist)
            val validPosition = checkPosition(landmarks)

            if (validPosition) {
                when {
                    positionState == "UP" && angle < minAngle -> {
                        positionState = "DOWN"
                    }
                    positionState == "DOWN" && angle > maxAngle -> {
                        positionState = "UP"
                        incrementPushUpCount()
                    }
                }
            }

            postInvalidate()
        }
    }

    private fun adjustCoordinates(x: Float, y: Float): Pair<Float, Float> {
        // Rotasi 90 derajat
        return Pair(1f - y, 1f - x)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Tambahkan log untuk debugging
        Log.d("PushUpCounter", "onDraw called")

        lastResult?.landmarks()?.firstOrNull()?.let { landmarks ->
            // Tambahkan log untuk memastikan landmarks ada
            Log.d("PushUpCounter", "Drawing landmarks: ${landmarks.size}")

            // Gambar skeleton
            poseConnections.forEach { (start, end) ->
                val startLandmark = landmarks[start]
                val endLandmark = landmarks[end]

                val (startXAdjusted, startYAdjusted) = adjustCoordinates(startLandmark.x(), startLandmark.y())
                val (endXAdjusted, endYAdjusted) = adjustCoordinates(endLandmark.x(), endLandmark.y())

                val startX = startXAdjusted * width
                val startY = startYAdjusted * height
                val endX = endXAdjusted * width
                val endY = endYAdjusted * height

                canvas.drawLine(startX, startY, endX, endY, skeletonPaint)
            }

            // Gambar landmark points
            landmarks.forEach { landmark ->
                val (xAdjusted, yAdjusted) = adjustCoordinates(landmark.x(), landmark.y())
                canvas.drawCircle(
                    xAdjusted * width,
                    yAdjusted  * height,
                    8f,
                    landmarkPaint
                )
            }

            // Gambar informasi
            canvas.drawText("Push-ups: $pushUpCount", 30f, 60f, textPaint)

            val angle = calculateAngle(landmarks[11], landmarks[13], landmarks[15])
            canvas.drawText("Angle: ${angle.toInt()}", 30f, 120f, textPaint)

            if (!checkPosition(landmarks)) {
                textPaint.color = Color.RED
                canvas.drawText("Invalid Position!", 30f, 180f, textPaint)
                textPaint.color = Color.GREEN
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun setOnPushUpCompletedListener(listener: (Int) -> Unit) {
        onPushUpCompleted = listener
    }

    private fun incrementPushUpCount() {
        pushUpCount++
        totalPushUps++
        onPushUpCompleted?.invoke(1) // Memanggil callback setiap push up
    }
}