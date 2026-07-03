package com.astrotarot.ui.theme

import androidx.compose.ui.graphics.Brush

// Horizontal gilt-lettering gradient: pale → deep → pale reads as metallic
val GoldTextBrush = Brush.linearGradient(
    colors = listOf(PaleGold, Gold, DeepGold, Gold, PaleGold),
)

val GoldFrameBrush = Brush.linearGradient(
    colors = listOf(PaleGold, Gold, DeepGold),
)
