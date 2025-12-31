package com.jalay.manageexpenses.util

object MerchantNormalizer {
    
    private val normalizationRules = mapOf(
        "zomato" to "Zomato",
        "swiggy" to "Swiggy",
        "amazon" to "Amazon",
        "flipkart" to "Flipkart",
        "uber" to "Uber",
        "ola" to "Ola",
        "netflix" to "Netflix",
        "spotify" to "Spotify",
        "google play" to "Google Play",
        "paytm" to "Paytm",
        "phonepe" to "PhonePe",
        "jiomart" to "JioMart",
        "blinkit" to "Blinkit",
        "bigbasket" to "BigBasket",
        "starbucks" to "Starbucks",
        "mcdonald" to "McDonald's",
        "burger king" to "Burger King",
        "domino" to "Domino's",
        "pizza hut" to "Pizza Hut",
        "kfc" to "KFC",
        "pvr" to "PVR Cinemas",
        "inox" to "INOX",
        "bookmyshow" to "BookMyShow"
    )

    fun normalize(name: String): String {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return "Unknown Merchant"

        // Remove common transaction suffixes and prefixes
        var normalized = trimmedName
            .replace(Regex("\\*\\d+"), "") // Remove *123
            .replace(Regex("\\d+$"), "")   // Remove trailing numbers
            .trim()

        // Check against normalization rules
        for ((keyword, target) in normalizationRules) {
            if (normalized.contains(keyword, ignoreCase = true)) {
                return target
            }
        }

        // Title case for unknown merchants
        return normalized.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
