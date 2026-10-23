package com.blackened.healthapp.domain.usecase.healthrecords

import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.domain.repository.HealthRepository

class UpdateRecordUseCase(private val repository: HealthRepository) {

    suspend operator fun invoke(
        recordId: Long,
        type: String,
        v1: Double,
        v2: Double?,
        note: String?,
        timestamp: Long,
        offsetZone: Int
    ): Result<HealthRecordResponseDTO> {
        return repository.updateRecord(recordId, type, v1, v2, note, timestamp, offsetZone)
    }

}