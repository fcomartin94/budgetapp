package com.finanzapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.finanzapp.ui.screens.HomeScreen
import com.finanzapp.ui.screens.ResumenScreen
import com.finanzapp.ui.screens.TransaccionesScreen
import com.finanzapp.ui.viewmodel.BudgetViewModel

object Routes {
    const val HOME = "home"
    const val TRANSACCIONES = "transacciones"
    const val RESUMEN = "resumen"
}

@Composable
fun FinanzAppNavHost(viewModel: BudgetViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                uiState = viewModel.uiState,
                onNavigateToTransacciones = { navController.navigate(Routes.TRANSACCIONES) },
                onNavigateToResumen = { navController.navigate(Routes.RESUMEN) }
            )
        }
        composable(Routes.TRANSACCIONES) {
            TransaccionesScreen(
                uiState = viewModel.uiState,
                onRegistrarTransaccion = viewModel::registrarTransaccion,
                onEliminarTransaccion = viewModel::eliminarTransaccion,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.RESUMEN) {
            ResumenScreen(
                uiState = viewModel.uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
