package com.blackened.healthapp.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.NetworkClient
import com.blackened.healthapp.data.RegisterAccountRequest
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

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

    fun onActionClick(onRegistrationSuccess: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) return

        isLoading = true

        // Корутина
        viewModelScope.launch {
            try {
                if (isRegisterMode) {
                   val requestData =
                       RegisterAccountRequest(
                           email = email,
                           password = password,
                           firstName = firstName,
                           systemRole = selectedRole.serverValue
                       )

                    val response = NetworkClient.authApi.register(requestData)

                    email = response.email
                    password = ""
                    firstName = ""

                    onRegistrationSuccess(response.email)
                } else {
                    // TODO: Реализовать вход
                }
            } catch (e: Exception) {
                /*onRegistrationFailure(e.message)*/
            } finally {
                isLoading = false
            }
        }
    }
}