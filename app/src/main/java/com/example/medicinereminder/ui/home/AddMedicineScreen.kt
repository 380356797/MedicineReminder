package com.example.medicinereminder.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medicinereminder.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    onBack: () -> Unit,
    viewModel: AddMedicineViewModel = appViewModel(),
) {
    var searchQuery by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }
    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val filteredMedicines = remember(allMedicines, searchQuery) {
        if (searchQuery.isBlank()) allMedicines
        else allMedicines.filter { it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加药物") },
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
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索药物") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showCustomInput = !showCustomInput },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("自定义药物名称")
            }

            if (showCustomInput) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("药物名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("剂量（如：1片/次）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            viewModel.addMedicine(customName.trim(), dosage.trim())
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = customName.isNotBlank(),
                ) {
                    Text("添加")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                val grouped = filteredMedicines.groupBy { it.category }
                grouped.forEach { (category, medicines) ->
                    item {
                        Text(
                            category.ifEmpty { "其他" },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    items(medicines) { medicine ->
                        ListItem(
                            headlineContent = { Text(medicine.name) },
                            supportingContent = if (medicine.dosage.isNotEmpty()) {
                                { Text(medicine.dosage) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
