package com.example.medicinereminder.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE isEnabled = 1 ORDER BY name")
    fun getAllEnabled(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines ORDER BY isPreset DESC, name ASC")
    fun getAll(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE isPreset = 0 ORDER BY createdAt DESC")
    fun getUserMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): Medicine?

    @Query("SELECT * FROM medicines WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Medicine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine)

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("UPDATE medicines SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM medicines")
    suspend fun count(): Int
}

@Dao
interface MedicineScheduleDao {
    @Query("SELECT * FROM medicine_schedules WHERE medicineId = :medicineId")
    fun getByMedicine(medicineId: Long): Flow<List<MedicineSchedule>>

    @Query("SELECT * FROM medicine_schedules WHERE isEnabled = 1")
    suspend fun getAllEnabled(): List<MedicineSchedule>

    @Query("SELECT * FROM medicine_schedules WHERE medicineId = :medicineId AND isEnabled = 1")
    suspend fun getEnabledByMedicine(medicineId: Long): List<MedicineSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: MedicineSchedule): Long

    @Update
    suspend fun update(schedule: MedicineSchedule)

    @Delete
    suspend fun delete(schedule: MedicineSchedule)

    @Query("DELETE FROM medicine_schedules WHERE medicineId = :medicineId")
    suspend fun deleteByMedicine(medicineId: Long)
}

@Dao
interface MedicineLogDao {
    @Query(
        """
        SELECT ml.* FROM medicine_logs ml
        INNER JOIN medicines m ON ml.medicineId = m.id
        WHERE ml.scheduledTime >= :startOfDay AND ml.scheduledTime < :endOfDay
        ORDER BY ml.scheduledTime ASC
        """
    )
    fun getTodayLogs(startOfDay: Long, endOfDay: Long): Flow<List<MedicineLog>>

    @Query(
        """
        SELECT ml.*, m.name as medicineName, m.dosage as medicineDosage
        FROM medicine_logs ml
        INNER JOIN medicines m ON ml.medicineId = m.id
        WHERE ml.scheduledTime >= :startOfDay AND ml.scheduledTime < :endOfDay
        ORDER BY ml.scheduledTime ASC
        """
    )
    fun getTodayLogsWithMedicine(startOfDay: Long, endOfDay: Long): Flow<List<MedicineLogWithMedicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MedicineLog): Long

    @Update
    suspend fun update(log: MedicineLog)

    @Query("UPDATE medicine_logs SET status = :status, takenTime = :takenTime WHERE id = :id")
    suspend fun updateStatus(id: Long, status: LogStatus, takenTime: Long?)

    @Query("DELETE FROM medicine_logs WHERE scheduledTime < :before")
    suspend fun cleanup(before: Long)
}

data class MedicineLogWithMedicine(
    val id: Long,
    val medicineId: Long,
    val scheduledTime: Long,
    val takenTime: Long?,
    val status: LogStatus,
    val medicineName: String,
    val medicineDosage: String,
)
