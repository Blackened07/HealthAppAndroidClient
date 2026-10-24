package com.blackened.healthapp.presentation.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.repository.AuthRepositoryImpl
import com.blackened.healthapp.domain.usecase.auth.VerifyJwtTokenUseCase
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val jwtUseCase: VerifyJwtTokenUseCase =
        VerifyJwtTokenUseCase(AuthRepositoryImpl())): ViewModel() {
    private val _authState = MutableStateFlow<AuthStateUi>(AuthStateUi.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuthentication()
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            val isTokenValid = jwtUseCase.invoke()

            if (isTokenValid) {
                _authState.value = AuthStateUi.Authenticated
            } else {
                _authState.value = AuthStateUi.NotAuthenticated
            }
        }
    }
}