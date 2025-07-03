package com.example.quickplan.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime



@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: LocalDate,
    val time: LocalTime,
    val content: String,
    val urgency: Urgency = Urgency.BEYOND_ONE_MONTH
)