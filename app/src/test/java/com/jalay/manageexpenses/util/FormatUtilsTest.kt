package com.jalay.manageexpenses.util

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class FormatUtilsTest {

    @Test
    fun `formatLargeAmount should format lakhs correctly`() {
        assertEquals("10.0L", FormatUtils.formatLargeAmount(10_00_000.0))
        assertEquals("15.5L", FormatUtils.formatLargeAmount(15_50_000.0))
        assertEquals("100.0L", FormatUtils.formatLargeAmount(1_00_00_000.0))
    }

    @Test
    fun `formatLargeAmount should format thousands with commas`() {
        assertEquals("50,000", FormatUtils.formatLargeAmount(50000.0))
        assertEquals("1,500", FormatUtils.formatLargeAmount(1500.0))
        assertEquals("9,99,999", FormatUtils.formatLargeAmount(999999.0))
    }

    @Test
    fun `formatLargeAmount should format small amounts with decimals`() {
        assertEquals("500.00", FormatUtils.formatLargeAmount(500.0))
        assertEquals("99.50", FormatUtils.formatLargeAmount(99.5))
        assertEquals("0.50", FormatUtils.formatLargeAmount(0.5))
    }

    @Test
    fun `formatAmountWithSign should add minus for expense`() {
        assertTrue(FormatUtils.formatAmountWithSign(1000.0, true).startsWith("-₹"))
    }

    @Test
    fun `formatAmountWithSign should add plus for income`() {
        assertTrue(FormatUtils.formatAmountWithSign(1000.0, false).startsWith("+₹"))
    }

    @Test
    fun `formatAmountWithSymbol should add rupee symbol`() {
        assertTrue(FormatUtils.formatAmountWithSymbol(1000.0).startsWith("₹"))
    }

    @Test
    fun `formatAmount should handle whole numbers`() {
        assertEquals("1,500", FormatUtils.formatAmount(1500.0))
    }

    @Test
    fun `formatAmount should handle decimal numbers under 1000`() {
        assertEquals("500.00", FormatUtils.formatAmount(500.0))
        assertEquals("99.99", FormatUtils.formatAmount(99.99))
    }

    @Test
    fun `formatPercentage should format correctly`() {
        assertEquals("75%", FormatUtils.formatPercentage(0.75f))
        assertEquals("100%", FormatUtils.formatPercentage(1.0f))
        assertEquals("0%", FormatUtils.formatPercentage(0.0f))
        assertEquals("50%", FormatUtils.formatPercentage(0.5f))
    }

    @Test
    fun `formatPercentageDecimal should format with decimals`() {
        assertEquals("75.6%", FormatUtils.formatPercentageDecimal(0.756f))
        assertEquals("33.3%", FormatUtils.formatPercentageDecimal(0.333f))
    }

    @Test
    fun `formatTime should return correct format`() {
        // Create a specific timestamp
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 30)
        }
        val result = FormatUtils.formatTime(calendar.timeInMillis)
        
        // Should contain "PM" for afternoon time
        assertTrue(result.contains("PM") || result.contains("pm"))
    }

    @Test
    fun `formatRelativeDate should return Today for current date`() {
        val now = System.currentTimeMillis()
        assertEquals("Today", FormatUtils.formatRelativeDate(now))
    }

    @Test
    fun `formatRelativeDate should return Yesterday for previous day`() {
        val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        assertEquals("Yesterday", FormatUtils.formatRelativeDate(yesterday))
    }
}
