package com.blackened.healthapp.domain.model

data class UserSession(
    val token: String,
    val userId: Long,
    val firstName: String,
    val email: String
)