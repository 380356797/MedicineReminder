package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.local.db.*
import kotlinx.coroutines.flow.Flow

class HealthRepository(
    private val indicatorDao: HealthIndicatorDao,
    private val recordDao: HealthRecordDao,
) {
    fun getAllIndicators(): Flow<List<HealthIndicator>> = indicatorDao.getAll()
    fun getEnabledIndicators(): Flow<List<HealthIndicator>> = indicatorDao.getAllEnabled()
    suspend fun getIndicatorById(id: Long): HealthIndicator? = indicatorDao.getById(id)
    suspend fun insertIndicator(indicator: HealthIndicator): Long = indicatorDao.insert(indicator)
    suspend fun updateIndicator(indicator: HealthIndicator) = indicatorDao.update(indicator)
    suspend fun setIndicatorEnabled(id: Long, enabled: Boolean) = indicatorDao.setEnabled(id, enabled)
    fun getRecordsByIndicator(indicatorId: Long): Flow<List<HealthRecord>> = recordDao.getByIndicator(indicatorId)
    fun getRecordsWithIndicator(indicatorId: Long): Flow<List<HealthRecordWithIndicator>> = recordDao.getRecordsWithIndicator(indicatorId)
    fun getRecentRecords(limit: Int = 20): Flow<List<HealthRecordWithIndicator>> = recordDao.getRecentRecords(limit)
    suspend fun insertRecord(record: HealthRecord): Long = recordDao.insert(record)
    suspend fun updateRecord(record: HealthRecord) = recordDao.update(record)
    suspend fun deleteRecord(id: Long) = recordDao.deleteById(id)
}
