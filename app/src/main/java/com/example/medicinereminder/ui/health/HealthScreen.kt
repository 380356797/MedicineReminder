package com.example.medicinereminder.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medicinereminder.appViewModel
import com.example.medicinereminder.data.local.db.HealthIndicator
import com.example.medicinereminder.data.local.db.HealthRecordWithIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    onAddRecord: (Long) -> Unit,
    onViewIndicators: () -> Unit,
    onIndicatorClick: (Long) -> Unit,
    viewModel: HealthViewModel = appViewModel(),
) {
    val enabledIndicators by viewModel.enabledIndicators.collectAsStateWithLifecycle()
    val recentRecords by viewModel.recentRecords.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showIndicatorPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("健康记录", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    if (recentRecords.isNotEmpty()) {
                        IconButton(onClick = {
                            val uri = viewModel.exportRecords(context, recentRecords)
                            val intent = HealthExportUtil.createShareIntent(context, uri)
                            context.startActivity(
                                android.content.Intent.createChooser(intent, "导出健康记录")
                            )
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "导出CSV")
                        }
                    }
                    IconButton(onClick = onViewIndicators) {
                        Icon(Icons.Default.List, contentDescription = "指标管理")
                    }
                },
            )
        },
        floatingActionButton = {
            if (enabledIndicators.isNotEmpty()) {
                FloatingActionButton(onClick = { showIndicatorPicker = true }) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("记录数据", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
    ) { padding ->
        if (recentRecords.isEmpty()) {
            EmptyHealthState(
                onViewIndicators = onViewIndicators,
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                item {
                    Text(
                        "我的健康记录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
                items(recentRecords) { record ->
                    HealthRecordCard(
                        record = record,
                        onClick = { onIndicatorClick(record.indicatorId) },
                    )
                }
            }
        }
    }

    if (showIndicatorPicker) {
        IndicatorPickerDialog(
            indicators = enabledIndicators,
            onSelected = { indicatorId ->
                showIndicatorPicker = false
                onAddRecord(indicatorId)
            },
            onDismiss = { showIndicatorPicker = false },
        )
    }
}

@Composable
private fun EmptyHealthState(onViewIndicators: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("暂无健康记录", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "点击右下角按钮开始记录健康数据",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onViewIndicators) {
            Icon(Icons.Default.List, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("管理健康指标", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun HealthRecordCard(record: HealthRecordWithIndicator, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    record.indicatorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    dateFormat.format(Date(record.recordedAt)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (record.notes.isNotEmpty()) {
                    Text(
                        record.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    record.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp,
                )
                if (record.indicatorUnit.isNotEmpty()) {
                    Text(
                        record.indicatorUnit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorPickerDialog(
    indicators: List<HealthIndicator>,
    onSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val grouped = remember(indicators) { indicators.groupBy { it.category } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择要记录的指标", style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                grouped.forEach { (category, items) ->
                    item {
                        Text(
                            category,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    items(items) { indicator ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    indicator.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 17.sp,
                                )
                            },
                            supportingContent = if (indicator.unit.isNotEmpty()) {
                                { Text(indicator.unit, style = MaterialTheme.typography.bodyMedium) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", style = MaterialTheme.typography.titleMedium)
            }
        },
    )
}
