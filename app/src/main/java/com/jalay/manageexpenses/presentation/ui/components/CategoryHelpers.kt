package com.jalay.manageexpenses.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.presentation.ui.theme.*

/**
 * Returns the appropriate Material icon for a category.
 */
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "shopping" -> Icons.Default.ShoppingBag
        "food & dining" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "utilities" -> Icons.Default.Build
        "entertainment" -> Icons.Default.Movie
        "bills & recharges" -> Icons.Default.PhoneAndroid
        "transfers" -> Icons.Default.AccountBalance
        "health", "healthcare" -> Icons.Default.HealthAndSafety
        "travel" -> Icons.Default.Flight
        "education" -> Icons.Default.School
        "groceries" -> Icons.Default.LocalGroceryStore
        "savings" -> Icons.Default.Savings
        "investments" -> Icons.Default.TrendingUp
        "rent", "housing" -> Icons.Default.Home
        "insurance" -> Icons.Default.Security
        "gifts" -> Icons.Default.CardGiftcard
        "personal care" -> Icons.Default.Spa
        "subscriptions" -> Icons.Default.Subscriptions
        "salary", "income" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Category
    }
}

/**
 * Returns a color associated with a category for visual distinction.
 */
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "shopping" -> Color(0xFFF59E0B)       // Amber
        "food & dining" -> Color(0xFFEF4444) // Red
        "transport" -> Color(0xFF3B82F6)      // Blue
        "utilities" -> Color(0xFF6366F1)      // Indigo
        "entertainment" -> Color(0xFFEC4899) // Pink
        "bills & recharges" -> Color(0xFF8B5CF6) // Purple
        "transfers" -> Color(0xFF10B981)      // Emerald
        "health", "healthcare" -> Color(0xFF14B8A6) // Teal
        "travel" -> Color(0xFF06B6D4)         // Cyan
        "education" -> Color(0xFF0EA5E9)      // Sky
        "groceries" -> Color(0xFF22C55E)      // Green
        "savings" -> Color(0xFF84CC16)        // Lime
        "investments" -> Color(0xFF10B981)    // Emerald
        else -> Color(0xFF64748B)             // Slate
    }
}

/**
 * A circular badge containing a category icon.
 */
@Composable
fun CategoryIconBadge(
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = iconTint
        )
    }
}

/**
 * A colored circular badge containing a category icon.
 * Uses the category's associated color for the background.
 */
@Composable
fun ColoredCategoryBadge(
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val categoryColor = getCategoryColor(category)
    CategoryIconBadge(
        category = category,
        modifier = modifier,
        size = size,
        backgroundColor = categoryColor.copy(alpha = 0.15f),
        iconTint = categoryColor
    )
}

/**
 * A simple category icon without a background.
 */
@Composable
fun CategoryIcon(
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Icon(
        imageVector = getCategoryIcon(category),
        contentDescription = null,
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * List of all available categories for use in dropdowns and pickers.
 */
val availableCategories = listOf(
    "Shopping",
    "Food & Dining",
    "Transport",
    "Utilities",
    "Entertainment",
    "Bills & Recharges",
    "Transfers",
    "Health",
    "Travel",
    "Education",
    "Groceries",
    "Savings",
    "Investments",
    "Rent",
    "Insurance",
    "Gifts",
    "Personal Care",
    "Subscriptions",
    "Other"
)
