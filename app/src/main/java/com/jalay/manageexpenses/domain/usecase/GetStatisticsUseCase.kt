package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.BudgetRepository
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.BudgetWithSpending
import com.jalay.manageexpenses.domain.model.CategorySummary
import com.jalay.manageexpenses.domain.model.MonthlySummary
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import java.util.Calendar

class GetStatisticsUseCase(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetRepository? = null
) {
    suspend operator fun invoke(): Statistics {
        val transactions = repository.getAllTransactions().first()

        val totalSent = transactions
            .filter { it.transactionType == TransactionType.SENT }
            .sumOf { it.amount }

        val totalReceived = transactions
            .filter { it.transactionType == TransactionType.RECEIVED }
            .sumOf { it.amount }

        val categorySummaries = transactions
            .groupBy { it.category }
            .map { (category, trans) ->
                val sentAmount = trans
                    .filter { it.transactionType == TransactionType.SENT }
                    .sumOf { it.amount }
                val receivedAmount = trans
                    .filter { it.transactionType == TransactionType.RECEIVED }
                    .sumOf { it.amount }

                CategorySummary(
                    category = category,
                    icon = getCategoryIcon(category),
                    totalAmount = trans.sumOf { it.amount },
                    transactionCount = trans.size,
                    sentAmount = sentAmount,
                    receivedAmount = receivedAmount
                )
            }
            .sortedByDescending { it.totalAmount }

        val monthlySummaries = getMonthlySummaries(transactions)

        // Get recent transactions sorted by timestamp (most recent first)
        val recentTransactions = transactions
            .sortedByDescending { it.timestamp }
            .take(50)

        // Calculate insights for current month
        val insights = calculateInsights(transactions)

        // Get budget alerts (budgets that are near limit or over budget)
        val budgetAlerts = budgetRepository?.let { repo ->
            try {
                repo.getBudgetsWithSpending()
                    .filter { it.isNearLimit || it.isOverBudget }
                    .sortedByDescending { it.percentageUsed }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return Statistics(
            totalSent = totalSent,
            totalReceived = totalReceived,
            netBalance = totalReceived - totalSent,
            categorySummaries = categorySummaries,
            monthlySummaries = monthlySummaries,
            recentTransactions = recentTransactions,
            insights = insights,
            budgetAlerts = budgetAlerts
        )
    }

    private fun calculateInsights(transactions: List<Transaction>): Insights {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Filter to current month's sent transactions
        val currentMonthTransactions = transactions.filter { transaction ->
            calendar.timeInMillis = transaction.timestamp
            calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.YEAR) == currentYear &&
                    transaction.transactionType == TransactionType.SENT
        }

        // Top merchant by total amount spent
        val topMerchant = currentMonthTransactions
            .groupBy { it.recipientName }
            .mapValues { (_, trans) -> trans.sumOf { it.amount } }
            .maxByOrNull { it.value }
            ?.let { MerchantInsight(it.key, it.value) }

        // Most frequent category by transaction count
        val frequentCategory = currentMonthTransactions
            .groupBy { it.category }
            .mapValues { (_, trans) -> trans.size }
            .maxByOrNull { it.value }
            ?.let { CategoryInsight(it.key, it.value) }

        // Average daily spend this month
        val calendar2 = Calendar.getInstance()
        val dayOfMonth = calendar2.get(Calendar.DAY_OF_MONTH)
        val totalSpentThisMonth = currentMonthTransactions.sumOf { it.amount }
        val avgDailySpend = if (dayOfMonth > 0) totalSpentThisMonth / dayOfMonth else 0.0

        return Insights(
            topMerchant = topMerchant,
            frequentCategory = frequentCategory,
            avgDailySpend = avgDailySpend,
            totalSpentThisMonth = totalSpentThisMonth,
            transactionsThisMonth = currentMonthTransactions.size
        )
    }

    private fun getMonthlySummaries(transactions: List<Transaction>): List<MonthlySummary> {
        val calendar = Calendar.getInstance()
        return transactions
            .groupBy {
                calendar.timeInMillis = it.timestamp
                // Use numeric key for proper chronological sorting: YYYYMM format
                calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
            }
            .toSortedMap() // Sort by numeric year-month key (e.g., 202401, 202402)
            .map { (yearMonth, trans) ->
                val year = yearMonth / 100
                val month = yearMonth % 100
                val monthLabel = "${getMonthName(month)} $year"

                MonthlySummary(
                    month = monthLabel,
                    sentAmount = trans.filter { it.transactionType == TransactionType.SENT }.sumOf { it.amount },
                    receivedAmount = trans.filter { it.transactionType == TransactionType.RECEIVED }.sumOf { it.amount },
                    transactionCount = trans.size
                )
            }
            .takeLast(6)
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return months[month - 1]
    }

    private fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "shopping" -> "shopping_bag"
            "food & dining" -> "restaurant"
            "transport" -> "directions_car"
            "utilities" -> "bolt"
            "entertainment" -> "movie"
            "bills & recharges" -> "phone_android"
            "transfers" -> "account_balance"
            "upi" -> "payment"
            else -> "category"
        }
    }

    data class Statistics(
        val totalSent: Double,
        val totalReceived: Double,
        val netBalance: Double,
        val categorySummaries: List<CategorySummary>,
        val monthlySummaries: List<MonthlySummary>,
        val recentTransactions: List<Transaction>,
        val insights: Insights = Insights(),
        val budgetAlerts: List<BudgetWithSpending> = emptyList()
    )

    data class Insights(
        val topMerchant: MerchantInsight? = null,
        val frequentCategory: CategoryInsight? = null,
        val avgDailySpend: Double = 0.0,
        val totalSpentThisMonth: Double = 0.0,
        val transactionsThisMonth: Int = 0
    )

    data class MerchantInsight(
        val name: String,
        val amount: Double
    )

    data class CategoryInsight(
        val name: String,
        val transactionCount: Int
    )
}