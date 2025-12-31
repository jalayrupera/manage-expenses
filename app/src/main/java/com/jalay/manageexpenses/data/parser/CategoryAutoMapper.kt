package com.jalay.manageexpenses.data.parser

import com.jalay.manageexpenses.domain.model.CategoryMapping

class CategoryAutoMapper(
    private val getMappings: suspend () -> List<CategoryMapping>
) {

    suspend fun categorize(recipient: String, sender: String): String {
        val mappings = getMappings()

        for (mapping in mappings) {
            if (recipient.contains(mapping.keyword, ignoreCase = true)) {
                return mapping.category
            }
        }

        return when {
            sender.contains("bank", ignoreCase = true) -> "Transfers"
            sender.contains("wallet", ignoreCase = true) -> "Transfers"
            else -> "Other"
        }
    }

    fun getDefaultMappings(): List<CategoryMapping> {
        return listOf(
            CategoryMapping(keyword = "amazon", category = "Shopping", icon = "shopping_bag"),
            CategoryMapping(keyword = "flipkart", category = "Shopping", icon = "shopping_bag"),
            CategoryMapping(keyword = "myntra", category = "Shopping", icon = "shopping_bag"),
            CategoryMapping(keyword = "ajio", category = "Shopping", icon = "shopping_bag"),
            CategoryMapping(keyword = "tata cliq", category = "Shopping", icon = "shopping_bag"),
            CategoryMapping(keyword = "zomato", category = "Food & Dining", icon = "restaurant"),
            CategoryMapping(keyword = "swiggy", category = "Food & Dining", icon = "restaurant"),
            CategoryMapping(keyword = "domino", category = "Food & Dining", icon = "restaurant"),
            CategoryMapping(keyword = "pizza hut", category = "Food & Dining", icon = "restaurant"),
            CategoryMapping(keyword = "kfc", category = "Food & Dining", icon = "restaurant"),
            CategoryMapping(keyword = "uber", category = "Transport", icon = "directions_car"),
            CategoryMapping(keyword = "ola", category = "Transport", icon = "directions_car"),
            CategoryMapping(keyword = "metro", category = "Transport", icon = "train"),
            CategoryMapping(keyword = "irctc", category = "Transport", icon = "train"),
            CategoryMapping(keyword = "electricity", category = "Utilities", icon = "bolt"),
            CategoryMapping(keyword = "broadband", category = "Utilities", icon = "wifi"),
            CategoryMapping(keyword = "gas", category = "Utilities", icon = "local_fire_department"),
            CategoryMapping(keyword = "water", category = "Utilities", icon = "water_drop"),
            CategoryMapping(keyword = "netflix", category = "Entertainment", icon = "movie"),
            CategoryMapping(keyword = "spotify", category = "Entertainment", icon = "music_note"),
            CategoryMapping(keyword = "bookmyshow", category = "Entertainment", icon = "theater_comedy"),
            CategoryMapping(keyword = "prime", category = "Entertainment", icon = "play_circle"),
            CategoryMapping(keyword = "mobile", category = "Bills & Recharges", icon = "phone_android"),
            CategoryMapping(keyword = "dth", category = "Bills & Recharges", icon = "tv"),
            CategoryMapping(keyword = "recharge", category = "Bills & Recharges", icon = "payments"),
            CategoryMapping(keyword = "bank", category = "Transfers", icon = "account_balance"),
            CategoryMapping(keyword = "wallet", category = "Transfers", icon = "account_balance_wallet"),
            CategoryMapping(keyword = "paytm wallet", category = "Transfers", icon = "account_balance_wallet")
        )
    }
}