package com.blackened.healthapp.domain.usecase.auth

import com.blackened.healthapp.domain.model.UserSession
import com.blackened.healthapp.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Поля не могут быть пустыми"))
        }
        return repository.login(email, password)
    }
}