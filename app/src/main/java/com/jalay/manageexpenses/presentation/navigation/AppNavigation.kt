package com.jalay.manageexpenses.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.presentation.ui.dashboard.DashboardScreen
import com.jalay.manageexpenses.presentation.ui.transactions.TransactionsListScreen
import com.jalay.manageexpenses.presentation.ui.detail.TransactionDetailScreen
import com.jalay.manageexpenses.presentation.ui.export.ExportScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction_detail/$transactionId"
    }
    object Export : Screen("export")
}

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                appContainer = appContainer,
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToExport = { navController.navigate(Screen.Export.route) }
            )
        }
        composable(Screen.Transactions.route) {
            TransactionsListScreen(
                appContainer = appContainer,
                onNavigateToDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TransactionDetail.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull() ?: 0L
            TransactionDetailScreen(
                appContainer = appContainer,
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Export.route) {
            ExportScreen(
                appContainer = appContainer,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}