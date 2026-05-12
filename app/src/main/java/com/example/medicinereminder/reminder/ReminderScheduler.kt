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
        when (schedule.repeatType) {
            RepeatType.DAILY -> scheduleAt(schedule, medicineName, nextDaily(schedule.hour, schedule.minute))
            RepeatType.EVERY_OTHER_DAY -> scheduleAt(schedule, medicineName, nextEveryOtherDay(schedule.hour, schedule.minute))
            RepeatType.WEEKLY, RepeatType.CUSTOM -> {
                for (dayOfWeek in schedule.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }) {
                    val requestCode = schedule.alarmId + dayOfWeek
                    val triggerTime = nextWeekly(schedule.hour, schedule.minute, dayOfWeek)
                    val intent = buildIntent(schedule, medicineName, requestCode)
                    val pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
                }
            }
        }
    }

    private fun scheduleAt(schedule: MedicineSchedule, medicineName: String, triggerTime: Long) {
        val intent = buildIntent(schedule, medicineName, schedule.alarmId)
        val pi = PendingIntent.getBroadcast(context, schedule.alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
    }

    /** Schedule the next alarm after one just fired. Called from ReminderReceiver. */
    fun reschedule(scheduleId: Long, medicineId: Long, medicineName: String, alarmId: Int,
                   doseAmount: Int, doseUnit: String, repeatType: String, repeatDays: String,
                   hour: Int, minute: Int) {
        val type = try { RepeatType.valueOf(repeatType) } catch (_: Exception) { RepeatType.DAILY }
        val triggerTime = when (type) {
            RepeatType.DAILY -> nextDaily(hour, minute)
            RepeatType.EVERY_OTHER_DAY -> nextEveryOtherDay(hour, minute)
            RepeatType.WEEKLY, RepeatType.CUSTOM -> nextWeeklyFromDays(hour, minute, repeatDays)
        }
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = REMINDER_ACTION
            putExtra("schedule_id", scheduleId)
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("alarm_id", alarmId)
            putExtra("dose_amount", doseAmount)
            putExtra("dose_unit", doseUnit)
            putExtra("repeat_type", repeatType)
            putExtra("repeat_days", repeatDays)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }
        val pi = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
    }

    private fun buildIntent(schedule: MedicineSchedule, medicineName: String, alarmId: Int): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            action = REMINDER_ACTION
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

    fun cancel(schedule: MedicineSchedule) {
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = REMINDER_ACTION }
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
            action = REMINDER_ACTION
            putExtra("schedule_id", scheduleId)
            putExtra("medicine_id", medicineId)
            putExtra("medicine_name", medicineName)
            putExtra("alarm_id", (scheduleId + 10000).toInt())
            putExtra("dose_amount", doseAmount)
            putExtra("dose_unit", doseUnit)
            // Snooze is one-shot, no repeat info needed
            putExtra("repeat_type", "")
            putExtra("repeat_days", "")
            putExtra("hour", 0)
            putExtra("minute", 0)
        }
        val pi = PendingIntent.getBroadcast(context, (scheduleId + 10000).toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + minutes * 60_000L, pi)
    }

    companion object {
        const val REMINDER_ACTION = "com.example.medicinereminder.REMINDER"

        private fun nextDaily(hour: Int, minute: Int): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis
        }

        private fun nextEveryOtherDay(hour: Int, minute: Int): Long {
            val epoch = Calendar.getInstance().apply {
                set(2024, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val now = Calendar.getInstance()
            val epochDay = epoch.timeInMillis / 86_400_000L
            val todayDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis / 86_400_000L

            var candidate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.before(now)) candidate.add(Calendar.DAY_OF_YEAR, 1)

            while (((candidate.timeInMillis / 86_400_000L - epochDay) % 2L) != 0L) {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            return candidate.timeInMillis
        }

        private fun nextWeekly(hour: Int, minute: Int, dayOfWeek: Int): Long {
            return Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                if (before(Calendar.getInstance())) add(Calendar.WEEK_OF_YEAR, 1)
            }.timeInMillis
        }

        private fun nextWeeklyFromDays(hour: Int, minute: Int, repeatDays: String): Long {
            val days = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (days.isEmpty()) return nextDaily(hour, minute)
            return days.map { nextWeekly(hour, minute, it) }.min()
        }
    }
}
