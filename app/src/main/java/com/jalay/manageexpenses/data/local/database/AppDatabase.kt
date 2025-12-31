package com.jalay.manageexpenses.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jalay.manageexpenses.data.local.dao.BudgetDao
import com.jalay.manageexpenses.data.local.dao.CategoryMappingDao
import com.jalay.manageexpenses.data.local.dao.TransactionDao
import com.jalay.manageexpenses.data.local.entity.BudgetEntity
import com.jalay.manageexpenses.data.local.entity.CategoryMappingEntity
import com.jalay.manageexpenses.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryMappingEntity::class, BudgetEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryMappingDao(): CategoryMappingDao
    abstract fun budgetDao(): BudgetDao
}
