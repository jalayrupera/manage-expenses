package com.jalay.manageexpenses.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// === LIGHT THEME COLORS (Zinc palette) ===
val LightBackground = Color(0xFFFFFFFF)      // Pure white
val LightSurface = Color(0xFFFAFAFA)         // Zinc-50 - cards
val LightSurfaceVariant = Color(0xFFF4F4F5)  // Zinc-100 - secondary surface
val LightBorder = Color(0xFFE4E4E7)          // Zinc-200 - borders
val LightMuted = Color(0xFFA1A1AA)           // Zinc-400 - muted text
val LightMutedForeground = Color(0xFF71717A) // Zinc-500 - secondary text
val LightForeground = Color(0xFF09090B)      // Zinc-950 - primary text

// === DARK THEME COLORS (Zinc palette) ===
val DarkBackground = Color(0xFF09090B)       // Zinc-950 - background
val DarkSurface = Color(0xFF18181B)          // Zinc-900 - cards
val DarkSurfaceVariant = Color(0xFF27272A)   // Zinc-800 - secondary surface
val DarkBorder = Color(0xFF3F3F46)           // Zinc-700 - borders
val DarkMuted = Color(0xFF71717A)            // Zinc-500 - muted elements
val DarkMutedForeground = Color(0xFFA1A1AA)  // Zinc-400 - secondary text
val DarkForeground = Color(0xFFFAFAFA)       // Zinc-50 - primary text

// === SEMANTIC COLORS (consistent across themes) ===
val ExpenseRed = Color(0xFFEF4444)           // Red-500 - sent/expense
val ExpenseRedLight = Color(0xFFFEE2E2)      // Red-100 - light background
val ExpenseRedDark = Color(0xFF7F1D1D)       // Red-900 - dark background

val IncomeGreen = Color(0xFF22C55E)          // Green-500 - received/income
val IncomeGreenLight = Color(0xFFDCFCE7)     // Green-100 - light background
val IncomeGreenDark = Color(0xFF14532D)      // Green-900 - dark background

val WarningAmber = Color(0xFFF59E0B)         // Amber-500
val InfoBlue = Color(0xFF3B82F6)             // Blue-500

// === LEGACY COLORS (for backward compatibility during migration) ===
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val SentColor = ExpenseRed
val ReceivedColor = IncomeGreen
val AccentColor = Color(0xFF2196F3)
