package com.blackened.healthapp.data

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class RegisterAccountRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val systemRole: String
)

@Serializable
data class RegisterAccountResponseDTO(
    val id: Long,
    val email: String,
    val firstName: String,
    val familyRole: String,
    val lastActivity: String?,
)

interface AuthApiService {
    @POST("/auth/register")
    suspend fun register(@Body request: RegisterAccountRequest): RegisterAccountResponseDTO
}