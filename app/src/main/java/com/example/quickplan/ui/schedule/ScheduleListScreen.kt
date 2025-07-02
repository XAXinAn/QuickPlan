package com.example.quickplan.ui.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.quickplan.R
import com.example.quickplan.data.ScheduleItem
import com.example.quickplan.data.Urgency
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.quickplan.ui.theme.Orange

@Composable
fun getUrgencyColor(urgency: Urgency): Color {
    return when (urgency) {
        Urgency.OVERDUE -> Color.Red
        Urgency.WITHIN_ONE_DAY -> Orange
        Urgency.WITHIN_THREE_DAYS -> Color.Yellow
        Urgency.WITHIN_ONE_WEEK -> Color.Green
        Urgency.WITHIN_ONE_MONTH -> Color.Blue
        Urgency.BEYOND_ONE_MONTH -> Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    navController: NavController,
    scheduleViewModel: ScheduleViewModel
) {
    val schedules by scheduleViewModel.scheduleItems.collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ScheduleItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.all_schedules_title)) },
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
            items(schedules.sortedWith(compareBy({ it.date }, { it.time }))) { schedule ->
                ScheduleListItem(
                    schedule = schedule,
                    onEdit = { 
                        navController.navigate("add_edit_schedule/${schedule.date}?scheduleId=${schedule.id}")
                    },
                    onDelete = { 
                        itemToDelete = it
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message, itemToDelete?.content ?: "")) },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { 
                        itemToDelete?.let { scheduleViewModel.deleteScheduleItem(it) }
                        showDeleteDialog = false
                        itemToDelete = null
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { 
                        showDeleteDialog = false
                        itemToDelete = null
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }
}

@Composable
fun ScheduleListItem(schedule: ScheduleItem, onEdit: (ScheduleItem) -> Unit, onDelete: (ScheduleItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.content,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${schedule.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))} ${schedule.time.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "紧急程度: ${when (schedule.urgency) {
                        Urgency.OVERDUE -> stringResource(R.string.urgency_overdue)
                        Urgency.WITHIN_ONE_DAY -> stringResource(R.string.urgency_within_one_day)
                        Urgency.WITHIN_THREE_DAYS -> stringResource(R.string.urgency_within_three_days)
                        Urgency.WITHIN_ONE_WEEK -> stringResource(R.string.urgency_within_one_week)
                        Urgency.WITHIN_ONE_MONTH -> stringResource(R.string.urgency_within_one_month)
                        Urgency.BEYOND_ONE_MONTH -> stringResource(R.string.urgency_beyond_one_month)
                        else -> "未知紧急程度" // Fallback for any unhandled urgency
                    }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = getUrgencyColor(schedule.urgency)
                )
            }
            Row {
                IconButton(onClick = { onEdit(schedule) }) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_schedule_title))
                }
                IconButton(onClick = { onDelete(schedule) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_schedule))
                }
            }
        }
    }
}