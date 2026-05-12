package com.example.medicinereminder.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.HealthIndicator
import com.example.medicinereminder.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthIndicatorListViewModel(private val repository: HealthRepository) : ViewModel() {
    val indicators: StateFlow<List<HealthIndicator>> = repository.getAllIndicators()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleIndicator(id: Long, enabled: Boolean) {
        viewModelScope.launch { repository.setIndicatorEnabled(id, enabled) }
    }
}
