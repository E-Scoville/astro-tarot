package com.astrotarot.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.AspectType
import com.astrotarot.engine.domain.model.PlanetPosition
import com.astrotarot.engine.domain.model.WeightedCard
import com.astrotarot.ui.ReadingUiState
import com.astrotarot.ui.theme.CardBack
import com.astrotarot.ui.theme.CardSurface
import com.astrotarot.ui.theme.DimWhite
import com.astrotarot.ui.theme.Gold
import com.astrotarot.ui.theme.HarmonyTeal
import com.astrotarot.ui.theme.ReversedRed
import com.astrotarot.ui.theme.StarWhite
import com.astrotarot.ui.theme.TensionRose
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")

private val POSITION_LABELS = listOf(
    "I — Self & Present",
    "II — Action & Will",
    "III — Foundation",
)

@Composable
fun ReadingScreen(
    state: ReadingUiState.Success,
    onNewReading: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val utcTime = Instant.ofEpochMilli(state.timestamp)
        .atOffset(ZoneOffset.UTC)
        .format(TIME_FMT)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
    ) {
        // ── Header ─────────────────────────────────────────────
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "YOUR READING",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp,
            )
            Text(
                text = "${"%.3f".format(state.lat)}°, ${"%.3f".format(state.lon)}°  ·  $utcTime",
                style = MaterialTheme.typography.bodySmall,
                color = DimWhite,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.height(24.dp))
        }

        // ── 3-Card Spread ──────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.reading.forEachIndexed { i, wc ->
                    FlippingTarotCard(
                        weightedCard = wc,
                        positionLabel = POSITION_LABELS[i],
                        revealDelayMs = 300L + i * 500L,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // ── Active Aspects ─────────────────────────────────────
        item {
            ExpandableSection(
                title = "ACTIVE ASPECTS  (${state.aspects.size})",
            ) {
                state.aspects.forEach { aspect ->
                    AspectRow(aspect)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Planet Positions ───────────────────────────────────
        item {
            ExpandableSection(title = "PLANETARY POSITIONS") {
                state.positions.forEach { pos ->
                    PlanetRow(pos)
                }
                Text(
                    text = "Ascendant ${"%.1f".format(state.ascendantDegree)}°" +
                            "  ·  MC ${"%.1f".format(state.midheavenDegree)}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = DimWhite,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        // ── New Reading button ─────────────────────────────────
        item {
            Button(
                onClick = onNewReading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("NEW READING", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Flipping card ─────────────────────────────────────────────────────────────

@Composable
fun FlippingTarotCard(
    weightedCard: WeightedCard,
    positionLabel: String,
    revealDelayMs: Long,
    modifier: Modifier = Modifier,
) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(revealDelayMs)
        revealed = true
    }

    val rotation by animateFloatAsState(
        targetValue = if (revealed) 0f else 180f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                },
        ) {
            if (rotation <= 90f) {
                CardFace(weightedCard)
            } else {
                // Mirror the back so it faces forward during reverse spin
                Box(Modifier.graphicsLayer { rotationY = 180f }) {
                    CardBackFace()
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = positionLabel,
            style = MaterialTheme.typography.labelSmall,
            color = DimWhite,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CardFace(wc: WeightedCard) {
    val borderColor = if (wc.reversed) ReversedRed else Gold
    val card = wc.card

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurface)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Card name
        Text(
            text = card.name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = borderColor,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // Reversed badge
        if (wc.reversed) {
            Text(
                text = "REVERSED",
                style = MaterialTheme.typography.labelSmall,
                color = ReversedRed,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
            )
        } else {
            Spacer(Modifier.height(12.dp))
        }

        // Description
        Text(
            text = if (wc.reversed) card.reversedDescription else card.baseDescription,
            style = MaterialTheme.typography.bodySmall,
            color = StarWhite,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )

        // Weight
        Text(
            text = "${"%.1f".format(wc.weight)}×",
            style = MaterialTheme.typography.labelSmall,
            color = Gold,
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
private fun CardBackFace() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(CardBack)
            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("✦", fontSize = 36.sp, color = Gold.copy(alpha = 0.5f))
    }
}

// ── Expandable section ────────────────────────────────────────────────────────

@Composable
private fun ExpandableSection(
    title: String,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
            Text(
                text = if (expanded) "▲" else "▼",
                color = DimWhite,
                fontSize = 10.sp,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                HorizontalDivider(color = Gold.copy(alpha = 0.15f))
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}

// ── Aspect row ────────────────────────────────────────────────────────────────

@Composable
private fun AspectRow(aspect: Aspect) {
    val color = if (aspect.type.isHarmonious) HarmonyTeal else TensionRose
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${aspect.planet1.take(4)} ${aspect.type.symbol} ${aspect.planet2.take(4)}",
            style = MaterialTheme.typography.bodySmall,
            color = StarWhite,
            modifier = Modifier.width(130.dp),
        )
        Text(
            text = aspect.type.label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = "${"%.1f".format(aspect.orb)}° orb",
            style = MaterialTheme.typography.bodySmall,
            color = DimWhite,
        )
    }
}

// ── Planet row ────────────────────────────────────────────────────────────────

@Composable
private fun PlanetRow(pos: PlanetPosition) {
    val retro = if (pos.isRetrograde) " ℞" else ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = pos.planet.take(7) + retro,
            style = MaterialTheme.typography.bodySmall,
            color = if (pos.isRetrograde) TensionRose else StarWhite,
            modifier = Modifier.width(90.dp),
        )
        Text(
            text = pos.sign.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodySmall,
            color = DimWhite,
            modifier = Modifier.width(90.dp),
        )
        Text(
            text = "${"%.1f".format(pos.longitude)}°",
            style = MaterialTheme.typography.bodySmall,
            color = DimWhite,
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.End,
        )
        Text(
            text = "H${pos.house}",
            style = MaterialTheme.typography.bodySmall,
            color = Gold.copy(alpha = 0.7f),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End,
        )
    }
}
