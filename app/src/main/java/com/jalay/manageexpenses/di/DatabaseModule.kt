package com.jalay.manageexpenses.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jalay.manageexpenses.data.local.dao.BudgetDao
import com.jalay.manageexpenses.data.local.dao.CategoryMappingDao
import com.jalay.manageexpenses.data.local.dao.RecurringTransactionDao
import com.jalay.manageexpenses.data.local.dao.TransactionDao
import com.jalay.manageexpenses.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Migration from version 1 to 2 - Adding budgets table
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category TEXT NOT NULL,
                    limitAmount REAL NOT NULL,
                    period TEXT NOT NULL,
                    alertThreshold REAL NOT NULL DEFAULT 0.8,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL
                )
            """)
        }
    }

    // Migration from version 2 to 3 - Adding recurring_transactions table
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS recurring_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    amount REAL NOT NULL,
                    category TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    nextDate INTEGER NOT NULL,
                    notes TEXT,
                    isActive INTEGER NOT NULL DEFAULT 1
                )
            """)
        }
    }

    // Migration from version 3 to 4 - Adding indices for performance
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add indices to transactions table
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_timestamp ON transactions(timestamp)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_category ON transactions(category)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_transactionType ON transactions(transactionType)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_transactions_transactionRef ON transactions(transactionRef)")

            // Add unique index to category_mappings table
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_category_mappings_keyword ON category_mappings(keyword)")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "expenses_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryMappingDao(database: AppDatabase): CategoryMappingDao {
        return database.categoryMappingDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideRecurringTransactionDao(database: AppDatabase): RecurringTransactionDao {
        return database.recurringTransactionDao()
    }
}
