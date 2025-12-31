package com.jalay.manageexpenses.data.parser

import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import java.util.regex.Pattern

class SmsParser(private val categoryAutoMapper: CategoryAutoMapper) {

    private val patterns by lazy {
        listOf(
            GooglePayPattern(),
            PhonePePattern(),
            PaytmPattern(),
            BhimPattern(),
            GenericUpiPattern()
        )
    }

    suspend fun parseSms(smsBody: String, sender: String, timestamp: Long): Transaction? {
        for (pattern in patterns) {
            val result = pattern.parse(smsBody, sender)
            if (result != null) {
                val category = categoryAutoMapper.categorize(result.recipient, sender)
                return Transaction(
                    id = null,
                    amount = result.amount,
                    recipientName = result.recipient,
                    transactionType = result.type,
                    category = category,
                    notes = null,
                    timestamp = timestamp,
                    rawSms = smsBody,
                    upiApp = result.upiApp,
                    transactionRef = result.ref,
                    isParsed = true
                )
            }
        }
        return null
    }

    interface Upipattern {
        fun parse(smsBody: String, sender: String): ParseResult?
    }

    data class ParseResult(
        val amount: Double,
        val recipient: String,
        val type: TransactionType,
        val upiApp: String,
        val ref: String?
    )

    private class GooglePayPattern : Upipattern {
        override fun parse(smsBody: String, sender: String): ParseResult? {
            if (!sender.contains("gpay", ignoreCase = true)) return null

            val type = when {
                smsBody.contains("debited", ignoreCase = true) -> TransactionType.SENT
                smsBody.contains("credited", ignoreCase = true) -> TransactionType.RECEIVED
                else -> return null
            }

            val amountPattern = Pattern.compile("Rs\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            val amountMatcher = amountPattern.matcher(smsBody)
            if (!amountMatcher.find()) return null

            val amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

            val recipientPattern = Pattern.compile("to\\s+([^\\n]+?)(?:\\s+on|\\s+at|\\s+via|$)", Pattern.CASE_INSENSITIVE)
            val recipientMatcher = recipientPattern.matcher(smsBody)
            val recipient = if (recipientMatcher.find()) {
                recipientMatcher.group(1)?.trim()
            } else {
                ""
            } ?: return null

            val refPattern = Pattern.compile("Ref\\s*#?:?\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE)
            val refMatcher = refPattern.matcher(smsBody)
            val ref = if (refMatcher.find()) refMatcher.group(1) else null

            return ParseResult(amount, recipient, type, "Google Pay", ref)
        }
    }

    private class PhonePePattern : Upipattern {
        override fun parse(smsBody: String, sender: String): ParseResult? {
            if (!sender.contains("phonepe", ignoreCase = true)) return null

            val type = when {
                smsBody.contains("debited", ignoreCase = true) -> TransactionType.SENT
                smsBody.contains("credited", ignoreCase = true) -> TransactionType.RECEIVED
                else -> return null
            }

            val amountPattern = Pattern.compile("Rs\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            val amountMatcher = amountPattern.matcher(smsBody)
            if (!amountMatcher.find()) return null

            val amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

            val recipientPattern = Pattern.compile("to\\s+([^\\n]+?)(?:\\s+via|\\s+on|$)", Pattern.CASE_INSENSITIVE)
            val recipientMatcher = recipientPattern.matcher(smsBody)
            val recipient = if (recipientMatcher.find()) {
                recipientMatcher.group(1)?.trim()
            } else {
                ""
            } ?: return null

            val refPattern = Pattern.compile("transaction\\s+id[:\\s]*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE)
            val refMatcher = refPattern.matcher(smsBody)
            val ref = if (refMatcher.find()) refMatcher.group(1) else null

            return ParseResult(amount, recipient, type, "PhonePe", ref)
        }
    }

    private class PaytmPattern : Upipattern {
        override fun parse(smsBody: String, sender: String): ParseResult? {
            if (!sender.contains("paytm", ignoreCase = true)) return null

            val type = when {
                smsBody.contains("paid", ignoreCase = true) || smsBody.contains("debited", ignoreCase = true) -> TransactionType.SENT
                smsBody.contains("received", ignoreCase = true) || smsBody.contains("credited", ignoreCase = true) -> TransactionType.RECEIVED
                else -> return null
            }

            val amountPattern = Pattern.compile("INR\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            val amountMatcher = amountPattern.matcher(smsBody)
            if (!amountMatcher.find()) return null

            val amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

            val recipientPattern = Pattern.compile("to\\s+([^\\n]+?)(?:\\s+using|\\s+via|$)", Pattern.CASE_INSENSITIVE)
            val recipientMatcher = recipientPattern.matcher(smsBody)
            val recipient = if (recipientMatcher.find()) {
                recipientMatcher.group(1)?.trim()
            } else {
                ""
            } ?: return null

            val refPattern = Pattern.compile("order\\s+id[:\\s]*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE)
            val refMatcher = refPattern.matcher(smsBody)
            val ref = if (refMatcher.find()) refMatcher.group(1) else null

            return ParseResult(amount, recipient, type, "Paytm", ref)
        }
    }

    private class BhimPattern : Upipattern {
        override fun parse(smsBody: String, sender: String): ParseResult? {
            if (!sender.contains("bhim", ignoreCase = true)) return null

            val type = when {
                smsBody.contains("debited", ignoreCase = true) -> TransactionType.SENT
                smsBody.contains("credited", ignoreCase = true) -> TransactionType.RECEIVED
                else -> return null
            }

            val amountPattern = Pattern.compile("Rs\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            val amountMatcher = amountPattern.matcher(smsBody)
            if (!amountMatcher.find()) return null

            val amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

            val recipientPattern = Pattern.compile("beneficiary[:\\s]*([^\\n]+?)(?:\\s+via|$)", Pattern.CASE_INSENSITIVE)
            val recipientMatcher = recipientPattern.matcher(smsBody)
            val recipient = if (recipientMatcher.find()) {
                recipientMatcher.group(1)?.trim()
            } else {
                ""
            } ?: return null

            val refPattern = Pattern.compile("ref[:\\s]*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE)
            val refMatcher = refPattern.matcher(smsBody)
            val ref = if (refMatcher.find()) refMatcher.group(1) else null

            return ParseResult(amount, recipient, type, "BHIM", ref)
        }
    }

    private class GenericUpiPattern : Upipattern {
        override fun parse(smsBody: String, sender: String): ParseResult? {
            if (!smsBody.contains("upi", ignoreCase = true)) return null

            val type = when {
                smsBody.contains("debited", ignoreCase = true) || smsBody.contains("sent", ignoreCase = true) || smsBody.contains("paid", ignoreCase = true) -> TransactionType.SENT
                smsBody.contains("credited", ignoreCase = true) || smsBody.contains("received", ignoreCase = true) -> TransactionType.RECEIVED
                else -> return null
            }

            val amountPattern = Pattern.compile("(?:Rs|INR)\\.?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE)
            val amountMatcher = amountPattern.matcher(smsBody)
            if (!amountMatcher.find()) return null

            val amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

            val recipientPattern = Pattern.compile("to\\s+([^\\n]+?)(?:\\s+(?:via|using|on)|$)", Pattern.CASE_INSENSITIVE)
            val recipientMatcher = recipientPattern.matcher(smsBody)
            val recipient = if (recipientMatcher.find()) {
                recipientMatcher.group(1)?.trim()
            } else {
                ""
            } ?: return null

            val refPattern = Pattern.compile("(?:ref|transaction)\\s*(?:#|id)?[:\\s]*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE)
            val refMatcher = refPattern.matcher(smsBody)
            val ref = if (refMatcher.find()) refMatcher.group(1) else null

            return ParseResult(amount, recipient, type, "UPI", ref)
        }
    }
}