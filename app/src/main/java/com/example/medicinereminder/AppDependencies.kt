package com.example.medicinereminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicinereminder.data.repository.HealthRepository
import com.example.medicinereminder.data.repository.MedicineRepository
import com.example.medicinereminder.reminder.ReminderScheduler

val LocalMedicineRepository = staticCompositionLocalOf<MedicineRepository> {
    error("No MedicineRepository provided")
}
val LocalHealthRepository = staticCompositionLocalOf<HealthRepository> {
    error("No HealthRepository provided")
}
val LocalReminderScheduler = staticCompositionLocalOf<ReminderScheduler> {
    error("No ReminderScheduler provided")
}

@Composable
fun AppDependencies(
    medicineRepository: MedicineRepository,
    healthRepository: HealthRepository,
    reminderScheduler: ReminderScheduler,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMedicineRepository provides medicineRepository,
        LocalHealthRepository provides healthRepository,
        LocalReminderScheduler provides reminderScheduler,
    ) {
        content()
    }
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(): VM {
    val medicineRepository = LocalMedicineRepository.current
    val healthRepository = LocalHealthRepository.current
    val reminderScheduler = LocalReminderScheduler.current
    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val constructors = modelClass.constructors
                for (constructor in constructors) {
                    val params = constructor.parameterTypes
                    if (params.isEmpty()) return constructor.newInstance() as T
                    val args = params.map { param ->
                        when {
                            param.isAssignableFrom(MedicineRepository::class.java) -> medicineRepository
                            param.isAssignableFrom(HealthRepository::class.java) -> healthRepository
                            param.isAssignableFrom(ReminderScheduler::class.java) -> reminderScheduler
                            else -> throw IllegalArgumentException("Unknown parameter type: ${param.name}")
                        }
                    }.toTypedArray()
                    return constructor.newInstance(*args) as T
                }
                throw IllegalArgumentException("No suitable constructor found for ${modelClass.name}")
            }
        }
    )
}
