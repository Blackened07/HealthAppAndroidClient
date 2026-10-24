package com.blackened.healthapp.presentation.login

sealed interface AuthStateUi {
    object Loading: AuthStateUi
    object Authenticated: AuthStateUi
    object NotAuthenticated: AuthStateUi
}