package com.jalay.manageexpenses

import android.content.Context
import com.jalay.manageexpenses.data.local.database.AppDatabase
import com.jalay.manageexpenses.data.parser.CategoryAutoMapper
import com.jalay.manageexpenses.data.parser.SmsParser
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.usecase.*
import kotlinx.coroutines.flow.first

class AppContainer(private val context: Context) {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    fun getContext(): Context = context

    private val transactionRepository by lazy {
        TransactionRepository(
            transactionDao = database.transactionDao(),
            categoryMappingDao = database.categoryMappingDao()
        )
    }

    private val categoryAutoMapper by lazy {
        CategoryAutoMapper(
            getMappings = {
                transactionRepository.getAllCategoryMappingsSync()
            }
        )
    }

    private val smsParser by lazy {
        SmsParser(categoryAutoMapper)
    }

    fun getTransactionRepository(context: Context): TransactionRepository {
        return transactionRepository
    }

    fun getParseSmsUseCase(context: Context): ParseSmsUseCase {
        return ParseSmsUseCase(smsParser)
    }

    fun getImportHistoricalSmsUseCase(context: Context): ImportHistoricalSmsUseCase {
        return ImportHistoricalSmsUseCase(
            contentResolver = context.contentResolver,
            parseSmsUseCase = getParseSmsUseCase(context),
            transactionRepository = transactionRepository
        )
    }

    fun getGetTransactionsUseCase(context: Context): GetTransactionsUseCase {
        return GetTransactionsUseCase(transactionRepository)
    }

    fun getSearchTransactionsUseCase(context: Context): SearchTransactionsUseCase {
        return SearchTransactionsUseCase(transactionRepository)
    }

    fun getGetStatisticsUseCase(context: Context): GetStatisticsUseCase {
        return GetStatisticsUseCase(transactionRepository)
    }

    fun getUpdateTransactionNotesUseCase(context: Context): UpdateTransactionNotesUseCase {
        return UpdateTransactionNotesUseCase(transactionRepository)
    }

    fun getUpdateCategoryUseCase(context: Context): UpdateCategoryUseCase {
        return UpdateCategoryUseCase(transactionRepository)
    }

    fun getExportDataUseCase(context: Context): ExportDataUseCase {
        return ExportDataUseCase(context, transactionRepository)
    }

    suspend fun initializeDefaultCategories() {
        val count = database.categoryMappingDao().getAllMappings().first().size
        if (count == 0) {
            val defaultMappings = categoryAutoMapper.getDefaultMappings()
            defaultMappings.forEach { mapping ->
                database.categoryMappingDao().insert(
                    com.jalay.manageexpenses.data.local.entity.CategoryMappingEntity(
                        keyword = mapping.keyword,
                        category = mapping.category,
                        icon = mapping.icon,
                        isCustom = mapping.isCustom
                    )
                )
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return INSTANCE ?: synchronized(this) {
                val instance = AppContainer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}