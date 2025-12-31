package com.jalay.manageexpenses.presentation.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.util.FormatUtils
import java.util.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.jalay.manageexpenses.domain.model.SortType
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.FilterType
import com.jalay.manageexpenses.presentation.viewmodel.TransactionsListViewModel
import com.jalay.manageexpenses.presentation.viewmodel.TransactionListItem
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    filterCategory: String? = null,
    showBackButton: Boolean = true,
    viewModel: TransactionsListViewModel = hiltViewModel()
) {
    val transactionsPaged = viewModel.transactionsPaged.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val sortType by viewModel.sortType.collectAsState()

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
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                    currentFilter = filterType,
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
                        SortMenuItem(
                            text = "Latest First",
                            isSelected = sortType == SortType.DATE_DESC,
                            onClick = {
                                viewModel.sortBy(SortType.DATE_DESC)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Oldest First",
                            isSelected = sortType == SortType.DATE_ASC,
                            onClick = {
                                viewModel.sortBy(SortType.DATE_ASC)
                                showSortMenu = false
                            }
                        )
                        HorizontalDivider()
                        SortMenuItem(
                            text = "Highest Amount",
                            isSelected = sortType == SortType.AMOUNT_DESC,
                            onClick = {
                                viewModel.sortBy(SortType.AMOUNT_DESC)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Lowest Amount",
                            isSelected = sortType == SortType.AMOUNT_ASC,
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
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md)
                ) {
                    items(
                        count = transactionsPaged.itemCount,
                        key = transactionsPaged.itemKey { item ->
                            when (item) {
                                is TransactionListItem.TransactionItem -> item.transaction.id ?: item.hashCode()
                                is TransactionListItem.HeaderItem -> item.date
                            }
                        },
                        contentType = transactionsPaged.itemContentType { "transaction" }
                    ) { index ->
                        val item = transactionsPaged[index]
                        when (item) {
                            is TransactionListItem.TransactionItem -> {
                                TransactionCard(
                                    transaction = item.transaction,
                                    onClick = { onNavigateToDetail(item.transaction.id ?: 0) },
                                    modifier = Modifier.padding(vertical = Spacing.xs)
                                )
                            }
                            is TransactionListItem.HeaderItem -> {
                                DateStickyHeader(dateHeader = item.date)
                            }
                            null -> {
                                // Placeholder if needed
                            }
                        }
                    }

                    // Handle loading and error states within LazyColumn
                    transactionsPaged.apply {
                        when {
                            loadState.refresh is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            loadState.append is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.md),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            loadState.refresh is LoadState.Error -> {
                                val e = transactionsPaged.loadState.refresh as LoadState.Error
                                item {
                                    ErrorState(
                                        message = e.error.localizedMessage ?: "Unknown Error",
                                        onRetry = { retry() }
                                    )
                                }
                            }
                            loadState.refresh is LoadState.NotLoading && transactionsPaged.itemCount == 0 -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        EmptyState(
                                            icon = Icons.Default.Receipt,
                                            title = "No transactions found",
                                            subtitle = if (searchQuery.isNotEmpty())
                                                "Try a different search term"
                                            else
                                                "Your transactions will appear here"
                                        )
                                    }
                                }
                            }
                        }
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
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DateStickyHeader(dateHeader: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = Spacing.sm)
    ) {
        Text(
            text = dateHeader,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
