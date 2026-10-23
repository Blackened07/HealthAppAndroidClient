package com.blackened.healthapp.presentation.family

import com.blackened.healthapp.domain.model.FamilyInfo
import com.blackened.healthapp.domain.model.FamilyMember

sealed class FamilyUiState {
    object Loading : FamilyUiState()
    data class NoFamily(val info: FamilyInfo) : FamilyUiState()
    data class HasFamily(val info: FamilyInfo, val members: List<FamilyMember>) : FamilyUiState()
    object Error : FamilyUiState()
}