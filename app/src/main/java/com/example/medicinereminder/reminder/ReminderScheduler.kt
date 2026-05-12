package com.example.medicinereminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.medicinereminder.data.local.db.MedicineSchedule
import com.example.medicinereminder.data.local.db.RepeatType
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(schedule: MedicineSchedule, medicineName: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.medicinereminder.REMINDER"
            putExtra("schedule_id", schedule.id)
            putExtra("medicine_id", schedule.medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("alarm_id", schedule.alarmId)
            putExtra("dose_amount", schedule.doseAmount)
            putExtra("dose_unit", schedule.doseUnit)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, schedule.alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        when (schedule.repeatType) {
            RepeatType.DAILY, RepeatType.EVERY_OTHER_DAY -> {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
            }
            RepeatType.WEEKLY, RepeatType.CUSTOM -> {
                val days = schedule.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
                for (dayOfWeek in days) {
                    val dayCalendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, schedule.hour); set(Calendar.MINUTE, schedule.minute)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        set(Calendar.DAY_OF_WEEK, dayOfWeek)
                        if (before(Calendar.getInstance())) add(Calendar.WEEK_OF_YEAR, 1)
                    }
                    val dayIntent = Intent(context, ReminderReceiver::class.java).apply {
                        action = "com.example.medicinereminder.REMINDER"
                        putExtra("schedule_id", schedule.id); putExtra("medicine_id", schedule.medicineId)
                        putExtra("medicine_name", medicineName); putExtra("alarm_id", schedule.alarmId + dayOfWeek)
                        putExtra("dose_amount", schedule.doseAmount); putExtra("dose_unit", schedule.doseUnit)
                    }
                    val dayPendingIntent = PendingIntent.getBroadcast(
                        context, schedule.alarmId + dayOfWeek, dayIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dayCalendar.timeInMillis, AlarmManager.INTERVAL_DAY * 7, dayPendingIntent)
                }
                return
            }
        }
    }

    fun cancel(schedule: MedicineSchedule) {
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = "com.example.medicinereminder.REMINDER" }
        if (schedule.repeatType == RepeatType.DAILY) {
            alarmManager.cancel(PendingIntent.getBroadcast(context, schedule.alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        } else {
            for (dayOfWeek in 1..7) {
                alarmManager.cancel(PendingIntent.getBroadcast(context, schedule.alarmId + dayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            }
        }
    }

    fun snooze(scheduleId: Long, medicineId: Long, medicineName: String, minutes: Int = 15) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.medicinereminder.REMINDER"
            putExtra("schedule_id", scheduleId); putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName); putExtra("alarm_id", (scheduleId + 10000).toInt())
            putExtra("dose_amount", 1); putExtra("dose_unit", "片")
        }
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + minutes * 60 * 1000,
            PendingIntent.getBroadcast(context, (scheduleId + 10000).toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
        )
    }
}
