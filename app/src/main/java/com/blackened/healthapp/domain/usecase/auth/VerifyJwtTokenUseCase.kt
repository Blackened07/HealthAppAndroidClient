package com.blackened.healthapp.domain.usecase.auth

import com.blackened.healthapp.domain.repository.AuthRepository

class VerifyJwtTokenUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isTokenValid()
    }
}