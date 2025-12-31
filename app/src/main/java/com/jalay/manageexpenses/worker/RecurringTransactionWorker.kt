package com.jalay.manageexpenses.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jalay.manageexpenses.data.local.dao.RecurringTransactionDao
import com.jalay.manageexpenses.domain.model.RecurringFrequency
import com.jalay.manageexpenses.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringTransactionDao: RecurringTransactionDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val dueTransactions = recurringTransactionDao.getDueTransactions(now)

            for (entity in dueTransactions) {
                // Convert to domain model for notification
                val transaction = entity.toDomainModel()

                // Show reminder notification
                NotificationHelper.showRecurringReminder(context, transaction)

                // Calculate and update next date
                val nextDate = calculateNextDate(entity.nextDate, entity.frequency)
                entity.id?.let { id ->
                    recurringTransactionDao.updateNextDate(id, nextDate)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun calculateNextDate(currentDate: Long, frequency: String): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentDate
        }

        when (RecurringFrequency.valueOf(frequency)) {
            RecurringFrequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RecurringFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.timeInMillis
    }

    companion object {
        const val WORK_NAME = "recurring_transaction_reminder"
    }
}
