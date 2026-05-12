package com.example.medicinereminder.ui.profile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medicinereminder.LocalHealthRepository
import com.example.medicinereminder.LocalMedicineRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val medicineRepo = LocalMedicineRepository.current
    val healthRepo = LocalHealthRepository.current

    val medicines by medicineRepo.getAllMedicines().collectAsStateWithLifecycle(initialValue = emptyList())
    val healthRecords by healthRepo.getRecentRecords(1000).collectAsStateWithLifecycle(initialValue = emptyList())

    var showCalendar by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    var showTrends by remember { mutableStateOf(false) }

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
            // 用药统计概览
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem("药物种类", "${medicines.size}")
                        StatItem("健康记录", "${healthRecords.size}")
                        StatItem("今日待服", "—")
                    }
                }
            }

            // 功能列表
            item {
                Text("功能", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("用药日历") },
                            supportingContent = { Text("查看每日服药记录") },
                            leadingContent = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("健康趋势") },
                            supportingContent = { Text("查看指标变化趋势") },
                            leadingContent = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("用药统计") },
                            supportingContent = { Text("查看服药统计报告") },
                            leadingContent = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
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
                        ListItem(
                            headlineContent = { Text("通知设置") },
                            supportingContent = { Text("提醒声音、震动、重复提醒") },
                            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("数据备份") },
                            supportingContent = { Text("备份所有数据到本地") },
                            leadingContent = { Icon(Icons.Default.Backup, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("数据恢复") },
                            supportingContent = { Text("从本地备份恢复数据") },
                            leadingContent = { Icon(Icons.Default.Restore, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
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
                            supportingContent = { Text("1.0.0") },
                            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("使用帮助") },
                            supportingContent = { Text("了解如何使用本应用") },
                            leadingContent = { Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
