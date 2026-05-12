package com.example.medicinereminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.Medicine
import com.example.medicinereminder.data.local.db.MedicineSchedule
import com.example.medicinereminder.data.repository.MedicineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditMedicineViewModel(private val repository: MedicineRepository) : ViewModel() {
    private val _medicineId = MutableStateFlow(0L)
    val medicine: StateFlow<Medicine?> = _medicineId.flatMapLatest { id ->
        flow { emit(repository.getMedicineById(id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val schedules: StateFlow<List<MedicineSchedule>> = _medicineId.flatMapLatest { id ->
        repository.getSchedules(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadMedicine(id: Long) { _medicineId.value = id }
    fun updateMedicine(medicine: Medicine) {
        viewModelScope.launch { repository.updateMedicine(medicine) }
    }
    fun deleteMedicine() {
        viewModelScope.launch {
            repository.getMedicineById(_medicineId.value)?.let { repository.deleteMedicine(it) }
        }
    }
}
