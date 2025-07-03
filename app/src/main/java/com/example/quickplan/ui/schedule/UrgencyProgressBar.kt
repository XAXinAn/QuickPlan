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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import com.example.quickplan.R
import androidx.compose.material3.MaterialTheme
import com.example.quickplan.data.getUrgencyColor

@Composable
fun UrgencyProgressBar(
    schedules: List<ScheduleItem>,
    onUrgencySelected: (Urgency) -> Unit
) {
    val totalSchedules = schedules.size

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                        .clip(RoundedCornerShape(4.dp))
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
                                .padding(horizontal = 1.dp) // Add 2dp gap between rectangles
                                .clip(RoundedCornerShape(4.dp)) // Slightly smaller corner radius
                                .background(getUrgencyColor(urgency))
                                .clickable { onUrgencySelected(urgency) }
                        )
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Urgency.values().forEach { urgency ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(getUrgencyColor(urgency))
                    )
                    Text(
                        text = when (urgency) {
                            Urgency.OVERDUE -> stringResource(R.string.urgency_overdue)
                            Urgency.WITHIN_ONE_DAY -> stringResource(R.string.urgency_within_one_day)
                            Urgency.WITHIN_THREE_DAYS -> stringResource(R.string.urgency_within_three_days)
                            Urgency.WITHIN_ONE_WEEK -> stringResource(R.string.urgency_within_one_week)
                            Urgency.WITHIN_ONE_MONTH -> stringResource(R.string.urgency_within_one_month)
                            Urgency.BEYOND_ONE_MONTH -> stringResource(R.string.urgency_beyond_one_month)
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
