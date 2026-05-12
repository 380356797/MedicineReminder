package com.example.medicinereminder.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicine_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("medicineId")],
)
data class MedicineSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val hour: Int,
    val minute: Int,
    val doseAmount: Int = 1,
    val doseUnit: String = "片",
    val repeatType: RepeatType = RepeatType.DAILY,
    val repeatDays: String = "",
    val isEnabled: Boolean = true,
    val alarmId: Int = 0,
)

enum class RepeatType(val label: String) {
    DAILY("每天"),
    EVERY_OTHER_DAY("隔天"),
    WEEKLY("每周"),
    CUSTOM("自定义"),
}
