package com.jalay.manageexpenses.presentation.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.domain.model.CategorySummary
import com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    appContainer: AppContainer,
    onNavigateBack: () -> Unit,
    onNavigateToCategory: (String) -> Unit
) {
    val viewModel: CategoriesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CategoriesViewModelFactory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Categories",
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
            is CategoriesUiState.Loading -> {
                LoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = "Loading categories..."
                )
            }

            is CategoriesUiState.Success -> {
                if (state.categories.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Category,
                        title = "No categories yet",
                        subtitle = "Your spending categories will appear here",
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
                        // Calculate total for progress bars
                        val totalSpent = state.categories.sumOf { it.sentAmount }

                        items(
                            items = state.categories,
                            key = { it.category }
                        ) { category ->
                            CategoryCard(
                                category = category,
                                totalSpent = totalSpent,
                                onClick = { onNavigateToCategory(category.category) }
                            )
                        }
                    }
                }
            }

            is CategoriesUiState.Error -> {
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
private fun CategoryCard(
    category: CategorySummary,
    totalSpent: Double,
    onClick: () -> Unit
) {
    val progress = if (totalSpent > 0) (category.sentAmount / totalSpent).toFloat() else 0f

    ShadcnCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryIconBadge(
                        category = category.category,
                        size = 44.dp
                    )

                    Spacer(modifier = Modifier.width(Spacing.md))

                    Column {
                        Text(
                            text = category.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "${category.transactionCount} transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${formatAmount(category.totalAmount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (category.sentAmount > 0) {
                            Text(
                                text = "-₹${formatAmount(category.sentAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(Spacing.sm))

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Progress bar (only if there's spending data)
            if (category.sentAmount > 0 && totalSpent > 0) {
                Spacer(modifier = Modifier.height(Spacing.md))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = ExpenseRed.copy(alpha = 0.8f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "${(progress * 100).toInt()}% of total spending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return when {
        amount >= 10_00_000 -> String.format("%.1fL", amount / 100000)
        amount >= 1000 -> String.format("%,.0f", amount)
        else -> String.format("%.2f", amount)
    }
}

class CategoriesViewModel(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val statistics = getStatisticsUseCase()
                // Sort by sent amount (highest spending first)
                val sortedCategories = statistics.categorySummaries
                    .sortedByDescending { it.sentAmount }
                _uiState.value = CategoriesUiState.Success(sortedCategories)
            } catch (e: Exception) {
                _uiState.value = CategoriesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CategoriesUiState {
    object Loading : CategoriesUiState()
    data class Success(val categories: List<CategorySummary>) : CategoriesUiState()
    data class Error(val message: String) : CategoriesUiState()
}

class CategoriesViewModelFactory(
    private val appContainer: AppContainer
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriesViewModel(
                getStatisticsUseCase = appContainer.getGetStatisticsUseCase(appContainer.getContext())
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
