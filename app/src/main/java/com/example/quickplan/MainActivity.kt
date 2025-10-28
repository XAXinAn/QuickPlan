package com.example.quickplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.quickplan.components.BottomNavBar
import com.example.quickplan.navigation.NavGraph
import com.example.quickplan.ui.theme.QuickPlanTheme

// 全局深色模式状态
object ThemeState {
    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> = _isDarkMode

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
}

// 简单认证状态：用于演示登录/登出
object AuthState {
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userName = mutableStateOf<String?>(null)
    val userName: State<String?> = _userName

    fun login(user: String?) {
        _isLoggedIn.value = true
        _userName.value = user
    }

    fun logout() {
        _isLoggedIn.value = false
        _userName.value = null
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val isDarkMode by ThemeState.isDarkMode
            
            QuickPlanTheme(darkTheme = isDarkMode) {
                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->   // ✅ 这里命名为 innerPadding
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding) // ✅ 名字一致
                    )
                }
            }
        }
    }
}
