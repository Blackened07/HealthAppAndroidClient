package com.blackened.healthapp.presentation.family

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.repository.FamilyRepositoryImpl
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.model.FamilyInfo
import com.blackened.healthapp.domain.usecase.family.CheckFamilyStatusUseCase
import com.blackened.healthapp.domain.usecase.family.GetFamilyMembersUseCase
import kotlinx.coroutines.launch

class FamilyViewModel(
    private val checkFamilyStatusUseCase: CheckFamilyStatusUseCase = CheckFamilyStatusUseCase(FamilyRepositoryImpl()),
    private val getFamilyMembersUseCase: GetFamilyMembersUseCase = GetFamilyMembersUseCase(FamilyRepositoryImpl()
    )
) : ViewModel() {

    var familyUiState by mutableStateOf<FamilyUiState>(FamilyUiState.Loading)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorResponse by mutableStateOf<ErrorResponseDTO?>(null)
        private set

    init {
        checkUserFamilyState()
    }

    fun clearError() { errorResponse = null }

    fun checkUserFamilyState() {
        isLoading = true
        errorResponse = null

        familyUiState = FamilyUiState.Loading

        viewModelScope.launch {
            checkFamilyStatusUseCase()
                .onSuccess { info ->
                    if (info.hasFamily) {
                        loadMembers(info)
                    } else {
                        familyUiState = FamilyUiState.NoFamily(info)
                    }
                }
                .onFailure { exception ->
                    familyUiState = FamilyUiState.Error
                    if (exception is AppNetworkException) {
                        errorResponse = exception.errorData
                    }
                }
            isLoading = false
        }
    }

    private suspend fun loadMembers(info: FamilyInfo) {
        getFamilyMembersUseCase(info.id)
            .onSuccess { membersList ->
                familyUiState = FamilyUiState.HasFamily(info, membersList)
            }
            .onFailure { exception ->
                familyUiState = FamilyUiState.Error
                if (exception is AppNetworkException) {
                    errorResponse = exception.errorData
                }
            }
    }
}