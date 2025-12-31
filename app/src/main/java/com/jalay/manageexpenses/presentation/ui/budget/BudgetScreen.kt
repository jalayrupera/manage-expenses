package com.jalay.manageexpenses.presentation.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jalay.manageexpenses.domain.model.BudgetPeriod
import com.jalay.manageexpenses.domain.model.BudgetWithSpending
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.AddEditBudgetState
import com.jalay.manageexpenses.presentation.viewmodel.BudgetUiState
import com.jalay.manageexpenses.presentation.viewmodel.BudgetViewModel
import com.jalay.manageexpenses.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val addEditState by viewModel.addEditState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Budgets",
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
                actions = {
                    IconButton(onClick = { viewModel.showAddBudget() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Budget",
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
            is BudgetUiState.Loading -> {
                LoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = "Loading budgets..."
                )
            }

            is BudgetUiState.Success -> {
                if (state.budgets.isEmpty()) {
                    EmptyBudgetsState(
                        modifier = Modifier.padding(paddingValues),
                        onAddBudget = { viewModel.showAddBudget() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        // Summary Card
                        item {
                            BudgetSummaryCard(budgets = state.budgets)
                        }

                        item {
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            SectionHeader(title = "Category Budgets")
                        }

                        items(
                            items = state.budgets,
                            key = { it.budget.id ?: it.budget.category }
                        ) { budgetWithSpending ->
                            BudgetCard(
                                budgetWithSpending = budgetWithSpending,
                                onEdit = { viewModel.showEditBudget(budgetWithSpending.budget) },
                                onDelete = { 
                                    budgetWithSpending.budget.id?.let { viewModel.deleteBudget(it) }
                                }
                            )
                        }
                    }
                }
            }

            is BudgetUiState.Error -> {
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
                    Spacer(modifier = Modifier.height(Spacing.md))
                    SecondaryButton(
                        text = "Retry",
                        onClick = { viewModel.loadBudgets() }
                    )
                }
            }
        }
    }

    // Add/Edit Budget Dialog
    when (val state = addEditState) {
        is AddEditBudgetState.Adding -> {
            AddEditBudgetDialog(
                title = "Add Budget",
                category = state.category,
                limitAmount = state.limitAmount,
                period = state.period,
                alertThreshold = state.alertThreshold,
                error = state.error,
                availableCategories = viewModel.availableCategories,
                onCategoryChange = viewModel::updateCategory,
                onLimitAmountChange = viewModel::updateLimitAmount,
                onPeriodChange = viewModel::updatePeriod,
                onAlertThresholdChange = viewModel::updateAlertThreshold,
                onSave = viewModel::saveBudget,
                onDismiss = viewModel::hideAddEdit
            )
        }
        
        is AddEditBudgetState.Editing -> {
            AddEditBudgetDialog(
                title = "Edit Budget",
                category = state.category,
                limitAmount = state.limitAmount,
                period = state.period,
                alertThreshold = state.alertThreshold,
                error = state.error,
                availableCategories = viewModel.availableCategories,
                onCategoryChange = viewModel::updateCategory,
                onLimitAmountChange = viewModel::updateLimitAmount,
                onPeriodChange = viewModel::updatePeriod,
                onAlertThresholdChange = viewModel::updateAlertThreshold,
                onSave = viewModel::saveBudget,
                onDismiss = viewModel::hideAddEdit,
                isCategoryEditable = false
            )
        }
        
        AddEditBudgetState.Hidden -> {}
    }
}

@Composable
private fun EmptyBudgetsState(
    modifier: Modifier = Modifier,
    onAddBudget: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconBadge(
            icon = Icons.Default.Savings,
            size = 64.dp,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        Text(
            text = "No Budgets Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = "Set spending limits for categories to track your budget",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        PrimaryButton(
            text = "Create Budget",
            onClick = onAddBudget
        )
    }
}

@Composable
private fun BudgetSummaryCard(budgets: List<BudgetWithSpending>) {
    val totalBudget = budgets.sumOf { it.budget.limitAmount }
    val totalSpent = budgets.sumOf { it.currentSpending }
    val overBudgetCount = budgets.count { it.isOverBudget }

    ShadcnCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "Monthly Overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${FormatUtils.formatLargeAmount(totalBudget)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${FormatUtils.formatLargeAmount(totalSpent)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (totalSpent > totalBudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (overBudgetCount > 0) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = ExpenseRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$overBudgetCount ${if (overBudgetCount == 1) "category" else "categories"} over budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpenseRed
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCard(
    budgetWithSpending: BudgetWithSpending,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val budget = budgetWithSpending.budget
    val progressColor = when {
        budgetWithSpending.isOverBudget -> ExpenseRed
        budgetWithSpending.percentageUsed >= 0.8f -> WarningOrange
        else -> IncomeGreen
    }

    var showMenu by remember { mutableStateOf(false) }

    ShadcnCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    CategoryIconBadge(
                        category = budget.category,
                        size = 44.dp
                    )
                    Column {
                        Text(
                            text = budget.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = budget.period.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ExpenseRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Progress bar
            LinearProgressIndicator(
                progress = budgetWithSpending.percentageUsed.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(Radius.full)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${FormatUtils.formatLargeAmount(budgetWithSpending.currentSpending)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = progressColor
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(budgetWithSpending.percentageUsed * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${FormatUtils.formatLargeAmount(budget.limitAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (budgetWithSpending.isOverBudget) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = ExpenseRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Over budget by ₹${FormatUtils.formatLargeAmount(budgetWithSpending.currentSpending - budget.limitAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpenseRed
                    )
                }
            } else if (budgetWithSpending.remainingAmount > 0) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "₹${FormatUtils.formatLargeAmount(budgetWithSpending.remainingAmount)} remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditBudgetDialog(
    title: String,
    category: String,
    limitAmount: String,
    period: BudgetPeriod,
    alertThreshold: Float,
    error: String?,
    availableCategories: List<String>,
    onCategoryChange: (String) -> Unit,
    onLimitAmountChange: (String) -> Unit,
    onPeriodChange: (BudgetPeriod) -> Unit,
    onAlertThresholdChange: (Float) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isCategoryEditable: Boolean = true
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                // Error message
                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpenseRed
                    )
                }

                // Category selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box {
                    OutlinedButton(
                        onClick = { if (isCategoryEditable) showCategoryDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.md),
                        enabled = isCategoryEditable
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category)
                            if (isCategoryEditable) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    onCategoryChange(cat)
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Amount input
                Text(
                    text = "Budget Limit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = limitAmount,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onLimitAmountChange(value)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(Radius.md)
                )

                // Period selector
                Text(
                    text = "Period",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    FilterChip(
                        selected = period == BudgetPeriod.MONTHLY,
                        onClick = { onPeriodChange(BudgetPeriod.MONTHLY) },
                        label = { Text("Monthly") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = period == BudgetPeriod.WEEKLY,
                        onClick = { onPeriodChange(BudgetPeriod.WEEKLY) },
                        label = { Text("Weekly") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Alert threshold slider
                Text(
                    text = "Alert at ${(alertThreshold * 100).toInt()}% spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = alertThreshold,
                    onValueChange = onAlertThresholdChange,
                    valueRange = 0.5f..1f,
                    steps = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Add a warning orange color to theme
private val WarningOrange = androidx.compose.ui.graphics.Color(0xFFF59E0B)
