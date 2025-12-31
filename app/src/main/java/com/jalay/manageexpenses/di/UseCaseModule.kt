package com.jalay.manageexpenses.di

import android.content.ContentResolver
import android.content.Context
import com.jalay.manageexpenses.data.parser.SmsParser
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.usecase.AddTransactionUseCase
import com.jalay.manageexpenses.domain.usecase.ExportDataUseCase
import com.jalay.manageexpenses.domain.usecase.GetStatisticsUseCase
import com.jalay.manageexpenses.domain.usecase.GetTransactionsUseCase
import com.jalay.manageexpenses.domain.usecase.ImportHistoricalSmsUseCase
import com.jalay.manageexpenses.domain.usecase.ParseSmsUseCase
import com.jalay.manageexpenses.domain.usecase.SearchTransactionsUseCase
import com.jalay.manageexpenses.domain.usecase.UpdateCategoryUseCase
import com.jalay.manageexpenses.domain.usecase.UpdateTransactionNotesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    fun provideParseSmsUseCase(smsParser: SmsParser): ParseSmsUseCase {
        return ParseSmsUseCase(smsParser)
    }

    @Provides
    fun provideImportHistoricalSmsUseCase(
        contentResolver: ContentResolver,
        parseSmsUseCase: ParseSmsUseCase,
        repository: TransactionRepository
    ): ImportHistoricalSmsUseCase {
        return ImportHistoricalSmsUseCase(contentResolver, parseSmsUseCase, repository)
    }

    @Provides
    fun provideGetTransactionsUseCase(repository: TransactionRepository): GetTransactionsUseCase {
        return GetTransactionsUseCase(repository)
    }

    @Provides
    fun provideSearchTransactionsUseCase(repository: TransactionRepository): SearchTransactionsUseCase {
        return SearchTransactionsUseCase(repository)
    }

    @Provides
    fun provideGetStatisticsUseCase(repository: TransactionRepository): GetStatisticsUseCase {
        return GetStatisticsUseCase(repository)
    }

    @Provides
    fun provideUpdateTransactionNotesUseCase(repository: TransactionRepository): UpdateTransactionNotesUseCase {
        return UpdateTransactionNotesUseCase(repository)
    }

    @Provides
    fun provideUpdateCategoryUseCase(repository: TransactionRepository): UpdateCategoryUseCase {
        return UpdateCategoryUseCase(repository)
    }

    @Provides
    fun provideExportDataUseCase(
        @ApplicationContext context: Context,
        repository: TransactionRepository
    ): ExportDataUseCase {
        return ExportDataUseCase(context, repository)
    }

    @Provides
    fun provideAddTransactionUseCase(repository: TransactionRepository): AddTransactionUseCase {
        return AddTransactionUseCase(repository)
    }
}
