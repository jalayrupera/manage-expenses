package com.jalay.manageexpenses.presentation.ui.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.DashboardViewModel
import com.jalay.manageexpenses.presentation.viewmodel.DashboardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    appContainer: AppContainer,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit
) {
    val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DashboardViewModelFactory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    onStartImport = { viewModel.performInitialImport(30) }
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
                DashboardContent(
                    modifier = Modifier.padding(paddingValues),
                    statistics = state.statistics,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToTransactionDetail = onNavigateToTransactionDetail
                )
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
            text = "Import Last 30 Days",
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
                progress = processed.toFloat() / total.toFloat(),
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
    statistics: com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase.Statistics,
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

@Composable
private fun ErrorState(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconBadge(
            icon = Icons.Default.ErrorOutline,
            size = 64.dp,
            backgroundColor = ExpenseRed.copy(alpha = 0.1f),
            iconColor = ExpenseRed
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        SecondaryButton(
            text = "Try Again",
            onClick = onRetry
        )
    }
}

private fun formatLargeAmount(amount: Double): String {
    return when {
        amount >= 10_00_000 -> String.format("%.1fL", amount / 100000)
        amount >= 1000 -> String.format("%,.0f", amount)
        else -> String.format("%.2f", amount)
    }
}

class DashboardViewModelFactory(
    private val appContainer: AppContainer
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            val context = appContainer.getContext()
            val sharedPreferences = context.getSharedPreferences("manage_expenses_prefs", Context.MODE_PRIVATE)
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                getStatisticsUseCase = appContainer.getGetStatisticsUseCase(context),
                importHistoricalSmsUseCase = appContainer.getImportHistoricalSmsUseCase(context),
                sharedPreferences = sharedPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
