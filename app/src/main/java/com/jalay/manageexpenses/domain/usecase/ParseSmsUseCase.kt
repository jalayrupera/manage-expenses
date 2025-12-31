package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.parser.CategoryAutoMapper
import com.jalay.manageexpenses.data.parser.SmsParser
import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.Transaction

class ParseSmsUseCase(
    private val smsParser: SmsParser
) {
    suspend operator fun invoke(smsBody: String, sender: String, timestamp: Long): Transaction? {
        return smsParser.parseSms(smsBody, sender, timestamp)
    }
}