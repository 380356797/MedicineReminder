package com.example.medicinereminder.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.HealthIndicator
import com.example.medicinereminder.data.local.db.HealthRecord
import com.example.medicinereminder.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthIndicatorDetailViewModel(private val repository: HealthRepository) : ViewModel() {
    private val _indicatorId = MutableStateFlow(0L)
    val indicator: StateFlow<HealthIndicator?> = _indicatorId.flatMapLatest { id ->
        flow { emit(repository.getIndicatorById(id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val records: StateFlow<List<HealthRecord>> = _indicatorId.flatMapLatest { id ->
        repository.getRecordsByIndicator(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun load(indicatorId: Long) { _indicatorId.value = indicatorId }
    fun deleteRecord(recordId: Long) {
        viewModelScope.launch { repository.deleteRecord(recordId) }
    }
}
