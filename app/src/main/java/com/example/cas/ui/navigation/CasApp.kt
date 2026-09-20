package com.example.cas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cas.data.model.CaseStatus
import com.example.cas.ui.home.HomeCaseItem
import com.example.cas.ui.home.HomeScreen
import com.example.cas.ui.home.HomeUiState

@Composable
fun CasApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (bottomDestinations.any { it.route == currentRoute }) {
                CasBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = navigateToTab
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                // Datos provisionales: se reemplazan por el ViewModel en el paso 7
                HomeScreen(
                    state = HomeUiState(
                        activeCases = 12,
                        interviews = 28,
                        conclusions = 4,
                        investigatingCases = listOf(
                            HomeCaseItem(1, "Red de sobornos en obra pública", "12 mar 2025", 4, CaseStatus.INVESTIGATING),
                            HomeCaseItem(2, "Homicidio en zona industrial", "20 feb 2025", 1, CaseStatus.INVESTIGATING)
                        )
                    ),
                    onNewCase = { navController.navigate(Routes.NEW_CASE) },
                    onNewInterview = { navController.navigate(Routes.NEW_INTERVIEW) },
                    onCaseClick = { caseId -> navController.navigate(Routes.caseDetail(caseId)) },
                    onSeeAllCases = { navigateToTab(Routes.CASES) }
                )
            }
            composable(Routes.CASES) { PlaceholderScreen("Casos") }
            composable(Routes.SETTINGS) { PlaceholderScreen("Ajustes") }
            composable(Routes.NEW_CASE) { PlaceholderScreen("Nuevo caso") }
            composable(Routes.NEW_INTERVIEW) { PlaceholderScreen("Nueva entrevista") }
            composable(
                route = Routes.CASE_DETAIL,
                arguments = listOf(navArgument(Routes.CASE_ID_ARG) { type = NavType.LongType })
            ) {
                PlaceholderScreen("Detalle del caso")
            }
        }
    }
}