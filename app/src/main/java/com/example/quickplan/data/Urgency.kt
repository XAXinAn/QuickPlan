package com.example.quickplan.data

import androidx.compose.ui.graphics.Color

enum class Urgency {
    OVERDUE,
    WITHIN_ONE_DAY,
    WITHIN_THREE_DAYS,
    WITHIN_ONE_WEEK,
    WITHIN_ONE_MONTH,
    BEYOND_ONE_MONTH
}

fun getUrgencyColor(urgency: Urgency): Color {
    return when (urgency) {
        Urgency.OVERDUE -> Color(0xFFCC0000)
        Urgency.WITHIN_ONE_DAY -> Color(0xFFE64A19)
        Urgency.WITHIN_THREE_DAYS -> Color(0xFFCC7A00)
        Urgency.WITHIN_ONE_WEEK -> Color(0xFFCC9A00)
        Urgency.WITHIN_ONE_MONTH -> Color(0xFF888888)
        Urgency.BEYOND_ONE_MONTH -> Color(0xFFAAAAAA)
    }
}