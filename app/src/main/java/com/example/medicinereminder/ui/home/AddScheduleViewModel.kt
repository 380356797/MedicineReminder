package com.example.medicinereminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.Medicine
import com.example.medicinereminder.data.local.db.MedicineSchedule
import com.example.medicinereminder.data.repository.MedicineRepository
import com.example.medicinereminder.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddScheduleViewModel(
    private val repository: MedicineRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val allMedicines: StateFlow<List<Medicine>> = repository.getAllEnabledMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getMedicineById(id: Long): Medicine? = repository.getMedicineById(id)

    fun addSchedule(schedule: MedicineSchedule) {
        viewModelScope.launch {
            val id = repository.insertSchedule(schedule)
            val medicine = repository.getMedicineById(schedule.medicineId)
            if (medicine != null) {
                reminderScheduler.schedule(schedule.copy(id = id), medicine.name)
            }
        }
    }
}
