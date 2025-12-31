package com.jalay.manageexpenses.data.repository

import com.jalay.manageexpenses.data.local.dao.RecurringTransactionDao
import com.jalay.manageexpenses.data.local.entity.RecurringTransactionEntity
import com.jalay.manageexpenses.domain.model.RecurringFrequency
import com.jalay.manageexpenses.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepository @Inject constructor(
    private val recurringTransactionDao: RecurringTransactionDao
) {
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return recurringTransactionDao.getAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun addRecurringTransaction(transaction: RecurringTransaction): Long {
        return recurringTransactionDao.insert(transaction.toEntity())
    }

    suspend fun deleteRecurringTransaction(transaction: RecurringTransaction) {
        recurringTransactionDao.delete(transaction.toEntity())
    }

    suspend fun setRecurringTransactionStatus(id: Long, isActive: Boolean) {
        recurringTransactionDao.setStatus(id, isActive)
    }

    private fun RecurringTransactionEntity.toDomainModel(): RecurringTransaction {
        return RecurringTransaction(
            id = id,
            name = name,
            amount = amount,
            category = category,
            frequency = RecurringFrequency.valueOf(frequency),
            nextDate = nextDate,
            notes = notes,
            isActive = isActive
        )
    }

    private fun RecurringTransaction.toEntity(): RecurringTransactionEntity {
        return RecurringTransactionEntity(
            id = id,
            name = name,
            amount = amount,
            category = category,
            frequency = frequency.name,
            nextDate = nextDate,
            notes = notes,
            isActive = isActive
        )
    }
}
