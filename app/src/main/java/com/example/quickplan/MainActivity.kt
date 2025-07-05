package com.example.quickplan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.quickplan.data.ScheduleDao
import com.example.quickplan.ui.calendar.CalendarScreen
import com.example.quickplan.ui.schedule.AddEditScheduleScreen
import com.example.quickplan.ui.schedule.ScheduleListScreen
import com.example.quickplan.ui.schedule.ScheduleViewModel
import com.example.quickplan.ui.schedule.UrgentScheduleListScreen
import com.example.quickplan.ui.theme.QuickPlanTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.google.gson.Gson // Import Gson

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start the foreground service
        val serviceIntent = Intent(this, UrgencyNotificationService::class.java)
        androidx.core.content.ContextCompat.startForegroundService(this, serviceIntent)

        setContent {
            QuickPlanTheme {
                val navController = rememberNavController()
                val application = application as QuickPlanApplication
                val scheduleViewModel: ScheduleViewModel = application.scheduleViewModel
                val uploadImageViewModel: UploadImageViewModel = application.uploadImageViewModel

                var showDialog by remember { mutableStateOf(false) }
                var dialogContent by remember { mutableStateOf("") }

                // Handle intent from notification
                LaunchedEffect(intent) {
                    val urgency = intent.getStringExtra(UrgencyNotificationService.EXTRA_URGENCY)
                    if (urgency != null) {
                        navController.navigate("urgent_schedule_list/${urgency}") {
                            // Clear back stack to prevent navigating back to calendar
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                }

                // Observe modelResponse and show dialog
                LaunchedEffect(uploadImageViewModel.modelResponse.collectAsState().value) {
                    uploadImageViewModel.modelResponse.collect { response ->
                        response?.let {
                            val gson = Gson()
                            dialogContent = gson.toJson(it)
                            showDialog = true
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val modelResponse by uploadImageViewModel.modelResponse.collectAsState()
                        val isLoading by uploadImageViewModel.isLoading.collectAsState()
                        val error by uploadImageViewModel.error.collectAsState()

                        val pickImageLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            uri?.let {
                                uploadImageViewModel.uploadImage(it)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { pickImageLauncher.launch("image/*") },
                                enabled = !isLoading
                            ) {
                                Text(if (isLoading) "上传中..." else "上传图片获取截止日期")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            modelResponse?.let { response ->
                                Text("截止日期: ${response.deadlineDate ?: "N/A"}")
                                Text("截止时间: ${response.deadlineTime ?: "N/A"}")
                                Text("通知内容: ${response.notificationContent ?: "N/A"}")
                            }

                            error?.let { errorMessage ->
                                Text("错误: $errorMessage", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController = navController, startDestination = "calendar", modifier = Modifier.padding(innerPadding)) {
                        composable("calendar?date={date}",
                            arguments = listOf(navArgument("date") { type = NavType.StringType; nullable = true })
                        ) {
                            val dateString = it.arguments?.getString("date")
                            CalendarScreen(navController = navController, scheduleViewModel = scheduleViewModel, initialDateString = dateString)
                        }
                        composable(
                            "add_edit_schedule/{date}?scheduleId={scheduleId}",
                            arguments = listOf(
                                navArgument("date") { type = NavType.StringType },
                                navArgument("scheduleId") { type = NavType.StringType; nullable = true }
                            ),
                            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(700)) },
                            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(700)) }
                        ) {
                            val dateString = it.arguments?.getString("date")
                            val scheduleId = it.arguments?.getString("scheduleId")
                            AddEditScheduleScreen(navController = navController, dateString = dateString, scheduleId = scheduleId, scheduleViewModel = scheduleViewModel)
                        }
                        composable("schedule_list",
                            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(700)) },
                            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(700)) }
                        ) {
                            ScheduleListScreen(navController = navController, scheduleViewModel = scheduleViewModel)
                        }
                        composable("urgent_schedule_list/{urgency}",
                            arguments = listOf(navArgument("urgency") { type = NavType.StringType }),
                            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(700)) },
                            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(700)) }
                        ) {
                            val urgencyString = it.arguments?.getString("urgency")
                            UrgentScheduleListScreen(navController = navController, scheduleViewModel = scheduleViewModel, urgencyString = urgencyString)
                        }
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("大模型响应") },
                        text = { Text(dialogContent) },
                        confirmButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("确定")
                            }
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    QuickPlanTheme {
        // For preview, we can pass a dummy NavController and a dummy ViewModel
        CalendarScreen(navController = rememberNavController(), scheduleViewModel = ScheduleViewModel(object : ScheduleDao {
            override fun getAllScheduleItems() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.quickplan.data.ScheduleItem>())
            override fun getAllScheduleItemsByUrgency() = kotlinx.coroutines.flow.flowOf(emptyList<com.example.quickplan.data.ScheduleItem>())
            override suspend fun insertScheduleItem(item: com.example.quickplan.data.ScheduleItem) {}
            override suspend fun updateScheduleItem(item: com.example.quickplan.data.ScheduleItem) {}
            override suspend fun deleteScheduleItem(item: com.example.quickplan.data.ScheduleItem) {}
        }))
    }
}