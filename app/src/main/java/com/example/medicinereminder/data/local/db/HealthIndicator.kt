package com.example.medicinereminder.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_indicators")
data class HealthIndicator(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unit: String = "",
    val category: String = "",
    val normalRange: String = "",
    val isPreset: Boolean = false,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
)
