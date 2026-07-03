package com.astrotarot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AstroColorScheme = darkColorScheme(
    primary             = Gold,
    onPrimary           = MidnightVelvet,
    primaryContainer    = DeepGold,
    onPrimaryContainer  = Ivory,
    secondary           = Oxblood,            // deep oxblood
    onSecondary         = Ivory,
    secondaryContainer  = IndigoCard,
    onSecondaryContainer = RoseGold,          // soft rose metallic
    background          = MidnightVelvet,     // deep midnight blue
    onBackground        = Ivory,              // warm ivory
    surface             = IndigoSurface,      // indigo panel
    onSurface           = Ivory,
    surfaceVariant      = IndigoCard,
    onSurfaceVariant    = DimIvory,           // cool muted ivory-blue
    error               = ReversedRed,        // rich crimson-oxblood
    onError             = Ivory,
)

@Composable
fun AstroTarotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AstroColorScheme,
        typography  = Typography,
        content     = content
    )
}
