package com.blackened.healthapp.domain.repository

import com.blackened.healthapp.data.network.HealthRecordResponseDTO

interface HealthRepository {
    suspend fun getHistory(targetId: Long, type: String?, from: String?, to: String?): Result<List<HealthRecordResponseDTO>>
    suspend fun createRecord(targetId: Long, type: String, v1: Double, v2: Double?, note: String?, timestamp: Long, offset: Int): Result<HealthRecordResponseDTO>
    suspend fun updateRecord(recordId: Long, type: String, v1: Double, v2: Double?, note: String?, timestamp: Long, offset: Int): Result<HealthRecordResponseDTO>
    suspend fun deleteRecord(recordId: Long): Result<Unit>
}