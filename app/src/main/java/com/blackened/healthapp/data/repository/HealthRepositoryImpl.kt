package com.blackened.healthapp.data.repository

import com.blackened.healthapp.data.network.HealthRecordRequestDTO
import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.data.network.NetworkClient
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.repository.HealthRepository
import com.blackened.healthapp.utils.ErrorUtil
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class HealthRepositoryImpl : HealthRepository {
    override suspend fun getHistory(
        targetId: Long,
        type: String?,
        from: String?,
        to: String?
    ): Result<List<HealthRecordResponseDTO>> {
        return try {
            val response = NetworkClient.healthApi.getHistory(targetId, type, from, to)
            val domainRecords = response.map { dto ->
                HealthRecordResponseDTO(
                    dto.id,
                    dto.type,
                    dto.value1,
                    dto.value2,
                    dto.note,
                    dto.timestamp,
                    dto.userName
                )
            }
            Result.success(domainRecords)
        } catch (e: HttpException) {
            val parsedError = ErrorUtil.parseError(e)
            Result.failure(AppNetworkException(parsedError))
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }


    override suspend fun createRecord(
        targetId: Long,
        type: String,
        v1: Double,
        v2: Double?,
        note: String?,
        timestamp: Long,
        offset: Int
    ): Result<HealthRecordResponseDTO> {
        return try {
            val requestDTO = HealthRecordRequestDTO(type, v1, v2, note, timestamp, offset)
            val response = NetworkClient.healthApi.createRecord(targetId, requestDTO)
            val domainRecord = HealthRecordResponseDTO(
                response.id,
                response.type,
                response.value1,
                response.value2,
                response.note,
                response.timestamp,
                response.userName
            )
            Result.success(domainRecord)
        } catch (e: HttpException) {
            val parsedError = ErrorUtil.parseError(e)
            Result.failure(AppNetworkException(parsedError))
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }

    override suspend fun updateRecord(
        recordId: Long,
        type: String,
        v1: Double,
        v2: Double?,
        note: String?,
        timestamp: Long,
        offset: Int
    ): Result<HealthRecordResponseDTO> {
        return try {
            val requestDTO = HealthRecordRequestDTO(type, v1, v2, note, timestamp, offset)
            val response = NetworkClient.healthApi.updateRecord(recordId, requestDTO)
            val domainRecord = HealthRecordResponseDTO(
                response.id,
                response.type,
                response.value1,
                response.value2,
                response.note,
                response.timestamp,
                response.userName
            )
            Result.success(domainRecord)
        } catch (e: HttpException) {
            val parsedError = ErrorUtil.parseError(e)
            Result.failure(AppNetworkException(parsedError))
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }

    override suspend fun deleteRecord(recordId: Long): Result<Unit> {
        return try {
            NetworkClient.healthApi.deleteRecord(recordId)
            Result.success(Unit)
        } catch (e: HttpException) {
            val parsedError = ErrorUtil.parseError(e)
            Result.failure(AppNetworkException(parsedError))
        } catch (e: Exception) {
            Result.failure(AppNetworkException(ErrorUtil.getDefault(e)))
        }
    }
}