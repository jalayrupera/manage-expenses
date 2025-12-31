package com.jalay.manageexpenses.data.repository

import com.jalay.manageexpenses.data.local.dao.BudgetDao
import com.jalay.manageexpenses.data.local.dao.TransactionDao
import com.jalay.manageexpenses.data.local.entity.BudgetEntity
import com.jalay.manageexpenses.domain.model.Budget
import com.jalay.manageexpenses.domain.model.BudgetPeriod
import com.jalay.manageexpenses.domain.model.BudgetWithSpending
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) {

    fun getAllActiveBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllActiveBudgets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getBudgetById(id: Long): Budget? {
        return budgetDao.getBudgetById(id)?.toDomainModel()
    }

    suspend fun getBudgetByCategory(category: String): Budget? {
        return budgetDao.getBudgetByCategory(category)?.toDomainModel()
    }

    suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insert(budget.toEntity())
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget.toEntity())
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(budget.toEntity())
    }

    suspend fun deactivateBudget(id: Long) {
        budgetDao.deactivateBudget(id)
    }

    suspend fun getBudgetsWithSpending(): List<BudgetWithSpending> {
        val budgets = budgetDao.getAllActiveBudgets().first()
        if (budgets.isEmpty()) return emptyList()

        // Group budgets by period to minimize queries
        val budgetsByPeriod = budgets.groupBy { BudgetPeriod.valueOf(it.period.uppercase()) }

        // Get spending aggregations for each period type (max 2 queries instead of N)
        val spendingByPeriod = mutableMapOf<BudgetPeriod, Map<String, Double>>()

        budgetsByPeriod.keys.forEach { period ->
            val (startTime, endTime) = getDateRangeForPeriod(period)
            val categorySpending = transactionDao.getCategorySpendingInRange(startTime, endTime)
            spendingByPeriod[period] = categorySpending.associate { it.category to it.totalSpending }
        }

        // Map budgets to BudgetWithSpending using pre-fetched aggregations
        return budgets.map { budgetEntity ->
            val budget = budgetEntity.toDomainModel()
            val periodSpending = spendingByPeriod[budget.period] ?: emptyMap()
            val categorySpending = periodSpending[budget.category] ?: 0.0

            val percentageUsed = (categorySpending / budget.limitAmount).toFloat().coerceIn(0f, 2f)
            val remainingAmount = (budget.limitAmount - categorySpending).coerceAtLeast(0.0)

            BudgetWithSpending(
                budget = budget,
                currentSpending = categorySpending,
                remainingAmount = remainingAmount,
                percentageUsed = percentageUsed,
                isOverBudget = categorySpending > budget.limitAmount,
                isNearLimit = percentageUsed >= budget.alertThreshold
            )
        }
    }

    suspend fun getBudgetWithSpendingForCategory(category: String): BudgetWithSpending? {
        val budgetEntity = budgetDao.getBudgetByCategory(category) ?: return null
        val budget = budgetEntity.toDomainModel()
        val (startTime, endTime) = getDateRangeForPeriod(budget.period)

        // Use aggregated query instead of fetching all transactions
        val categorySpendingList = transactionDao.getCategorySpendingInRange(startTime, endTime)
        val categorySpending = categorySpendingList.find { it.category == category }?.totalSpending ?: 0.0

        val percentageUsed = (categorySpending / budget.limitAmount).toFloat().coerceIn(0f, 2f)
        val remainingAmount = (budget.limitAmount - categorySpending).coerceAtLeast(0.0)

        return BudgetWithSpending(
            budget = budget,
            currentSpending = categorySpending,
            remainingAmount = remainingAmount,
            percentageUsed = percentageUsed,
            isOverBudget = categorySpending > budget.limitAmount,
            isNearLimit = percentageUsed >= budget.alertThreshold
        )
    }

    private fun getDateRangeForPeriod(period: BudgetPeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        when (period) {
            BudgetPeriod.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            BudgetPeriod.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        
        return calendar.timeInMillis to endTime
    }
}

private fun BudgetEntity.toDomainModel(): Budget {
    return Budget(
        id = id,
        category = category,
        limitAmount = limitAmount,
        period = BudgetPeriod.valueOf(period.uppercase()),
        alertThreshold = alertThreshold,
        isActive = isActive,
        createdAt = createdAt
    )
}

private fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        category = category,
        limitAmount = limitAmount,
        period = period.name.lowercase(),
        alertThreshold = alertThreshold,
        isActive = isActive,
        createdAt = createdAt
    )
}
