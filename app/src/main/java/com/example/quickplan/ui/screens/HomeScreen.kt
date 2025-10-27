package com.example.quickplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.TextStyle
import java.util.*

@Composable
fun HomeScreen() {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    val startMonth = remember { YearMonth.now().minusMonths(12) }
    val endMonth = remember { YearMonth.now().plusMonths(12) }
    val firstDayOfWeek = remember { DayOfWeek.MONDAY }

    val coroutineScope = rememberCoroutineScope()

    // 日历状态
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = YearMonth.now(),
        firstDayOfWeek = firstDayOfWeek
    )

    // 页面加载后自动滚动到当前月份
    LaunchedEffect(Unit) {
        calendarState.scrollToMonth(YearMonth.now())
    }

    val currentMonth = calendarState.firstVisibleMonth.yearMonth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 顶部月份切换栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val previous = currentMonth.minusMonths(1)
                        calendarState.animateScrollToMonth(previous)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text("上一月")
            }

            Text(
                text = "${currentMonth.year}年 ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.CHINA)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val next = currentMonth.plusMonths(1)
                        calendarState.animateScrollToMonth(next)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text("下一月")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ 周标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")

            weekLabels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = if (index >= 5) // 六、日变主色
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 日历控件
        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                DayCell(
                    day = day,
                    isSelected = day.date == selectedDate,
                    isToday = day.date == today,
                    onClick = { selectedDate = day.date }
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "已选择日期：${selectedDate.year}年${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// ✅ 单个日期格子（带今日圆点）
@Composable
fun DayCell(day: CalendarDay, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        day.position != DayPosition.MonthDate -> Color.Transparent
        else -> Color(0xFFF3F3F3)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 16.sp,
                color = when {
                    isSelected -> Color.White
                    day.position != DayPosition.MonthDate -> Color.Gray
                    else -> Color.Black
                }
            )

            // ✅ 今天的小圆点标记
            if (isToday) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
