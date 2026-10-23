package com.blackened.healthapp.domain.repository

import com.blackened.healthapp.domain.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun register(email: String, firstName: String, role: String, password: String): Result<Long>
    suspend fun verifyCode(email: String, code: String): Result<String>
    suspend fun logout(): Result<Unit>

    suspend fun isTokenValid(): Boolean

}