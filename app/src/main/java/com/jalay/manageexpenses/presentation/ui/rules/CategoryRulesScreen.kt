package com.jalay.manageexpenses.presentation.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jalay.manageexpenses.domain.model.CategoryMapping
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.CategoryRulesUiState
import com.jalay.manageexpenses.presentation.viewmodel.CategoryRulesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryRulesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryRulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<CategoryMapping?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Category Rules",
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
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Reset to Defaults",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CategoryRulesUiState.Loading -> {
                LoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = "Loading rules..."
                )
            }

            is CategoryRulesUiState.Success -> {
                CategoryRulesContent(
                    modifier = Modifier.padding(paddingValues),
                    groupedRules = state.groupedRules,
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::selectCategory,
                    onEditRule = { showEditDialog = it },
                    onDeleteRule = viewModel::deleteRule
                )
            }

            is CategoryRulesUiState.Error -> {
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

    // Add Rule Dialog
    if (showAddDialog && uiState is CategoryRulesUiState.Success) {
        AddEditRuleDialog(
            rule = null,
            availableCategories = (uiState as CategoryRulesUiState.Success).availableCategories,
            onDismiss = { showAddDialog = false },
            onSave = { keyword, category ->
                viewModel.addRule(keyword, category)
                showAddDialog = false
            }
        )
    }

    // Edit Rule Dialog
    showEditDialog?.let { rule ->
        if (uiState is CategoryRulesUiState.Success) {
            AddEditRuleDialog(
                rule = rule,
                availableCategories = (uiState as CategoryRulesUiState.Success).availableCategories,
                onDismiss = { showEditDialog = null },
                onSave = { keyword, category ->
                    viewModel.updateRule(rule, keyword, category)
                    showEditDialog = null
                }
            )
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Defaults") },
            text = { Text("This will remove all custom rules and restore the default category mappings. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryRulesContent(
    modifier: Modifier = Modifier,
    groupedRules: Map<String, List<CategoryMapping>>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onEditRule: (CategoryMapping) -> Unit,
    onDeleteRule: (CategoryMapping) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Info Card
        item {
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "How Rules Work",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keywords are matched against merchant names. When a match is found, the transaction is automatically categorized.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Category Sections
        groupedRules.forEach { (category, rules) ->
            item {
                CategoryHeader(
                    category = category,
                    ruleCount = rules.size,
                    isExpanded = selectedCategory == category || selectedCategory == null,
                    onClick = {
                        onCategorySelected(if (selectedCategory == category) null else category)
                    }
                )
            }

            if (selectedCategory == category || selectedCategory == null) {
                items(rules, key = { it.id ?: it.keyword }) { rule ->
                    RuleItem(
                        rule = rule,
                        onEdit = { onEditRule(rule) },
                        onDelete = { onDeleteRule(rule) }
                    )
                }
            }
        }

        // Empty state
        if (groupedRules.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Rule,
                    title = "No rules configured",
                    subtitle = "Add rules to automatically categorize your transactions"
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    category: String,
    ruleCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            CategoryIconBadge(category = category, size = 32.dp)
            Column {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$ruleCount rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RuleItem(
    rule: CategoryMapping,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ShadcnCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.xl)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.keyword,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (rule.isCustom) {
                    Text(
                        text = "Custom rule",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Rule") },
            text = { Text("Are you sure you want to delete the rule for \"${rule.keyword}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditRuleDialog(
    rule: CategoryMapping?,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (keyword: String, category: String) -> Unit
) {
    var keyword by remember { mutableStateOf(rule?.keyword ?: "") }
    var selectedCategory by remember { mutableStateOf(rule?.category ?: availableCategories.first()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val isValid = keyword.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (rule == null) "Add Rule" else "Edit Rule") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Keyword Input
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("Keyword") },
                    placeholder = { Text("e.g., amazon, zomato") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md)
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(Radius.md)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        CategoryIconBadge(category = category, size = 24.dp)
                                        Text(category)
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "When a merchant name contains this keyword, the transaction will be categorized as \"$selectedCategory\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(keyword, selectedCategory) },
                enabled = isValid
            ) {
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
