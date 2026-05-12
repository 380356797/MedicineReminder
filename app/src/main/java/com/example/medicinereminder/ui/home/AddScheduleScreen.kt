package com.example.medicinereminder.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.medicinereminder.appViewModel
import com.example.medicinereminder.data.local.db.Medicine
import com.example.medicinereminder.data.local.db.MedicineSchedule
import com.example.medicinereminder.data.local.db.RepeatType
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    medicineId: Long,
    onBack: () -> Unit,
    viewModel: AddScheduleViewModel = appViewModel(),
) {
    // Step state
    var step by remember { mutableStateOf(if (medicineId > 0) 2 else 1) }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var doseAmount by remember { mutableStateOf("1") }
    var doseUnit by remember { mutableStateOf("片") }
    var timesPerDay by remember { mutableStateOf("1") }
    val timePoints = remember { mutableStateListOf(Pair(8, 0)) }
    var repeatType by remember { mutableStateOf(RepeatType.DAILY) }
    val selectedDays = remember { mutableStateListOf<Int>() }

    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    // Load pre-selected medicine
    LaunchedEffect(medicineId) {
        if (medicineId > 0) {
            selectedMedicine = viewModel.getMedicineById(medicineId)
            step = 2
        }
    }

    // Update time points when timesPerDay changes
    LaunchedEffect(timesPerDay) {
        val count = timesPerDay.toIntOrNull()?.coerceIn(1, 6) ?: 1
        while (timePoints.size < count) {
            val defaultHours = listOf(8, 12, 18, 22, 7, 20)
            timePoints.add(Pair(defaultHours.getOrElse(timePoints.size) { 12 }, 0))
        }
        while (timePoints.size > count) {
            timePoints.removeLast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加提醒") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1) step-- else onBack()
                    }) {
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
                .padding(16.dp),
        ) {
            // Step indicator
            StepIndicator(step = step, totalSteps = 5)

            Spacer(modifier = Modifier.height(16.dp))

            when (step) {
                1 -> Column(Modifier.weight(1f)) {
                    StepSelectMedicine(
                        medicines = allMedicines,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onSelect = { selectedMedicine = it; step = 2 },
                    )
                }
                2 -> Column(Modifier.weight(1f)) {
                    StepDose(
                        doseAmount = doseAmount,
                        onDoseAmountChange = { doseAmount = it },
                        doseUnit = doseUnit,
                        onDoseUnitChange = { doseUnit = it },
                        onNext = { if (doseAmount.isNotBlank()) step = 3 },
                    )
                }
                3 -> Column(Modifier.weight(1f)) {
                    StepFrequency(
                        timesPerDay = timesPerDay,
                        onTimesPerDayChange = { timesPerDay = it },
                        onNext = { step = 4 },
                    )
                }
                4 -> Column(Modifier.weight(1f)) {
                    StepTimePoints(
                        timePoints = timePoints,
                        onTimeChange = { index, h, m -> timePoints[index] = Pair(h, m) },
                        onNext = { step = 5 },
                    )
                }
                5 -> Column(Modifier.weight(1f)) {
                    StepRepeatType(
                        repeatType = repeatType,
                        onRepeatTypeChange = { repeatType = it },
                        selectedDays = selectedDays,
                        onDayToggle = { day ->
                            if (selectedDays.contains(day)) selectedDays.remove(day)
                            else selectedDays.add(day)
                        },
                        onSave = {
                            val repeatDaysStr = if (repeatType == RepeatType.DAILY || repeatType == RepeatType.EVERY_OTHER_DAY) {
                                ""
                            } else {
                                selectedDays.sorted().joinToString(",")
                            }
                            val med = selectedMedicine ?: return@StepRepeatType
                            for ((h, m) in timePoints) {
                                viewModel.addSchedule(
                                    MedicineSchedule(
                                        medicineId = med.id,
                                        hour = h,
                                        minute = m,
                                        doseAmount = doseAmount.toIntOrNull() ?: 1,
                                        doseUnit = doseUnit,
                                        repeatType = repeatType,
                                        repeatDays = repeatDaysStr,
                                    )
                                )
                            }
                            onBack()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        for (i in 1..totalSteps) {
            val isActive = i <= step
            Surface(
                modifier = Modifier
                    .width(if (isActive) 32.dp else 16.dp)
                    .height(8.dp)
                    .padding(horizontal = 2.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            ) {}
        }
    }
}

@Composable
private fun StepSelectMedicine(
    medicines: List<Medicine>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelect: (Medicine) -> Unit,
) {
    val filtered = remember(medicines, searchQuery) {
        if (searchQuery.isBlank()) medicines
        else medicines.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }
    val grouped = filtered.groupBy { it.category.ifEmpty { "自定义" } }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("选择药物", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("搜索药物") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            grouped.forEach { (category, meds) ->
                item {
                    Text(
                        category,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
                items(meds) { medicine ->
                    Card(
                        onClick = { onSelect(medicine) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(medicine.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepDose(
    doseAmount: String,
    onDoseAmountChange: (String) -> Unit,
    doseUnit: String,
    onDoseUnitChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    val units = listOf("片", "粒", "毫升", "袋", "支", "颗", "滴", "贴")

    Column(modifier = Modifier.fillMaxSize()) {
        Text("每次用量", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            OutlinedTextField(
                value = doseAmount,
                onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 3) onDoseAmountChange(v) },
                modifier = Modifier.width(100.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
            )
            Spacer(modifier = Modifier.width(16.dp))

            var unitExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                OutlinedTextField(
                    value = doseUnit,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().width(80.dp),
                    textStyle = MaterialTheme.typography.headlineSmall,
                )
                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    units.forEach { u ->
                        DropdownMenuItem(text = { Text(u, style = MaterialTheme.typography.bodyLarge) }, onClick = {
                            onDoseUnitChange(u)
                            unitExpanded = false
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = doseAmount.isNotBlank(),
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StepFrequency(
    timesPerDay: String,
    onTimesPerDayChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("每天吃几次", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val v = (timesPerDay.toIntOrNull() ?: 2) - 1
                        if (v >= 1) onTimesPerDayChange(v.toString())
                    },
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "减少", modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    timesPerDay,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(24.dp))
                IconButton(
                    onClick = {
                        val v = (timesPerDay.toIntOrNull() ?: 0) + 1
                        if (v <= 6) onTimesPerDayChange(v.toString())
                    },
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "增加", modifier = Modifier.size(32.dp))
                }
            }
            Text("次 / 天", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepTimePoints(
    timePoints: List<Pair<Int, Int>>,
    onTimeChange: (index: Int, hour: Int, minute: Int) -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("选择时间", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(timePoints.size) { index ->
                val (h, m) = timePoints[index]
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
                        Text(
                            "第${index + 1}次",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.width(70.dp),
                        )

                        var hourExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = hourExpanded, onExpandedChange = { hourExpanded = it }) {
                            OutlinedTextField(
                                value = String.format("%02d", h),
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor().width(72.dp),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            ExposedDropdownMenu(expanded = hourExpanded, onDismissRequest = { hourExpanded = false }) {
                                (0..23).forEach { hh ->
                                    DropdownMenuItem(
                                        text = { Text(String.format("%02d", hh)) },
                                        onClick = { onTimeChange(index, hh, m); hourExpanded = false },
                                    )
                                }
                            }
                        }

                        Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 4.dp))

                        var minuteExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = minuteExpanded, onExpandedChange = { minuteExpanded = it }) {
                            OutlinedTextField(
                                value = String.format("%02d", m),
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor().width(72.dp),
                                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            ExposedDropdownMenu(expanded = minuteExpanded, onDismissRequest = { minuteExpanded = false }) {
                                listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55).forEach { mm ->
                                    DropdownMenuItem(
                                        text = { Text(String.format("%02d", mm)) },
                                        onClick = { onTimeChange(index, h, mm); minuteExpanded = false },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("下一步", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StepRepeatType(
    repeatType: RepeatType,
    onRepeatTypeChange: (RepeatType) -> Unit,
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit,
    onSave: () -> Unit,
) {
    val dayNames = listOf("日", "一", "二", "三", "四", "五", "六")

    Column(modifier = Modifier.fillMaxSize()) {
        Text("重复方式", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RepeatType.entries.forEach { type ->
                Card(
                    onClick = { onRepeatTypeChange(type) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (repeatType == type) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = repeatType == type, onClick = { onRepeatTypeChange(type) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(type.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        if (repeatType == RepeatType.WEEKLY || repeatType == RepeatType.CUSTOM) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("选择星期", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                (1..7).forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = { onDayToggle(day) },
                        label = { Text("周${dayNames[day % 7]}") },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = when (repeatType) {
                RepeatType.DAILY, RepeatType.EVERY_OTHER_DAY -> true
                RepeatType.WEEKLY, RepeatType.CUSTOM -> selectedDays.isNotEmpty()
            },
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存提醒", style = MaterialTheme.typography.titleMedium)
        }
    }
}
