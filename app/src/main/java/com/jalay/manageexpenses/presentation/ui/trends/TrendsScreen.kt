package com.jalay.manageexpenses.presentation.ui.trends

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.*
import com.jalay.manageexpenses.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trends",
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
            is TrendsUiState.Loading -> {
                LoadingState(
                    modifier = Modifier.padding(paddingValues),
                    message = "Analyzing your spending..."
                )
            }

            is TrendsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    // Date Range Chips
                    item {
                        DateRangeSelector(
                            selectedRange = selectedRange,
                            onRangeSelected = viewModel::selectRange
                        )
                    }

                    // Summary Cards
                    item {
                        TrendsSummaryCards(
                            totalSpent = state.totalSpent,
                            avgDailySpend = state.avgDailySpend,
                            transactionCount = state.transactionCount,
                            spendingChange = state.spendingChange
                        )
                    }

                    // Spending Chart
                    item {
                        SpendingChart(
                            dailySpending = state.dailySpending
                        )
                    }

                    // Category Breakdown
                    item {
                        SectionHeader(title = "Spending by Category")
                    }

                    if (state.categoryBreakdown.isEmpty()) {
                        item {
                            Text(
                                text = "No spending data for this period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(state.categoryBreakdown.take(6)) { category ->
                            CategoryBreakdownItem(
                                category = category,
                                totalSpent = state.totalSpent
                            )
                        }
                    }

                    // Top Merchants
                    item {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        SectionHeader(title = "Top Merchants")
                    }

                    if (state.topMerchants.isEmpty()) {
                        item {
                            Text(
                                text = "No merchant data for this period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(state.topMerchants.take(5)) { merchant ->
                            MerchantItem(merchant = merchant)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Spacing.lg))
                    }
                }
            }

            is TrendsUiState.Error -> {
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
private fun DateRangeSelector(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        DateRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = {
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun TrendsSummaryCards(
    totalSpent: Double,
    avgDailySpend: Double,
    transactionCount: Int,
    spendingChange: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        // Total Spent Card
        ShadcnCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${FormatUtils.formatLargeAmount(totalSpent)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }

                // Change indicator
                if (spendingChange != 0f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Icon(
                            if (spendingChange > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (spendingChange > 0) ExpenseRed else IncomeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${if (spendingChange > 0) "+" else ""}${spendingChange.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (spendingChange > 0) ExpenseRed else IncomeGreen
                        )
                    }
                }
            }
        }

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ShadcnCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Text(
                        text = "Avg Daily",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${FormatUtils.formatLargeAmount(avgDailySpend)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            ShadcnCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$transactionCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingChart(
    dailySpending: List<DailySpending>
) {
    if (dailySpending.isEmpty()) return

    val maxAmount = dailySpending.maxOfOrNull { it.amount } ?: 1.0
    val chartColor = ExpenseRed

    ShadcnCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "Spending Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 16.dp.toPx()

                if (dailySpending.size < 2) return@Canvas

                val stepX = (width - padding * 2) / (dailySpending.size - 1)

                // Draw the line
                val path = Path()
                dailySpending.forEachIndexed { index, data ->
                    val x = padding + index * stepX
                    val y = height - padding - (data.amount / maxAmount * (height - padding * 2)).toFloat()

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = chartColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw dots
                dailySpending.forEachIndexed { index, data ->
                    val x = padding + index * stepX
                    val y = height - padding - (data.amount / maxAmount * (height - padding * 2)).toFloat()

                    drawCircle(
                        color = chartColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelsToShow = listOf(
                    dailySpending.firstOrNull(),
                    dailySpending.getOrNull(dailySpending.size / 2),
                    dailySpending.lastOrNull()
                ).filterNotNull().distinctBy { it.label }

                labelsToShow.forEach { data ->
                    Text(
                        text = data.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownItem(
    category: CategorySpending,
    totalSpent: Double
) {
    val percentage = if (totalSpent > 0) (category.amount / totalSpent).toFloat() else 0f

    ShadcnCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.weight(1f)
            ) {
                CategoryIconBadge(
                    category = category.category,
                    size = 40.dp
                )
                Column {
                    Text(
                        text = category.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${category.transactionCount} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${FormatUtils.formatLargeAmount(category.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MerchantItem(merchant: MerchantSpending) {
    ShadcnCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = merchant.merchantName.ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${merchant.transactionCount} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "₹${FormatUtils.formatLargeAmount(merchant.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ExpenseRed
            )
        }
    }
}
