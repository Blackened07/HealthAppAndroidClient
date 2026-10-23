package com.blackened.healthapp.data.repository

import com.blackened.healthapp.data.network.NetworkClient
import com.blackened.healthapp.data.network.UserResponseDTO
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.model.FamilyInfo
import com.blackened.healthapp.domain.model.FamilyMember
import com.blackened.healthapp.domain.repository.FamilyRepository
import com.blackened.healthapp.presentation.home.formatServerDate
import com.blackened.healthapp.utils.ErrorUtil
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class FamilyRepositoryImpl : FamilyRepository {

    override suspend fun isNoFamily(): Result<FamilyInfo> {
        return try {
            val response = NetworkClient.familyApi.isNoFamily()
            val domain = FamilyInfo(
                response.familyId,
                response.familyName,
                response.familyRole,
                hasFamily = response.familyId != 0L
            )
            Result.success(domain)
        } catch (e: HttpException) {
            if (e.code() == 403) {
                Result.success(
                    FamilyInfo(0L, "", "", false)
                )
            } else {
                val parsedError = ErrorUtil.parseError(e)
                Result.failure(AppNetworkException(parsedError))
            }
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }

    override suspend fun getFamilyMembers(familyId: Long): Result<List<FamilyMember>> {
        return try {
            val response = NetworkClient.familyApi.getFamilyMembers(familyId)
            val members = mapToMembers(response)
            Result.success(members)
        } catch (e: HttpException) {
            val parsedError = ErrorUtil.parseError(e)
            Result.failure(AppNetworkException(parsedError))
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }


    private fun mapToMembers(response: List<UserResponseDTO>): List<FamilyMember> {
        return response.map { dto ->
            FamilyMember(
                id = dto.id,
                email = dto.email,
                firstName = dto.firstName,
                familyRole = dto.familyRole,
                lastActivityFormatted = dto.lastActivity?.let {
                    formatServerDate(it)
                } ?: ""
            )
        }
    }

}