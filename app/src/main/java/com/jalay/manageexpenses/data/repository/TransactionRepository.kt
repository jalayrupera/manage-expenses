package com.jalay.manageexpenses.data.repository

import com.jalay.manageexpenses.data.local.dao.TransactionDao
import com.jalay.manageexpenses.data.local.dao.CategoryMappingDao
import com.jalay.manageexpenses.data.local.entity.CategoryMappingEntity
import com.jalay.manageexpenses.data.local.entity.TransactionEntity
import com.jalay.manageexpenses.domain.model.CategoryMapping
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryMappingDao: CategoryMappingDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getPagedTransactions(pageSize: Int = 50, type: TransactionType? = null): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { 
                if (type == null) transactionDao.getPagedTransactions()
                else transactionDao.getPagedTransactionsByType(type.name)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    fun getPagedTransactionsByCategory(category: String, pageSize: Int = 50, type: TransactionType? = null): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { 
                if (type == null) transactionDao.getPagedTransactionsByCategory(category)
                else transactionDao.getPagedTransactionsByCategoryAndType(category, type.name)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    fun searchPagedTransactions(query: String, pageSize: Int = 50, type: TransactionType? = null): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { 
                if (type == null) transactionDao.searchPagedTransactions(query)
                else transactionDao.searchPagedTransactionsByType(query, type.name)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomainModel()
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun searchTransactionsWithNotes(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactionsWithNotes(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startTime, endTime).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun insertTransaction(transaction: Transaction): Long? {
        return transactionDao.insert(transaction.toEntity())
    }

    suspend fun insertTransactions(transactions: List<Transaction>) {
        transactionDao.insertAll(transactions.map { it.toEntity() })
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction.toEntity())
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction.toEntity())
    }

    suspend fun getTransactionCount(): Int {
        return transactionDao.getTransactionCount()
    }

    suspend fun isTransactionDuplicate(ref: String): Boolean {
        return transactionDao.getTransactionByRef(ref) != null
    }

    fun getAllCategoryMappings(): Flow<List<CategoryMapping>> {
        return categoryMappingDao.getAllMappings().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getAllCategoryMappingsSync(): List<CategoryMapping> {
        return categoryMappingDao.getAllMappingsSync().map { it.toDomainModel() }
    }

    suspend fun insertCategoryMapping(mapping: CategoryMapping): Long? {
        return categoryMappingDao.insert(mapping.toEntity())
    }

    suspend fun insertCategoryMappings(mappings: List<CategoryMapping>) {
        categoryMappingDao.insertAll(mappings.map { it.toEntity() })
    }

    suspend fun deleteCategoryMapping(mapping: CategoryMapping) {
        categoryMappingDao.delete(mapping.toEntity())
    }

    suspend fun deleteCustomMappings() {
        categoryMappingDao.deleteCustomMappings()
    }

    suspend fun getAllCategories(): List<String> {
        return categoryMappingDao.getAllCategories()
    }

    fun getTransactionsForMonth(year: Int, month: Int): Flow<List<Transaction>> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.set(year, month, 1, 0, 0, 0)
        val endTime = calendar.timeInMillis

        return getTransactionsByDateRange(startTime, endTime)
    }

    fun getTransactionsForLastDays(days: Int): Flow<List<Transaction>> {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24L * 60L * 60L * 1000L)

        return getTransactionsByDateRange(startTime, endTime)
    }
}

private fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        recipientName = recipientName,
        transactionType = TransactionType.valueOf(transactionType),
        category = category,
        notes = notes,
        timestamp = timestamp,
        rawSms = rawSms,
        upiApp = upiApp,
        transactionRef = transactionRef,
        isParsed = isParsed
    )
}

private fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        recipientName = recipientName,
        transactionType = transactionType.name,
        category = category,
        notes = notes,
        timestamp = timestamp,
        rawSms = rawSms,
        upiApp = upiApp,
        transactionRef = transactionRef,
        isParsed = isParsed
    )
}

private fun CategoryMappingEntity.toDomainModel(): CategoryMapping {
    return CategoryMapping(
        id = id,
        keyword = keyword,
        category = category,
        icon = icon,
        isCustom = isCustom
    )
}

private fun CategoryMapping.toEntity(): CategoryMappingEntity {
    return CategoryMappingEntity(
        id = id,
        keyword = keyword,
        category = category,
        icon = icon,
        isCustom = isCustom
    )
}