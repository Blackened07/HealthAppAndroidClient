package com.blackened.healthapp.domain.usecase.family

import com.blackened.healthapp.domain.model.FamilyInfo
import com.blackened.healthapp.domain.repository.FamilyRepository

class CheckFamilyStatusUseCase(private val repository: FamilyRepository) {
    suspend operator fun invoke(): Result<FamilyInfo> {
        return repository.isNoFamily()
    }
}