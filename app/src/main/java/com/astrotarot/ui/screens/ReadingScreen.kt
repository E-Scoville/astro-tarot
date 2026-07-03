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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AlertDialog
import com.astrotarot.ui.artNouveauBackground
import com.astrotarot.ui.LocalSoundManager
import com.astrotarot.ui.Lore
import com.astrotarot.ui.ASPECT_LORE
import com.astrotarot.ui.PLANET_LORE
import com.astrotarot.ui.markerLore
import com.astrotarot.engine.domain.model.Aspect
import com.astrotarot.engine.domain.model.PlanetPosition
import com.astrotarot.engine.domain.model.WeightedCard
import com.astrotarot.ui.ReadingUiState
import com.astrotarot.ui.theme.CardBack
import com.astrotarot.ui.theme.IndigoCard
import com.astrotarot.ui.theme.DimIvory
import com.astrotarot.ui.theme.Gold
import com.astrotarot.ui.theme.GoldFrameBrush
import com.astrotarot.ui.theme.GoldTextBrush
import com.astrotarot.ui.theme.HarmonyTeal
import com.astrotarot.ui.theme.IndigoSurface
import com.astrotarot.ui.theme.ReversedRed
import com.astrotarot.ui.theme.Ivory
import com.astrotarot.ui.theme.TensionRose
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform

private val TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm zzz")

private val PLANET_GLYPHS = mapOf(
    "SUN"     to "☉",
    "MOON"    to "☽",
    "MERCURY" to "☿",
    "VENUS"   to "♀",
    "MARS"    to "♂",
    "JUPITER" to "♃",
    "SATURN"  to "♄",
    "URANUS"  to "♅",
    "NEPTUNE" to "♆",
    "PLUTO"   to "♇",
)

@Composable
fun ReadingScreen(
    state: ReadingUiState.Success,
    onNewReading: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val utcTime      = Instant.ofEpochMilli(state.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(TIME_FMT)
    val soundManager = LocalSoundManager.current
    var soundOn      by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .artNouveauBackground()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        // ── Header ────────────────────────────────────────────
        item {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "YOUR READING",
                    style = MaterialTheme.typography.titleLarge.copy(brush = GoldTextBrush),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                )
                TextButton(
                    onClick = {
                        soundOn = !soundOn
                        soundManager?.muted = !soundOn
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                ) {
                    Text(
                        text = "♪",
                        color = if (soundOn) Gold.copy(alpha = 0.7f) else DimIvory.copy(alpha = 0.3f),
                        fontSize = 16.sp,
                    )
                }
            }
            Text(
                text = "${"%.3f".format(state.lat)}°, ${"%.3f".format(state.lon)}°  ·  $utcTime",
                style = MaterialTheme.typography.bodySmall,
                color = DimIvory,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = state.spread.name,
                style = MaterialTheme.typography.labelSmall,
                color = DimIvory,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.height(24.dp))
        }

        // ── Spread ──────────────────────────────────────────────
        item {
            // For larger spreads (e.g. the Twelve Houses) a shorter per-card stagger keeps
            // the full reveal from taking forever; 3-card spreads keep the original pacing.
            val cardCount = state.reading.size
            val staggerMs = if (cardCount > 3) 200L else 500L
            // Grid of up to 3 cards per row; partial rows are centered.
            // A single-card spread gets a larger, centered presentation.
            state.reading.chunked(3).forEachIndexed { rowIdx, rowCards ->
                if (rowIdx > 0) Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val sidePad = if (cardCount == 1) 0.5f else (3 - rowCards.size) / 2f
                    if (sidePad > 0f) Spacer(Modifier.weight(sidePad))
                    rowCards.forEachIndexed { j, wc ->
                        val i = rowIdx * 3 + j
                        FlippingTarotCard(
                            weightedCard   = wc,
                            positionLabel  = state.spread.positions.getOrNull(i)?.label ?: "",
                            revealDelayMs  = 300L + i * staggerMs,
                            modifier       = Modifier.weight(1f),
                        )
                    }
                    if (sidePad > 0f) Spacer(Modifier.weight(sidePad))
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // ── Active Aspects ─────────────────────────────────────
        item {
            ExpandableSection(title = "ACTIVE ASPECTS  (${state.aspects.size})") {
                state.aspects.forEach { AspectRow(it) }
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Planet Positions ───────────────────────────────────
        item {
            ExpandableSection(title = "PLANETARY POSITIONS") {
                state.positions.forEach { PlanetRow(it) }
                Text(
                    text = "Ascendant ${"%.1f".format(state.ascendantDegree)}°" +
                            "  ·  MC ${"%.1f".format(state.midheavenDegree)}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = DimIvory,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        // ── New Reading ────────────────────────────────────────
        item {
            androidx.compose.material3.OutlinedButton(
                onClick = onNewReading,
                shape  = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldFrameBrush),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = IndigoSurface.copy(alpha = 0.4f),
                    contentColor   = Gold,
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("✦  NEW READING  ✦", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
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
    val context = LocalContext.current
    var revealed by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }

    // Derive the drawable resource name from the card name.
    // Expected pattern: "card_the_fool", "card_ace_of_wands", "card_king_of_swords", etc.
    val imageResId = remember(weightedCard.card.name) {
        val resName = "card_" + weightedCard.card.name.lowercase().replace(" ", "_")
        context.resources.getIdentifier(resName, "drawable", context.packageName)
    }

    val soundManager = LocalSoundManager.current
    LaunchedEffect(Unit) {
        delay(revealDelayMs)
        soundManager?.playFlip()   // swoosh as card begins to turn
        revealed = true
        delay(350)                 // halfway through the 700 ms flip animation
        soundManager?.playReveal() // chime as face appears
    }

    val rotation by animateFloatAsState(
        targetValue    = if (revealed) 0f else 180f,
        animationSpec  = tween(700, easing = FastOutSlowInEasing),
        label          = "cardFlip",
    )

    if (showDetail) {
        CardDetailDialog(weightedCard, imageResId, onDismiss = { showDetail = false })
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .graphicsLayer {
                    rotationY      = rotation
                    cameraDistance = 12f * density
                }
                .then(
                    if (revealed) Modifier.clickable { showDetail = true } else Modifier
                ),
        ) {
            if (rotation <= 90f) {
                if (imageResId != 0) {
                    CardImageFace(imageResId, weightedCard.reversed)
                } else {
                    CardTextFace(weightedCard)
                }
            } else {
                Box(Modifier.graphicsLayer { rotationY = 180f }) { CardBackFace() }
            }
        }

        Spacer(Modifier.height(5.dp))

        // Card name appears after the flip completes
        if (revealed) {
            Text(
                text      = weightedCard.card.name,
                style     = MaterialTheme.typography.labelSmall,
                color     = if (weightedCard.reversed) ReversedRed else Gold,
                textAlign = TextAlign.Center,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis,
                fontSize  = 10.sp,
            )
            if (weightedCard.reversed) {
                Text(
                    text     = "reversed",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = ReversedRed.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                )
            }
            weightedCard.primaryInfluence?.let { planet ->
                val glyph  = PLANET_GLYPHS[planet.name] ?: ""
                val label  = planet.name.lowercase().replaceFirstChar { it.uppercase() }
                val marker = weightedCard.reversalMarker ?: ""
                Text(
                    text      = "$glyph $label $marker".trim(),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = if (weightedCard.reversed) TensionRose.copy(alpha = 0.65f)
                                else Gold.copy(alpha = 0.60f),
                    textAlign = TextAlign.Center,
                    fontSize  = 8.sp,
                )
            }
        }

        Spacer(Modifier.height(3.dp))
        Text(
            text      = positionLabel,
            style     = MaterialTheme.typography.labelSmall,
            color     = DimIvory,
            textAlign = TextAlign.Center,
            fontSize  = 9.sp,
        )
    }
}

// ── Ornate card frame ─────────────────────────────────────────────────────────
// Draws a double border + L-shaped corner flourishes over the card content.

private fun Modifier.ornateFrame(accentColor: Color): Modifier = this
    .clip(RoundedCornerShape(6.dp))
    .drawWithContent {
        drawContent()
        val sw   = 1.dp.toPx()
        val r    = 6.dp.toPx()
        val ins  = 5.dp.toPx()   // inner border inset
        val cl   = 11.dp.toPx()  // corner L-mark length
        val lsw  = 1.5f * sw     // L-mark stroke width
        val w = size.width
        val h = size.height

        // Outer border
        drawRoundRect(color = accentColor, style = Stroke(sw), cornerRadius = CornerRadius(r))
        // Inner border
        drawRoundRect(
            color     = accentColor.copy(alpha = 0.35f),
            topLeft   = Offset(ins, ins),
            size      = Size(w - ins * 2, h - ins * 2),
            style     = Stroke(sw * 0.6f),
            cornerRadius = CornerRadius(r * 0.5f),
        )
        // Corner L-marks at inner border corners
        listOf(
            Offset(ins, ins) to Pair( 1f,  1f),
            Offset(w - ins, ins) to Pair(-1f,  1f),
            Offset(ins, h - ins) to Pair( 1f, -1f),
            Offset(w - ins, h - ins) to Pair(-1f, -1f),
        ).forEach { (corner, dir) ->
            drawLine(accentColor, corner, corner.copy(x = corner.x + dir.first  * cl), lsw)
            drawLine(accentColor, corner, corner.copy(y = corner.y + dir.second * cl), lsw)
        }
    }

// ── Card faces ────────────────────────────────────────────────────────────────

@Composable
private fun CardImageFace(resId: Int, reversed: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .ornateFrame(if (reversed) ReversedRed else Gold),
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (reversed) 180f else 0f),
        )
    }
}

@Composable
private fun CardTextFace(wc: WeightedCard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoCard)
            .ornateFrame(if (wc.reversed) ReversedRed else Gold)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text       = wc.card.name.uppercase(),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = if (wc.reversed) ReversedRed else Gold,
            textAlign  = TextAlign.Center,
            lineHeight = 16.sp,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
        )
        if (wc.reversed) {
            Text("REVERSED", style = MaterialTheme.typography.labelSmall,
                color = ReversedRed, fontSize = 9.sp, letterSpacing = 1.sp)
        } else {
            Spacer(Modifier.height(12.dp))
        }
        Text(
            text      = if (wc.reversed) wc.card.reversedDescription else wc.card.baseDescription,
            style     = MaterialTheme.typography.bodySmall,
            color     = Ivory,
            textAlign = TextAlign.Center,
            fontSize  = 10.sp,
            lineHeight = 13.sp,
            maxLines  = 4,
            overflow  = TextOverflow.Ellipsis,
        )
        Text(
            text      = "${"%.1f".format(wc.weight)}×",
            style     = MaterialTheme.typography.labelSmall,
            color     = Gold,
            fontSize  = 10.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
private fun CardBackFace() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBack)
            .drawWithContent {
                drawContent()
                drawArtNouveauPattern()
            }
            .ornateFrame(Gold.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Text("✦", fontSize = 24.sp, color = Gold.copy(alpha = 0.50f))
    }
}

// ── Art Nouveau card-back pattern ─────────────────────────────────────────────

private fun DrawScope.drawArtNouveauPattern() {
    val w    = size.width
    val h    = size.height
    val cx   = w / 2f
    val cy   = h / 2f
    val ss   = minOf(w, h)
    val hair = 0.5.dp.toPx()
    val fine = 0.8.dp.toPx()
    val g    = Gold

    // ── Micro crosshatch background texture ──────────────────────────────
    val spacing = 10.dp.toPx()
    var pos = -h
    while (pos < w + h) {
        drawLine(g.copy(alpha = 0.055f), Offset(pos, 0f), Offset(pos + h, h), hair)
        drawLine(g.copy(alpha = 0.055f), Offset(pos, 0f), Offset(pos - h, h), hair)
        pos += spacing
    }

    // ── Corner vine brackets ─────────────────────────────────────────────
    // Each corner: two nested bezier arcs + a leaf bud at the midpoint
    val ins    = 10.dp.toPx()   // inset from card edge (inside ornateFrame)
    val vLen   = ss * 0.20f     // vine length
    listOf(
        Offset(ins, ins)         to Pair( 1f,  1f),   // TL
        Offset(w - ins, ins)     to Pair(-1f,  1f),   // TR
        Offset(ins, h - ins)     to Pair( 1f, -1f),   // BL
        Offset(w - ins, h - ins) to Pair(-1f, -1f),   // BR
    ).forEach { (c, d) ->
        val (dx, dy) = d
        // Outer arc
        drawPath(Path().apply {
            moveTo(c.x + dx * vLen, c.y)
            cubicTo(
                c.x + dx * vLen * 0.55f, c.y,
                c.x,                      c.y + dy * vLen * 0.55f,
                c.x,                      c.y + dy * vLen,
            )
        }, g.copy(alpha = 0.26f), style = Stroke(hair))
        // Inner echo arc
        val gap = 4.dp.toPx()
        drawPath(Path().apply {
            moveTo(c.x + dx * (vLen - gap), c.y + dy * gap)
            cubicTo(
                c.x + dx * (vLen - gap) * 0.55f, c.y + dy * gap,
                c.x + dx * gap,                   c.y + dy * (vLen - gap) * 0.55f,
                c.x + dx * gap,                   c.y + dy * (vLen - gap),
            )
        }, g.copy(alpha = 0.14f), style = Stroke(hair * 0.7f))
        // Leaf bud at arc midpoint
        val lx = c.x + dx * vLen * 0.46f
        val ly = c.y + dy * vLen * 0.46f
        val lr = 2.2.dp.toPx()
        withTransform({
            rotate(if (dx == dy) 45f else -45f, pivot = Offset(lx, ly))
        }) {
            drawOval(g.copy(alpha = 0.30f),
                topLeft = Offset(lx - lr * 0.55f, ly - lr * 1.1f),
                size    = Size(lr * 1.1f, lr * 2.2f),
                style   = Stroke(hair))
        }
    }

    // ── Mid-edge diamond accents ──────────────────────────────────────────
    val ds = 3.8.dp.toPx()
    listOf(
        Offset(cx, ins),         // top
        Offset(cx, h - ins),     // bottom
        Offset(ins, cy),         // left
        Offset(w - ins, cy),     // right
    ).forEach { pt ->
        drawPath(Path().apply {
            moveTo(pt.x,        pt.y - ds)
            lineTo(pt.x + ds * 0.55f, pt.y)
            lineTo(pt.x,        pt.y + ds)
            lineTo(pt.x - ds * 0.55f, pt.y)
            close()
        }, g.copy(alpha = 0.38f))
    }

    // ── Central Art Nouveau medallion ─────────────────────────────────────
    val R  = ss * 0.29f          // outer petal ring radius
    val r1 = ss * 0.17f          // inner ring / petal base radius
    val r2 = ss * 0.08f          // center circle radius

    // Outer guide circle (hairline)
    drawCircle(g.copy(alpha = 0.18f), R, Offset(cx, cy), style = Stroke(hair))

    // 8 petal ovals — each is an oval centred on the mid-ring, rotated outward
    val petalRingR = (R + r1) / 2f
    val petalH     = (R - r1) * 0.88f
    val petalW     = petalH * 0.38f
    for (i in 0..7) {
        val angle = (2.0 * PI * i / 8).toFloat()
        val px    = cx + petalRingR * cos(angle)
        val py    = cy + petalRingR * sin(angle)
        withTransform({
            rotate(degrees = angle * (180f / PI.toFloat()) + 90f, pivot = Offset(px, py))
        }) {
            drawOval(g.copy(alpha = 0.22f),
                topLeft = Offset(px - petalW / 2f, py - petalH / 2f),
                size    = Size(petalW, petalH),
                style   = Stroke(hair))
        }
    }

    // 8 tiny dots on the inner ring between petals
    for (i in 0..7) {
        val angle = (2.0 * PI * (i + 0.5) / 8).toFloat()
        drawCircle(g.copy(alpha = 0.38f),
            radius = 1.1.dp.toPx(),
            center = Offset(cx + r1 * cos(angle), cy + r1 * sin(angle)))
    }

    // Inner ring
    drawCircle(g.copy(alpha = 0.28f), r1, Offset(cx, cy), style = Stroke(fine))

    // 4-line cross through the inner ring
    drawLine(g.copy(alpha = 0.22f), Offset(cx, cy - r1), Offset(cx, cy + r1), hair)
    drawLine(g.copy(alpha = 0.22f), Offset(cx - r1, cy), Offset(cx + r1, cy), hair)

    // 45° diagonal cross (lighter)
    val diag = r1 * 0.70f
    drawLine(g.copy(alpha = 0.12f), Offset(cx - diag, cy - diag), Offset(cx + diag, cy + diag), hair)
    drawLine(g.copy(alpha = 0.12f), Offset(cx + diag, cy - diag), Offset(cx - diag, cy + diag), hair)

    // Center circle
    drawCircle(g.copy(alpha = 0.48f), r2, Offset(cx, cy), style = Stroke(fine))

    // Tiny filled center dot
    drawCircle(g.copy(alpha = 0.40f), r2 * 0.32f, Offset(cx, cy))
}

// ── Card detail dialog ────────────────────────────────────────────────────────

@Composable
private fun CardDetailDialog(
    wc: WeightedCard,
    imageResId: Int,
    onDismiss: () -> Unit,
) {
    val nameColor = if (wc.reversed) ReversedRed else Gold
    val description = if (wc.reversed) wc.card.reversedDescription else wc.card.baseDescription

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = IndigoSurface),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp),
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(imageResId),
                        contentDescription = wc.card.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(260.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .rotate(if (wc.reversed) 180f else 0f),
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    text       = wc.card.name.uppercase(),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = nameColor,
                    letterSpacing = 2.sp,
                )
                if (wc.reversed) {
                    Text(
                        text  = "REVERSED",
                        color = ReversedRed,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                    )
                }
                PlanetInfluenceLabel(wc)

                Spacer(Modifier.height(12.dp))

                Text(
                    text      = description,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Ivory,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text      = "Resonance: ${"%.2f".format(wc.weight)}×",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = Gold,
                    fontStyle = FontStyle.Italic,
                )

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("CLOSE", color = DimIvory, letterSpacing = 2.sp)
                }
            }
        }
    }
}

// ── Planet influence label ────────────────────────────────────────────────────

@Composable
private fun PlanetInfluenceLabel(wc: WeightedCard) {
    val planet = wc.primaryInfluence ?: return
    val glyph  = PLANET_GLYPHS[planet.name] ?: ""
    val name   = planet.name.lowercase().replaceFirstChar { it.uppercase() }

    var showLore by remember { mutableStateOf(false) }
    if (showLore) {
        val lores = listOfNotNull(PLANET_LORE[planet], if (wc.reversed) markerLore(wc.reversalMarker) else null)
        LoreDialog(lores = lores, onDismiss = { showLore = false })
    }

    if (wc.reversed) {
        val marker     = wc.reversalMarker ?: ""
        val flavorText = when (wc.reversalMarker) {
            "℞"  -> "retrograde — resisting this card's emergence"
            "□"  -> "in square tension — this card appears against its current"
            "☍"  -> "in opposition — this card appears under resistance"
            else -> "in tension — this card appears against the current sky"
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { showLore = true },
        ) {
            Text(
                text  = "$glyph  $name  $marker".trim(),
                color = TensionRose.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
                letterSpacing = 1.sp,
            )
            Text(
                text      = flavorText,
                color     = DimIvory,
                style     = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { showLore = true },
        ) {
            Text(
                text  = "$glyph  $name",
                color = Gold.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
                letterSpacing = 1.sp,
            )
            Text(
                text      = "strongest planetary influence in this reading",
                color     = DimIvory,
                style     = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Expandable section ────────────────────────────────────────────────────────

@Composable
private fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .ornateFrame(Gold.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Decorative ruled title: line ✦ TITLE ✦ line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(12.dp),
                    color    = Gold.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                )
                Text(
                    " ✦ ",
                    color    = Gold.copy(alpha = 0.6f),
                    fontSize = 8.sp,
                )
                Text(
                    title,
                    style        = MaterialTheme.typography.labelLarge,
                    color        = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                )
            }
            Text(if (expanded) "▲" else "▼", color = DimIvory, fontSize = 9.sp)
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                HorizontalDivider(color = Gold.copy(alpha = 0.2f), thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
}

// ── Aspect row ────────────────────────────────────────────────────────────────

@Composable
private fun AspectRow(aspect: Aspect) {
    val color = if (aspect.type.isHarmonious) HarmonyTeal else TensionRose
    var showLore by remember { mutableStateOf(false) }
    if (showLore) {
        ASPECT_LORE[aspect.type]?.let { lore ->
            LoreDialog(lores = listOf(lore), onDismiss = { showLore = false })
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showLore = true }
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text("${aspect.planet1.name.take(4)} ${aspect.type.symbol} ${aspect.planet2.name.take(4)}",
            style = MaterialTheme.typography.bodySmall, color = Ivory,
            modifier = Modifier.width(130.dp))
        Text(aspect.type.label, style = MaterialTheme.typography.bodySmall,
            color = color, modifier = Modifier.width(80.dp))
        Text("${"%.1f".format(aspect.orb)}° orb",
            style = MaterialTheme.typography.bodySmall, color = DimIvory)
    }
}

// ── Lore dialog ───────────────────────────────────────────────────────────────
// Tap-to-reveal plain-language explanation of a planet or aspect influence.

@Composable
private fun LoreDialog(lores: List<Lore>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = IndigoSurface,
        shape            = RoundedCornerShape(8.dp),
        modifier         = Modifier.border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        text = {
            Column {
                lores.forEachIndexed { i, lore ->
                    if (i > 0) {
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Gold.copy(alpha = 0.2f), thickness = 0.5.dp)
                        Spacer(Modifier.height(14.dp))
                    }
                    Text(
                        text  = "${lore.glyph}  ${lore.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = lore.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ivory,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DimIvory, style = MaterialTheme.typography.bodySmall)
            }
        },
    )
}

// ── Planet row ────────────────────────────────────────────────────────────────

@Composable
private fun PlanetRow(pos: PlanetPosition) {
    val retro = if (pos.isRetrograde) " ℞" else ""
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(pos.planet.name.take(7) + retro, style = MaterialTheme.typography.bodySmall,
            color = if (pos.isRetrograde) TensionRose else Ivory,
            modifier = Modifier.width(90.dp))
        Text(pos.sign.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodySmall, color = DimIvory,
            modifier = Modifier.width(90.dp))
        Text("${"%.1f".format(pos.longitude)}°", style = MaterialTheme.typography.bodySmall,
            color = DimIvory, modifier = Modifier.width(52.dp), textAlign = TextAlign.End)
        Text("H${pos.house}", style = MaterialTheme.typography.bodySmall,
            color = Gold.copy(alpha = 0.7f), modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End)
    }
}
