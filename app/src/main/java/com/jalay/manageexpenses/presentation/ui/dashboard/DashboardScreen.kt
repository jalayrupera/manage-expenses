package com.jalay.manageexpenses.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.presentation.ui.components.SummaryCard
import com.jalay.manageexpenses.presentation.ui.components.TransactionCard
import com.jalay.manageexpenses.presentation.viewmodel.DashboardViewModel
import com.jalay.manageexpenses.presentation.viewmodel.DashboardUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    appContainer: AppContainer,
    onNavigateToTransactions: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DashboardViewModelFactory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.importHistoricalSms(30) }) {
                        Icon(Icons.Default.Refresh, "Import SMS")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToExport) {
                Icon(Icons.Default.IosShare, contentDescription = "Export")
            }
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
                    CircularProgressIndicator()
                }
            }
            is DashboardUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                title = "Total Sent",
                                value = "₹${state.statistics.totalSent}",
                                icon = Icons.Default.ArrowUpward,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToTransactions
                            )
                            SummaryCard(
                                title = "Total Received",
                                value = "₹${state.statistics.totalReceived}",
                                icon = Icons.Default.ArrowDownward,
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToTransactions
                            )
                        }
                    }
                    item {
                        SummaryCard(
                            title = "Net Balance",
                            value = "₹${state.statistics.netBalance}",
                            icon = Icons.Default.AccountBalance,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Top Categories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(state.statistics.categorySummaries.take(5)) { category ->
                        CategorySummaryItem(category)
                    }
                    item {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            is DashboardUiState.Importing -> {
                ImportProgressDialog(
                    processed = state.processed,
                    total = state.total,
                    onDismiss = { viewModel.loadStatistics() }
                )
            }
            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
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
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                getStatisticsUseCase = appContainer.getGetStatisticsUseCase(appContainer.getContext()),
                importHistoricalSmsUseCase = appContainer.getImportHistoricalSmsUseCase(appContainer.getContext())
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}