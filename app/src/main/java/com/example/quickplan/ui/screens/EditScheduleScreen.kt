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

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(0, 1)) }

    LaunchedEffect(editingSchedule) {
        editingSchedule?.let {
            title = it.title
            location = it.location
            selectedDate = LocalDate.parse(it.date)
            selectedTime = LocalTime.parse(it.time)
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (editingSchedule != null) "编辑日程" else "新建日程", fontWeight = FontWeight.Bold) })
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
                // Title, Location, Date, Time fields...

                //... (rest of the UI is the same)
                 OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("地点") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

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
                        Text("日期: ${selectedDate.format(dateFormatter)}", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
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


                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            scope.launch {
                                val scheduleToSave = editingSchedule?.copy(
                                    title = title,
                                    location = location,
                                    date = selectedDate.toString(),
                                    time = selectedTime.format(timeFormatter)
                                ) ?: Schedule(
                                    title = title,
                                    location = location,
                                    date = selectedDate.toString(),
                                    time = selectedTime.format(timeFormatter)
                                )

                                if (editingSchedule != null) {
                                    repository.updateSchedule(scheduleToSave)
                                } else {
                                    repository.addSchedule(scheduleToSave)
                                }

                                navController.previousBackStackEntry?.savedStateHandle?.set("selectedDate", selectedDate.toString())
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
                    Text(if (editingSchedule != null) "保存修改" else "保存", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
