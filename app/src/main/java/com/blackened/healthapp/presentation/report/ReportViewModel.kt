package com.blackened.healthapp.presentation.report

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.ExcelReport
import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.data.repository.HealthRepositoryImpl
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.usecase.healthrecords.ReportUseCase
import com.blackened.healthapp.presentation.MetricType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class ReportViewModel(
    private val reportUseCase: ReportUseCase = ReportUseCase(HealthRepositoryImpl())
) : ViewModel() {

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

    var errorResponse by mutableStateOf<ErrorResponseDTO?>(null)
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

    fun clearError() {
        errorResponse = null
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
            reportUseCase(SessionManager.userId, selectedType.serverName, fromIso, toIso)
                .onSuccess { response ->
                    reportRecords = response.sortedBy { it.timestamp }
                }
                .onFailure {  e ->
                    if (e is AppNetworkException) {
                        errorResponse = e.errorData
                    }
                    reportRecords = emptyList()
                }
            isLoading = false
        }
    }

    fun exportAndShareExcel(context: Context) {

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