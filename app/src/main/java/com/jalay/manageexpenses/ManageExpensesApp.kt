package com.jalay.manageexpenses

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jalay.manageexpenses.data.local.dao.CategoryMappingDao
import com.jalay.manageexpenses.data.local.entity.CategoryMappingEntity
import com.jalay.manageexpenses.data.parser.CategoryAutoMapper
import com.jalay.manageexpenses.util.NotificationHelper
import com.jalay.manageexpenses.worker.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ManageExpensesApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var categoryMappingDao: CategoryMappingDao

    @Inject
    lateinit var categoryAutoMapper: CategoryAutoMapper

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        NotificationHelper.createNotificationChannel(this)

        // Initialize default categories
        initializeDefaultCategories()

        // Schedule recurring transaction check
        scheduleRecurringTransactionWork()
    }

    private fun initializeDefaultCategories() {
        applicationScope.launch {
            try {
                val existingMappings = categoryMappingDao.getAllMappings().first()
                if (existingMappings.isEmpty()) {
                    val defaultMappings = categoryAutoMapper.getDefaultMappings()
                    val entities = defaultMappings.map { mapping ->
                        CategoryMappingEntity(
                            id = null,
                            keyword = mapping.keyword,
                            category = mapping.category,
                            icon = mapping.icon,
                            isCustom = mapping.isCustom
                        )
                    }
                    categoryMappingDao.insertAll(entities)
                }
            } catch (e: Exception) {
                // Log error but don't crash the app
                e.printStackTrace()
            }
        }
    }

    private fun scheduleRecurringTransactionWork() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RecurringTransactionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
