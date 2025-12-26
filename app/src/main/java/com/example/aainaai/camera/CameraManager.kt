package com.example.aainaai.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * High-level CameraX manager for document scanning and face detection
 * Handles camera initialization, preview, and image capture
 */
class CameraManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    /**
     * Initialize camera with preview
     * @param previewView The PreviewView to display camera preview
     * @param lifecycleOwner Lifecycle owner for camera binding
     * @param lensFacing Front or back camera
     * @param imageAnalyzer Optional analyzer for real-time processing (face detection)
     */
    suspend fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        imageAnalyzer: ImageAnalysis.Analyzer? = null
    ) {
        Log.i(TAG, "================================================")
        Log.i(TAG, "startCamera() called")
        Log.i(TAG, "  lensFacing: ${if (lensFacing == CameraSelector.LENS_FACING_FRONT) "FRONT" else "BACK"}")
        Log.i(TAG, "  imageAnalyzer provided: ${imageAnalyzer != null}")
        Log.i(TAG, "  lifecycleOwner: $lifecycleOwner")
        Log.i(TAG, "  lifecycleOwner state: ${lifecycleOwner.lifecycle.currentState}")
        Log.i(TAG, "================================================")

        Log.d(TAG, "Getting camera provider...")
        val provider = getCameraProvider()
        cameraProvider = provider
        Log.d(TAG, "Camera provider obtained: $provider")

        // Preview use case
        Log.d(TAG, "Building Preview use case...")
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
                Log.d(TAG, "Surface provider set on preview")
            }

        // Image capture use case
        Log.d(TAG, "Building ImageCapture use case...")
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        // Camera selector
        Log.d(TAG, "Building CameraSelector for lens facing: $lensFacing")
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            // Unbind all use cases before rebinding
            Log.d(TAG, "Unbinding all existing use cases...")
            provider.unbindAll()
            Log.d(TAG, "All use cases unbound")

            // Bind use cases to camera
            if (imageAnalyzer != null) {
                Log.i(TAG, "Image analyzer provided - building ImageAnalysis use case")
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, imageAnalyzer)
                        Log.d(TAG, "ImageAnalyzer set on ImageAnalysis use case")
                    }

                Log.i(TAG, "Binding to lifecycle with 3 use cases: Preview + ImageCapture + ImageAnalysis")
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
                Log.i(TAG, "Bound to lifecycle successfully! Camera info: ${camera.cameraInfo}")
            } else {
                Log.i(TAG, "No image analyzer - binding to lifecycle with 2 use cases: Preview + ImageCapture")
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                Log.i(TAG, "Bound to lifecycle successfully! Camera info: ${camera.cameraInfo}")
            }

            Log.i(TAG, "✓✓✓ CAMERA STARTED SUCCESSFULLY ✓✓✓")
        } catch (e: Exception) {
            Log.e(TAG, "!!! FAILED TO BIND CAMERA USE CASES !!!", e)
            Log.e(TAG, "Exception type: ${e::class.java.name}")
            Log.e(TAG, "Exception message: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Capture a photo
     * @return File where the photo was saved
     */
    suspend fun capturePhoto(): File {
        val imageCapture = imageCapture ?: throw IllegalStateException("Camera not initialized")

        val outputFile = File(
            context.cacheDir,
            "kyc_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        return suspendCoroutine { continuation ->
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Log.d(TAG, "Photo captured: ${outputFile.absolutePath}")
                        continuation.resume(outputFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed", exception)
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }

    /**
     * Get CameraProvider instance
     */
    private suspend fun getCameraProvider(): ProcessCameraProvider {
        return suspendCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                continuation.resume(cameraProviderFuture.get())
            }, ContextCompat.getMainExecutor(context))
        }
    }

    /**
     * Release camera resources
     */
    fun shutdown() {
        // Do NOT call unbindAll() here! 
        // In Compose navigation, the previous screen is disposed AFTER the new screen has started.
        // If we unbindAll() here, it will kill the camera that the new screen just started!
        // lifecycleOwner binding handles unbinding automatically.
        // cameraProvider?.unbindAll() 
        cameraExecutor.shutdown()
        Log.d(TAG, "Camera shutdown")
    }

    companion object {
        private const val TAG = "CameraManager"
    }
}
