package com.example.quickplan.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quickplan.data.ScheduleRepository
import com.example.quickplan.model.Schedule
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(navController: NavController, scheduleId: Long) {
    val context = LocalContext.current
    val repository = remember { ScheduleRepository(context) }
    val scope = rememberCoroutineScope()

    val schedules by repository.schedules.collectAsState(initial = emptyList())
    val editingSchedule = schedules.find { it.id == scheduleId }

    if (editingSchedule == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到要编辑的日程")
        }
        return
    }

    var title by remember { mutableStateOf(editingSchedule.title) }
    var location by remember { mutableStateOf(editingSchedule.location) }
    var selectedDate by remember { mutableStateOf(LocalDate.parse(editingSchedule.date)) }
    var selectedTime by remember { mutableStateOf(LocalTime.parse(editingSchedule.time)) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("编辑日程", fontWeight = FontWeight.Bold) })
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // 标题
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 地点
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("地点") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 日期和时间按钮
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    // 日期按钮
                    Button(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _: android.widget.DatePicker, year: Int, month: Int, day: Int ->
                                    selectedDate = LocalDate.of(year, month + 1, day)
                                },
                                selectedDate.year,
                                selectedDate.monthValue - 1,
                                selectedDate.dayOfMonth
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("日期: ${selectedDate.format(dateFormatter)}", color = Color.Black)
                    }

                    // 时间按钮
                    Button(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _: TimePicker, hour: Int, minute: Int ->
                                    selectedTime = LocalTime.of(hour, minute)
                                },
                                selectedTime.hour,
                                selectedTime.minute,
                                true
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("时间: ${selectedTime.format(timeFormatter)}", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 保存修改按钮
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val updated = Schedule(
                                id = editingSchedule.id,
                                title = title,
                                location = location,
                                date = selectedDate.toString(),
                                time = selectedTime.format(timeFormatter)
                            )
                            scope.launch {
                                repository.updateSchedule(updated)
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("保存修改", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
