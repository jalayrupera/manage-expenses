package com.jalay.manageexpenses.domain.model

/**
 * Sort options for transaction lists.
 */
enum class SortType {
    DATE_DESC,      // Most recent first (default)
    DATE_ASC,       // Oldest first
    AMOUNT_DESC,    // Highest amount first
    AMOUNT_ASC      // Lowest amount first
}
