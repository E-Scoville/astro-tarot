package com.astrotarot.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.astrotarot.ui.theme.Gold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws a subtle Art Nouveau ornamental pattern behind the composable.
 * Corner botanical sprays, mid-edge diamonds, and a faint central medallion —
 * all at low opacity so underlying content stays readable.
 */
fun Modifier.artNouveauBackground(): Modifier = this.drawBehind {
    drawScreenPattern(Gold)
}

private fun DrawScope.drawScreenPattern(g: Color) {
    val w    = size.width
    val h    = size.height
    val cx   = w / 2f
    val cy   = h / 2f
    val hair = 0.6.dp.toPx()
    val fine = 1.0.dp.toPx()

    // ── Micro crosshatch — just barely visible ──────────────────────────
    val gridStep = 24.dp.toPx()
    var pos = -h
    while (pos < w + h) {
        drawLine(g.copy(alpha = 0.030f), Offset(pos, 0f), Offset(pos + h, h), hair)
        drawLine(g.copy(alpha = 0.030f), Offset(pos, 0f), Offset(pos - h, h), hair)
        pos += gridStep
    }

    // ── Border rule — thin line inset from every edge ───────────────────
    val borderInset = 16.dp.toPx()
    val bPath = Path().apply {
        moveTo(borderInset, borderInset)
        lineTo(w - borderInset, borderInset)
        lineTo(w - borderInset, h - borderInset)
        lineTo(borderInset, h - borderInset)
        close()
    }
    drawPath(bPath, g.copy(alpha = 0.12f), style = Stroke(hair * 0.6f))

    // ── Corner botanical sprays ─────────────────────────────────────────
    val spray = minOf(w, h) * 0.24f   // spray reach from corner
    listOf(
        Offset(borderInset, borderInset)       to Pair( 1f,  1f),
        Offset(w - borderInset, borderInset)   to Pair(-1f,  1f),
        Offset(borderInset, h - borderInset)   to Pair( 1f, -1f),
        Offset(w - borderInset, h - borderInset) to Pair(-1f, -1f),
    ).forEach { (c, d) ->
        val (dx, dy) = d
        drawCornerSpray(c, dx, dy, spray, g, hair)
    }

    // ── Mid-edge ornaments ──────────────────────────────────────────────
    listOf(
        Offset(cx, borderInset),
        Offset(cx, h - borderInset),
        Offset(borderInset, cy),
        Offset(w - borderInset, cy),
    ).forEach { pt ->
        drawMidEdgeOrnament(pt, g, hair)
    }

    // ── Central faint medallion ─────────────────────────────────────────
    val R  = minOf(w, h) * 0.32f
    val r1 = R * 0.60f
    val r2 = R * 0.32f
    val r3 = R * 0.14f

    drawCircle(g.copy(alpha = 0.06f), R,  Offset(cx, cy), style = Stroke(hair))
    drawCircle(g.copy(alpha = 0.08f), r1, Offset(cx, cy), style = Stroke(hair * 0.7f))

    // 12 petal ovals on the mid-ring
    val petalRingR = (R + r1) / 2f
    val petalH     = (R - r1) * 0.82f
    val petalW     = petalH * 0.36f
    for (i in 0..11) {
        val angle = (2.0 * PI * i / 12).toFloat()
        val px    = cx + petalRingR * cos(angle)
        val py    = cy + petalRingR * sin(angle)
        withTransform({
            rotate(degrees = angle * (180f / PI.toFloat()) + 90f, pivot = Offset(px, py))
        }) {
            drawOval(
                g.copy(alpha = 0.07f),
                topLeft = Offset(px - petalW / 2f, py - petalH / 2f),
                size    = Size(petalW, petalH),
                style   = Stroke(hair * 0.6f),
            )
        }
    }

    // 12 dots between petals on inner ring
    for (i in 0..11) {
        val angle = (2.0 * PI * (i + 0.5) / 12).toFloat()
        drawCircle(
            g.copy(alpha = 0.10f),
            radius = 1.4.dp.toPx(),
            center = Offset(cx + r1 * cos(angle), cy + r1 * sin(angle)),
        )
    }

    drawCircle(g.copy(alpha = 0.10f), r2, Offset(cx, cy), style = Stroke(hair * 0.8f))
    drawLine(g.copy(alpha = 0.08f), Offset(cx, cy - r2), Offset(cx, cy + r2), hair * 0.6f)
    drawLine(g.copy(alpha = 0.08f), Offset(cx - r2, cy), Offset(cx + r2, cy), hair * 0.6f)
    drawCircle(g.copy(alpha = 0.12f), r3, Offset(cx, cy), style = Stroke(fine * 0.7f))
}

// ── Corner botanical spray ────────────────────────────────────────────────────
// Two nested bezier arcs, three leaf buds, and a curling tendril.

private fun DrawScope.drawCornerSpray(
    c: Offset, dx: Float, dy: Float,
    reach: Float, g: Color, hair: Float,
) {
    // Primary arc — main stem sweeping from corner inward
    drawPath(Path().apply {
        moveTo(c.x + dx * reach, c.y)
        cubicTo(
            c.x + dx * reach * 0.55f, c.y,
            c.x,                       c.y + dy * reach * 0.55f,
            c.x,                       c.y + dy * reach,
        )
    }, g.copy(alpha = 0.18f), style = Stroke(hair))

    // Secondary arc — inner echo, slightly smaller
    val gap = 8.dp.toPx()
    drawPath(Path().apply {
        moveTo(c.x + dx * (reach - gap), c.y + dy * gap)
        cubicTo(
            c.x + dx * (reach - gap) * 0.55f, c.y + dy * gap,
            c.x + dx * gap,                    c.y + dy * (reach - gap) * 0.55f,
            c.x + dx * gap,                    c.y + dy * (reach - gap),
        )
    }, g.copy(alpha = 0.10f), style = Stroke(hair * 0.6f))

    // Diagonal cross-stem from corner toward centre
    val diag = reach * 0.62f
    drawPath(Path().apply {
        moveTo(c.x, c.y)
        cubicTo(
            c.x + dx * diag * 0.4f, c.y + dy * diag * 0.2f,
            c.x + dx * diag * 0.6f, c.y + dy * diag * 0.7f,
            c.x + dx * diag,        c.y + dy * diag,
        )
    }, g.copy(alpha = 0.14f), style = Stroke(hair * 0.8f))

    // Three leaf buds at 25%, 50%, 75% along the primary arc
    listOf(0.25f, 0.50f, 0.75f).forEachIndexed { idx, t ->
        // Approximate arc position using a simple parametric guess
        // For a symmetric cubic, midpoint ≈ corner + (dx*reach*(1-t), dy*reach*t)
        val lx = c.x + dx * reach * (1f - t) * 0.8f
        val ly = c.y + dy * reach * t * 0.8f
        val lr = (3.5f - idx * 0.6f).dp.toPx()   // outer buds slightly larger
        val rotDeg = if (dx == dy) 45f + idx * 15f else -45f - idx * 15f
        withTransform({
            rotate(rotDeg, pivot = Offset(lx, ly))
        }) {
            drawOval(
                g.copy(alpha = 0.16f),
                topLeft = Offset(lx - lr * 0.50f, ly - lr * 1.05f),
                size    = Size(lr, lr * 2.1f),
                style   = Stroke(hair * 0.7f),
            )
        }
    }

    // Tendril curl at end of diagonal cross-stem
    val tx = c.x + dx * diag
    val ty = c.y + dy * diag
    drawPath(Path().apply {
        moveTo(tx, ty)
        cubicTo(
            tx + dx * diag * 0.18f, ty + dy * diag * -0.08f,
            tx + dx * diag * 0.22f, ty + dy * diag * 0.18f,
            tx + dx * diag * 0.10f, ty + dy * diag * 0.22f,
        )
    }, g.copy(alpha = 0.12f), style = Stroke(hair * 0.6f))
}

// ── Mid-edge ornament ─────────────────────────────────────────────────────────
// Small fleur-de-lis-inspired shape: diamond body + two side lobes.

private fun DrawScope.drawMidEdgeOrnament(pt: Offset, g: Color, hair: Float) {
    val ds = 5.dp.toPx()
    // Central diamond
    drawPath(Path().apply {
        moveTo(pt.x,        pt.y - ds)
        lineTo(pt.x + ds * 0.55f, pt.y)
        lineTo(pt.x,        pt.y + ds)
        lineTo(pt.x - ds * 0.55f, pt.y)
        close()
    }, g.copy(alpha = 0.22f))
    // Side dots
    drawCircle(g.copy(alpha = 0.16f), ds * 0.28f,
        center = Offset(pt.x - ds * 0.85f, pt.y))
    drawCircle(g.copy(alpha = 0.16f), ds * 0.28f,
        center = Offset(pt.x + ds * 0.85f, pt.y))
}
