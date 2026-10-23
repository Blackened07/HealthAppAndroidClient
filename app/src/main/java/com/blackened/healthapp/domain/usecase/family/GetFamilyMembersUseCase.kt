package com.blackened.healthapp.domain.usecase.family

import com.blackened.healthapp.domain.model.FamilyMember
import com.blackened.healthapp.domain.repository.FamilyRepository

class GetFamilyMembersUseCase(private val repository: FamilyRepository) {
    suspend operator fun invoke(familyId: Long) : Result<List<FamilyMember>> {
        return repository.getFamilyMembers(familyId)
    }
}