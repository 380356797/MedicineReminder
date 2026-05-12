package com.example.medicinereminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.Medicine
import com.example.medicinereminder.data.repository.MedicineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddMedicineViewModel(private val repository: MedicineRepository) : ViewModel() {
    val allMedicines: StateFlow<List<Medicine>> = repository.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getAllMedicines()
        .map { list -> list.map { it.category }.distinct().filter { it.isNotEmpty() }.sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedicine(name: String, dosage: String) {
        viewModelScope.launch { repository.insertMedicine(Medicine(name = name, dosage = dosage, isPreset = false)) }
    }
}
