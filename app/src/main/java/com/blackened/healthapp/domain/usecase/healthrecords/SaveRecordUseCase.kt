package com.blackened.healthapp.domain.usecase.healthrecords

import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.domain.repository.HealthRepository

class SaveRecordUseCase(private val repository: HealthRepository) {
    suspend operator fun invoke(
        targetId: Long,
        type: String,
        v1: Double,
        v2: Double?,
        note: String?,
        timestamp: Long,
        offset: Int
    ): Result<HealthRecordResponseDTO> {
        if (v1 <= 0.0 || v1 > 999.9) {
            return Result.failure(IllegalArgumentException("Недопустимое основное значение"))
        }
        return repository.createRecord(targetId, type, v1, v2, note, timestamp, offset)
    }
}