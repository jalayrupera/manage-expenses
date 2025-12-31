package com.jalay.manageexpenses.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.presentation.ui.theme.*

// === SPACING CONSTANTS ===
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

// === CORNER RADIUS CONSTANTS ===
object Radius {
    val sm = 6.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val full = 9999.dp
}

// === SHADCN-STYLE CARD ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadcnCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    Card(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Column(content = content)
    }
}

// === BADGE COMPONENT ===
@Composable
fun Badge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor = color.copy(alpha = 0.15f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.sm),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// === STATUS BADGE ===
@Composable
fun StatusBadge(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) IncomeGreen else ExpenseRed
    Badge(text = text, color = color, modifier = modifier)
}

// === ICON BADGE (icon in a circle) ===
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

// === DIVIDER ===
@Composable
fun ShadcnDivider(
    modifier: Modifier = Modifier
) {
    Divider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}

// === SECTION HEADER ===
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        action?.invoke()
    }
}

// === STAT CARD (for summary stats) ===
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    iconBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    ShadcnCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconBadge(
                    icon = icon,
                    backgroundColor = iconBackgroundColor,
                    iconColor = iconColor,
                    size = 32.dp
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

// === EMPTY STATE ===
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBadge(
            icon = icon,
            size = 64.dp,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// === LOADING INDICATOR ===
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// === SHADCN-STYLE BUTTON ===
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// === AMOUNT TEXT (formatted currency) ===
@Composable
fun AmountText(
    amount: Double,
    isExpense: Boolean,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium
) {
    val color = if (isExpense) ExpenseRed else IncomeGreen
    val prefix = if (isExpense) "-" else "+"

    Text(
        text = "$prefix₹${String.format("%,.2f", kotlin.math.abs(amount))}",
        modifier = modifier,
        style = style,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}
