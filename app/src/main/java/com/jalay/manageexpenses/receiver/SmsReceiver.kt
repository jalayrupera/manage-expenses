package com.jalay.manageexpenses.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.usecase.ParseSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var parseSmsUseCase: ParseSmsUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        // Use goAsync() to extend receiver lifetime for async work
        val pendingResult = goAsync()

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        scope.launch {
            try {
                messages.forEach { smsMessage ->
                    val sender = smsMessage.originatingAddress
                    val body = smsMessage.messageBody
                    val timestamp = smsMessage.timestampMillis

                    Log.d("SmsReceiver", "Received SMS from $sender")

                    try {
                        val transaction = parseSmsUseCase(body ?: "", sender ?: "", timestamp)
                        if (transaction != null) {
                            processTransaction(transaction)
                        }
                    } catch (e: Exception) {
                        Log.e("SmsReceiver", "Error processing SMS", e)
                    }
                }
            } finally {
                // Complete the pending result to allow system to kill receiver
                pendingResult.finish()
            }
        }
    }

    private suspend fun processTransaction(transaction: Transaction) {
        // Check for duplicate by transaction reference
        val isDuplicateRef = transaction.transactionRef?.let { ref ->
            transactionRepository.isTransactionDuplicate(ref)
        } ?: false

        if (isDuplicateRef) {
            Log.d("SmsReceiver", "Duplicate transaction ref detected, skipping")
            return
        }

        // Time-based debouncing: check if same amount + recipient within 5 seconds
        if (isRecentDuplicate(transaction)) {
            Log.d("SmsReceiver", "Debounce: Similar transaction within 5 seconds, skipping")
            return
        }

        try {
            transactionRepository.insertTransaction(transaction)
            // Track this transaction for debouncing
            recordTransaction(transaction)
            Log.d("SmsReceiver", "Transaction saved: ${transaction.recipientName} - ₹${transaction.amount}")
        } catch (e: Exception) {
            // SQLite unique constraint will also prevent duplicates
            Log.e("SmsReceiver", "Error inserting transaction (may be duplicate)", e)
        }
    }

    private fun isRecentDuplicate(transaction: Transaction): Boolean {
        synchronized(recentTransactions) {
            val now = System.currentTimeMillis()
            // Clean up old entries (older than 10 seconds)
            recentTransactions.entries.removeAll { now - it.value > DEBOUNCE_CLEANUP_MS }

            val key = createDebounceKey(transaction)
            val lastTime = recentTransactions[key]
            return lastTime != null && (now - lastTime) < DEBOUNCE_WINDOW_MS
        }
    }

    private fun recordTransaction(transaction: Transaction) {
        synchronized(recentTransactions) {
            val key = createDebounceKey(transaction)
            recentTransactions[key] = System.currentTimeMillis()
        }
    }

    private fun createDebounceKey(transaction: Transaction): String {
        return "${transaction.amount}_${transaction.recipientName.lowercase()}"
    }

    companion object {
        private const val DEBOUNCE_WINDOW_MS = 5_000L // 5 seconds
        private const val DEBOUNCE_CLEANUP_MS = 10_000L // 10 seconds

        // In-memory cache for debouncing (key: amount_recipient -> timestamp)
        private val recentTransactions = mutableMapOf<String, Long>()
    }
}
