package com.blackened.healthapp.presentation

import android.icu.util.TimeZone
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.HealthRecordRequestDTO
import com.blackened.healthapp.data.NetworkClient
import com.blackened.healthapp.data.SessionManager
import kotlinx.coroutines.launch

class AddRecordViewModel : ViewModel() {

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

    fun prepareForEdit(record: com.blackened.healthapp.data.HealthRecordResponseDTO) {
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
    }

    fun saveRecord(onRecordAdded: () -> Unit) {
        val v1 = value1Text.toDoubleOrNull() ?: return
        val v2 = if (selectedType != MetricType.BLOOD_PRESSURE) null else value2Text.toDoubleOrNull()

        val currentTimestamp = System.currentTimeMillis()
        val currentOffset =
            TimeZone.getDefault().getOffset(currentTimestamp)

        isLoading = true

        viewModelScope.launch {
            try {
                val request = HealthRecordRequestDTO(

                    type = selectedType.serverName,
                    value1 = v1,
                    value2 = v2,
                    note = noteText.ifBlank { null },
                    TIMESTAMP = currentTimestamp,
                    OFFSET_ZONE = currentOffset
                )

                val currentEditId = editingRecordId

                if (currentEditId != null) {
                    NetworkClient.healthApi.updateRecord(currentEditId, request)
                } else {
                    NetworkClient.healthApi.createRecord(targetId = SessionManager.userId, request = request)
                }

                clearForm()
                editingRecordId = null
                onRecordAdded()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}