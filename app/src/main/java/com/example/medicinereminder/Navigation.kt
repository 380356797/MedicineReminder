package com.example.medicinereminder

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.medicinereminder.ui.components.BottomNavBar
import com.example.medicinereminder.ui.health.AddHealthRecordScreen
import com.example.medicinereminder.ui.health.HealthIndicatorDetailScreen
import com.example.medicinereminder.ui.health.HealthIndicatorListScreen
import com.example.medicinereminder.ui.health.HealthScreen
import com.example.medicinereminder.ui.home.AddMedicineScreen
import com.example.medicinereminder.ui.home.HomeScreen
import com.example.medicinereminder.ui.home.MedicineListScreen
import com.example.medicinereminder.ui.home.MedicineSchedulesScreen
import com.example.medicinereminder.ui.home.AddScheduleScreen
import com.example.medicinereminder.ui.home.EditMedicineScreen
import com.example.medicinereminder.ui.profile.ProfileScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(HomeTab)
    var selectedTab by remember { mutableStateOf<NavKey>(HomeTab) }

    Scaffold(
        bottomBar = {
            if (backStack.lastOrNull() is HomeTab ||
                backStack.lastOrNull() is HealthTab ||
                backStack.lastOrNull() is ProfileTab
            ) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        backStack.clear()
                        backStack.add(tab)
                    },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<HomeTab> {
                    HomeScreen(
                        onAddMedicine = { backStack.add(AddSchedule(0)) },
                        onViewMedicines = { backStack.add(MedicineList) },
                        onMedicineClick = { backStack.add(EditMedicine(it)) },
                    )
                }
                entry<HealthTab> {
                    HealthScreen(
                        onAddRecord = { backStack.add(AddHealthRecord(it)) },
                        onViewIndicators = { backStack.add(HealthIndicatorList) },
                        onIndicatorClick = { backStack.add(HealthIndicatorDetail(it)) },
                    )
                }
                entry<ProfileTab> {
                    ProfileScreen()
                }
                entry<AddMedicine> {
                    AddMedicineScreen(onBack = { backStack.removeLastOrNull() })
                }
                entry<EditMedicine> { key ->
                    EditMedicineScreen(
                        medicineId = key.medicineId,
                        onBack = { backStack.removeLastOrNull() },
                        onManageSchedules = { backStack.add(MedicineSchedules(key.medicineId)) },
                    )
                }
                entry<MedicineList> {
                    MedicineListScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onMedicineClick = { backStack.add(EditMedicine(it)) },
                    )
                }
                entry<MedicineSchedules> { key ->
                    MedicineSchedulesScreen(
                        medicineId = key.medicineId,
                        onBack = { backStack.removeLastOrNull() },
                        onAddSchedule = { backStack.add(AddSchedule(key.medicineId)) },
                    )
                }
                entry<AddSchedule> { key ->
                    AddScheduleScreen(
                        medicineId = key.medicineId,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<HealthIndicatorDetail> { key ->
                    HealthIndicatorDetailScreen(
                        indicatorId = key.indicatorId,
                        onBack = { backStack.removeLastOrNull() },
                        onAddRecord = { backStack.add(AddHealthRecord(key.indicatorId)) },
                    )
                }
                entry<AddHealthRecord> { key ->
                    AddHealthRecordScreen(
                        indicatorId = key.indicatorId,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<HealthIndicatorList> {
                    HealthIndicatorListScreen(onBack = { backStack.removeLastOrNull() })
                }
            },
        )
    }
}
