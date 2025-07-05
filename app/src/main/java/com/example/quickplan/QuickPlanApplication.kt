package com.example.quickplan

import android.app.Application
import com.example.quickplan.data.AppDatabase
import com.example.quickplan.data.ScheduleDao
import com.example.quickplan.ui.schedule.ScheduleViewModel
import com.example.quickplan.ui.schedule.ScheduleViewModelFactory
import com.example.quickplan.data.model.LargeModelService
import com.example.quickplan.data.model.LargeModelServiceImpl

class QuickPlanApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val scheduleDao: ScheduleDao by lazy { database.scheduleDao() }
    val scheduleViewModel: ScheduleViewModel by lazy { ScheduleViewModelFactory(scheduleDao).create(ScheduleViewModel::class.java) }

    // Provide a LargeModelService instance
    val largeModelService: LargeModelService by lazy { LargeModelServiceImpl() }

    val uploadImageViewModel: UploadImageViewModel by lazy { UploadImageViewModelFactory(this, largeModelService).create(UploadImageViewModel::class.java) }

    override fun onCreate() {
        super.onCreate()
    }
}

