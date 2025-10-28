package com.example.quickplan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quickplan.ui.screens.AIScreen
import com.example.quickplan.ui.screens.HomeScreen
import com.example.quickplan.ui.screens.AddScheduleScreen
import com.example.quickplan.ui.screens.EditScheduleScreen
import com.example.quickplan.ui.screens.ProfileScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text


@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable(
            route = "home?date={date}",
            arguments = listOf(navArgument("date") { nullable = true })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            HomeScreen(navController, date)
        }
        composable("addSchedule/{date}") { backStackEntry ->
            val dateArg = backStackEntry.arguments?.getString("date")
            AddScheduleScreen(navController, dateArg)
        }
        composable(Screen.AI.route) { AIScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
        composable("editSchedule/{id}") { backStackEntry ->
            val idString = backStackEntry.arguments?.getString("id")
            val id = idString?.toLongOrNull()
            if (id != null) {
                EditScheduleScreen(navController = navController, scheduleId = id)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("未找到要编辑的日程")
                }
            }
        }
    }
}
