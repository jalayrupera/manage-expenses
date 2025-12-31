package com.jalay.manageexpenses.presentation.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.TransactionDetailUiState
import com.jalay.manageexpenses.presentation.viewmodel.TransactionDetailViewModel
import com.jalay.manageexpenses.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
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
            is TransactionDetailUiState.Loading -> {
                LoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = "Loading details..."
                )
            }

            is TransactionDetailUiState.Success -> {
                state.transaction?.let { transaction ->
                    TransactionDetailContent(
                        transaction = transaction,
                        availableCategories = state.availableCategories,
                        onNotesChange = { viewModel.updateNotes(it.ifBlank { null }) },
                        onCategoryChange = { viewModel.updateCategory(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }

            is TransactionDetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExpenseRed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailContent(
    transaction: Transaction,
    availableCategories: List<String>,
    onNotesChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var notesText by remember { mutableStateOf(transaction.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val isExpense = transaction.transactionType == TransactionType.SENT
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val amountPrefix = if (isExpense) "-" else "+"

    Column(
        modifier = modifier
            .padding(Spacing.lg)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Amount Header Card
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category Icon
                CategoryIconBadge(
                    category = transaction.category,
                    size = 56.dp
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Amount
                Text(
                    text = "$amountPrefix₹${FormatUtils.formatAmount(transaction.amount)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Recipient
                Text(
                    text = transaction.recipientName.ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Status Badge
                StatusBadge(
                    text = if (isExpense) "Sent" else "Received",
                    isPositive = !isExpense
                )
            }
        }

        // Transaction Details Card
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ShadcnDivider()

                DetailRow("Date", FormatUtils.formatFullDate(transaction.timestamp))
                DetailRow("Time", FormatUtils.formatTime(transaction.timestamp))
                DetailRow("UPI App", transaction.upiApp)
                DetailRow("Reference", transaction.transactionRef ?: "N/A")
            }
        }

        // Category Picker Card
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Category Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCategoryPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.md),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                CategoryIcon(
                                    category = selectedCategory,
                                    size = 20.dp
                                )
                                Text(
                                    text = selectedCategory,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryPicker,
                        onDismissRequest = { showCategoryPicker = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        CategoryIcon(category = category, size = 20.dp)
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    onCategoryChange(category)
                                    showCategoryPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Notes Card
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = {
                        notesText = it
                        onNotesChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Add a note...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(Radius.md),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        cursorColor = MaterialTheme.colorScheme.onSurface
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Spacer at the bottom
        Spacer(modifier = Modifier.height(Spacing.lg))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


