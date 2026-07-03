package com.astrotarot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AstroColorScheme = darkColorScheme(
    primary             = Gold,
    onPrimary           = DeepNavy,
    primaryContainer    = DimGold,
    onPrimaryContainer  = StarWhite,
    secondary           = MysticPurple,       // forest green
    onSecondary         = StarWhite,
    secondaryContainer  = CardSurface,
    onSecondaryContainer = LightPurple,       // sage green
    background          = DeepNavy,           // deep warm brown
    onBackground        = StarWhite,          // warm ivory
    surface             = MidnightBlue,       // mahogany
    onSurface           = StarWhite,
    surfaceVariant      = CardSurface,
    onSurfaceVariant    = DimWhite,           // muted warm ivory
    error               = ReversedRed,        // deep burgundy
    onError             = StarWhite,
)

@Composable
fun AstroTarotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AstroColorScheme,
        typography  = Typography,
        content     = content
    )
}
