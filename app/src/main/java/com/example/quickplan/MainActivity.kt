package com.example.quickplan

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.room.Room
import com.example.quickplan.data.AppDatabase
import com.example.quickplan.data.ScheduleDao
import com.example.quickplan.ui.calendar.CalendarScreen
import com.example.quickplan.ui.schedule.AddEditScheduleScreen
import com.example.quickplan.ui.schedule.ScheduleListScreen
import com.example.quickplan.ui.schedule.ScheduleViewModel
import com.example.quickplan.ui.schedule.ScheduleListViewModelFactory
import com.example.quickplan.ui.schedule.UrgentScheduleListScreen
import com.example.quickplan.ui.schedule.ScheduleViewModelFactory
import com.example.quickplan.ui.theme.QuickPlanTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var scheduleDao: ScheduleDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "schedule-db"
        ).build()
        scheduleDao = database.scheduleDao()

        setContent {
            QuickPlanTheme {
                val navController = rememberNavController()
                val scheduleViewModel: ScheduleViewModel = viewModel(factory = ScheduleViewModelFactory(scheduleDao))

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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