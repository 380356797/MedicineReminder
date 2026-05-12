package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.local.db.*
import kotlinx.coroutines.flow.Flow

class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val scheduleDao: MedicineScheduleDao,
    private val logDao: MedicineLogDao,
) {
    fun getAllMedicines(): Flow<List<Medicine>> = medicineDao.getAll()
    fun getAllEnabledMedicines(): Flow<List<Medicine>> = medicineDao.getAllEnabled()
    fun getUserMedicines(): Flow<List<Medicine>> = medicineDao.getUserMedicines()
    suspend fun getMedicineById(id: Long): Medicine? = medicineDao.getById(id)
    suspend fun getMedicineByName(name: String): Medicine? = medicineDao.getByName(name)
    suspend fun insertMedicine(medicine: Medicine): Long = medicineDao.insert(medicine)
    suspend fun updateMedicine(medicine: Medicine) = medicineDao.update(medicine)
    suspend fun deleteMedicine(medicine: Medicine) = medicineDao.delete(medicine)
    suspend fun setMedicineEnabled(id: Long, enabled: Boolean) = medicineDao.setEnabled(id, enabled)
    fun getSchedules(medicineId: Long): Flow<List<MedicineSchedule>> = scheduleDao.getByMedicine(medicineId)
    suspend fun getAllEnabledSchedules(): List<MedicineSchedule> = scheduleDao.getAllEnabled()
    suspend fun insertSchedule(schedule: MedicineSchedule): Long = scheduleDao.insert(schedule)
    suspend fun updateSchedule(schedule: MedicineSchedule) = scheduleDao.update(schedule)
    suspend fun deleteSchedule(schedule: MedicineSchedule) = scheduleDao.delete(schedule)
    fun getTodayLogs(startOfDay: Long, endOfDay: Long): Flow<List<MedicineLogWithMedicine>> = logDao.getTodayLogsWithMedicine(startOfDay, endOfDay)
    suspend fun insertLog(log: MedicineLog): Long = logDao.insert(log)
    suspend fun updateLogStatus(id: Long, status: LogStatus, takenTime: Long?) = logDao.updateStatus(id, status, takenTime)
    suspend fun findLogId(medicineId: Long, scheduledTime: Long): Long? = logDao.findLogId(medicineId, scheduledTime)
}
