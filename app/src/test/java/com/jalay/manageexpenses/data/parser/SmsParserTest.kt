package com.jalay.manageexpenses.data.parser

import com.jalay.manageexpenses.domain.model.CategoryMapping
import com.jalay.manageexpenses.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SmsParserTest {

    private lateinit var smsParser: SmsParser
    private lateinit var categoryAutoMapper: CategoryAutoMapper

    @Before
    fun setup() {
        categoryAutoMapper = CategoryAutoMapper(
            getMappings = { defaultMappings }
        )
        smsParser = SmsParser(categoryAutoMapper)
    }

    private val defaultMappings = listOf(
        CategoryMapping(keyword = "amazon", category = "Shopping", icon = "shopping_bag"),
        CategoryMapping(keyword = "zomato", category = "Food & Dining", icon = "restaurant"),
        CategoryMapping(keyword = "uber", category = "Transport", icon = "directions_car")
    )

    @Test
    fun `parse Google Pay debit SMS should return SENT transaction`() = runTest {
        val smsBody = "Rs.500.00 debited from your account to John Doe on 15-Jan-2025 via Google Pay. Ref #123456789"
        val sender = "gpay"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNotNull(result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals(TransactionType.SENT, result.transactionType)
        assertEquals("Google Pay", result.upiApp)
        assertEquals("123456789", result.transactionRef)
    }

    @Test
    fun `parse Google Pay credit SMS should return RECEIVED transaction`() = runTest {
        val smsBody = "Rs.1000.00 credited to your account from Jane Smith on 15-Jan-2025 via Google Pay. Ref #987654321"
        val sender = "gpay"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNotNull(result)
        assertEquals(1000.0, result!!.amount, 0.01)
        assertEquals(TransactionType.RECEIVED, result.transactionType)
        assertEquals("Google Pay", result.upiApp)
    }

    @Test
    fun `parse PhonePe debit SMS should return SENT transaction`() = runTest {
        val smsBody = "Rs.750.00 debited from your account to Amazon Pay on 15-Jan-2025 via PhonePe. Transaction id: TXN123456"
        val sender = "phonepe"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNotNull(result)
        assertEquals(750.0, result!!.amount, 0.01)
        assertEquals(TransactionType.SENT, result.transactionType)
        assertEquals("PhonePe", result.upiApp)
        assertEquals("Shopping", result.category) // Should match "amazon" keyword
    }

    @Test
    fun `parse Paytm SMS should return correct transaction`() = runTest {
        val smsBody = "INR 250.00 paid to Zomato using Paytm. Order id: ORD12345"
        val sender = "paytm"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNotNull(result)
        assertEquals(250.0, result!!.amount, 0.01)
        assertEquals(TransactionType.SENT, result.transactionType)
        assertEquals("Paytm", result.upiApp)
        assertEquals("Food & Dining", result.category) // Should match "zomato" keyword
    }

    @Test
    fun `parse generic UPI SMS should return correct transaction`() = runTest {
        val smsBody = "Rs.100.00 debited from your account to Uber via UPI. Ref: UPI123456"
        val sender = "sms_bank"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNotNull(result)
        assertEquals(100.0, result!!.amount, 0.01)
        assertEquals(TransactionType.SENT, result.transactionType)
        assertEquals("Transport", result.category) // Should match "uber" keyword
    }

    @Test
    fun `parse SMS with comma-separated amount should work correctly`() = runTest {
        val smsBody = "Rs.1,500.00 debited from your account to John on 15-Jan via Google Pay. Ref #123"
        val sender = "gpay"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNotNull(result)
        assertEquals(1500.0, result!!.amount, 0.01)
    }

    @Test
    fun `non-UPI SMS should return null`() = runTest {
        val smsBody = "Your OTP is 123456. Valid for 5 minutes."
        val sender = "otpsender"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNull(result)
    }

    @Test
    fun `SMS without amount should return null`() = runTest {
        val smsBody = "Your payment was successful via Google Pay."
        val sender = "gpay"
        val timestamp = System.currentTimeMillis()

        val result = smsParser.parseSms(smsBody, sender, timestamp)

        assertNull(result)
    }
}
