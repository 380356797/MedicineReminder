package com.example.medicinereminder.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.HealthIndicator
import com.example.medicinereminder.data.local.db.HealthRecord
import com.example.medicinereminder.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddHealthRecordViewModel(private val repository: HealthRepository) : ViewModel() {

    private val _indicatorId = MutableStateFlow(0L)

    val indicator: StateFlow<HealthIndicator?> = _indicatorId.flatMapLatest { id ->
        flow { emit(repository.getIndicatorById(id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(indicatorId: Long) {
        _indicatorId.value = indicatorId
    }

    fun addRecord(record: HealthRecord) {
        viewModelScope.launch { repository.insertRecord(record) }
    }
}
