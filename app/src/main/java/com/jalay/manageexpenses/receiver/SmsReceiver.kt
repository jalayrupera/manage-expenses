package com.jalay.manageexpenses.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.jalay.manageexpenses.data.repository.TransactionRepository
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
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages.forEach { smsMessage ->
                val sender = smsMessage.originatingAddress
                val body = smsMessage.messageBody
                val timestamp = smsMessage.timestampMillis

                Log.d("SmsReceiver", "Received SMS from $sender")

                scope.launch {
                    try {
                        val transaction = parseSmsUseCase(body ?: "", sender ?: "", timestamp)
                        if (transaction != null) {
                            val isDuplicate = transaction.transactionRef?.let { ref ->
                                transactionRepository.isTransactionDuplicate(ref)
                            } ?: false

                            if (!isDuplicate) {
                                transactionRepository.insertTransaction(transaction)
                                Log.d("SmsReceiver", "Transaction saved: ${transaction.recipientName} - ₹${transaction.amount}")
                            } else {
                                Log.d("SmsReceiver", "Duplicate transaction detected, skipping")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SmsReceiver", "Error processing SMS", e)
                    }
                }
            }
        }
    }
}
