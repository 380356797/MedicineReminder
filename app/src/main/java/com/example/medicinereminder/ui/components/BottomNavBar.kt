package com.example.medicinereminder.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.example.medicinereminder.HealthTab
import com.example.medicinereminder.HomeTab
import com.example.medicinereminder.ProfileTab

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val key: NavKey,
)

val bottomNavItems = listOf(
    BottomNavItem("今日吃药", Icons.Filled.Home, Icons.Outlined.Home, HomeTab),
    BottomNavItem("健康记录", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, HealthTab),
    BottomNavItem("我的", Icons.Filled.Person, Icons.Outlined.Person, ProfileTab),
)

@Composable
fun BottomNavBar(
    selectedTab: NavKey,
    onTabSelected: (NavKey) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = selectedTab == item.key
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = { onTabSelected(item.key) },
            )
        }
    }
}
