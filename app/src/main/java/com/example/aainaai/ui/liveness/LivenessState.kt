package com.example.aainaai.ui.liveness

/**
 * State machine for the liveness detection challenge
 * Guides the user through: Turn Left -> Turn Right -> Smile
 */
sealed class LivenessChallenge {
    object Initializing : LivenessChallenge()
    object TurnLeft : LivenessChallenge()
    object TurnRight : LivenessChallenge()
    object Smile : LivenessChallenge()
    object FaceCapture : LivenessChallenge()
    object Completed : LivenessChallenge()

    fun getInstruction(): String {
        return when (this) {
            is Initializing -> "Position your face in the frame"
            is TurnLeft -> "Turn your head LEFT"
            is TurnRight -> "Turn your head RIGHT"
            is Smile -> "Now SMILE!"
            is FaceCapture -> "Hold Steady"
            is Completed -> "Verification Complete!"
        }
    }

    fun getInstructionNepali(): String {
        return when (this) {
            is Initializing -> "आफ्नो अनुहार फ्रेममा राख्नुहोस्"
            is TurnLeft -> "आफ्नो टाउको बायाँ घुमाउनुहोस्"
            is TurnRight -> "आफ्नो टाउको दायाँ घुमाउनुहोस्"
            is Smile -> "अब मुस्कुराउनुहोस्!"
            is FaceCapture -> "स्थिर रहनुहोस्"
            is Completed -> "प्रमाणीकरण पूर्ण भयो!"
        }
    }
}

/**
 * UI state for the liveness screen
 */
data class LivenessUiState(
    val challenge: LivenessChallenge = LivenessChallenge.Initializing,
    val faceDetected: Boolean = false,
    val faceSizeSufficient: Boolean = false,
    val progress: Float = 0f,  // 0.0 to 1.0
    val completedSteps: Set<LivenessChallenge> = emptySet(),
    val errorMessage: String? = null
) {
    val currentInstruction: String
        get() = challenge.getInstruction()

    val isComplete: Boolean
        get() = challenge is LivenessChallenge.Completed
}
