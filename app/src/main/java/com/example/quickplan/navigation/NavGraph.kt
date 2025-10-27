package com.example.quickplan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quickplan.ui.screens.HomeScreen
import com.example.quickplan.ui.screens.AIScreen
import com.example.quickplan.ui.screens.ProfileScreen
import com.example.quickplan.ui.screens.login.*
import com.example.quickplan.ui.screens.register.*

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.AI.route) { AIScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }

        // 登录路由
        composable(Screen.PhoneLogin.route) { PhoneLoginScreen(navController) }
        composable(Screen.WeChatLogin.route) { WeChatLoginScreen(navController) }
        composable(Screen.QQLogin.route) { QQLoginScreen(navController) }
        composable(Screen.EmailLogin.route) { EmailLoginScreen(navController) }

        // 注册路由
        composable(Screen.PhoneRegister.route) { PhoneRegisterScreen(navController) }
        composable(Screen.WeChatRegister.route) { WeChatRegisterScreen(navController) }
        composable(Screen.QQRegister.route) { QQRegisterScreen(navController) }
        composable(Screen.EmailRegister.route) { EmailRegisterScreen(navController) }
    }
}
