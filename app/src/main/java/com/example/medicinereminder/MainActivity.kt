package com.example.medicinereminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicinereminder.data.repository.HealthRepository
import com.example.medicinereminder.data.repository.MedicineRepository
import com.example.medicinereminder.reminder.ReminderScheduler
import com.example.medicinereminder.theme.MedicineReminderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as MedicineApp
        setContent {
            MedicineReminderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppDependencies(
                        medicineRepository = app.medicineRepository,
                        healthRepository = app.healthRepository,
                        reminderScheduler = app.reminderScheduler,
                    ) {
                        MainNavigation()
                    }
                }
            }
        }
    }
}
