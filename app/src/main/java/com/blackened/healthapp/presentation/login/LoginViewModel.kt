package com.blackened.healthapp.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.data.repository.AuthRepositoryImpl
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.usecase.auth.LoginUseCase
import com.blackened.healthapp.domain.usecase.auth.RegisterUseCase
import com.blackened.healthapp.presentation.FamilyRole
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(
    private val loginUseCase: LoginUseCase = LoginUseCase(AuthRepositoryImpl()),
    private val registerUseCase: RegisterUseCase = RegisterUseCase(AuthRepositoryImpl())
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var firstName by mutableStateOf("")
        private set

    var isRegisterMode by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    val roleList = FamilyRole.entries

    var selectedRole by mutableStateOf(FamilyRole.USER)
        private set

    var errorResponse by mutableStateOf<ErrorResponseDTO?>(null)
        private set

    fun onRoleSelected(role: FamilyRole) {
        selectedRole = role
    }

    fun onEmailChanged(newValue: String) {
        email = newValue
    }

    fun onPasswordChanged(newValue: String) {
        password = newValue
    }

    fun onFirstNameChanged(newValue: String) {
        firstName = newValue
    }

    fun toggleMode() {
        isRegisterMode = !isRegisterMode
    }

    fun switchToLogin() {
        isRegisterMode = false
    }

    fun switchToRegister() {
        isRegisterMode = true
    }

    fun clearError() { errorResponse = null }

    fun onActionClick(
        onNeedVerification: (String) -> Unit,
        onLoginSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) return

        isLoading = true

        viewModelScope.launch {
            if (isRegisterMode) {

                registerUseCase(email, firstName, selectedRole.serverValue, password)
                    .onSuccess {
                        isLoading = false
                        onNeedVerification(email)
                    }
                    .onFailure { exception ->
                        isLoading = false
                        if (exception is AppNetworkException) {
                            errorResponse = exception.errorData
                        }
                    }

            } else {
                loginUseCase(email, password)
                    .onSuccess { session ->
                        SessionManager.token = session.token
                        SessionManager.userEmail = session.email
                        SessionManager.userId = session.userId
                        SessionManager.userName = session.firstName

                        isLoading = false
                        onLoginSuccess()
                    }
                    .onFailure { exception ->
                        isLoading = false
                        if (exception is HttpException && exception.code() == 401) {
                            onNeedVerification(email)
                        } else {
                            if (exception is AppNetworkException) {
                                errorResponse = exception.errorData
                            }
                        }
                    }
            }
        }
    }
}