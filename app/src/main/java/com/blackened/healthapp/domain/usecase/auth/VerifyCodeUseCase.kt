package com.blackened.healthapp.domain.usecase.auth

import com.blackened.healthapp.domain.repository.AuthRepository

class VerifyCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String): Result<String> {
        return repository.verifyCode(email, code)
    }
}