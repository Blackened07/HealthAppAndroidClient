package com.blackened.healthapp.data.network

import retrofit2.http.GET
import retrofit2.http.Path

interface FamilyApiService {

    @GET("families/is-no-family")
    suspend fun isNoFamily() : FamilyDTO

    @GET("families/{familyId}/members")
    suspend fun getFamilyMembers(@Path("familyId") familyId: Long) : List<UserResponseDTO>

}