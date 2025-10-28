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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    navController: NavController,
    defaultDate: String? // ← 从导航参数接收日期字符串
) {
    val context = LocalContext.current
    val repository = remember { ScheduleRepository(context) }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // ✅ 默认日期为传入的 defaultDate，否则为今天
    var selectedDate by remember {
        mutableStateOf(defaultDate?.let { LocalDate.parse(it) } ?: LocalDate.now())
    }

    // ✅ 默认时间为 00:01
    var selectedTime by remember { mutableStateOf(LocalTime.of(0, 1)) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建日程", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
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

            // 日期 + 时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 日期按钮
                Button(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
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
                    Text(text = "日期: ${selectedDate.format(dateFormatter)}", color = Color.Black)
                }

                // 时间按钮
                Button(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _: TimePicker, hour, minute ->
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
                    Text(text = "时间: ${selectedTime.format(timeFormatter)}", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            repository.addSchedule(
                                Schedule(
                                    title = title,
                                    location = location,
                                    date = selectedDate.toString(),
                                    time = selectedTime.format(timeFormatter)
                                )
                            )
                            navController.navigate("home?date=${selectedDate}") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("保存", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
