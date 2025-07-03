package com.example.quickplan.ui.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quickplan.R
import com.example.quickplan.data.Urgency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrgentScheduleListScreen(
    navController: NavController,
    scheduleViewModel: ScheduleViewModel,
    urgencyString: String?
) {
    val allSchedules by scheduleViewModel.scheduleItemsByUrgency.collectAsState(initial = emptyList())
    val selectedUrgency = urgencyString?.let { Urgency.valueOf(it) }

    val filteredSchedules = if (selectedUrgency != null) {
        allSchedules.filter { it.urgency == selectedUrgency }
    } else {
        allSchedules
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedUrgency?.let { urgency ->
                    when (urgency) {
                        Urgency.OVERDUE -> stringResource(R.string.urgency_overdue)
                        Urgency.WITHIN_ONE_DAY -> stringResource(R.string.urgency_within_one_day)
                        Urgency.WITHIN_THREE_DAYS -> stringResource(R.string.urgency_within_three_days)
                        Urgency.WITHIN_ONE_WEEK -> stringResource(R.string.urgency_within_one_week)
                        Urgency.WITHIN_ONE_MONTH -> stringResource(R.string.urgency_within_one_month)
                        Urgency.BEYOND_ONE_MONTH -> stringResource(R.string.urgency_beyond_one_month)
                    }
                } ?: stringResource(R.string.urgent_schedules_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp)
        ) {
            items(filteredSchedules) { schedule ->
                ScheduleListItem(
                    schedule = schedule,
                    onEdit = {
                        navController.navigate("add_edit_schedule/${schedule.date}?scheduleId=${schedule.id}")
                    },
                    onDelete = { /* Urgent schedules list does not support deletion directly */ }
                )
            }
        }
    }
}
