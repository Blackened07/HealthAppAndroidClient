package com.blackened.healthapp.presentation.home

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.data.SessionManager
import com.blackened.healthapp.data.repository.HealthRepositoryImpl
import com.blackened.healthapp.domain.model.AppNetworkException
import com.blackened.healthapp.domain.model.ErrorResponseDTO
import com.blackened.healthapp.domain.usecase.healthrecords.DeleteRecordUseCase
import com.blackened.healthapp.domain.usecase.healthrecords.GetHistoryUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarViewModel(
    private val getHistoryUseCase: GetHistoryUseCase = GetHistoryUseCase(HealthRepositoryImpl()),
    private val deleteRecordUseCase: DeleteRecordUseCase = DeleteRecordUseCase(HealthRepositoryImpl())
) : ViewModel() {
    private var currentYearMonth by mutableStateOf(YearMonth.now())

    var selectedDay by mutableStateOf(LocalDate.now().dayOfMonth)
        private set


    val currentMonthYearText: String
        get() = currentYearMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
            .replaceFirstChar { it.uppercase() }


    var daysInMonthList by mutableStateOf<List<Int?>>(emptyList())
        private set

    var recordsList: List<HealthRecordResponseDTO> by mutableStateOf(emptyList())
        private set

    var isListLoading by mutableStateOf(false)
        private set

    var selectedNoteText by mutableStateOf<String?>(null)
        private set

    var editingRecordId by mutableStateOf<Long?>(null)
        private set

    var errorResponse by mutableStateOf<ErrorResponseDTO?>(null)
        private set

    init {
        generateCalendarDays()
        loadRecordsForDay(selectedDay)
    }

    fun showNote(note: String?) {
        selectedNoteText = if (note.isNullOrBlank()) "К этой записи нет примечания." else note
    }

    fun dismissNote() {
        selectedNoteText = null
    }

    fun clearError() {errorResponse = null}

    fun deleteRecord(recordId: Long) {
        isListLoading = true

        viewModelScope.launch {
            deleteRecordUseCase(recordId)
                .onSuccess {
                    recordsList = recordsList.filter {it.id != recordId}.toMutableList()
                }
                .onFailure { e ->
                    if (e is AppNetworkException) {
                        errorResponse = e.errorData
                    }
                }
            loadRecordsForDay(selectedDay)
            isListLoading = false
        }
    }

    fun startEditing(recordId: Long) {
        editingRecordId = recordId
    }

    fun stopEditing() {
        editingRecordId = null
    }

    private fun generateCalendarDays() {
        val list = mutableListOf<Int?>()

        val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value

        for (i in 1 until firstDayOfWeek) {
            list.add(null)
        }

        for (day in 1..currentYearMonth.lengthOfMonth()) {
            list.add(day)
        }

        daysInMonthList = list
    }

    fun previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1)
        generateCalendarDays()
        selectedDay = 1
        loadRecordsForDay(selectedDay)
    }

    fun nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1)
        generateCalendarDays()
        selectedDay = 1
        loadRecordsForDay(selectedDay)
    }

    fun onDaySelected(day: Int) {
        if (selectedDay == day) return
        selectedDay = day
        loadRecordsForDay(day)
    }

    @SuppressLint("DefaultLocale")
    public fun loadRecordsForDay(day: Int) {
        isListLoading = true

        val year = currentYearMonth.year
        val monthStr = String.format("%02d", currentYearMonth.monthValue)
        val dayStr = String.format("%02d", day)
        val fromTime = "$year-$monthStr-${dayStr}T00:00:00"
        val toTime = "$year-$monthStr-${dayStr}T23:59:59"

        viewModelScope.launch {

            getHistoryUseCase(SessionManager.userId, null, fromTime, toTime)
                .onSuccess { domainList -> recordsList = domainList }
                .onFailure { e ->
                    if (e is AppNetworkException) {
                        errorResponse = e.errorData
                    }
                    recordsList = emptyList()
                }

                isListLoading = false
        }
    }
}