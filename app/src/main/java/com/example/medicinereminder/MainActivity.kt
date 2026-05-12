package com.example.medicinereminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.medicinereminder.theme.MedicineReminderTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true
        if (!notificationGranted) {
            Toast.makeText(this, "需要通知权限才能提醒吃药", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()

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

    private fun requestPermissions() {
        // 1. Request POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }

        // 2. Request exact alarm permission
        //    Android 12-13: SCHEDULE_EXACT_ALARM is a runtime permission
        //    Android 14+: must use ACTION_REQUEST_SCHEDULE_EXACT_ALARM (app-op, not runtime)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (_: Exception) {
                    // Fallback: open app settings
                    val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(fallback)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM))
            }
        }

        // 3. Request battery optimization exemption (critical for alarms on Xiaomi/Huawei/Samsung)
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {
                // Some devices don't support this intent
            }
        }
    }
}
