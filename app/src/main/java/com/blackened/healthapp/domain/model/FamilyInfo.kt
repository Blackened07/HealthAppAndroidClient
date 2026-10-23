package com.blackened.healthapp.domain.model

data class FamilyInfo(
    val id: Long,
    val name: String,
    val role: String,
    val hasFamily: Boolean
)