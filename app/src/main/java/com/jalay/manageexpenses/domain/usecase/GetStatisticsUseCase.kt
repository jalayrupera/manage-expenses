package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.CategorySummary
import com.jalay.manageexpenses.domain.model.MonthlySummary
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import java.util.Calendar

class GetStatisticsUseCase(
    private val repository: TransactionRepository
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

        return Statistics(
            totalSent = totalSent,
            totalReceived = totalReceived,
            netBalance = totalReceived - totalSent,
            categorySummaries = categorySummaries,
            monthlySummaries = monthlySummaries
        )
    }

    private fun getMonthlySummaries(transactions: List<Transaction>): List<MonthlySummary> {
        val calendar = Calendar.getInstance()
        return transactions
            .groupBy {
                calendar.timeInMillis = it.timestamp
                "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
            }
            .map { (month, trans) ->
                val parts = month.split("-")
                val monthLabel = getMonthName(parts[1].toInt()) + " ${parts[0]}"

                MonthlySummary(
                    month = monthLabel,
                    sentAmount = trans.filter { it.transactionType == TransactionType.SENT }.sumOf { it.amount },
                    receivedAmount = trans.filter { it.transactionType == TransactionType.RECEIVED }.sumOf { it.amount },
                    transactionCount = trans.size
                )
            }
            .sortedBy { it.month }
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
        val monthlySummaries: List<MonthlySummary>
    )
}