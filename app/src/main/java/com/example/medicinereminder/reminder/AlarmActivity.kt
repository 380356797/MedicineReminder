package com.example.medicinereminder.reminder

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    private var medicineName: String = ""
    private var scheduleId: Long = 0L
    private var medicineId: Long = 0L
    private var alarmId: Int = 0
    private var doseAmount: Int = 1
    private var doseUnit: String = "片"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Read intent extras
        medicineName = intent.getStringExtra("medicine_name") ?: "药物"
        scheduleId = intent.getLongExtra("schedule_id", 0L)
        medicineId = intent.getLongExtra("medicine_id", 0L)
        alarmId = intent.getIntExtra("alarm_id", 0)
        doseAmount = intent.getIntExtra("dose_amount", 1)
        doseUnit = intent.getStringExtra("dose_unit") ?: "片"

        // Start alarm sound and vibration
        startAlarm()

        setContent {
            MaterialTheme {
                AlarmScreen(
                    medicineName = medicineName,
                    doseAmount = doseAmount,
                    doseUnit = doseUnit,
                    onTaken = {
                        stopAlarm()
                        logAsTaken()
                        finish()
                    },
                    onSnooze = {
                        stopAlarm()
                        scheduleSnooze()
                        finish()
                    },
                )
            }
        }
    }

    private fun startAlarm() {
        // Play alarm sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (alarmUri != null) {
            ringtone = RingtoneManager.getRingtone(this, alarmUri)
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone?.isLooping = true
            ringtone?.play()
        }

        // Vibrate
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    private fun logAsTaken() {
        // TODO: Insert a record into the database marking this dose as taken.
        //  Example: MedicineReminderRepository(context).markAsTaken(scheduleId, medicineId)
    }

    private fun scheduleSnooze() {
        val scheduler = ReminderScheduler(this)
        scheduler.snooze(scheduleId, medicineId, medicineName, 15)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}

@Composable
private fun AlarmScreen(
    medicineName: String,
    doseAmount: Int,
    doseUnit: String,
    onTaken: () -> Unit,
    onSnooze: () -> Unit,
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val currentTime = timeFormat.format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Clock icon / header
        Text(
            text = "吃药时间到了",
            fontSize = 22.sp,
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Medicine name - large and prominent
        Text(
            text = medicineName,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 50.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current time
        Text(
            text = currentTime,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF00D2FF),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dose info
        Text(
            text = "每次 $doseAmount $doseUnit",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFFFD700),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Snooze button
            Button(
                onClick = onSnooze,
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF444466),
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "稍后提醒",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Taken button
            Button(
                onClick = onTaken,
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "已吃",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
