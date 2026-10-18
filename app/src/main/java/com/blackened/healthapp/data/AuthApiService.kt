package com.blackened.healthapp.data


import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class RegisterAccountRequestDTO(
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

@Serializable
data class LoginRequestDTO(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDTO(
    val message: String,
    val userId: Long,
    val firstName: String,
    val success: Boolean
)

@Serializable
data class VerificationRequestDTO(
    val email: String,
    val code: String
)

@Serializable
data class ErrorResponseDTO(
    val timestamp: String,
    val status: Int,
    val message: String,
    val critical: Boolean
)


interface AuthApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterAccountRequestDTO): RegisterAccountResponseDTO

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDTO): AuthResponseDTO

    @POST("auth/verify")
    suspend fun verify(@Body request: VerificationRequestDTO): ResponseBody



}