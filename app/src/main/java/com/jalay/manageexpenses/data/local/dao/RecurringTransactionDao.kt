package com.jalay.manageexpenses.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jalay.manageexpenses.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurringTransaction: RecurringTransactionEntity): Long

    @Query("SELECT * FROM recurring_transactions")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1")
    fun getActive(): Flow<List<RecurringTransactionEntity>>

    @Delete
    suspend fun delete(recurringTransaction: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET isActive = :isActive WHERE id = :id")
    suspend fun setStatus(id: Long, isActive: Boolean)

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND nextDate <= :currentTime")
    suspend fun getDueTransactions(currentTime: Long): List<RecurringTransactionEntity>

    @Query("UPDATE recurring_transactions SET nextDate = :nextDate WHERE id = :id")
    suspend fun updateNextDate(id: Long, nextDate: Long)
}
