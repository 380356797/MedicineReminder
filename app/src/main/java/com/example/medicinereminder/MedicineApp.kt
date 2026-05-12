package com.example.medicinereminder

import android.app.Application
import com.example.medicinereminder.data.local.db.AppDatabase
import com.example.medicinereminder.data.repository.HealthRepository
import com.example.medicinereminder.data.repository.MedicineRepository
import com.example.medicinereminder.reminder.ReminderReceiver
import com.example.medicinereminder.reminder.ReminderScheduler

class MedicineApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val medicineRepository by lazy {
        MedicineRepository(database.medicineDao(), database.medicineScheduleDao(), database.medicineLogDao())
    }
    val healthRepository by lazy {
        HealthRepository(database.healthIndicatorDao(), database.healthRecordDao())
    }
    val reminderScheduler by lazy { ReminderScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        // Create notification channel early so it's ready before any alarm fires
        val nm = getSystemService(android.app.NotificationManager::class.java)
        ReminderReceiver.createChannel(nm)
    }
}
