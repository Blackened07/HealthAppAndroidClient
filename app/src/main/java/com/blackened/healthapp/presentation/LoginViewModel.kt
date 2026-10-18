package com.blackened.healthapp.presentation

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.blackened.healthapp.data.ErrorResponseDTO
import com.blackened.healthapp.data.NetworkClient
import com.blackened.healthapp.data.RegisterAccountRequestDTO
import com.blackened.healthapp.data.LoginRequestDTO
import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.data.TokenManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException

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

    fun onActionClick(
        onNeedVerification: (String) -> Unit,
        onLoginSuccess: () -> Unit)
    {
        if (email.isBlank() || password.isBlank()) return

        isLoading = true

        // Корутина
        viewModelScope.launch {
            try {
                if (isRegisterMode) {
                   val requestData =
                       RegisterAccountRequestDTO(
                           email = email,
                           password = password,
                           firstName = firstName,
                           systemRole = selectedRole.serverValue
                       )

                    val response = NetworkClient.authApi.register(requestData)

                    email = response.email
                    password = ""
                    firstName = ""

                    onNeedVerification(response.email)
                } else {

                    val loginData = LoginRequestDTO(
                        email = email,
                        password = password
                    )

                    val authResponse = NetworkClient.authApi.login(loginData)

                    SessionManager.token = authResponse.message
                    SessionManager.userEmail = email
                    SessionManager.userId = authResponse.userId
                    SessionManager.userName = authResponse.firstName

                    email = ""
                    password = ""

                   /* TokenManager.token = response.message*/

                    onLoginSuccess()
                }
            } catch (e: HttpException) {

                val errorBody = e.response()?.errorBody()?.string()

                if (!errorBody.isNullOrBlank()) {

                    try {
                        val errDto = Json.decodeFromString<ErrorResponseDTO>(errorBody ?: "")

                        if (errDto.status == 403 && errDto.message.contains("NEED_VERIFICATION")) {
                            onNeedVerification(email)
                        } else {
                            /*Toast.makeText(this errDto.message, Toast.LENGTH_LONG).show()*/
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}