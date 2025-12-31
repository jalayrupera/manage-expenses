package com.jalay.manageexpenses.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jalay.manageexpenses.data.local.dao.BudgetDao
import com.jalay.manageexpenses.data.local.dao.CategoryMappingDao
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

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "expenses_database"
        )
            .addMigrations(MIGRATION_1_2)
            // Keep fallbackToDestructiveMigration for development, remove for production
            .fallbackToDestructiveMigration()
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
}
