package com.example.medicinereminder.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminder.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medicinereminder.ui.medicine.PhotoCaptureSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicineScreen(
    medicineId: Long,
    onBack: () -> Unit,
    onManageSchedules: () -> Unit,
    viewModel: EditMedicineViewModel = appViewModel(),
) {
    val medicine by viewModel.medicine.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(medicineId) {
        viewModel.loadMedicine(medicineId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑药物") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                },
            )
        },
    ) { padding ->
        val med = medicine
        if (med == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                var name by remember(med) { mutableStateOf(med!!.name) }
                var dosage by remember(med) { mutableStateOf(med!!.dosage) }
                var notes by remember(med) { mutableStateOf(med!!.notes) }
                var photoPath by remember(med) { mutableStateOf(med!!.photoPath) }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("药物名称") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !med!!.isPreset,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("剂量（如：1片/次）") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )

                Spacer(modifier = Modifier.height(16.dp))

                PhotoCaptureSection(
                    currentPhotoPath = photoPath,
                    onPhotoCaptured = { photoPath = it },
                    onPhotoDeleted = { photoPath = null },
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updateMedicine(
                            med!!.copy(
                                name = name.trim(),
                                dosage = dosage.trim(),
                                notes = notes.trim(),
                                photoPath = photoPath,
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank(),
                ) {
                    Text("保存")
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("提醒设置", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onManageSchedules) {
                        Icon(Icons.Default.Add, contentDescription = "添加提醒")
                    }
                }

                if (schedules.isEmpty()) {
                    Text(
                        "暂无提醒，点击 + 添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                } else {
                    schedules.forEach { schedule ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    String.format("%02d:%02d", schedule.hour, schedule.minute),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    when (schedule.repeatType) {
                                        com.example.medicinereminder.data.local.db.RepeatType.DAILY -> "每天"
                                        com.example.medicinereminder.data.local.db.RepeatType.EVERY_OTHER_DAY -> "隔天"
                                        com.example.medicinereminder.data.local.db.RepeatType.WEEKLY -> "每周"
                                        com.example.medicinereminder.data.local.db.RepeatType.CUSTOM -> "自定义"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除 ${medicine?.name ?: ""} 及其所有提醒吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMedicine()
                    showDeleteDialog = false
                    onBack()
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
        )
    }
}
