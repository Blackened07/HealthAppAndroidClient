package com.blackened.healthapp.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackened.healthapp.data.HealthRecordResponseDTO
import com.blackened.healthapp.data.NetworkClient
import com.blackened.healthapp.data.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarViewModel : ViewModel() {
    private var currentYearMonth by mutableStateOf(YearMonth.now())

    var selectedDay by mutableStateOf(LocalDate.now().dayOfMonth)
        private set


    val currentMonthYearText: String
        get() = currentYearMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
            .replaceFirstChar { it.uppercase() }


    var daysInMonthList by mutableStateOf<List<Int?>>(emptyList())
        private set

    var recordsList by mutableStateOf<List<HealthRecordResponseDTO>>(emptyList())
        private set

    var isListLoading by mutableStateOf(false)
        private set

    var selectedNoteText by mutableStateOf<String?>(null)
        private set

    var editingRecordId by mutableStateOf<Long?>(null)
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

    fun deleteRecord(recordId: Long) {
        isListLoading = true

        viewModelScope.launch {
            try {
                NetworkClient.healthApi.deleteRecord(recordId)
                loadRecordsForDay(selectedDay)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isListLoading = false
            }
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
            try {
                val currentTargetId = SessionManager.userId
                val response = NetworkClient.healthApi.getHistory(
                    currentTargetId,
                    null,
                    fromTime,
                    toTime
                )

                recordsList = response

            } catch (e: HttpException) {
                recordsList = emptyList()
                e.printStackTrace()
            } catch (e: Exception) {
                recordsList = emptyList()
                e.printStackTrace()
            }finally {
                isListLoading = false
            }
        }
    }
}