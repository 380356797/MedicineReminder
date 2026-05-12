package com.example.medicinereminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.db.*
import com.example.medicinereminder.data.repository.MedicineRepository
import com.example.medicinereminder.reminder.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class TodayScheduleItem(
    val logId: Long?,
    val scheduleId: Long,
    val medicineId: Long,
    val medicineName: String,
    val medicineCategory: String,
    val doseAmount: Int,
    val doseUnit: String,
    val hour: Int,
    val minute: Int,
    val status: LogStatus?,
)

class HomeViewModel(
    private val repository: MedicineRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val medicines: StateFlow<List<Medicine>> = repository.getUserMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _todayItems = MutableStateFlow<List<TodayScheduleItem>>(emptyList())
    val todayItems: StateFlow<List<TodayScheduleItem>> = _todayItems.asStateFlow()

    init {
        generateTodayLogs()
    }

    fun refreshToday() {
        generateTodayLogs()
    }

    private fun generateTodayLogs() {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            val endOfDay = startOfDay + 86_400_000L

            val allSchedules = repository.getAllEnabledSchedules()
            val enabledMedicines = repository.getAllEnabledMedicines().first()
            val medicineMap = enabledMedicines.associateBy { it.id }
            val todayLogs = repository.getTodayLogs(startOfDay, endOfDay).first()
            val logMap = todayLogs.associateBy { it.medicineId }

            val items = mutableListOf<TodayScheduleItem>()

            for (schedule in allSchedules) {
                if (!schedule.isEnabled) continue
                if (!isScheduleForToday(schedule)) continue
                val medicine = medicineMap[schedule.medicineId] ?: continue
                if (!medicine.isEnabled) continue

                val scheduledTime = startOfDay + schedule.hour * 3_600_000L + schedule.minute * 60_000L
                val existingLog = todayLogs.find {
                    it.medicineId == schedule.medicineId &&
                        kotlin.math.abs(it.scheduledTime - scheduledTime) < 60_000L
                }

                if (existingLog == null) {
                    val logId = repository.insertLog(
                        MedicineLog(
                            medicineId = schedule.medicineId,
                            scheduledTime = scheduledTime,
                            status = LogStatus.PENDING,
                        )
                    )
                    items.add(
                        TodayScheduleItem(
                            logId = logId,
                            scheduleId = schedule.id,
                            medicineId = schedule.medicineId,
                            medicineName = medicine.name,
                            medicineCategory = medicine.category,
                            doseAmount = schedule.doseAmount,
                            doseUnit = schedule.doseUnit,
                            hour = schedule.hour,
                            minute = schedule.minute,
                            status = LogStatus.PENDING,
                        )
                    )
                } else {
                    items.add(
                        TodayScheduleItem(
                            logId = existingLog.id,
                            scheduleId = schedule.id,
                            medicineId = schedule.medicineId,
                            medicineName = medicine.name,
                            medicineCategory = medicine.category,
                            doseAmount = schedule.doseAmount,
                            doseUnit = schedule.doseUnit,
                            hour = schedule.hour,
                            minute = schedule.minute,
                            status = existingLog.status,
                        )
                    )
                }
            }

            _todayItems.value = items.sortedWith(compareBy({ it.hour }, { it.minute }))
        }
    }

    fun markTaken(logId: Long) {
        viewModelScope.launch {
            repository.updateLogStatus(logId, LogStatus.TAKEN, System.currentTimeMillis())
            refreshToday()
        }
    }

    fun markSkipped(logId: Long) {
        viewModelScope.launch {
            repository.updateLogStatus(logId, LogStatus.SKIPPED, null)
            refreshToday()
        }
    }

    private fun isScheduleForToday(schedule: MedicineSchedule): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return when (schedule.repeatType) {
            RepeatType.DAILY -> true
            RepeatType.EVERY_OTHER_DAY -> {
                val epochCal = Calendar.getInstance().apply {
                    set(2024, Calendar.JANUARY, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val epochDay = epochCal.timeInMillis / 86_400_000L
                val todayDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis / 86_400_000L
                ((todayDay - epochDay) % 2L) == 0L
            }
            RepeatType.WEEKLY, RepeatType.CUSTOM ->
                schedule.repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(today)
        }
    }
}
