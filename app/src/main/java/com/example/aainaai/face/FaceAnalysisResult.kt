package com.example.aainaai.face

/**
 * Result of face analysis from ML Kit
 * Contains key metrics for liveness detection
 */
data class FaceAnalysisResult(
    val faceDetected: Boolean,
    val headEulerAngleY: Float = 0f,  // Horizontal head rotation (-180 to 180)
    val headEulerAngleZ: Float = 0f,  // Tilt (roll) angle
    val smilingProbability: Float = 0f,  // 0.0 to 1.0
    val faceBounds: android.graphics.RectF? = null,
    val faceSize: Float = 0f  // Relative size to detect if user is too far
) {
    /**
     * Check if user turned head to the left
     */
    fun isHeadTurnedLeft(threshold: Float = -20f): Boolean {
        return faceDetected && headEulerAngleY < threshold
    }

    /**
     * Check if user turned head to the right
     */
    fun isHeadTurnedRight(threshold: Float = 20f): Boolean {
        return faceDetected && headEulerAngleY > threshold
    }

    /**
     * Check if user is smiling
     */
    fun isSmiling(threshold: Float = 0.75f): Boolean {
        return faceDetected && smilingProbability > threshold
    }

    /**
     * Check if face is large enough (user is close to camera)
     * Prevents spoofing with printed photos from a distance
     */
    fun isFaceSizeSufficient(minSize: Float = 0.3f): Boolean {
        return faceDetected && faceSize > minSize
    }

    companion object {
        fun noFaceDetected() = FaceAnalysisResult(faceDetected = false)
    }
}
