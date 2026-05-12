package com.example.medicinereminder.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_records",
    foreignKeys = [
        ForeignKey(
            entity = HealthIndicator::class,
            parentColumns = ["id"],
            childColumns = ["indicatorId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("indicatorId")],
)
data class HealthRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val indicatorId: Long,
    val value: String,
    val notes: String = "",
    val photoPath: String? = null,
    val recordedAt: Long = System.currentTimeMillis(),
)
