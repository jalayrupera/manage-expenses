package com.jalay.manageexpenses.presentation.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.DashboardViewModel
import com.jalay.manageexpenses.presentation.viewmodel.DashboardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTrends: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (uiState is DashboardUiState.Success) {
                FloatingActionButton(
                    onClick = onNavigateToAddTransaction,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Transaction"
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToTrends) {
                        Icon(
                            Icons.Outlined.TrendingUp,
                            contentDescription = "Trends",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToRecurring) {
                        Icon(
                            Icons.Outlined.Repeat,
                            contentDescription = "Recurring",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToBudget) {
                        Icon(
                            Icons.Outlined.Savings,
                            contentDescription = "Budgets",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(
                            Icons.Outlined.Category,
                            contentDescription = "Categories",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToExport) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = "Export",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                LoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = "Loading your data..."
                )
            }

            is DashboardUiState.InitialSetup -> {
                InitialSetupScreen(
                    modifier = Modifier.padding(paddingValues),
                    onStartImport = { viewModel.performInitialImport(null) }
                )
            }

            is DashboardUiState.Importing -> {
                ImportingScreen(
                    modifier = Modifier.padding(paddingValues),
                    processed = state.processed,
                    total = state.total,
                    isInitial = state.isInitial
                )
            }

            is DashboardUiState.Success -> {
                var isRefreshing by remember { mutableStateOf(false) }
                val pullToRefreshState = rememberPullToRefreshState()

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = {
                        isRefreshing = true
                        viewModel.loadStatistics()
                        isRefreshing = false
                    },
                    modifier = Modifier.padding(paddingValues)
                ) {
                    DashboardContent(
                        statistics = state.statistics,
                        onNavigateToTransactions = onNavigateToTransactions,
                        onNavigateToTransactionDetail = onNavigateToTransactionDetail
                    )
                }
            }

            is DashboardUiState.Error -> {
                ErrorState(
                    modifier = Modifier.padding(paddingValues),
                    message = state.message,
                    onRetry = { viewModel.loadStatistics() }
                )
            }
        }
    }
}

@Composable
private fun InitialSetupScreen(
    modifier: Modifier = Modifier,
    onStartImport: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(Radius.xl))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = "Welcome",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        Text(
            text = "Import your UPI transaction history from SMS messages to get started.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "This is a one-time process. Future transactions will be tracked automatically.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        PrimaryButton(
            text = "Import All History",
            onClick = onStartImport,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ImportingScreen(
    modifier: Modifier = Modifier,
    processed: Int,
    total: Int,
    isInitial: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = if (isInitial) "Setting up..." else "Importing...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        if (total > 0) {
            // Progress bar
            LinearProgressIndicator(
                progress = { processed.toFloat() / total.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(Radius.full)),
                color = MaterialTheme.colorScheme.onSurface,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "$processed of $total messages",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Scanning SMS messages...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DashboardContent(
    modifier: Modifier = Modifier,
    statistics: GetStatisticsUseCase.Statistics,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Balance Overview Card
        item {
            BalanceOverviewCard(
                totalSent = statistics.totalSent,
                totalReceived = statistics.totalReceived
            )
        }

        // Summary Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StatCard(
                    title = "Spent",
                    value = "₹${formatLargeAmount(statistics.totalSent)}",
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f),
                    valueColor = ExpenseRed,
                    iconBackgroundColor = ExpenseRed.copy(alpha = 0.1f),
                    iconColor = ExpenseRed
                )
                StatCard(
                    title = "Received",
                    value = "₹${formatLargeAmount(statistics.totalReceived)}",
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f),
                    valueColor = IncomeGreen,
                    iconBackgroundColor = IncomeGreen.copy(alpha = 0.1f),
                    iconColor = IncomeGreen
                )
            }
        }

        // Monthly Insights Section
        if (statistics.insights.transactionsThisMonth > 0) {
            item {
                SectionHeader(title = "This Month's Insights")
            }

            item {
                InsightsGrid(insights = statistics.insights)
            }
        }

        // Recent Transactions Header
        item {
            SectionHeader(
                title = "Recent Transactions",
                action = {
                    TextButton(onClick = onNavigateToTransactions) {
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        // Transaction List or Empty State
        if (statistics.recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Receipt,
                    title = "No transactions yet",
                    subtitle = "Your recent transactions will appear here"
                )
            }
        } else {
            items(
                items = statistics.recentTransactions,
                key = { it.id ?: it.hashCode() }
            ) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onClick = { transaction.id?.let { onNavigateToTransactionDetail(it) } }
                )
            }
        }
    }
}

@Composable
private fun BalanceOverviewCard(
    totalSent: Double,
    totalReceived: Double
) {
    val netBalance = totalReceived - totalSent
    val isPositive = netBalance >= 0

    ShadcnCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl)
        ) {
            Text(
                text = "Net Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isPositive) "+" else "-"}₹${formatLargeAmount(kotlin.math.abs(netBalance))}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) IncomeGreen else ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            ShadcnDivider()

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${formatLargeAmount(totalSent)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Received",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${formatLargeAmount(totalReceived)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen
                    )
                }
            }
        }
    }
}

private fun formatLargeAmount(amount: Double): String {
    return when {
        amount >= 10_00_000 -> String.format("%.1fL", amount / 100000)
        amount >= 1000 -> String.format("%,.0f", amount)
        else -> String.format("%.2f", amount)
    }
}

@Composable
private fun InsightsGrid(
    insights: GetStatisticsUseCase.Insights
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Row 1: Avg Daily Spend and Total This Month
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            InsightCard(
                title = "Avg Daily",
                value = "₹${formatLargeAmount(insights.avgDailySpend)}",
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.weight(1f)
            )
            InsightCard(
                title = "This Month",
                value = "₹${formatLargeAmount(insights.totalSpentThisMonth)}",
                icon = Icons.Default.AccountBalanceWallet,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Top Merchant and Frequent Category
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            insights.topMerchant?.let { merchant ->
                InsightCard(
                    title = "Top Merchant",
                    value = merchant.name.take(15) + if (merchant.name.length > 15) "..." else "",
                    subtitle = "₹${formatLargeAmount(merchant.amount)}",
                    icon = Icons.Default.Store,
                    modifier = Modifier.weight(1f)
                )
            } ?: InsightCard(
                title = "Top Merchant",
                value = "No data",
                icon = Icons.Default.Store,
                modifier = Modifier.weight(1f)
            )

            insights.frequentCategory?.let { category ->
                InsightCard(
                    title = "Top Category",
                    value = category.name.take(12) + if (category.name.length > 12) "..." else "",
                    subtitle = "${category.transactionCount} transactions",
                    icon = Icons.Default.Category,
                    modifier = Modifier.weight(1f)
                )
            } ?: InsightCard(
                title = "Top Category",
                value = "No data",
                icon = Icons.Default.Category,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    ShadcnCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
