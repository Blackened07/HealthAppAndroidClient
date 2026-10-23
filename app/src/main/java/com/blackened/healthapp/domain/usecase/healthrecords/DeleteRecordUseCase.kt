package com.blackened.healthapp.domain.usecase.healthrecords

import com.blackened.healthapp.domain.repository.HealthRepository

class DeleteRecordUseCase(private val repository: HealthRepository) {
    suspend operator fun invoke(recordId: Long): Result<Unit> {
        return repository.deleteRecord(recordId)
    }
}