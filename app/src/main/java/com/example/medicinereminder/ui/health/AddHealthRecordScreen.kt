package com.example.medicinereminder.ui.health

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medicinereminder.appViewModel
import com.example.medicinereminder.data.local.db.HealthRecord
import com.example.medicinereminder.ui.medicine.PhotoCaptureSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthRecordScreen(
    indicatorId: Long,
    onBack: () -> Unit,
    viewModel: AddHealthRecordViewModel = appViewModel(),
) {
    val indicator by viewModel.indicator.collectAsStateWithLifecycle()
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(indicatorId) {
        viewModel.load(indicatorId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "记录 ${indicator?.name ?: ""}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            indicator?.let { ind ->
                if (ind.unit.isNotEmpty()) {
                    Text(
                        ind.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                    Text(
                        "单位: ${ind.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        ind.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                }

                if (ind.normalRange.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "正常范围: ${ind.normalRange}${if (ind.unit.isNotEmpty()) " ${ind.unit}" else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = {
                        Text(
                            if (ind.unit.isNotEmpty()) "数值 (${ind.unit})" else "数值",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text("备注（可选）", style = MaterialTheme.typography.titleMedium)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(16.dp))

                PhotoCaptureSection(
                    currentPhotoPath = photoPath,
                    onPhotoCaptured = { photoPath = it },
                    onPhotoDeleted = { photoPath = null },
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.addRecord(
                            HealthRecord(
                                indicatorId = indicatorId,
                                value = value.trim(),
                                notes = notes.trim(),
                                photoPath = photoPath,
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = value.isNotBlank(),
                ) {
                    Text("保存记录", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                }
            }
        }
    }
}
