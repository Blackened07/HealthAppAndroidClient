package com.blackened.healthapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDTO(
    val timestamp: String,
    val status: Int,
    val message: String,
    val critical: Boolean
)

class AppNetworkException(val errorData: ErrorResponseDTO) : RuntimeException(errorData.message)