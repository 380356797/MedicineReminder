package com.example.medicinereminder.ui.health

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.HealthIndicator
import com.example.medicinereminder.data.local.db.HealthRecordWithIndicator
import com.example.medicinereminder.data.repository.HealthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HealthViewModel(private val repository: HealthRepository) : ViewModel() {

    val enabledIndicators: StateFlow<List<HealthIndicator>> = repository.getEnabledIndicators()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentRecords: StateFlow<List<HealthRecordWithIndicator>> = repository.getRecentRecords(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun exportRecords(context: Context, records: List<HealthRecordWithIndicator>): Uri {
        return HealthExportUtil.exportToCsv(context, records)
    }
}
