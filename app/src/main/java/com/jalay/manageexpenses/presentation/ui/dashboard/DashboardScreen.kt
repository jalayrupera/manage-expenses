package com.jalay.manageexpenses.presentation.ui.dashboard

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.presentation.ui.components.TransactionCard
import com.jalay.manageexpenses.presentation.viewmodel.DashboardViewModel
import com.jalay.manageexpenses.presentation.viewmodel.DashboardUiState
import java.text.NumberFormat
import java.util.Locale

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
        topBar = {
            TopAppBar(
                title = { Text("Manage Expenses") },
                actions = {
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.Default.Category, "Categories")
                    }
                    IconButton(onClick = onNavigateToExport) {
                        Icon(Icons.Default.IosShare, "Export")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading data...")
                    }
                }
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadStatistics() }) {
                            Text("Retry")
                        }
                    }
                }
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Let's import your UPI transaction history from SMS messages.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This is a one-time process. Future transactions will be tracked automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartImport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import Last 30 Days")
        }
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isInitial) "Setting up your data..." else "Importing SMS...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (total > 0) {
            LinearProgressIndicator(
                progress = processed.toFloat() / total.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$processed of $total messages processed",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Scanning SMS messages...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This may take a moment. Please wait...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DashboardContent(
    modifier: Modifier = Modifier,
    statistics: com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase.Statistics,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTransactionDetail: (Long) -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Sent",
                    amount = statistics.totalSent,
                    icon = Icons.Default.ArrowUpward,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Received",
                    amount = statistics.totalReceived,
                    icon = Icons.Default.ArrowDownward,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Transactions Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToTransactions) {
                    Text("See All")
                }
            }
        }

        // Transaction List
        if (statistics.recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(statistics.recentTransactions) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onClick = { transaction.id?.let { onNavigateToTransactionDetail(it) } }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategorySummaryItem(summary: com.jalay.manageexpenses.domain.model.CategorySummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.jalay.manageexpenses.presentation.ui.components.CategoryIcon(
                    category = summary.category,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = summary.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${summary.transactionCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "₹${summary.totalAmount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ImportProgressDialog(
    processed: Int,
    total: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Importing SMS") },
        text = {
            Column {
                Text("Processing SMS messages...")
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = if (total > 0) processed.toFloat() / total.toFloat() else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("$processed / $total messages processed")
            }
        },
        confirmButton = {
            if (processed == total) {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        }
    )
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