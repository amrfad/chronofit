package com.singularitech.chronofit

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.singularitech.chronofit.utils.PushUpCounter
import com.singularitech.chronofit.utils.TimeCreditsManager

class ExerciseFragment : Fragment(R.layout.fragment_exercise) {
    private lateinit var pushUpCounter: PushUpCounter
    private lateinit var poseLandmarker: PoseLandmarker
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var previewView: PreviewView
    private lateinit var timeCreditsManager: TimeCreditsManager
    private var isExercising = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            // Handle permission denied
            findNavController().navigateUp()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeCreditsManager = TimeCreditsManager(requireContext())
        previewView = view.findViewById(R.id.previewView)
        pushUpCounter = view.findViewById(R.id.pushUpCounter)
        sharedPreferences = requireContext().getSharedPreferences("PushUpTimer", Context.MODE_PRIVATE)

        setupPushUpCounter()
        setupFinishButton()
        setupPoseLandmarker()
        requestCameraPermission()
    }

    private fun setupPushUpCounter() {
        pushUpCounter.setOnPushUpCompletedListener { count ->
            timeCreditsManager.addCredits(count)
            val minutesPerPushup = sharedPreferences.getInt("minutes_per_pushup", 2)

            // Optional: Show feedback
            view?.let { view ->
                Snackbar.make(view, "+${count * minutesPerPushup} minutes earned!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFinishButton() {
        view?.findViewById<MaterialButton>(R.id.finishButton)?.setOnClickListener {
            findNavController().navigate(R.id.action_exercise_to_home)
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun setupPoseLandmarker() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(
            this.requireContext(),
            PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, image ->
                    pushUpCounter.processFrame(result)
                    image.close()
                }
                .build()
        )
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                cameraProvider.unbindAll()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetRotation(previewView.display.rotation)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()

                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(this.requireContext())
                ) { imageProxy ->
                    val image = imageProxy.image
                    if (image != null) {
                        val frameTime = System.currentTimeMillis()
                        val mpImage = MediaImageBuilder(image).build()
                        poseLandmarker.detectAsync(mpImage, frameTime)
                    }
                    imageProxy.close()
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e:Exception) {
                Log.e("ExerciseFragment", "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraProvider.isInitialized) {
            cameraProvider.unbindAll()
        }
    }
}