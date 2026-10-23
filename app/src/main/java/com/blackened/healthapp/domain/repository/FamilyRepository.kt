package com.blackened.healthapp.domain.repository

import com.blackened.healthapp.domain.model.FamilyInfo
import com.blackened.healthapp.domain.model.FamilyMember

interface FamilyRepository {
    suspend fun isNoFamily() : Result<FamilyInfo>
    suspend fun getFamilyMembers(familyId: Long) : Result<List<FamilyMember>>
}