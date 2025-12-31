package com.jalay.manageexpenses.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import com.jalay.manageexpenses.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getPagedTransactions(): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE transactionType = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getPagedTransactionsByCategory(category: String): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE recipientName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE transactionType = :type ORDER BY timestamp DESC")
    fun getPagedTransactionsByType(type: String): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE category = :category AND transactionType = :type ORDER BY timestamp DESC")
    fun getPagedTransactionsByCategoryAndType(category: String, type: String): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE recipientName LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPagedTransactions(query: String): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE (recipientName LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') AND transactionType = :type ORDER BY timestamp DESC")
    fun searchPagedTransactionsByType(query: String, type: String): PagingSource<Int, TransactionEntity>

    @Query("SELECT * FROM transactions WHERE recipientName LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTransactionsWithNotes(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT * FROM transactions WHERE transactionRef = :ref LIMIT 1")
    suspend fun getTransactionByRef(ref: String): TransactionEntity?

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}