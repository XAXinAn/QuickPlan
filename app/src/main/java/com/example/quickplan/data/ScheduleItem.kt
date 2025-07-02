package com.example.quickplan.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

enum class Urgency {
    OVERDUE,
    WITHIN_ONE_DAY,
    WITHIN_THREE_DAYS,
    WITHIN_ONE_WEEK,
    WITHIN_ONE_MONTH,
    BEYOND_ONE_MONTH
}

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: LocalDate,
    val time: LocalTime,
    val content: String,
    val urgency: Urgency = Urgency.BEYOND_ONE_MONTH
)