package com.jalay.manageexpenses.di

import com.jalay.manageexpenses.data.local.dao.BudgetDao
import com.jalay.manageexpenses.data.local.dao.CategoryMappingDao
import com.jalay.manageexpenses.data.local.dao.RecurringTransactionDao
import com.jalay.manageexpenses.data.local.dao.TransactionDao
import com.jalay.manageexpenses.data.repository.BudgetRepository
import com.jalay.manageexpenses.data.repository.RecurringTransactionRepository
import com.jalay.manageexpenses.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        categoryMappingDao: CategoryMappingDao
    ): TransactionRepository {
        return TransactionRepository(transactionDao, categoryMappingDao)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: BudgetDao,
        transactionDao: TransactionDao
    ): BudgetRepository {
        return BudgetRepository(budgetDao, transactionDao)
    }

    @Provides
    @Singleton
    fun provideRecurringTransactionRepository(
        recurringTransactionDao: RecurringTransactionDao
    ): RecurringTransactionRepository {
        return RecurringTransactionRepository(recurringTransactionDao)
    }
}
