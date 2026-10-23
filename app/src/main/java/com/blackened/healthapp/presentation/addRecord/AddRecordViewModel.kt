package com.blackened.healthapp.presentation.addRecord

import android.icu.util.TimeZone
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.data.repository.HealthRepositoryImpl
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.usecase.healthrecords.SaveRecordUseCase
import com.blackened.healthapp.domain.usecase.healthrecords.UpdateRecordUseCase
import com.blackened.healthapp.presentation.MetricType
import kotlinx.coroutines.launch

class AddRecordViewModel(
    private val saveRecordUseCase: SaveRecordUseCase = SaveRecordUseCase(HealthRepositoryImpl()),
    private val updateRecordUseCase: UpdateRecordUseCase = UpdateRecordUseCase(HealthRepositoryImpl())
) : ViewModel() {

    val availableMetrics = MetricType.entries

    var selectedType by mutableStateOf(MetricType.BLOOD_PRESSURE)
        private set

    var value1Text by mutableStateOf("")
        private set

    var value2Text by mutableStateOf("")
        private set

    var noteText by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorResponse by mutableStateOf<ErrorResponseDTO?>(null)
        private set

    fun clearError() { errorResponse = null }

    fun onTypeChanged(newType: MetricType) {
        selectedType = newType
    }

    fun onValue1Changed(newValue: String) {
        value1Text = newValue
    }

    fun onValue2Changed(newValue: String) {
        value2Text = newValue
    }

    fun onNoteChanged(newValue: String) {
        noteText = newValue
    }

    var editingRecordId by mutableStateOf<Long?>(null)
        private set

    fun prepareForEdit(record: HealthRecordResponseDTO) {
        editingRecordId = record.id
        value1Text = record.value1.toString()
        value2Text = record.value2?.toString() ?: ""
        noteText = record.note ?: ""

        val matchingEnum = MetricType.entries.find { it.serverName == record.type }
        if (matchingEnum != null) {
            onTypeChanged(matchingEnum)
        }
    }

    private fun clearForm() {
        value1Text = ""
        value2Text = ""
        noteText = ""
        editingRecordId = null
    }

    fun saveRecord(onRecordAdded: () -> Unit) {
        val v1 = value1Text.toDoubleOrNull() ?: return
        val v2 = if (selectedType != MetricType.BLOOD_PRESSURE) 0.0 else value2Text.toDoubleOrNull()

        val currentTimestamp = System.currentTimeMillis()
        val currentOffset =
            TimeZone.getDefault().getOffset(currentTimestamp)

        isLoading = true

        viewModelScope.launch {
            val recordId = editingRecordId

            val result = if (recordId == null) {
                saveRecordUseCase(
                    SessionManager.userId,
                    selectedType.serverName,
                    v1, v2,
                    noteText,
                    currentTimestamp,
                    currentOffset
                )
            } else {
                updateRecordUseCase(
                    recordId,
                    selectedType.serverName,
                    v1, v2,
                    noteText,
                    currentTimestamp,
                    currentOffset
                )
            }

            result.onSuccess {
                clearForm()
                onRecordAdded()
            }.onFailure { e ->
                clearForm()
                if (e is AppNetworkException) {
                    errorResponse = e.errorData
                }

            }
            isLoading = false

        }
    }
}