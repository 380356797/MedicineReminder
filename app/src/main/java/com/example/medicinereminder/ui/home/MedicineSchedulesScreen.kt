package com.example.medicinereminder.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminder.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineSchedulesScreen(
    medicineId: Long,
    onBack: () -> Unit,
    onAddSchedule: () -> Unit,
    viewModel: ScheduleViewModel = appViewModel(),
) {
    val medicine by viewModel.medicine.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()

    LaunchedEffect(medicineId) {
        viewModel.load(medicineId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${medicine?.name ?: ""} 提醒设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onAddSchedule) {
                        Icon(Icons.Default.Add, contentDescription = "添加提醒")
                    }
                },
            )
        },
    ) { padding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("暂无提醒")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddSchedule) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加提醒")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(schedules) { schedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    String.format("%02d:%02d", schedule.hour, schedule.minute),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    when (schedule.repeatType) {
                                        com.example.medicinereminder.data.local.db.RepeatType.DAILY -> "每天重复"
                                        com.example.medicinereminder.data.local.db.RepeatType.EVERY_OTHER_DAY -> "隔天重复"
                                        com.example.medicinereminder.data.local.db.RepeatType.WEEKLY -> "每周: ${schedule.repeatDays}"
                                        com.example.medicinereminder.data.local.db.RepeatType.CUSTOM -> "自定义: ${schedule.repeatDays}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSchedule(schedule) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
