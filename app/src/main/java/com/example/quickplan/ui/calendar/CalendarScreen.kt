package com.example.quickplan.ui.calendar

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.quickplan.ui.theme.Orange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quickplan.R
import com.example.quickplan.data.ScheduleItem
import com.example.quickplan.data.Urgency
import com.example.quickplan.ui.schedule.ScheduleViewModel
import com.example.quickplan.ui.schedule.UrgencyProgressBar
import com.example.quickplan.ui.theme.QuickPlanTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, scheduleViewModel: ScheduleViewModel, initialDateString: String? = null) {
    val initialDate = initialDateString?.let { LocalDate.parse(it) } ?: LocalDate.now()
    var currentMonth by remember { mutableStateOf(initialDate) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    val allSchedules by scheduleViewModel.scheduleItemsByUrgency.collectAsState()
    val selectedUrgency by scheduleViewModel.selectedUrgency.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ScheduleItem?>(null) }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(onClick = { navController.navigate("schedule_list") }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.all_schedules_title))
                    }
                    FloatingActionButton(onClick = { navController.navigate("add_edit_schedule/${selectedDate}") }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_schedule_title))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.previous_month))
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f) // Allow text to take available space
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(id = R.string.next_month))
                }
                TextButton(onClick = { 
                    currentMonth = LocalDate.now()
                    selectedDate = LocalDate.now()
                }) {
                    Text(stringResource(R.string.today))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of the week header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf(
                    stringResource(id = R.string.sunday_short),
                    stringResource(id = R.string.monday_short),
                    stringResource(id = R.string.tuesday_short),
                    stringResource(id = R.string.wednesday_short),
                    stringResource(id = R.string.thursday_short),
                    stringResource(id = R.string.friday_short),
                    stringResource(id = R.string.saturday_short)
                ).forEach {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            CalendarGrid(currentMonth, selectedDate) { date ->
                selectedDate = date
            }

            if (allSchedules.isNotEmpty()) {
                UrgencyProgressBar(
                    schedules = allSchedules,
                    onUrgencySelected = { urgency ->
                        navController.navigate("urgent_schedule_list/${urgency.name}")
                    }
                )
            }

            // Schedule List for selected date or urgency
            val schedulesToShow = if (selectedUrgency != null) {
                allSchedules.filter { it.urgency == selectedUrgency }
            } else {
                allSchedules.filter { it.date == selectedDate }.sortedBy { it.time }
            }

            DailyScheduleList(
                scheduleItems = schedulesToShow,
                onDeleteItem = { item -> 
                    itemToDelete = item
                    showDeleteDialog = true
                },
                onEditItem = { item -> navController.navigate("add_edit_schedule/${item.date}?scheduleId=${item.id}") }
            )
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
fun DailyScheduleList(scheduleItems: List<ScheduleItem>, onDeleteItem: (ScheduleItem) -> Unit, onEditItem: (ScheduleItem) -> Unit) {
    if (scheduleItems.isEmpty()) {
        Text(text = stringResource(R.string.no_schedule_for_this_day), modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(scheduleItems) {
                schedule ->
                Card(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
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
                                text = schedule.time.format(DateTimeFormatter.ofPattern("HH:mm")),
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
                            IconButton(onClick = { onEditItem(schedule) }) {
                                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_schedule_title))
                            }
                            IconButton(onClick = { onDeleteItem(schedule) }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_schedule))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
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

@Composable
fun CalendarGrid(month: LocalDate, selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val firstDayOfMonth = month.withDayOfMonth(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 1 for Monday, etc.

    Column {
        var dayCounter = 1
        for (week in 0 until 6) { // Max 6 weeks in a month view
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayOfWeek in 0 until 7) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        // Empty cells before the first day of the month
                        Box(modifier = Modifier.size(40.dp)) {}
                    } else if (dayCounter <= daysInMonth) {
                        val currentDate = firstDayOfMonth.withDayOfMonth(dayCounter)
                        DayCell(date = currentDate, isSelected = currentDate == selectedDate, onDateSelected = onDateSelected)
                        dayCounter++
                    } else {
                        // Empty cells after the last day of the month
                        Box(modifier = Modifier.size(40.dp)) {}
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(date: LocalDate, isSelected: Boolean, onDateSelected: (LocalDate) -> Unit) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        date == LocalDate.now() -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) // Lighter shade for today
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        date == LocalDate.now() -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onBackground
    }

    Surface(
        modifier = Modifier
            .size(40.dp)
            .padding(4.dp), // Add padding for spacing
        shape = CircleShape,
        color = backgroundColor,
        contentColor = textColor,
        onClick = { onDateSelected(date) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString()
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    QuickPlanTheme {
        // For preview, we can pass a dummy NavController and a dummy ViewModel
        CalendarScreen(navController = rememberNavController(), scheduleViewModel = ScheduleViewModel(object : com.example.quickplan.data.ScheduleDao {
            override fun getAllScheduleItems() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.quickplan.data.ScheduleItem>())
            override fun getAllScheduleItemsByUrgency() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.quickplan.data.ScheduleItem>())
            override suspend fun insertScheduleItem(item: com.example.quickplan.data.ScheduleItem) {}
            override suspend fun updateScheduleItem(item: com.example.quickplan.data.ScheduleItem) {}
            override suspend fun deleteScheduleItem(item: com.example.quickplan.data.ScheduleItem) {}
        }))
    }
}