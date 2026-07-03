package com.astrotarot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrotarot.ui.theme.DimIvory
import com.astrotarot.ui.theme.Gold

@Composable
fun InfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onBack) {
            Text("← Back", color = Gold, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "HOW IT WORKS",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 4.sp,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "The theory behind the readings",
            style = MaterialTheme.typography.bodySmall,
            color = DimIvory,
            fontStyle = FontStyle.Italic,
        )

        Spacer(Modifier.height(24.dp))

        InfoSection(
            title = "A Living Correspondence",
            body = """
                Tarot and astrology have been intertwined for centuries. The 78 cards of a Tarot deck are not arbitrary images — each one carries a precise correspondence to the celestial world: a planet, a zodiac sign, or one of the four classical elements.

                This isn't decoration. It reflects a deeper idea: that the cosmos and the human psyche share the same underlying structure. Reading the cards becomes a way of reading the sky — and through the sky, yourself.
            """.trimIndent(),
        )

        InfoSection(
            title = "The Major Arcana",
            body = """
                The 22 Major Arcana cards represent the largest archetypal forces. Each is ruled by a planet or a sign of the zodiac. The Sun, The Moon, The Tower, The Wheel of Fortune — these aren't just evocative titles. They name the celestial body that governs that card's energy.

                When their ruling planet is prominent in the sky at the moment of your reading, these cards rise closer to the surface.
            """.trimIndent(),
        )

        InfoSection(
            title = "The Minor Arcana",
            body = """
                The 40 numbered cards of the Minor Arcana are woven through the zodiac in finer detail, each one associated with a specific slice of a specific sign. The Court Cards — Page, Knight, Queen, King — each preside over a broader arc of the heavens.

                At any given moment, the planets occupy exact positions in the zodiac. The cards corresponding to those positions become activated — more likely to speak, more likely to be heard.
            """.trimIndent(),
        )

        InfoSection(
            title = "Planetary Aspects",
            body = """
                Planets don't speak alone. When two planets align at a meaningful angle in the sky — what astrologers call an aspect — their influences intertwine. Some angles bring harmony and flow; others create tension and friction.

                Astro Tarot reads these relationships and lets them further shape which cards come forward, and how.
            """.trimIndent(),
        )

        InfoSection(
            title = "How the Reading Works",
            body = """
                When you draw a reading, the app quietly consults the actual sky above you — the real positions of the Sun, Moon, and planets at this moment and place, with no internet connection required.

                Every card carries a weight, and that weight shifts with the heavens. Cards whose celestial rulers are active, well-placed, or forming strong angles with other planets are drawn toward you more readily. The three cards that emerge are not chosen at random — they are the ones the sky is most insistently offering.

                The result is a snapshot of the celestial moment, translated into the symbolic language of Tarot.
            """.trimIndent(),
        )

        InfoSection(
            title = "Readings Across Time",
            body = """
                The sky can be read at any moment — past, present, or future. You can request a reading anchored to a date and time of your choosing: a night that changed everything, a birthday, an event yet to come.

                Past readings illuminate what the heavens were saying then. Future readings offer a glimpse of the planetary climate ahead — not a fixed fate, but an emerging atmosphere.
            """.trimIndent(),
        )

        Spacer(Modifier.height(32.dp))

        HorizontalDivider(color = DimIvory.copy(alpha = 0.2f))

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Card attributions follow the Western Hermetic tradition. Card imagery is from the Rider-Waite-Smith deck (1909), now in the public domain.",
            style = MaterialTheme.typography.bodySmall,
            color = DimIvory.copy(alpha = 0.5f),
            fontStyle = FontStyle.Italic,
            lineHeight = 18.sp,
        )

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun InfoSection(title: String, body: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Gold,
        letterSpacing = 1.5.sp,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = DimIvory,
        lineHeight = 22.sp,
    )
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(color = DimIvory.copy(alpha = 0.15f))
    Spacer(Modifier.height(24.dp))
}
