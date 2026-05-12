package com.example.medicinereminder.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthIndicatorDao {
    @Query("SELECT * FROM health_indicators WHERE isEnabled = 1 ORDER BY sortOrder ASC, name ASC")
    fun getAllEnabled(): Flow<List<HealthIndicator>>

    @Query("SELECT * FROM health_indicators ORDER BY isPreset DESC, sortOrder ASC, name ASC")
    fun getAll(): Flow<List<HealthIndicator>>

    @Query("SELECT * FROM health_indicators WHERE id = :id")
    suspend fun getById(id: Long): HealthIndicator?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(indicator: HealthIndicator): Long

    @Update
    suspend fun update(indicator: HealthIndicator)

    @Delete
    suspend fun delete(indicator: HealthIndicator)

    @Query("UPDATE health_indicators SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM health_indicators")
    suspend fun count(): Int
}

@Dao
interface HealthRecordDao {
    @Query(
        """
        SELECT hr.* FROM health_records hr
        WHERE hr.indicatorId = :indicatorId
        ORDER BY hr.recordedAt DESC
        LIMIT :limit
        """
    )
    fun getByIndicator(indicatorId: Long, limit: Int = 50): Flow<List<HealthRecord>>

    @Query(
        """
        SELECT hr.*, hi.name as indicatorName, hi.unit as indicatorUnit, hi.category as indicatorCategory
        FROM health_records hr
        INNER JOIN health_indicators hi ON hr.indicatorId = hi.id
        WHERE hr.recordedAt >= :startOfDay AND hr.recordedAt < :endOfDay
        ORDER BY hr.recordedAt DESC
        """
    )
    fun getTodayRecordsWithIndicator(startOfDay: Long, endOfDay: Long): Flow<List<HealthRecordWithIndicator>>

    @Query(
        """
        SELECT hr.*, hi.name as indicatorName, hi.unit as indicatorUnit, hi.category as indicatorCategory
        FROM health_records hr
        INNER JOIN health_indicators hi ON hr.indicatorId = hi.id
        WHERE hi.id = :indicatorId
        ORDER BY hr.recordedAt DESC
        LIMIT :limit
        """
    )
    fun getRecordsWithIndicator(indicatorId: Long, limit: Int = 50): Flow<List<HealthRecordWithIndicator>>

    @Query(
        """
        SELECT DISTINCT hi.id, hi.name, hi.unit, hi.category, hi.normalRange, hi.isPreset, hi.isEnabled, hi.sortOrder
        FROM health_indicators hi
        INNER JOIN health_records hr ON hi.id = hr.indicatorId
        WHERE hi.isEnabled = 1
        ORDER BY hi.sortOrder ASC, hi.name ASC
        """
    )
    fun getIndicatorsWithRecords(): Flow<List<HealthIndicator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HealthRecord): Long

    @Update
    suspend fun update(record: HealthRecord)

    @Delete
    suspend fun delete(record: HealthRecord)

    @Query("DELETE FROM health_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT hr.*, hi.name as indicatorName, hi.unit as indicatorUnit, hi.category as indicatorCategory
        FROM health_records hr
        INNER JOIN health_indicators hi ON hr.indicatorId = hi.id
        ORDER BY hr.recordedAt DESC
        LIMIT :limit
        """
    )
    fun getRecentRecords(limit: Int = 20): Flow<List<HealthRecordWithIndicator>>
}

data class HealthRecordWithIndicator(
    val id: Long,
    val indicatorId: Long,
    val value: String,
    val notes: String,
    val recordedAt: Long,
    val indicatorName: String,
    val indicatorUnit: String,
    val indicatorCategory: String,
)
