package com.example.medicinereminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medicinereminder.data.local.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = ReminderScheduler(context)
            val database = AppDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                val schedules = database.medicineScheduleDao().getAllEnabled()
                for (schedule in schedules) {
                    val medicine = database.medicineDao().getById(schedule.medicineId)
                    if (medicine != null) {
                        scheduler.schedule(schedule, medicine.name)
                    }
                }
            }
        }
    }
}
