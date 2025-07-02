package com.example.quickplan.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quickplan.data.ScheduleItem
import com.example.quickplan.data.Urgency

@Composable
fun UrgencyProgressBar(
    schedules: List<ScheduleItem>,
    onUrgencySelected: (Urgency) -> Unit
) {
    val totalSchedules = schedules.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(vertical = 8.dp)
    ) {
        if (totalSchedules == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            )
        } else {
            val urgencyCounts = schedules.groupingBy { it.urgency }.eachCount()
            val urgencyOrder = Urgency.values()

            urgencyOrder.forEach { urgency ->
                val count = urgencyCounts[urgency] ?: 0
                if (count > 0) {
                    val weight = count.toFloat() / totalSchedules.toFloat()
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxSize()
                            .background(getUrgencyColor(urgency))
                            .clickable { onUrgencySelected(urgency) }
                    )
                }
            }
        }
    }
}
