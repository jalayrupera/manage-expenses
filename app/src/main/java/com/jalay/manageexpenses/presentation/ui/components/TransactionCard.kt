package com.jalay.manageexpenses.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.domain.model.Transaction
import com.jalay.manageexpenses.domain.model.TransactionType
import com.jalay.manageexpenses.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    val isExpense = transaction.transactionType == TransactionType.SENT
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val amountPrefix = if (isExpense) "-" else "+"

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon + Details
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon with background
                CategoryIconBadge(
                    category = transaction.category,
                    size = 44.dp
                )

                Spacer(modifier = Modifier.width(Spacing.md))

                // Transaction details
                Column(modifier = Modifier.weight(1f)) {
                    // Recipient name
                    Text(
                        text = transaction.recipientName.ifEmpty { "Unknown" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    // Date and category
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(transaction.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right side: Amount + Badge
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Amount
                Text(
                    text = "$amountPrefix₹${formatAmount(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Status badge or note indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (transaction.notes != null) {
                        Icon(
                            imageVector = Icons.Outlined.StickyNote2,
                            contentDescription = "Has notes",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Transaction type badge
                    StatusBadge(
                        text = if (isExpense) "Sent" else "Received",
                        isPositive = !isExpense
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryIconBadge(
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = null,
            modifier = Modifier.size(size * 0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CategoryIcon(
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Icon(
        imageVector = getCategoryIcon(category),
        contentDescription = null,
        modifier = modifier.size(size),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "shopping" -> Icons.Default.ShoppingBag
        "food & dining" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "utilities" -> Icons.Default.Build
        "entertainment" -> Icons.Default.Movie
        "bills & recharges" -> Icons.Default.PhoneAndroid
        "transfers" -> Icons.Default.AccountBalance
        "health" -> Icons.Default.HealthAndSafety
        "travel" -> Icons.Default.Flight
        "education" -> Icons.Default.School
        "groceries" -> Icons.Default.LocalGroceryStore
        else -> Icons.Default.Category
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val dayInMillis = 24 * 60 * 60 * 1000L

    return when {
        diff < dayInMillis -> {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
        diff < 2 * dayInMillis -> "Yesterday"
        diff < 7 * dayInMillis -> {
            val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        String.format("%,.0f", amount)
    } else {
        String.format("%.2f", amount)
    }
}
