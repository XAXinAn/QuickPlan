package com.example.quickplan.model

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val location: String,
    val date: String,  // LocalDate 转 String 存储
    val time: String   // LocalTime 转 String 存储
)
