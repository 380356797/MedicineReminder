package com.example.medicinereminder.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medicinereminder.LocalHealthRepository
import com.example.medicinereminder.LocalMedicineRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val medicineRepo = LocalMedicineRepository.current
    val healthRepo = LocalHealthRepository.current

    val medicines by medicineRepo.getAllMedicines().collectAsStateWithLifecycle(initialValue = emptyList())
    val healthRecords by healthRepo.getRecentRecords(1000).collectAsStateWithLifecycle(initialValue = emptyList())

    val onClick: (String) -> Unit = { feature ->
        Toast.makeText(context, "$feature - 功能开发中", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的", fontSize = 20.sp, fontWeight = FontWeight.Bold) })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem("药物种类", "${medicines.size}")
                        StatItem("健康记录", "${healthRecords.size}")
                        StatItem("今日待服", "—")
                    }
                }
            }

            item {
                Text("功能", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ProfileListItem(
                            title = "用药日历",
                            subtitle = "查看每日服药记录",
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            onClick = { onClick("用药日历") },
                        )
                        HorizontalDivider()
                        ProfileListItem(
                            title = "健康趋势",
                            subtitle = "查看指标变化趋势",
                            icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) },
                            onClick = { onClick("健康趋势") },
                        )
                        HorizontalDivider()
                        ProfileListItem(
                            title = "用药统计",
                            subtitle = "查看服药统计报告",
                            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            onClick = { onClick("用药统计") },
                        )
                    }
                }
            }

            item {
                Text("设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ProfileListItem(
                            title = "通知设置",
                            subtitle = "提醒声音、震动、重复提醒",
                            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            onClick = { onClick("通知设置") },
                        )
                        HorizontalDivider()
                        ProfileListItem(
                            title = "数据备份",
                            subtitle = "备份所有数据到本地",
                            icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                            onClick = { onClick("数据备份") },
                        )
                        HorizontalDivider()
                        ProfileListItem(
                            title = "数据恢复",
                            subtitle = "从本地备份恢复数据",
                            icon = { Icon(Icons.Default.Restore, contentDescription = null) },
                            onClick = { onClick("数据恢复") },
                        )
                    }
                }
            }

            item {
                Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("版本") },
                            supportingContent = { Text("1.1") },
                            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                        )
                        HorizontalDivider()
                        ProfileListItem(
                            title = "使用帮助",
                            subtitle = "了解如何使用本应用",
                            icon = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null) },
                            onClick = { onClick("使用帮助") },
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "数据存储在本地设备上",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ProfileListItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick() },
    )
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
