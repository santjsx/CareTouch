package com.example.amma

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.amma.ui.admin.AdminDashboardScreen
import com.example.amma.ui.home.HomeScreen

@Composable
fun MainNavigation(initialKey: Any = HomeNavKey) {
    val backStack = rememberNavBackStack(initialKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeNavKey> {
                HomeScreen(
                    onNavigateToAdmin = {
                        backStack.add(AdminNavKey)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<AdminNavKey> {
                AdminDashboardScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
