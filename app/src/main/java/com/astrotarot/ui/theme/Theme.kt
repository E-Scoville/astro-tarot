package com.astrotarot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AstroColorScheme = darkColorScheme(
    primary          = Gold,
    onPrimary        = DeepNavy,
    primaryContainer = DimGold,
    onPrimaryContainer = StarWhite,
    secondary        = MysticPurple,
    onSecondary      = StarWhite,
    secondaryContainer = CardSurface,
    onSecondaryContainer = LightPurple,
    background       = DeepNavy,
    onBackground     = StarWhite,
    surface          = MidnightBlue,
    onSurface        = StarWhite,
    surfaceVariant   = CardSurface,
    onSurfaceVariant = DimWhite,
    error            = ReversedRed,
    onError          = StarWhite,
)

@Composable
fun AstroTarotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AstroColorScheme,
        typography  = Typography,
        content     = content
    )
}
