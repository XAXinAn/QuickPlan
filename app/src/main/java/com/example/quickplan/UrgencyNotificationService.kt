package com.example.quickplan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.quickplan.data.UrgencyProportions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.graphics.Color
import android.view.View
import com.example.quickplan.data.Urgency
import android.util.Log

class UrgencyNotificationService : Service() {

    private val NOTIFICATION_CHANNEL_ID = "urgency_channel"
    private val NOTIFICATION_ID = 1
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val application = application as QuickPlanApplication
        val scheduleViewModel = application.scheduleViewModel

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            scheduleViewModel.scheduleItems.collect {
                Log.d("UrgencyNotificationService", "Received scheduleItems update: ${it.size} items")
                showUrgencyNotification(it)
            }
        }

        return START_STICKY // Service will be re-created if killed by system
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "紧急程度通知"
            val descriptionText = "显示当前紧急程度的通知"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showUrgencyNotification(scheduleItems: List<com.example.quickplan.data.ScheduleItem>) {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_urgency_bar)

        val totalItems = scheduleItems.size
        val overdueCount = scheduleItems.count { it.urgency == Urgency.OVERDUE }
        val withinOneDayCount = scheduleItems.count { it.urgency == Urgency.WITHIN_ONE_DAY }
        val withinThreeDaysCount = scheduleItems.count { it.urgency == Urgency.WITHIN_THREE_DAYS }
        val withinOneWeekCount = scheduleItems.count { it.urgency == Urgency.WITHIN_ONE_WEEK }
        val withinOneMonthCount = scheduleItems.count { it.urgency == Urgency.WITHIN_ONE_MONTH }
        val beyondOneMonthCount = scheduleItems.count { it.urgency == Urgency.BEYOND_ONE_MONTH }

        val contentText = StringBuilder()
        if (overdueCount > 0) contentText.appendLine("已截止: $overdueCount")
        if (withinOneDayCount > 0) contentText.appendLine("一天内: $withinOneDayCount")
        if (withinThreeDaysCount > 0) contentText.appendLine("三天内: $withinThreeDaysCount")
        if (withinOneWeekCount > 0) contentText.appendLine("一周内: $withinOneWeekCount")
        if (withinOneMonthCount > 0) contentText.appendLine("一月内: $withinOneMonthCount")
        if (beyondOneMonthCount > 0) contentText.appendLine("一月后: $beyondOneMonthCount")

        notificationLayout.setTextViewText(R.id.notification_title, "日程统计")
        notificationLayout.setTextViewText(R.id.notification_content, contentText.toString().trim())

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("QuickPlan 日程统计")
            .setContentText(contentText.toString().trim())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, builder.build())
    }
}