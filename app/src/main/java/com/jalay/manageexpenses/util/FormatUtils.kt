package com.jalay.manageexpenses.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized formatting utilities for currency, date, and time.
 */
object FormatUtils {

    // Currency formatting
    
    /**
     * Formats a large amount with L suffix for lakhs.
     * Examples: 1500000 -> "15.0L", 50000 -> "50,000", 999 -> "999.00"
     */
    fun formatLargeAmount(amount: Double): String {
        return when {
            amount >= 10_00_000 -> String.format("%.1fL", amount / 100000)
            amount >= 1000 -> String.format("%,.0f", amount)
            else -> String.format("%.2f", amount)
        }
    }

    /**
     * Formats amount with rupee symbol and sign based on transaction type.
     * Examples: isExpense=true -> "-₹1,500", isExpense=false -> "+₹1,500"
     */
    fun formatAmountWithSign(amount: Double, isExpense: Boolean): String {
        val prefix = if (isExpense) "-" else "+"
        return "$prefix₹${formatLargeAmount(kotlin.math.abs(amount))}"
    }

    /**
     * Formats amount with just the rupee symbol.
     * Examples: 1500 -> "₹1,500"
     */
    fun formatAmountWithSymbol(amount: Double): String {
        return "₹${formatLargeAmount(amount)}"
    }

    /**
     * Formats amount without any symbol, comma-separated for thousands.
     * Examples: 1500.50 -> "1,500.50" or "1,500" for whole numbers
     */
    fun formatAmount(amount: Double): String {
        return if (amount >= 1000) {
            String.format("%,.0f", amount)
        } else {
            String.format("%.2f", amount)
        }
    }

    // Date formatting
    
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
    
    /**
     * Full date format: "Wednesday, January 15, 2025"
     */
    fun formatFullDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    /**
     * Short date format: "15 Jan 2025"
     */
    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    /**
     * Month and year format: "January 2025"
     */
    fun formatMonthYear(timestamp: Long): String {
        return monthYearFormat.format(Date(timestamp))
    }

    /**
     * Day and month format: "15 Jan"
     */
    fun formatDayMonth(timestamp: Long): String {
        return dayMonthFormat.format(Date(timestamp))
    }

    /**
     * Time format: "3:45 PM"
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Date and time format: "15 Jan 2025, 3:45 PM"
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /**
     * Relative date description: "Today", "Yesterday", or the short date.
     */
    fun formatRelativeDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, date) -> "Today"
            isYesterday(now, date) -> "Yesterday"
            isSameWeek(now, date) -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
            isSameYear(now, date) -> formatDayMonth(timestamp)
            else -> formatShortDate(timestamp)
        }
    }

    /**
     * Date header format for grouping transactions.
     * Returns "Today", "Yesterday", or "Wednesday, Jan 15" for this year,
     * or "Wednesday, Jan 15, 2024" for previous years.
     */
    fun formatDateHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        return when {
            isSameDay(now, date) -> "Today"
            isYesterday(now, date) -> "Yesterday"
            isSameYear(now, date) -> {
                SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(timestamp))
            }
            else -> {
                SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, date)
    }

    private fun isSameWeek(now: Calendar, date: Calendar): Boolean {
        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                now.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameYear(now: Calendar, date: Calendar): Boolean {
        return now.get(Calendar.YEAR) == date.get(Calendar.YEAR)
    }

    // Percentage formatting

    /**
     * Formats a decimal as percentage: 0.75 -> "75%"
     */
    fun formatPercentage(value: Float): String {
        return "${(value * 100).toInt()}%"
    }

    /**
     * Formats a decimal as percentage with decimal places: 0.756 -> "75.6%"
     */
    fun formatPercentageDecimal(value: Float, decimals: Int = 1): String {
        return String.format("%.${decimals}f%%", value * 100)
    }
}
