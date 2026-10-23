package com.blackened.healthapp.presentation.verification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.repository.AuthRepositoryImpl
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.usecase.auth.VerifyCodeUseCase
import kotlinx.coroutines.launch

class VerificationViewModel(
    private val verifyCodeUseCase: VerifyCodeUseCase = VerifyCodeUseCase(AuthRepositoryImpl())
) : ViewModel() {
    var code by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorResponse by mutableStateOf<ErrorResponseDTO?>(null)
        private set

    fun onCodeChanged(newValue: String) {
        code = newValue
    }

    fun clearError() { errorResponse = null }

    fun onVerificationClick(email: String, onVerificationSuccess: () -> Unit) {
        if (code.isBlank()) return

        isLoading = true

        viewModelScope.launch {
            viewModelScope.launch {
                verifyCodeUseCase(email, code)
                    .onSuccess { serverMessage ->
                        if (serverMessage.contains("Почта успешно подтверждена")) {
                            onVerificationSuccess()
                        }
                    }
                    .onFailure { exception ->
                        if (exception is AppNetworkException) {
                            errorResponse = exception.errorData
                        }
                    }
                isLoading = false
            }
        }
    }
}