package com.blackened.healthapp.presentation

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.ExcelReport
import com.blackened.healthapp.data.HealthRecordResponseDTO
import com.blackened.healthapp.data.NetworkClient
import com.blackened.healthapp.data.SessionManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    var selectedType by mutableStateOf(MetricType.BLOOD_PRESSURE)
        private set

    var dateFrom by mutableStateOf<LocalDate?>(null)
        private set

    var dateTo by mutableStateOf<LocalDate?>(null)
        private set

    var reportRecords by mutableStateOf<List<HealthRecordResponseDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var activeClickedRecord by mutableStateOf<HealthRecordResponseDTO?>(null)
        private set

    fun onTypeChanged(newType: MetricType) {
        selectedType = newType
    }

    fun onDateFromChanged(date: LocalDate?) {
        dateFrom = date
    }

    fun onDateToChanged(date: LocalDate?) {
        dateTo = date
    }

    fun showRecordDetails(record: HealthRecordResponseDTO) {
        activeClickedRecord = record
    }

    fun dismissRecordDetails() {
        activeClickedRecord = null
    }

    fun loadReport() {
        isLoading = true

        val finalDateFrom = dateFrom ?: LocalDate.now().minusDays(30)
        val finalDateTo = dateTo ?: LocalDate.now()
        val validatedDateFrom = if (finalDateFrom.isAfter(finalDateTo))
            finalDateTo else finalDateFrom

        val fromIso = validatedDateFrom
            .atStartOfDay()
            .format(DateTimeFormatter.ISO_DATE_TIME)
        val toIso = finalDateTo
            .atTime(LocalTime.MAX)
            .format(DateTimeFormatter.ISO_DATE_TIME)

        viewModelScope.launch {
            try {
                val currentTargetId = SessionManager.userId

                val response = NetworkClient.healthApi.getHistory(
                    targetId = currentTargetId,
                    type = selectedType.serverName,
                    from = fromIso,
                    to = toIso
                )

                reportRecords = response.sortedBy { it.timestamp }

            } catch (e: Exception) {
                e.printStackTrace()
                reportRecords = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun exportAndShareExcel(context: android.content.Context) {

        if (reportRecords.isEmpty()) {
            Toast.makeText(
                context,
                "Нет записей для отчета",
                Toast.LENGTH_LONG)
                .show()
            return
        }

        val currentUserName = SessionManager.userName ?: "User"

        val generatedFile = ExcelReport.createReport(
            context = context,
            reportList = reportRecords,
            userName = currentUserName
        )

        if (generatedFile != null) {
            Toast.makeText(
                context,
                "Файл успешно создан",
                Toast.LENGTH_SHORT)
                .show()

            ExcelReport.shareFile(
                context = context,
                file = generatedFile
            )
        }
    }
}