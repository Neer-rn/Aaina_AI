package com.example.aainaai.face

import android.graphics.RectF
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Wrapper around ML Kit Face Detection
 * Optimized for real-time liveness detection with head pose and smile detection
 */
class FaceDetector {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  // Enable smile detection
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .enableTracking()  // Track faces across frames
            .build()

        FaceDetection.getClient(options)
    }

    /**
     * Analyze an image frame for face detection
     * Returns FaceAnalysisResult with head pose and smile data
     */
    suspend fun detectFace(imageProxy: ImageProxy): FaceAnalysisResult {
        return suspendCancellableCoroutine { continuation ->
            @androidx.camera.core.ExperimentalGetImage
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                android.util.Log.w("FaceDetector", "MediaImage is null!")
                imageProxy.close() // MUST close detection assumes we are done
                continuation.resume(FaceAnalysisResult.noFaceDetected())
                return@suspendCancellableCoroutine
            }

            android.util.Log.v("FaceDetector", "Creating InputImage from MediaImage (rotation: ${imageProxy.imageInfo.rotationDegrees})")
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            android.util.Log.v("FaceDetector", "Processing image with ML Kit...")
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    android.util.Log.d("FaceDetector", "ML Kit SUCCESS - detected ${faces.size} face(s)")
                    val result = processFaces(faces, imageProxy.width, imageProxy.height)
                    continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("FaceDetector", "ML Kit FAILURE: ${exception.message}", exception)
                    continuation.resumeWithException(exception)
                }
                .addOnCompleteListener {
                    android.util.Log.v("FaceDetector", "ML Kit complete, closing imageProxy")
                    imageProxy.close()
                }
        }
    }

    /**
     * Process detected faces and return analysis result
     * Takes the largest face if multiple faces are detected
     */
    private fun processFaces(faces: List<Face>, imageWidth: Int, imageHeight: Int): FaceAnalysisResult {
        android.util.Log.d("FaceDetector", "processFaces: ${faces.size} face(s) detected")

        if (faces.isEmpty()) {
            android.util.Log.w("FaceDetector", "No faces detected - returning noFaceDetected()")
            return FaceAnalysisResult.noFaceDetected()
        }

        // Take the largest face (closest to camera)
        val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
            ?: return FaceAnalysisResult.noFaceDetected()

        // Calculate relative face size (0.0 to 1.0)
        val faceArea = face.boundingBox.width() * face.boundingBox.height()
        val imageArea = imageWidth * imageHeight
        val faceSize = (faceArea.toFloat() / imageArea.toFloat())

        android.util.Log.i("FaceDetector", "FACE DETECTED:")
        android.util.Log.i("FaceDetector", "  Bounds: ${face.boundingBox}")
        android.util.Log.i("FaceDetector", "  Face area: $faceArea, Image area: $imageArea")
        android.util.Log.i("FaceDetector", "  Face size (relative): $faceSize")
        android.util.Log.i("FaceDetector", "  headEulerAngleY: ${face.headEulerAngleY}")
        android.util.Log.i("FaceDetector", "  headEulerAngleZ: ${face.headEulerAngleZ}")
        android.util.Log.i("FaceDetector", "  smilingProbability: ${face.smilingProbability}")
        android.util.Log.i("FaceDetector", "  trackingId: ${face.trackingId}")

        val result = FaceAnalysisResult(
            faceDetected = true,
            headEulerAngleY = face.headEulerAngleY,  // Horizontal rotation
            headEulerAngleZ = face.headEulerAngleZ,  // Tilt
            smilingProbability = face.smilingProbability ?: 0f,
            faceBounds = RectF(face.boundingBox),
            faceSize = faceSize
        )

        android.util.Log.d("FaceDetector", "Returning FaceAnalysisResult")
        return result
    }

    /**
     * Clean up resources
     */
    fun close() {
        detector.close()
    }
}
