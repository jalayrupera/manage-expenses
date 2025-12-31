package com.jalay.manageexpenses.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.CategorySummary
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrendsUiState>(TrendsUiState.Loading)
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    private val _selectedRange = MutableStateFlow(DateRange.LAST_30_DAYS)
    val selectedRange: StateFlow<DateRange> = _selectedRange.asStateFlow()

    init {
        loadTrends()
    }

    fun selectRange(range: DateRange) {
        _selectedRange.value = range
        loadTrends()
    }

    private fun loadTrends() {
        viewModelScope.launch {
            _uiState.value = TrendsUiState.Loading
            try {
                val (startTime, endTime) = getDateRange(_selectedRange.value)
                val transactions = transactionRepository.getTransactionsByDateRange(startTime, endTime).first()

                val totalSpent = transactions
                    .filter { it.transactionType == TransactionType.SENT }
                    .sumOf { it.amount }

                val totalReceived = transactions
                    .filter { it.transactionType == TransactionType.RECEIVED }
                    .sumOf { it.amount }

                // Daily spending data for the chart
                val dailySpending = getDailySpending(transactions, startTime, endTime)

                // Category breakdown
                val categoryBreakdown = getCategoryBreakdown(transactions)

                // Top merchants
                val topMerchants = getTopMerchants(transactions)

                // Spending comparison with previous period
                val previousRange = getPreviousPeriod(startTime, endTime)
                val previousTransactions = transactionRepository.getTransactionsByDateRange(
                    previousRange.first, previousRange.second
                ).first()
                val previousSpent = previousTransactions
                    .filter { it.transactionType == TransactionType.SENT }
                    .sumOf { it.amount }

                val spendingChange = if (previousSpent > 0) {
                    ((totalSpent - previousSpent) / previousSpent * 100).toFloat()
                } else {
                    0f
                }

                _uiState.value = TrendsUiState.Success(
                    totalSpent = totalSpent,
                    totalReceived = totalReceived,
                    dailySpending = dailySpending,
                    categoryBreakdown = categoryBreakdown,
                    topMerchants = topMerchants,
                    spendingChange = spendingChange,
                    transactionCount = transactions.size,
                    avgDailySpend = if (dailySpending.isNotEmpty()) totalSpent / dailySpending.size else 0.0
                )
            } catch (e: Exception) {
                _uiState.value = TrendsUiState.Error(e.message ?: "Failed to load trends")
            }
        }
    }

    private fun getDateRange(range: DateRange): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        when (range) {
            DateRange.LAST_7_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            DateRange.LAST_30_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -30)
            DateRange.LAST_90_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -90)
            DateRange.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            DateRange.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
        }

        return calendar.timeInMillis to endTime
    }

    private fun getPreviousPeriod(startTime: Long, endTime: Long): Pair<Long, Long> {
        val duration = endTime - startTime
        return (startTime - duration) to startTime
    }

    private fun getDailySpending(
        transactions: List<Transaction>,
        startTime: Long,
        endTime: Long
    ): List<DailySpending> {
        val calendar = Calendar.getInstance()
        val dayFormat = java.text.SimpleDateFormat("dd", Locale.getDefault())
        val dateFormat = java.text.SimpleDateFormat("MMM dd", Locale.getDefault())

        // Group transactions by day
        val dailyMap = mutableMapOf<String, Double>()
        
        transactions
            .filter { it.transactionType == TransactionType.SENT }
            .forEach { transaction ->
                calendar.timeInMillis = transaction.timestamp
                val dayKey = dayFormat.format(calendar.time)
                dailyMap[dayKey] = (dailyMap[dayKey] ?: 0.0) + transaction.amount
            }

        // Generate all days in range
        val result = mutableListOf<DailySpending>()
        calendar.timeInMillis = startTime
        
        while (calendar.timeInMillis <= endTime) {
            val dayKey = dayFormat.format(calendar.time)
            val label = dateFormat.format(calendar.time)
            result.add(
                DailySpending(
                    date = calendar.timeInMillis,
                    label = label,
                    amount = dailyMap[dayKey] ?: 0.0
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result.takeLast(30) // Limit to prevent too many data points
    }

    private fun getCategoryBreakdown(transactions: List<Transaction>): List<CategorySpending> {
        return transactions
            .filter { it.transactionType == TransactionType.SENT }
            .groupBy { it.category }
            .map { (category, trans) ->
                CategorySpending(
                    category = category,
                    amount = trans.sumOf { it.amount },
                    transactionCount = trans.size
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun getTopMerchants(transactions: List<Transaction>): List<MerchantSpending> {
        return transactions
            .filter { it.transactionType == TransactionType.SENT }
            .groupBy { it.recipientName }
            .map { (merchant, trans) ->
                MerchantSpending(
                    merchantName = merchant,
                    amount = trans.sumOf { it.amount },
                    transactionCount = trans.size
                )
            }
            .sortedByDescending { it.amount }
            .take(10)
    }
}

sealed class TrendsUiState {
    object Loading : TrendsUiState()
    data class Success(
        val totalSpent: Double,
        val totalReceived: Double,
        val dailySpending: List<DailySpending>,
        val categoryBreakdown: List<CategorySpending>,
        val topMerchants: List<MerchantSpending>,
        val spendingChange: Float,
        val transactionCount: Int,
        val avgDailySpend: Double
    ) : TrendsUiState()
    data class Error(val message: String) : TrendsUiState()
}

enum class DateRange(val label: String) {
    LAST_7_DAYS("7 Days"),
    LAST_30_DAYS("30 Days"),
    LAST_90_DAYS("90 Days"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month")
}

data class DailySpending(
    val date: Long,
    val label: String,
    val amount: Double
)

data class CategorySpending(
    val category: String,
    val amount: Double,
    val transactionCount: Int
)

data class MerchantSpending(
    val merchantName: String,
    val amount: Double,
    val transactionCount: Int
)
