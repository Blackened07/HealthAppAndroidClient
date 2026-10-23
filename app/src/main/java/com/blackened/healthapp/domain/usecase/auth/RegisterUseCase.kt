package com.blackened.healthapp.domain.usecase.auth

import com.blackened.healthapp.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, firstName: String, role: String, password: String): Result<Long> {
        return repository.register(email, firstName, role, password)
    }
}