package com.example.quickplan.ui.schedule

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quickplan.R
import com.example.quickplan.data.ScheduleItem
import com.example.quickplan.data.Urgency
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleScreen(
    navController: NavController,
    dateString: String?,
    scheduleId: String? = null,
    scheduleViewModel: ScheduleViewModel
) {
    val selectedDate = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()

    var content by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf(LocalTime.MIDNIGHT) }

    val isEditing = scheduleId != null
    val screenTitle = if (isEditing) stringResource(R.string.edit_schedule_title) else stringResource(R.string.add_schedule_title)

    LaunchedEffect(scheduleId) {
        if (isEditing) {
            scheduleId?.let { id ->
                val existingItem = scheduleViewModel.getScheduleItemById(id)
                existingItem?.let {
                    content = it.content
                    selectedTime = it.time
                }
            }
        }
    }

    val context = LocalContext.current

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, hour: Int, minute: Int ->
            selectedTime = LocalTime.of(hour, minute)
        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.selected_date, selectedDate.format(DateTimeFormatter.ofPattern("yyyy年M月dd日"))),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val urgencyText = scheduleViewModel.getScheduleItemById(scheduleId ?: "")?.urgency?.let {
                when (it) {
                    Urgency.OVERDUE -> stringResource(R.string.urgency_overdue)
                    Urgency.WITHIN_ONE_DAY -> stringResource(R.string.urgency_within_one_day)
                    Urgency.WITHIN_THREE_DAYS -> stringResource(R.string.urgency_within_three_days)
                    Urgency.WITHIN_ONE_WEEK -> stringResource(R.string.urgency_within_one_week)
                    Urgency.WITHIN_ONE_MONTH -> stringResource(R.string.urgency_within_one_month)
                    Urgency.BEYOND_ONE_MONTH -> stringResource(R.string.urgency_beyond_one_month)
                }
            } ?: ""
            if (urgencyText.isNotBlank()) {
                Text(
                    text = "紧急程度: ${urgencyText}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.schedule_content_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { timePickerDialog.show() }) {
                Text(stringResource(R.string.select_time, selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        val updatedItem = scheduleViewModel.getScheduleItemById(scheduleId!!)?.copy(
                            time = selectedTime,
                            content = content
                        )
                        updatedItem?.let { scheduleViewModel.updateScheduleItem(it) }
                    } else {
                        val newItem = ScheduleItem(
                            date = selectedDate,
                            time = selectedTime,
                            content = content
                        )
                        scheduleViewModel.addScheduleItem(newItem)
                    }
                    navController.navigate("calendar?date=${selectedDate}")
                },
                enabled = content.isNotBlank()
            ) {
                Text(screenTitle)
            }
        }
    }
}