package com.example.quickplan.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quickplan.data.ScheduleDao
import com.example.quickplan.data.ScheduleItem
import kotlinx.coroutines.flow.Flow

class ScheduleListViewModel(private val scheduleDao: ScheduleDao) : ViewModel() {

    fun getAllSchedules(): Flow<List<ScheduleItem>> {
        return scheduleDao.getAllScheduleItems()
    }
}

class ScheduleListViewModelFactory(private val scheduleDao: ScheduleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleListViewModel(scheduleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}