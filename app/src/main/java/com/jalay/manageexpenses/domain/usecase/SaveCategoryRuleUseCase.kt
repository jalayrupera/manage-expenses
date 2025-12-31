package com.jalay.manageexpenses.domain.usecase

import com.jalay.manageexpenses.data.repository.TransactionRepository
import com.jalay.manageexpenses.domain.model.CategoryMapping

class SaveCategoryRuleUseCase(
    private val repository: TransactionRepository
) {
    /**
     * Extracts a keyword from a merchant/recipient name for rule matching.
     * Cleans up the name and returns a lowercase keyword.
     */
    fun extractKeyword(recipientName: String): String {
        return recipientName
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special chars
            .split("\\s+".toRegex())
            .firstOrNull { it.length >= 3 } // Take first word with 3+ chars
            ?: recipientName.trim().lowercase().take(10)
    }

    /**
     * Checks if a rule already exists for the given keyword.
     */
    suspend fun ruleExists(keyword: String): Boolean {
        return repository.getMappingByKeyword(keyword) != null
    }

    /**
     * Saves a new category rule for the given keyword.
     * Returns the ID of the new rule, or null if it failed.
     */
    suspend fun save(keyword: String, category: String, icon: String = "category"): Long? {
        val mapping = CategoryMapping(
            keyword = keyword,
            category = category,
            icon = icon,
            isCustom = true
        )
        return repository.insertCategoryMapping(mapping)
    }

    /**
     * Convenience method to check and save a rule for a recipient.
     * Returns true if a new rule was saved, false if rule already exists.
     */
    suspend operator fun invoke(recipientName: String, category: String, icon: String = "category"): Boolean {
        val keyword = extractKeyword(recipientName)
        if (ruleExists(keyword)) {
            return false
        }
        return save(keyword, category, icon) != null
    }
}
