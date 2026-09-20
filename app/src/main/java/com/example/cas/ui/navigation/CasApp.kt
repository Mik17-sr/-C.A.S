package com.example.cas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cas.ui.home.HomeScreen
import com.example.cas.ui.home.HomeViewModel

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
                val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    state = state,
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