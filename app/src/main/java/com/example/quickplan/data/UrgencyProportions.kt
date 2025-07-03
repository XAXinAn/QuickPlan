package com.example.quickplan.data

data class UrgencyProportions(
    val overdue: Float = 0f,
    val withinOneDay: Float = 0f,
    val withinThreeDays: Float = 0f,
    val withinOneWeek: Float = 0f,
    val withinOneMonth: Float = 0f,
    val beyondOneMonth: Float = 0f
)
