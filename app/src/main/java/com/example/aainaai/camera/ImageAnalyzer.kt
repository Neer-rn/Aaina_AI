package com.example.aainaai.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.aainaai.face.FaceAnalysisResult
import com.example.aainaai.face.FaceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Image analyzer for real-time face detection
 * Processes camera frames and provides analysis results via callback
 */
class FaceImageAnalyzer(
    private val onFaceAnalyzed: (FaceAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val faceDetector = FaceDetector()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        android.util.Log.v("FaceImageAnalyzer", "analyze() called - frame size: ${imageProxy.width}x${imageProxy.height}")
        scope.launch {
            try {
                android.util.Log.d("FaceImageAnalyzer", "Starting face detection...")
                val result = faceDetector.detectFace(imageProxy)
                android.util.Log.d("FaceImageAnalyzer", "Face detection complete: faceDetected=${result.faceDetected}")
                onFaceAnalyzed(result)
                android.util.Log.v("FaceImageAnalyzer", "Callback invoked")
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.e("FaceImageAnalyzer", "Face detection error: ${e.message}", e)
                imageProxy.close()
                onFaceAnalyzed(FaceAnalysisResult.noFaceDetected())
            }
        }
    }

    /**
     * Clean up resources
     */
    fun close() {
        scope.cancel()
        faceDetector.close()
    }
}
