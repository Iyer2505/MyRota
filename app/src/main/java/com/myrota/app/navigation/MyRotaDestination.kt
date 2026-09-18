package com.myrota.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class MyRotaDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {

    HOME(
        route = "home",
        label = "Home",
        icon = Icons.Default.Home
    ),

    CALENDAR(
        route = "calendar",
        label = "Calendar",
        icon = Icons.Default.CalendarMonth
    ),

    REPORTS(
        route = "reports",
        label = "Reports",
        icon = Icons.Default.Assessment
    ),

    SETTINGS(
        route = "settings",
        label = "Settings",
        icon = Icons.Default.Settings
    )
}