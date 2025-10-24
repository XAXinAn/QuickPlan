package com.example.quickplan.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "首页")
    object AI : Screen("ai", "AI")
    object Profile : Screen("profile", "我的")
    object AddSchedule : Screen("addSchedule", "添加日程")
}
