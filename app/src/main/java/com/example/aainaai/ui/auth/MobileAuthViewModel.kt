package com.example.aainaai.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MobileAuthUiState(
    val phoneNumber: String = "",
    val otp: String = "",
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isVerified: Boolean = false
)

class MobileAuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MobileAuthUiState())
    val uiState: StateFlow<MobileAuthUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChange(number: String) {
        // Simple numeric filter
        if (number.all { it.isDigit() } && number.length <= 10) {
            _uiState.update { it.copy(phoneNumber = number, error = null) }
        }
    }

    fun onOtpChange(otp: String) {
        if (otp.all { it.isDigit() } && otp.length <= 6) {
            _uiState.update { it.copy(otp = otp, error = null) }
        }
    }

    fun sendOtp() {
        val number = _uiState.value.phoneNumber
        if (number.length != 10) {
            _uiState.update { it.copy(error = "Please enter a valid 10-digit mobile number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Simulate network delay
            delay(1500)
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    isOtpSent = true,
                    // In a real app, we wouldn't show the OTP in the UI state like this
                    // But for demo, we'll let the UI use this to show a snackbar
                ) 
            }
        }
    }

    fun verifyOtp() {
        val otp = _uiState.value.otp
        if (otp.length != 6) {
            _uiState.update { it.copy(error = "Please enter a 6-digit OTP") }
            return
        }

        val phoneNumber = _uiState.value.phoneNumber
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(1500)
            
            if (otp == "123456") {
                try {
                    // Call Reset API
                    com.example.aainaai.network.NetworkManager.api.resetState()
                    // Store Phone for later Hash
                    com.example.aainaai.util.SessionManager.phoneNumber = phoneNumber
                    
                    _uiState.update { it.copy(isLoading = false, isVerified = true) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = "Network Error: ${e.localizedMessage}") }
                    e.printStackTrace()
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Invalid OTP. Try 123456") }
            }
        }
    }
}
