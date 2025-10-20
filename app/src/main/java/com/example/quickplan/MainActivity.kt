package com.example.quickplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.quickplan.components.BottomNavBar
import com.example.quickplan.navigation.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
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
