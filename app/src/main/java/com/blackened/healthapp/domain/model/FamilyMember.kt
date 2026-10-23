package com.blackened.healthapp.domain.model

data class FamilyMember(
    val id: Long,
    val email: String,
    val firstName: String,
    val familyRole: String,
    val lastActivityFormatted: String
)