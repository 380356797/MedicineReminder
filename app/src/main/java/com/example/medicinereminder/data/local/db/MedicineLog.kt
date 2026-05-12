package com.example.medicinereminder.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicine_logs",
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
data class MedicineLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val scheduledTime: Long,
    val takenTime: Long? = null,
    val status: LogStatus = LogStatus.PENDING,
)

enum class LogStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    SNOOZED,
}
