package com.example.quickplan.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickplan.data.ScheduleDao
import com.example.quickplan.data.ScheduleItem
import com.example.quickplan.data.Urgency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class ScheduleViewModel(private val scheduleDao: ScheduleDao) : ViewModel() {

    val scheduleItems: StateFlow<List<ScheduleItem>> = scheduleDao.getAllScheduleItems().map { items ->
        items.sortedWith(compareBy<ScheduleItem> { it.date }.thenBy { it.time })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val scheduleItemsByUrgency: StateFlow<List<ScheduleItem>> = scheduleDao.getAllScheduleItemsByUrgency().map { items ->
        items.sortedWith(compareBy<ScheduleItem> { it.urgency }.thenBy { it.date }.thenBy { it.time })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedUrgency = MutableStateFlow<Urgency?>(null)
    val selectedUrgency: StateFlow<Urgency?> = _selectedUrgency

    fun onUrgencySelected(urgency: Urgency) {
        if (_selectedUrgency.value == urgency) {
            _selectedUrgency.value = null
        } else {
            _selectedUrgency.value = urgency
        }
    }

    fun getScheduleForDate(date: LocalDate): List<ScheduleItem> {
        return scheduleItems.value.filter { it.date == date }.sortedBy { it.time }
    }

    fun getScheduleItemById(id: String): ScheduleItem? {
        return scheduleItems.value.find { it.id == id }
    }

    private fun calculateUrgency(date: LocalDate, time: LocalTime): Urgency {
        val now = java.time.LocalDateTime.now()
        val scheduleDateTime = java.time.LocalDateTime.of(date, time)
        val minutesUntil = ChronoUnit.MINUTES.between(now, scheduleDateTime)

        return when {
            minutesUntil < 0 -> Urgency.OVERDUE
            minutesUntil <= 24 * 60 -> Urgency.WITHIN_ONE_DAY
            minutesUntil <= 3 * 24 * 60 -> Urgency.WITHIN_THREE_DAYS
            minutesUntil <= 7 * 24 * 60 -> Urgency.WITHIN_ONE_WEEK
            minutesUntil <= 30 * 24 * 60 -> Urgency.WITHIN_ONE_MONTH
            else -> Urgency.BEYOND_ONE_MONTH
        }
    }

    fun addScheduleItem(item: ScheduleItem) {
        viewModelScope.launch {
            val itemWithUrgency = item.copy(urgency = calculateUrgency(item.date, item.time))
            scheduleDao.insertScheduleItem(itemWithUrgency)
            Log.d("ScheduleViewModel", "Added item to DB: $itemWithUrgency")
        }
    }

    fun updateScheduleItem(item: ScheduleItem) {
        viewModelScope.launch {
            val itemWithUrgency = item.copy(urgency = calculateUrgency(item.date, item.time))
            scheduleDao.updateScheduleItem(itemWithUrgency)
            Log.d("ScheduleViewModel", "Updated item in DB: $itemWithUrgency")
        }
    }

    fun deleteScheduleItem(item: ScheduleItem) {
        viewModelScope.launch {
            scheduleDao.deleteScheduleItem(item)
            Log.d("ScheduleViewModel", "Deleted item from DB: $item")
        }
    }
}