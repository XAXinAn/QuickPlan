package com.example.quickplan.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quickplan.data.ScheduleRepository
import com.example.quickplan.model.Schedule
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// 自定义 Saver 用于保存和恢复 LocalDate
val localDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = { LocalDate.parse(it) }
)

// 自定义 Saver 用于保存和恢复 YearMonth
val yearMonthSaver = Saver<YearMonth, String>(
    save = { it.toString() },
    restore = { YearMonth.parse(it) }
)

@Composable
fun ScheduleUrgencyBar(schedules: List<Schedule>) {
    val now = LocalDateTime.now()

    val categories = listOf(
        "已截止" to Color(0xFFFF0000),
        "半天内" to Color(0xFFFF5722),
        "一天内" to Color(0xFFFF9800),
        "三天内" to Color(0xFFFFC107),
        "一周内" to Color(0xFFFFEB3B),
        "一个月内" to Color(0xFFBDBDBD),
        "一个月外" to Color(0xFF9E9E9E)
    )

    val counts = categories.associate { it.first to 0 }.toMutableMap()

    for (schedule in schedules) {
        val scheduleDateTime = try {
            LocalDateTime.of(
                LocalDate.parse(schedule.date),
                if (schedule.time.isNotBlank()) LocalTime.parse(schedule.time, DateTimeFormatter.ofPattern("HH:mm")) else LocalTime.MIDNIGHT
            )
        } catch (e: Exception) {
            continue // Skip schedules with invalid date/time format
        }

        when {
            scheduleDateTime.isBefore(now) -> counts["已截止"] = counts["已截止"]!! + 1
            scheduleDateTime.isBefore(now.plusHours(12)) -> counts["半天内"] = counts["半天内"]!! + 1
            scheduleDateTime.isBefore(now.plusDays(1)) -> counts["一天内"] = counts["一天内"]!! + 1
            scheduleDateTime.isBefore(now.plusDays(3)) -> counts["三天内"] = counts["三天内"]!! + 1
            scheduleDateTime.isBefore(now.plusDays(7)) -> counts["一周内"] = counts["一周内"]!! + 1
            scheduleDateTime.isBefore(now.plusMonths(1)) -> counts["一个月内"] = counts["一个月内"]!! + 1
            else -> counts["一个月外"] = counts["一个月外"]!! + 1
        }
    }

    val total = counts.values.sum().toFloat()

    if (total > 0) {
        Column {
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(20.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                categories.forEach { (category, color) ->
                    val count = counts[category]!!
                    if (count > 0) {
                        Box(modifier = Modifier
                            .fillMaxHeight()
                            .weight(count / total)
                            .background(color))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            val legendItems = categories.filter { (category, _) ->
                counts[category]?.let { it > 0 } == true
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                legendItems.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { (category, color) ->
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = category, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)
    val totalCells = daysInMonth + firstDayOfWeek

    Column {
        for (week in 0 until (totalCells + 6) / 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dayOfWeek in 0..6) {
                    val index = week * 7 + dayOfWeek
                    val day = index - firstDayOfWeek + 1
                    if (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day)
                        val isToday = date == LocalDate.now()
                        val isSelected = date == selectedDate

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isSelected -> Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(2.dp, Color(0xFF2979FF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = day.toString(), fontWeight = FontWeight.Bold)
                                }
                                isToday -> Text(
                                    text = day.toString(),
                                    color = Color(0xFF2979FF),
                                    fontWeight = FontWeight.Bold
                                )
                                else -> Text(text = day.toString())
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, initialDate: String?) {
    val context = LocalContext.current
    val repository = remember { ScheduleRepository(context) }
    val scope = rememberCoroutineScope()
    val schedules by repository.schedules.collectAsState(initial = emptyList())

    var selectedDate by rememberSaveable(stateSaver = localDateSaver) {
        mutableStateOf(LocalDate.now())
    }
    var currentMonth by rememberSaveable(stateSaver = yearMonthSaver) {
        mutableStateOf(YearMonth.from(LocalDate.now()))
    }

    LaunchedEffect(initialDate) {
        initialDate?.let { dateString ->
            val newDate = LocalDate.parse(dateString)
            selectedDate = newDate
            currentMonth = YearMonth.from(newDate)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("addSchedule/${selectedDate}")
                },
                containerColor = Color(0xFF2979FF),
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加日程", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.Filled.ArrowBack, contentDescription = "上个月") }
                    Text("${currentMonth.year}年${currentMonth.monthValue}月", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.Filled.ArrowForward, contentDescription = "下个月") }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weekDays.forEach { day ->
                        Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                CalendarGrid(currentMonth = currentMonth, selectedDate = selectedDate, onDateSelected = {
                    selectedDate = it
                    currentMonth = YearMonth.from(it)
                })
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ScheduleUrgencyBar(schedules = schedules)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 的日程", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2979FF))
                Spacer(modifier = Modifier.height(8.dp))
            }

            val todaySchedules = schedules.filter { it.date == selectedDate.toString() }
            if (todaySchedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(vertical = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无日程", color = Color.Gray)
                    }
                }
            } else {
                items(todaySchedules, key = { it.id }) { schedule ->
                    Surface(
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.medium,
                        color = Color(0xFFF5F7FF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clickable {
                                navController.navigate("editSchedule/${schedule.id}")
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(schedule.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                var showDialog by remember { mutableStateOf(false) }
                                IconButton(onClick = { showDialog = true }) { Icon(Icons.Filled.Delete, contentDescription = "删除日程", tint = Color.Red) }

                                if (showDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDialog = false },
                                        title = { Text("确认删除该日程？") },
                                        text = { Text("删除后将无法恢复。") },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                scope.launch { repository.deleteSchedule(schedule.id) }
                                                showDialog = false
                                            }) { Text("确认", color = Color.Red) }
                                        },
                                        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("取消") } }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFF2979FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(schedule.location.ifEmpty { "未填写地点" })
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color(0xFF2979FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(schedule.time.ifEmpty { "未填写时间" })
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}
