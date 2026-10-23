package com.blackened.healthapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class FamilyDTO(
    val familyId: Long,
    val familyName: String,
    val familyRole: String
)