package com.blackened.healthapp.domain.usecase.healthrecords

import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.domain.repository.HealthRepository

class ReportUseCase(private val repository: HealthRepository) {
    suspend operator fun invoke( targetId: Long,
                                 type: String?,
                                 from: String?,
                                 to: String?): Result<List<HealthRecordResponseDTO>> {
       return repository.getHistory(
            targetId = targetId,
            type = type,
            from = from,
            to = to
        )
    }
}