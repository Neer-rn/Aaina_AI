package com.example.aainaai.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aainaai.network.HashRequest
import com.example.aainaai.network.NetworkManager
import com.example.aainaai.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Personal Details Form
 */
data class PersonalDetailsUiState(
    val fullName: String = "",
    val citizenshipNumber: String = "",
    val dateOfBirth: String = "", // Format: YYYY-MM-DD
    val error: String? = null,
    val isFormValid: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false
)

/**
 * ViewModel for handling Personal Details input
 */
class PersonalDetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalDetailsUiState())
    val uiState: StateFlow<PersonalDetailsUiState> = _uiState.asStateFlow()

    fun onNameChange(newValue: String) {
        _uiState.update { 
            it.copy(fullName = newValue, error = null) 
        }
        validateForm()
    }

    fun onCitizenshipNumberChange(newValue: String) {
        _uiState.update { 
            it.copy(citizenshipNumber = newValue, error = null) 
        }
        validateForm()
    }

    fun onDobChange(newValue: String) {
        _uiState.update { 
            it.copy(dateOfBirth = newValue, error = null) 
        }
        validateForm()
    }

    /**
     * Auto-fill logic for testing (as requested by user)
     */
    fun autoFill() {
        _uiState.update {
            it.copy(
                fullName = "Ram Bahadur Thapa",
                citizenshipNumber = "12-01-74-03211",
                dateOfBirth = "1995-04-12",
                error = null,
                isFormValid = true
            )
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.fullName.isNotBlank() && 
                      state.citizenshipNumber.isNotBlank() && 
                      state.dateOfBirth.isNotBlank()
        
        _uiState.update { it.copy(isFormValid = isValid) }
    }

    fun submit() {
        if (!_uiState.value.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            
            try {
                // 1. Store Citizenship for later usage
                SessionManager.citizenshipNumber = _uiState.value.citizenshipNumber

                // 2. Send POST /api/hash
                // API now generates the hash for us
                val payload = HashRequest(
                    name = _uiState.value.fullName,
                    citizen_no = _uiState.value.citizenshipNumber,
                    dob = _uiState.value.dateOfBirth
                )
                NetworkManager.api.sendHash(payload)

                // Success
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isSubmitting = false, error = "Failed: ${e.localizedMessage}") }
            }
        }
    }
}
