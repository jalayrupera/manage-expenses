package com.jalay.manageexpenses.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
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

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    object Transactions : Screen("transactions", "History", Icons.Default.History)
    object Trends : Screen("trends", "Trends", Icons.Default.TrendingUp)
    object Budget : Screen("budget", "Budgets", Icons.Default.AccountBalanceWallet)

    object Categories : Screen("categories")
    object TransactionsByCategory : Screen("transactions/{category}") {
        fun createRoute(category: String) = "transactions/$category"
    }
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction_detail/$transactionId"
    }
    object AddTransaction : Screen("add_transaction")
    object Export : Screen("export")
    object CategoryRules : Screen("category_rules")
    object RecurringTransactions : Screen("recurring_transactions")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Trends,
        Screen.Budget
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = items.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = null) },
                            label = { Text(screen.title!!) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f, animationSpec = tween(200))
            }
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
                    onNavigateBack = { navController.popBackStack() },
                    showBackButton = false
                )
            }
            composable(Screen.TransactionsByCategory.route) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                TransactionsListScreen(
                    filterCategory = category,
                    onNavigateToDetail = { transactionId ->
                        navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                    },
                    onNavigateBack = { navController.popBackStack() },
                    showBackButton = true
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
                    onNavigateBack = { navController.popBackStack() },
                    showBackButton = false
                )
            }
            composable(Screen.Trends.route) {
                TrendsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    showBackButton = false
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
}
