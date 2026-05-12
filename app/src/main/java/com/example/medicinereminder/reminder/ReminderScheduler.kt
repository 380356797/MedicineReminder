package com.example.medicinereminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medicinereminder.data.local.db.MedicineSchedule
import com.example.medicinereminder.data.local.db.RepeatType
import java.util.Calendar

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(schedule: MedicineSchedule, medicineName: String) {
        when (schedule.repeatType) {
            RepeatType.DAILY -> scheduleDaily(schedule, medicineName, intervalMs = AlarmManager.INTERVAL_DAY)
            RepeatType.EVERY_OTHER_DAY -> scheduleDaily(schedule, medicineName, intervalMs = AlarmManager.INTERVAL_DAY * 2)
            RepeatType.WEEKLY, RepeatType.CUSTOM -> scheduleWeekly(schedule, medicineName)
        }
    }

    private fun scheduleDaily(schedule: MedicineSchedule, medicineName: String, intervalMs: Long) {
        val intent = buildIntent(schedule, medicineName, schedule.alarmId)
        val pendingIntent = buildPendingIntent(schedule.alarmId, intent)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intervalMs, pendingIntent)
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intervalMs, pendingIntent)
        }
    }

    private fun scheduleWeekly(schedule: MedicineSchedule, medicineName: String) {
        val days = schedule.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        for (dayOfWeek in days) {
            val dayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.hour)
                set(Calendar.MINUTE, schedule.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                if (before(Calendar.getInstance())) add(Calendar.WEEK_OF_YEAR, 1)
            }
            val requestCode = schedule.alarmId + dayOfWeek
            val intent = buildIntent(schedule, medicineName, requestCode)
            val pendingIntent = buildPendingIntent(requestCode, intent)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dayCalendar.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
        }
    }

    private fun buildIntent(schedule: MedicineSchedule, medicineName: String, alarmId: Int): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.medicinereminder.REMINDER"
            putExtra("schedule_id", schedule.id)
            putExtra("medicine_id", schedule.medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("alarm_id", alarmId)
            putExtra("dose_amount", schedule.doseAmount)
            putExtra("dose_unit", schedule.doseUnit)
            putExtra("repeat_type", schedule.repeatType.name)
            putExtra("repeat_days", schedule.repeatDays)
            putExtra("hour", schedule.hour)
            putExtra("minute", schedule.minute)
        }
    }

    private fun buildPendingIntent(requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun cancel(schedule: MedicineSchedule) {
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = "com.example.medicinereminder.REMINDER" }
        if (schedule.repeatType == RepeatType.DAILY || schedule.repeatType == RepeatType.EVERY_OTHER_DAY) {
            alarmManager.cancel(PendingIntent.getBroadcast(context, schedule.alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        } else {
            for (dayOfWeek in 1..7) {
                alarmManager.cancel(PendingIntent.getBroadcast(context, schedule.alarmId + dayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            }
        }
    }

    fun snooze(scheduleId: Long, medicineId: Long, medicineName: String, doseAmount: Int, doseUnit: String, minutes: Int = 15) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.medicinereminder.REMINDER"
            putExtra("schedule_id", scheduleId)
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("alarm_id", (scheduleId + 10000).toInt())
            putExtra("dose_amount", doseAmount)
            putExtra("dose_unit", doseUnit)
        }
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + minutes * 60 * 1000,
            PendingIntent.getBroadcast(context, (scheduleId + 10000).toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),
        )
    }
}
