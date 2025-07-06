package com.example.quickplan.data.model

data class ModelResponse(
    val hasNotification: Boolean?,
    val events: List<Event>?,
    val confidence: Double?
)

data class Event(
    val title: String?,
    val deadline: String?,
    val description: String?,
    val priority: String?,
    val source: String?
)