package com.jalay.manageexpenses.presentation.ui.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.FilterType
import com.jalay.manageexpenses.presentation.viewmodel.SortType
import com.jalay.manageexpenses.presentation.viewmodel.TransactionsListViewModel
import com.jalay.manageexpenses.presentation.viewmodel.TransactionsListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    appContainer: AppContainer,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    filterCategory: String? = null
) {
    val viewModel: TransactionsListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TransactionsListViewModelFactory(appContainer, filterCategory)
    )
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    val title = filterCategory ?: "Transactions"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            ModernSearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    viewModel.searchTransactions(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            )

            // Filter and Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Chips
                ModernFilterChips(
                    currentFilter = (uiState as? TransactionsListUiState.Success)?.filterType
                        ?: FilterType.ALL,
                    onFilterSelected = { viewModel.filterByType(it) },
                    modifier = Modifier.weight(1f)
                )

                // Sort Button
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        val currentSort = (uiState as? TransactionsListUiState.Success)?.sortType
                            ?: SortType.DATE_DESC

                        SortMenuItem(
                            text = "Latest First",
                            isSelected = currentSort == SortType.DATE_DESC,
                            onClick = {
                                viewModel.sortBy(SortType.DATE_DESC)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Oldest First",
                            isSelected = currentSort == SortType.DATE_ASC,
                            onClick = {
                                viewModel.sortBy(SortType.DATE_ASC)
                                showSortMenu = false
                            }
                        )
                        Divider()
                        SortMenuItem(
                            text = "Highest Amount",
                            isSelected = currentSort == SortType.AMOUNT_DESC,
                            onClick = {
                                viewModel.sortBy(SortType.AMOUNT_DESC)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Lowest Amount",
                            isSelected = currentSort == SortType.AMOUNT_ASC,
                            onClick = {
                                viewModel.sortBy(SortType.AMOUNT_ASC)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Content
            when (val state = uiState) {
                is TransactionsListUiState.Initial,
                is TransactionsListUiState.Loading -> {
                    LoadingState(message = "Loading transactions...")
                }

                is TransactionsListUiState.Success -> {
                    if (state.filteredTransactions.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Receipt,
                            title = "No transactions found",
                            subtitle = if (searchQuery.isNotEmpty())
                                "Try a different search term"
                            else
                                "Your transactions will appear here"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(
                                items = state.filteredTransactions,
                                key = { it.id ?: it.hashCode() }
                            ) { transaction ->
                                TransactionCard(
                                    transaction = transaction,
                                    onClick = { onNavigateToDetail(transaction.id ?: 0) }
                                )
                            }
                        }
                    }
                }

                is TransactionsListUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
}

@Composable
private fun SortMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        onClick = onClick,
        trailingIcon = if (isSelected) {
            {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(52.dp),
        placeholder = {
            Text(
                text = "Search transactions...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(Radius.lg),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.onSurface
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFilterChips(
    currentFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        FilterType.entries.forEach { filter ->
            val isSelected = currentFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            FilterType.ALL -> "All"
                            FilterType.SENT -> "Sent"
                            FilterType.RECEIVED -> "Received"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(Radius.md),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

class TransactionsListViewModelFactory(
    private val appContainer: AppContainer,
    private val filterCategory: String? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsListViewModel(
                getTransactionsUseCase = appContainer.getGetTransactionsUseCase(appContainer.getContext()),
                searchTransactionsUseCase = appContainer.getSearchTransactionsUseCase(appContainer.getContext()),
                filterCategory = filterCategory
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
