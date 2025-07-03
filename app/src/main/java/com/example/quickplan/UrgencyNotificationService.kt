package com.example.quickplan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.compose.ui.graphics.toArgb
import com.example.quickplan.data.ScheduleItem
import com.example.quickplan.data.Urgency
import com.example.quickplan.data.getUrgencyColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UrgencyNotificationService : Service() {

    private val NOTIFICATION_CHANNEL_ID = "urgency_channel"
    private val NOTIFICATION_ID = 1
    private var job: Job? = null

    companion object {
        const val EXTRA_URGENCY = "extra_urgency"
    }

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

    private fun showUrgencyNotification(scheduleItems: List<ScheduleItem>) {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_urgency_bar)

        val totalItems = scheduleItems.size
        if (totalItems == 0) {
            notificationLayout.setViewVisibility(R.id.notification_urgency_bar, View.GONE)
            notificationLayout.setViewVisibility(R.id.notification_details_container, View.GONE)
            notificationLayout.setTextViewText(R.id.notification_title, "暂无日程")
        } else {
            notificationLayout.setViewVisibility(R.id.notification_urgency_bar, View.VISIBLE)
            notificationLayout.setViewVisibility(R.id.notification_details_container, View.VISIBLE)
            notificationLayout.setTextViewText(R.id.notification_title, "我的日程")

            val urgencyCounts = scheduleItems.groupingBy { it.urgency }.eachCount()

            // Define dimensions for the bitmap
            val barWidth = 1000 // pixels
            val barHeight = 100 // pixels
            val cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
            val gap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()

            val bitmap = Bitmap.createBitmap(barWidth, barHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            val totalGaps = urgencyCounts.filter { it.value > 0 }.size - 1
            val availableWidth = barWidth - (totalGaps * gap)

            var currentX = 0f
            val drawOrder = listOf(Urgency.OVERDUE, Urgency.WITHIN_ONE_DAY, Urgency.WITHIN_THREE_DAYS, Urgency.WITHIN_ONE_WEEK, Urgency.WITHIN_ONE_MONTH, Urgency.BEYOND_ONE_MONTH)
            for (urgency in drawOrder) {
                val count = urgencyCounts[urgency] ?: 0
                if (count > 0) {
                    val segmentWidth = (count.toFloat() / totalItems.toFloat()) * availableWidth
                    paint.color = getUrgencyColor(urgency).toArgb()
                    canvas.drawRoundRect(currentX, 0f, currentX + segmentWidth, barHeight.toFloat(), cornerRadius, cornerRadius, paint)
                    currentX += segmentWidth + gap
                }
            }
            notificationLayout.setImageViewBitmap(R.id.notification_urgency_bar, bitmap)

            // Clear previous detail rows
            notificationLayout.removeAllViews(R.id.notification_details_container)

            // Add detail rows
            for (urgency in drawOrder) {
                val count = urgencyCounts[urgency] ?: 0
                if (count > 0) {
                    val urgencyString = when (urgency) {
                        Urgency.OVERDUE -> getString(R.string.urgency_overdue)
                        Urgency.WITHIN_ONE_DAY -> getString(R.string.urgency_within_one_day)
                        Urgency.WITHIN_THREE_DAYS -> getString(R.string.urgency_within_three_days)
                        Urgency.WITHIN_ONE_WEEK -> getString(R.string.urgency_within_one_week)
                        Urgency.WITHIN_ONE_MONTH -> getString(R.string.urgency_within_one_month)
                        Urgency.BEYOND_ONE_MONTH -> getString(R.string.urgency_beyond_one_month)
                    }

                    val notificationDetailRow = RemoteViews(packageName, R.layout.notification_urgency_detail_row)
                    notificationDetailRow.setTextViewText(R.id.detail_text, "${urgencyString}: ${count} 项")

                    val detailIntent = Intent(this, MainActivity::class.java).apply {
                        putExtra(EXTRA_URGENCY, urgency.name)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        urgency.ordinal, // Use ordinal as a unique request code
                        detailIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    notificationDetailRow.setOnClickPendingIntent(R.id.detail_button, pendingIntent)

                    notificationLayout.addView(R.id.notification_details_container, notificationDetailRow)
                }
            }
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)

        startForeground(NOTIFICATION_ID, builder.build())
    }
}