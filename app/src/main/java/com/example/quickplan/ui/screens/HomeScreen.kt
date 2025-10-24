package com.example.quickplan.ui.screens

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Arrangement

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
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { ScheduleRepository(context) }
    val scope = rememberCoroutineScope()
    val schedules by repository.schedules.collectAsState(initial = emptyList())

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addSchedule") },
                containerColor = Color(0xFF2979FF),
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加日程", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 月份选择
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

            // 星期标题
            val weekDays = listOf("一","二","三","四","五","六","日")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekDays.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 日历
            CalendarGrid(currentMonth = currentMonth, selectedDate = selectedDate, onDateSelected = { selectedDate = it })

            Spacer(modifier = Modifier.height(16.dp))

            // 当天日程
            val todaySchedules = schedules.filter { it.date == selectedDate.toString() }
            Text("${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 的日程", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2979FF))
            Spacer(modifier = Modifier.height(8.dp))

            if (todaySchedules.isEmpty()) {
                Text("暂无日程", color = Color.Gray)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    todaySchedules.forEach { schedule ->
                        Surface(
                            tonalElevation = 2.dp,
                            shape = MaterialTheme.shapes.medium,
                            color = Color(0xFFF5F7FF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // 安全传递 id
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
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFF2979FF), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(schedule.location.ifEmpty { "未填写地点" })
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color(0xFF2979FF), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(schedule.time.ifEmpty { "未填写时间" })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
