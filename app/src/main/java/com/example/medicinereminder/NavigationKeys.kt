package com.example.medicinereminder

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Bottom nav tabs
@Serializable data object HomeTab : NavKey
@Serializable data object HealthTab : NavKey
@Serializable data object ProfileTab : NavKey

// Detail screens
@Serializable data object AddMedicine : NavKey
@Serializable data class EditMedicine(val medicineId: Long) : NavKey
@Serializable data class MedicineSchedules(val medicineId: Long) : NavKey
@Serializable data class AddSchedule(val medicineId: Long) : NavKey
@Serializable data class HealthIndicatorDetail(val indicatorId: Long) : NavKey
@Serializable data class AddHealthRecord(val indicatorId: Long) : NavKey
@Serializable data object MedicineList : NavKey
@Serializable data object HealthIndicatorList : NavKey
