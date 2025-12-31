package com.jalay.manageexpenses.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jalay.manageexpenses.presentation.ui.addtransaction.AddTransactionScreen
import com.jalay.manageexpenses.presentation.ui.budget.BudgetScreen
import com.jalay.manageexpenses.presentation.ui.categories.CategoriesScreen
import com.jalay.manageexpenses.presentation.ui.dashboard.DashboardScreen
import com.jalay.manageexpenses.presentation.ui.detail.TransactionDetailScreen
import com.jalay.manageexpenses.presentation.ui.export.ExportScreen
import com.jalay.manageexpenses.presentation.ui.recurring.RecurringTransactionsScreen
import com.jalay.manageexpenses.presentation.ui.rules.CategoryRulesScreen
import com.jalay.manageexpenses.presentation.ui.transactions.TransactionsListScreen
import com.jalay.manageexpenses.presentation.ui.trends.TrendsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Categories : Screen("categories")
    object Transactions : Screen("transactions")
    object TransactionsByCategory : Screen("transactions/{category}") {
        fun createRoute(category: String) = "transactions/$category"
    }
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction_detail/$transactionId"
    }
    object AddTransaction : Screen("add_transaction")
    object Budget : Screen("budget")
    object Export : Screen("export")
    object Trends : Screen("trends")
    object CategoryRules : Screen("category_rules")
    object RecurringTransactions : Screen("recurring_transactions")
}

@Composable
fun AppNavigation(
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
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToExport = { navController.navigate(Screen.Export.route) },
                onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToTrends = { navController.navigate(Screen.Trends.route) },
                onNavigateToRecurring = { navController.navigate(Screen.RecurringTransactions.route) },
                onNavigateToTransactionDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                }
            )
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategory = { category ->
                    navController.navigate(Screen.TransactionsByCategory.createRoute(category))
                },
                onNavigateToCategoryRules = { navController.navigate(Screen.CategoryRules.route) }
            )
        }
        composable(Screen.Transactions.route) {
            TransactionsListScreen(
                onNavigateToDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TransactionsByCategory.route) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            TransactionsListScreen(
                filterCategory = category,
                onNavigateToDetail = { transactionId ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TransactionDetail.route) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull() ?: 0L
            TransactionDetailScreen(
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Export.route) {
            ExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionAdded = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Budget.route) {
            BudgetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Trends.route) {
            TrendsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CategoryRules.route) {
            CategoryRulesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.RecurringTransactions.route) {
            RecurringTransactionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
