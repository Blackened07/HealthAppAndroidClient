package com.blackened.healthapp.presentation

import android.widget.Toast
import com.blackened.healthapp.data.NetworkClient
import com.blackened.healthapp.data.VerificationRequestDTO
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.ErrorResponseDTO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class VerificationViewModel : ViewModel() {
    var code by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onCodeChanged(newValue: String) {
        code = newValue
    }

    fun onVerificationClick(email: String, onVerificationSuccess: () -> Unit) {
        if (code.isBlank()) return

        isLoading = true

        viewModelScope.launch {
            try {
                val verificationData = VerificationRequestDTO(
                    email = email,
                    code = code
                )

                val response = NetworkClient.authApi.verify(verificationData)
                val responseMessage = response.string()

                if (responseMessage.contains("Почта успешно подтверждена!")) {
                    onVerificationSuccess()
                }
            } catch (e: HttpException) {
                //errorResponse
                val errorBody = e.response()?.errorBody()?.string()

                if (!errorBody.isNullOrBlank()) {

                    val errDto = Json.decodeFromString<ErrorResponseDTO>(errorBody)

                    /*  Toast.makeText(null, errDto.message, Toast.LENGTH_LONG).show()   */
                }

            } catch (e: Exception) {

            } finally {
                isLoading = false
            }
        }
    }
}