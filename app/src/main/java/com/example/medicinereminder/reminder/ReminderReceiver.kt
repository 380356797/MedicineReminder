package com.example.medicinereminder.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.medicinereminder.MainActivity
import com.example.medicinereminder.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicine_name") ?: return
        val alarmId = intent.getIntExtra("alarm_id", 0)
        val scheduleId = intent.getLongExtra("schedule_id", 0L)
        val medicineId = intent.getLongExtra("medicine_id", 0L)
        val doseAmount = intent.getIntExtra("dose_amount", 1)
        val doseUnit = intent.getStringExtra("dose_unit") ?: "片"

        // Launch full-screen alarm activity
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medicine_name", medicineName)
            putExtra("schedule_id", scheduleId)
            putExtra("medicine_id", medicineId)
            putExtra("alarm_id", alarmId)
            putExtra("dose_amount", doseAmount)
            putExtra("dose_unit", doseUnit)
        }
        context.startActivity(alarmIntent)

        // Also show a notification as fallback
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(notificationManager)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, alarmId, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("吃药提醒")
            .setContentText("该吃 $medicineName 了")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alarmId, notification)
    }

    companion object {
        const val CHANNEL_ID = "medicine_reminder_channel"

        fun createChannel(notificationManager: NotificationManager) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "吃药提醒",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "吃药时间提醒通知"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
