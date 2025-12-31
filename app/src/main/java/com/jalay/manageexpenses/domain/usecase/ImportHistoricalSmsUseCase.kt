package com.jalay.manageexpenses.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import android.provider.Telephony
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.Transaction

class ImportHistoricalSmsUseCase(
    private val contentResolver: ContentResolver,
    private val parseSmsUseCase: ParseSmsUseCase,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        days: Int? = null,
        onProgress: (Int, Int) -> Unit
    ): ImportResult {
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        var selection: String? = null
        var selectionArgs: Array<String>? = null

        if (days != null && days > 0) {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (days * 24L * 60L * 60L * 1000L)
            selection = "${Telephony.Sms.DATE} >= ?"
            selectionArgs = arrayOf(startTime.toString())
        }

        val cursor = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        ) ?: return ImportResult(0, 0)

        val transactions = mutableListOf<Transaction>()
        var processed = 0
        var found = 0

        cursor.use {
            val count = it.count
            while (it.moveToNext()) {
                val sender = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val date = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))

                processed++

                val transaction = parseSmsUseCase(body, sender, date)
                if (transaction != null) {
                    val isDuplicate = transaction.transactionRef?.let { ref ->
                        transactionRepository.isTransactionDuplicate(ref)
                    } ?: false

                    if (!isDuplicate) {
                        transactions.add(transaction)
                        found++
                    }
                }

                if (processed % 10 == 0) {
                    onProgress(processed, count)
                }
            }
        }

        onProgress(processed, processed)
        transactionRepository.insertTransactions(transactions)

        return ImportResult(processed, found)
    }

    data class ImportResult(
        val totalProcessed: Int,
        val transactionsFound: Int
    )
}