package com.example.quickplan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickplan.ThemeState
import com.example.quickplan.ui.theme.Purple40

@Composable
fun ProfileScreen() {
    val isDarkMode by ThemeState.isDarkMode

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "我的",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )

                // 深色模式切换按钮
                val darkModeInteraction = remember { MutableInteractionSource() }
                val isDarkModePressed by darkModeInteraction.collectIsPressedAsState()
                IconButton(
                    onClick = { ThemeState.toggleDarkMode() },
                    interactionSource = darkModeInteraction
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "切换深色模式",
                        tint = if (isDarkModePressed) Purple40
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 圆形头像
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "头像",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 登录对话框状态
            var showLoginDialog by remember { mutableStateOf(false) }
            var showRegisterDialog by remember { mutableStateOf(false) }
            
            // 登录按钮
            val loginInteraction = remember { MutableInteractionSource() }
            val isLoginPressed by loginInteraction.collectIsPressedAsState()
            Button(
                onClick = { showLoginDialog = true },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoginPressed) Purple40
                    else MaterialTheme.colorScheme.primary
                ),
                interactionSource = loginInteraction
            ) {
                Text("登录", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // 注册账号文本按钮
            Text(
                text = "注册账号",
                modifier = Modifier
                    .clickable { showRegisterDialog = true }
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 登录方式选择对话框
            if (showLoginDialog) {
                AlertDialog(
                    onDismissRequest = { showLoginDialog = false },
                    title = { Text("选择登录方式") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LoginMethodButton(
                                text = "手机号登录",
                                icon = Icons.Default.Phone
                            ) { /* TODO: 实现手机号登录 */ }
                            
                            LoginMethodButton(
                                text = "微信登录",
                                icon = Icons.Default.Message
                            ) { /* TODO: 实现微信登录 */ }
                            
                            LoginMethodButton(
                                text = "QQ登录",
                                icon = Icons.Default.Chat
                            ) { /* TODO: 实现QQ登录 */ }
                            
                            LoginMethodButton(
                                text = "邮箱登录",
                                icon = Icons.Default.Email
                            ) { /* TODO: 实现邮箱登录 */ }
                        }
                    },
                    confirmButton = { },
                    dismissButton = {
                        TextButton(onClick = { showLoginDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }

            // 注册方式选择对话框
            if (showRegisterDialog) {
                AlertDialog(
                    onDismissRequest = { showRegisterDialog = false },
                    title = { Text("选择注册方式") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LoginMethodButton(
                                text = "手机号注册",
                                icon = Icons.Default.Phone
                            ) { /* TODO: 实现手机号注册 */ }
                            
                            LoginMethodButton(
                                text = "微信注册",
                                icon = Icons.Default.Message
                            ) { /* TODO: 实现微信注册 */ }
                            
                            LoginMethodButton(
                                text = "QQ注册",
                                icon = Icons.Default.Chat
                            ) { /* TODO: 实现QQ注册 */ }
                            
                            LoginMethodButton(
                                text = "邮箱注册",
                                icon = Icons.Default.Email
                            ) { /* TODO: 实现邮箱注册 */ }
                        }
                    },
                    confirmButton = { },
                    dismissButton = {
                        TextButton(onClick = { showRegisterDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }

            // 功能按钮列表
            ProfileButton(
                text = "历史日程",
                icon = Icons.Default.History
            ) {
                /* TODO: 导航到历史日程页面 */
            }

            ProfileButton(
                text = "忘记密码",
                icon = Icons.Default.Lock
            ) {
                /* TODO: 导航到找回密码页面 */
            }
             ProfileButton(
                text = "语言切换",
                icon = Icons.Default.Language
            ) {
                /* TODO: 弹出语言选项*/
            }
        }
    }
}

@Composable
private fun LoginMethodButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isPressed) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isPressed) 
                MaterialTheme.colorScheme.primary
            else 
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shadowElevation = if (isPressed) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标容器
            Surface(
                shape = CircleShape,
                color = if (isPressed)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = if (isPressed)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 文本
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                ),
                color = if (isPressed)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 箭头图标
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isPressed)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ProfileButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) Purple40.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPressed) Purple40
                else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPressed) Purple40
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

