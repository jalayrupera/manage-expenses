package com.jalay.manageexpenses.di

import com.jalay.manageexpenses.data.parser.CategoryAutoMapper
import com.jalay.manageexpenses.data.parser.SmsParser
import com.jalay.manageexpenses.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @Singleton
    fun provideCategoryAutoMapper(
        repository: TransactionRepository
    ): CategoryAutoMapper {
        return CategoryAutoMapper(
            getMappings = { repository.getAllCategoryMappingsSync() }
        )
    }

    @Provides
    @Singleton
    fun provideSmsParser(
        categoryAutoMapper: CategoryAutoMapper
    ): SmsParser {
        return SmsParser(categoryAutoMapper)
    }
}
