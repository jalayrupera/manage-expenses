package com.jalay.manageexpenses.presentation.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jalay.manageexpenses.domain.model.RecurringFrequency
import com.jalay.manageexpenses.domain.model.RecurringTransaction
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.RecurringTransactionsUiState
import com.jalay.manageexpenses.presentation.viewmodel.RecurringTransactionsViewModel
import com.jalay.manageexpenses.util.FormatUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecurringTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Payments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is RecurringTransactionsUiState.Loading -> LoadingState(modifier = Modifier.padding(paddingValues))
            is RecurringTransactionsUiState.Success -> {
                if (state.transactions.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Repeat,
                        title = "No recurring payments",
                        subtitle = "Add subscriptions or recurring bills to track them",
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(state.transactions) { transaction ->
                            RecurringTransactionItem(
                                transaction = transaction,
                                onToggleStatus = { viewModel.toggleRecurringTransactionStatus(transaction) },
                                onDelete = { viewModel.deleteRecurringTransaction(transaction) }
                            )
                        }
                    }
                }
            }
            is RecurringTransactionsUiState.Error -> ErrorState(message = state.message, onRetry = {})
        }
    }

    if (showAddDialog) {
        AddRecurringDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, category, frequency, startDate, notes ->
                viewModel.addRecurringTransaction(name, amount, category, frequency, startDate, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecurringTransactionItem(
    transaction: RecurringTransaction,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    ShadcnCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(Spacing.lg)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(category = transaction.category, size = 40.dp)
            
            Spacer(modifier = Modifier.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "₹${transaction.amount} • ${transaction.frequency.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Next: ${FormatUtils.formatShortDate(transaction.nextDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Switch(
                checked = transaction.isActive,
                onCheckedChange = { onToggleStatus() }
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, RecurringFrequency, Long, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Subscription") }
    var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                // Simple category selection for now
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                
                Text("Frequency", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    RecurringFrequency.values().forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    onConfirm(name, amt, category, frequency, System.currentTimeMillis(), notes)
                },
                enabled = name.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
