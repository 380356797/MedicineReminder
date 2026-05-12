package com.example.medicinereminder.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medicinereminder.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthIndicatorListScreen(
    onBack: () -> Unit,
    viewModel: HealthIndicatorListViewModel = appViewModel(),
) {
    val indicators by viewModel.indicators.collectAsStateWithLifecycle()
    val grouped = indicators.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("健康指标管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            grouped.forEach { (category, items) ->
                item {
                    Text(
                        category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
                items(items) { indicator ->
                    ListItem(
                        headlineContent = { Text(indicator.name) },
                        supportingContent = {
                            Column {
                                if (indicator.unit.isNotEmpty()) {
                                    Text("单位: ${indicator.unit}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (indicator.normalRange.isNotEmpty()) {
                                    Text("正常范围: ${indicator.normalRange}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        },
                        trailingContent = {
                            Switch(
                                checked = indicator.isEnabled,
                                onCheckedChange = { viewModel.toggleIndicator(indicator.id, it) },
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
