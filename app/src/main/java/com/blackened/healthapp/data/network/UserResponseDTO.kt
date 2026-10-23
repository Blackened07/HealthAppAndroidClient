package com.blackened.healthapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    val id: Long,
    val email: String,
    val firstName: String,
    val familyRole: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val lastActivity: String?
)