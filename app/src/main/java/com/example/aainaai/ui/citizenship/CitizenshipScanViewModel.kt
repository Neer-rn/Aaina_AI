package com.example.aainaai.ui.citizenship

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * Stages of the Citizenship Scan process
 */
enum class ScanStep {
    FRONT_SCAN,
    FRONT_CONFIRM,
    BACK_SCAN,
    BACK_CONFIRM,
    COMPLETED
}

data class CitizenshipUiState(
    val currentStep: ScanStep = ScanStep.FRONT_SCAN,
    val frontImage: File? = null,
    val backImage: File? = null,
    val frontImageUri: Uri? = null, // For Gallery selection
    val backImageUri: Uri? = null,  // For Gallery selection
    val isProcessing: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Citizenship Card scanning with Dual-Side Support
 */
class CitizenshipScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CitizenshipUiState())
    val uiState: StateFlow<CitizenshipUiState> = _uiState.asStateFlow()
    private val TAG = "CitizenshipScanVM"

    fun onImageCaptured(file: File) {
        android.util.Log.d(TAG, "Image Captured: ${file.absolutePath}")
        _uiState.update { currentState ->
            when (currentState.currentStep) {
                ScanStep.FRONT_SCAN -> currentState.copy(
                    frontImage = file, 
                    currentStep = ScanStep.FRONT_CONFIRM,
                    isProcessing = false
                )
                ScanStep.BACK_SCAN -> currentState.copy(
                    backImage = file, 
                    currentStep = ScanStep.BACK_CONFIRM,
                    isProcessing = false
                )
                else -> currentState
            }
        }
    }

    fun onGalleryImageSelected(uri: Uri) {
        android.util.Log.d(TAG, "Gallery Image Selected: $uri")
        _uiState.update { currentState ->
            when (currentState.currentStep) {
                ScanStep.FRONT_SCAN -> currentState.copy(
                    frontImageUri = uri,
                    frontImage = null, // Ensure we know it's a URI
                    currentStep = ScanStep.FRONT_CONFIRM,
                    isProcessing = false
                )
                ScanStep.BACK_SCAN -> currentState.copy(
                    backImageUri = uri,
                    backImage = null, // Ensure we know it's a URI
                    currentStep = ScanStep.BACK_CONFIRM,
                    isProcessing = false
                )
                else -> currentState
            }
        }
    }

    fun confirmResult() {
        _uiState.update { currentState ->
            android.util.Log.d(TAG, "Confirming Step: ${currentState.currentStep}")
            
            when (currentState.currentStep) {
                ScanStep.FRONT_CONFIRM -> {
                    // Save Front Path
                    if (currentState.frontImage != null) {
                        com.example.aainaai.util.SessionManager.frontImagePath = currentState.frontImage.absolutePath
                        android.util.Log.d(TAG, "  ✅ Saved Front File Path: ${currentState.frontImage.absolutePath}")
                    } else if (currentState.frontImageUri != null) {
                         // Fallback: If we have URI, we might need it later.
                         // But LivenessViewModel expects a File path.
                         // We are logging this issue for now.
                         android.util.Log.w(TAG, "  ⚠️ Front Image is URI: ${currentState.frontImageUri}. Upload might fail if not handled!")
                         com.example.aainaai.util.SessionManager.frontImagePath = currentState.frontImageUri.toString()
                    }
                    
                    currentState.copy(
                        currentStep = ScanStep.BACK_SCAN,
                        isProcessing = false
                    )
                }
                ScanStep.BACK_CONFIRM -> {
                    // Save Back Path
                     if (currentState.backImage != null) {
                        com.example.aainaai.util.SessionManager.backImagePath = currentState.backImage.absolutePath
                        android.util.Log.d(TAG, "  ✅ Saved Back File Path: ${currentState.backImage.absolutePath}")
                    } else if (currentState.backImageUri != null) {
                         android.util.Log.w(TAG, "  ⚠️ Back Image is URI: ${currentState.backImageUri}. Upload might fail if not handled!")
                         com.example.aainaai.util.SessionManager.backImagePath = currentState.backImageUri.toString()
                    }
                    
                    currentState.copy(
                        currentStep = ScanStep.COMPLETED,
                        isProcessing = false
                    )
                }
                else -> currentState
            }
        }
    }

    fun retake() {
        _uiState.update { currentState ->
            when (currentState.currentStep) {
                ScanStep.FRONT_CONFIRM -> currentState.copy(
                    currentStep = ScanStep.FRONT_SCAN,
                    frontImage = null,
                    frontImageUri = null
                )
                ScanStep.BACK_CONFIRM -> currentState.copy(
                    currentStep = ScanStep.BACK_SCAN,
                    backImage = null,
                    backImageUri = null
                )
                else -> currentState
            }
        }
    }

    fun setProcessing(isProcessing: Boolean) {
        _uiState.update { it.copy(isProcessing = isProcessing) }
    }
}
