// 걸음 수 기반 피트니스 경쟁 앱
package com.example.powerwalking.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.powerwalking.R

object NotificationUtil {
    const val CHANNEL_ID = "step_sensor_channel"
    const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "걸음 수 측정",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "백그라운드에서 걸음 수를 측정합니다"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(context: Context, steps: Int): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("파워킹")
            .setContentText("오늘 걸음 수: $steps 걸음")
            .setSmallIcon(R.drawable.cat_foot) // 알림 아이콘도 고양이 발바닥으로 변경 시도 (가능하다면)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
