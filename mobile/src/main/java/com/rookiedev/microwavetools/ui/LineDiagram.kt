package com.rookiedev.microwavetools.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Schematic 3D drawings of each line type, drawn in an oblique projection: x to the right,
 * y up, z (the line's length) receding up and to the right. Not to scale.
 */

private data class V3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: V3) = V3(x + o.x, y + o.y, z + o.z)
}

/** What a shape is physically, so a field can highlight the matching part. */
private enum class Part { SIGNAL, GROUND, DIELECTRIC }

private sealed interface Shape { val part: Part }

/** Axis-aligned box. With [frontOnly] only the z0 face is drawn (a conductor buried in dielectric). */
private data class Block(
    val x0: Float, val x1: Float, val y0: Float, val y1: Float, val z0: Float, val z1: Float,
    override val part: Part, val frontOnly: Boolean = false,
) : Shape

/** Cylinder along z, centered at (cx, cy). */
private data class Tube(
    val cx: Float, val cy: Float, val r: Float, val z0: Float, val z1: Float, override val part: Part,
) : Shape

private sealed interface Dim { val field: FieldId; val label: String }

/** A dimension between [a] and [b], drawn shifted by [off] with extension lines. */
private data class Linear(
    override val field: FieldId, override val label: String,
    val a: V3, val b: V3, val off: V3, val labelDir: V3 = off,
    /** Draw a small cross at [a] and [b] (for center-to-center dimensions). */
    val centerMarks: Boolean = false,
) : Dim

/** A radius from [center] at [angleDeg] in the xy plane. */
private data class Radial(
    override val field: FieldId, override val label: String,
    val center: V3, val r: Float, val angleDeg: Float,
) : Dim

/** [zx]/[zy] set how z (the line's length) recedes on screen. */
private data class Scene(
    val shapes: List<Shape>,
    val dims: List<Dim>,
    val zx: Float = ZX,
    val zy: Float = ZY,
) {
    fun project(p: V3) = Offset(p.x + p.z * zx, p.y + p.z * zy)
}

private const val DEPTH = 7f
private val BELOW = V3(0f, -0.9f, 0f)
private val BELOW_BURIED = V3(0f, -0.7f, 0f)

private fun microstripScene(coupled: Boolean): Scene {
    val strips = if (coupled) listOf(2.5f to 5f, 6.2f to 8.7f) else listOf(4.5f to 7.5f)
    val h = 2.4f
    val t = if (coupled) 0.3f else 0.5f // coupled microstrip assumes zero thickness
    val top = h + t
    return Scene(
        shapes = listOf(
            Block(0f, 12f, -0.4f, 0f, 0f, DEPTH, Part.GROUND),
            Block(0f, 12f, 0f, h, 0f, DEPTH, Part.DIELECTRIC),
        ) + strips.map { (x0, x1) -> Block(x0, x1, h, top, 0f, DEPTH, Part.SIGNAL) },
        dims = buildList {
            val (w0, w1) = strips.first()
            add(Linear(FieldId.W, "W", V3(w0, h, 0f), V3(w1, h, 0f), BELOW))
            if (coupled) {
                add(Linear(FieldId.S, "G", V3(w1, h, 0f), V3(strips[1].first, h, 0f), BELOW))
            } else {
                add(Linear(FieldId.T, "T", V3(w1, h, 0f), V3(w1, top, 0f), V3(1.2f, 0f, 0f)))
            }
            add(Linear(FieldId.H, "H", V3(0f, 0f, 0f), V3(0f, h, 0f), V3(-1f, 0f, 0f)))
            add(Linear(FieldId.L, "L", V3(w0, h, 0f), V3(w0, h, DEPTH), V3(-1.6f, 0f, 0f)))
        },
    )
}

private fun striplineScene(coupled: Boolean): Scene {
    val strips = if (coupled) listOf(2.5f to 5f, 6.2f to 8.7f) else listOf(4.5f to 7.5f)
    val b = 4.4f
    val (y0, y1) = 1.95f to 2.45f
    return Scene(
        shapes = listOf(
            Block(0f, 12f, -0.4f, 0f, 0f, DEPTH, Part.GROUND),
            Block(0f, 12f, 0f, b, 0f, DEPTH, Part.DIELECTRIC),
            Block(0f, 12f, b, b + 0.4f, 0f, DEPTH, Part.GROUND),
        ) + strips.map { (x0, x1) -> Block(x0, x1, y0, y1, 0f, 0f, Part.SIGNAL, frontOnly = true) },
        dims = buildList {
            val (w0, w1) = strips.first()
            add(Linear(FieldId.W, "W", V3(w0, y0, 0f), V3(w1, y0, 0f), BELOW_BURIED))
            if (coupled) add(Linear(FieldId.S, "G", V3(w1, y0, 0f), V3(strips[1].first, y0, 0f), BELOW_BURIED))
            val last = strips.last().second
            add(Linear(FieldId.T, "T", V3(last, y0, 0f), V3(last, y1, 0f), V3(1.2f, 0f, 0f)))
            add(Linear(FieldId.H, "H", V3(0f, 0f, 0f), V3(0f, b, 0f), V3(-1f, 0f, 0f)))
            add(Linear(FieldId.L, "L", V3(w0, b + 0.4f, 0f), V3(w0, b + 0.4f, DEPTH), V3(-1.6f, 0f, 0f)))
        },
    )
}

private fun cpwScene(grounded: Boolean): Scene {
    val h = 2.4f
    val top = h + 0.5f
    return Scene(
        shapes = buildList {
            if (grounded) add(Block(0f, 13f, -0.4f, 0f, 0f, DEPTH, Part.GROUND))
            add(Block(0f, 13f, 0f, h, 0f, DEPTH, Part.DIELECTRIC))
            add(Block(0f, 4f, h, top, 0f, DEPTH, Part.GROUND))
            add(Block(5.2f, 7.8f, h, top, 0f, DEPTH, Part.SIGNAL))
            add(Block(9f, 13f, h, top, 0f, DEPTH, Part.GROUND))
        },
        dims = buildList {
            add(Linear(FieldId.W, "W", V3(5.2f, h, 0f), V3(7.8f, h, 0f), BELOW))
            add(Linear(FieldId.S, "G", V3(7.8f, h, 0f), V3(9f, h, 0f), BELOW))
            if (!grounded) add(Linear(FieldId.T, "T", V3(13f, h, 0f), V3(13f, top, 0f), V3(1.2f, 0f, 0f)))
            add(Linear(FieldId.H, "H", V3(0f, 0f, 0f), V3(0f, h, 0f), V3(-1f, 0f, 0f)))
            add(Linear(FieldId.L, "L", V3(2f, top, 0f), V3(2f, top, DEPTH), V3(0f, 0f, 0f), labelDir = V3(-1f, 0f, 0f)))
        },
    )
}

private fun coaxScene(): Scene {
    val shield = 3.4f
    val b = 3f
    val a = 0.85f
    val c = 0.8f
    val length = 6f
    return Scene(
        // Flatter, more sideways view than the planar lines so the end face stays large.
        zx = 0.7f * cos(Math.toRadians(20.0)).toFloat(),
        zy = 0.7f * sin(Math.toRadians(20.0)).toFloat(),
        shapes = listOf(
            Tube(0f, 0f, shield, 2f, length, Part.GROUND),
            Tube(0f, 0f, b, 1f, 2f, Part.DIELECTRIC),
            // Core face sits on the dielectric face, so a, b and c all measure in one plane.
            Tube(0f, c, a, 1f, 1f, Part.SIGNAL),
        ),
        dims = listOf(
            Radial(FieldId.B, "b", V3(0f, 0f, 1f), b, 225f),
            Radial(FieldId.A, "a", V3(0f, c, 1f), a, 135f),
            // Offset from the cable axis to the core center, measured to the right of the core.
            Linear(
                FieldId.C, "c", V3(0f, 0f, 1f), V3(0f, c, 1f), V3(a + 0.9f, 0f, 0f),
                centerMarks = true,
            ),
            Linear(FieldId.L, "L", V3(0f, shield, 2f), V3(0f, shield, length), V3(0f, 0.9f, 0f)),
        ),
    )
}

private fun LineType.scene(): Scene = when (this) {
    LineType.MLIN -> microstripScene(coupled = false)
    LineType.CMLIN -> microstripScene(coupled = true)
    LineType.SLIN -> striplineScene(coupled = false)
    LineType.CSLIN -> striplineScene(coupled = true)
    LineType.CPW -> cpwScene(grounded = false)
    LineType.GCPW -> cpwScene(grounded = true)
    LineType.COAX -> coaxScene()
}

/** Which physical parts light up while a field is being edited. */
private fun partsFor(field: FieldId?): Set<Part> = when (field) {
    FieldId.W, FieldId.L, FieldId.T, FieldId.A, FieldId.C,
    FieldId.Z0, FieldId.K, FieldId.Z0O, FieldId.Z0E, FieldId.PHS -> setOf(Part.SIGNAL)
    FieldId.H, FieldId.B, FieldId.ER -> setOf(Part.DIELECTRIC)
    else -> emptySet()
}

// Oblique projection of z: 35° up and to the right, at 0.55 scale.
private val ZX = 0.55f * cos(Math.toRadians(35.0)).toFloat()
private val ZY = 0.55f * sin(Math.toRadians(35.0)).toFloat()
private const val MARGIN = 1.5f

private fun Scene.bounds(): Rect {
    var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
    fun add(o: Offset, r: Float = 0f) {
        minX = min(minX, o.x - r); maxX = max(maxX, o.x + r)
        minY = min(minY, o.y - r); maxY = max(maxY, o.y + r)
    }
    for (s in shapes) when (s) {
        is Block -> for (x in listOf(s.x0, s.x1)) for (y in listOf(s.y0, s.y1)) for (z in listOf(s.z0, s.z1)) add(project(V3(x, y, z)))
        is Tube -> { add(project(V3(s.cx, s.cy, s.z0)), s.r); add(project(V3(s.cx, s.cy, s.z1)), s.r) }
    }
    for (d in dims) when (d) {
        is Linear -> { add(project(d.a + d.off)); add(project(d.b + d.off)) }
        is Radial -> add(project(d.center), d.r)
    }
    return Rect(minX, minY, maxX, maxY)
}

/**
 * Cross-section drawing of [line]. While a field is focused ([highlight]) its part and
 * dimension are drawn in the primary color and the rest fades back. Tapping a dimension
 * label calls [onSelect] with that field.
 */
@Composable
fun LineDiagram(
    line: LineType,
    highlight: FieldId?,
    onSelect: (FieldId) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    /** Fixed short height (for the pinned copy) instead of the natural aspect ratio. */
    compact: Boolean = false,
) {
    val scene = remember(line) { line.scene() }
    val bounds = remember(scene) { scene.bounds() }
    // Never taller than width / 1.7, so the diagram leaves room for the inputs.
    val aspect = max((bounds.width + 2 * MARGIN) / (bounds.height + 2 * MARGIN), 1.7f)

    // Keep showing the last highlighted field while the highlight fades out.
    var shown by remember(line) { mutableStateOf<FieldId?>(null) }
    if (highlight != null) shown = highlight
    val t by animateFloatAsState(if (highlight != null) 1f else 0f, tween(220), label = "highlight")

    val dark = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    val palette = remember(dark, scheme) {
        Palette.of(dark, scheme.primary, scheme.onSurfaceVariant, scheme.surface, scheme.surfaceContainerLow)
    }
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic)
    val hits = remember { mutableListOf<Pair<Rect, FieldId>>() }
    val fieldsOnScreen = remember(line) { line.fields.toSet() }

    Canvas(
        modifier
            .fillMaxWidth()
            .then(if (compact) Modifier.height(112.dp) else Modifier.aspectRatio(aspect))
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(line) {
                detectTapGestures { pos ->
                    val pad = 12.dp.toPx()
                    hits.firstOrNull { (rect, _) -> rect.inflate(pad).contains(pos) }?.let { onSelect(it.second) }
                }
            },
    ) {
        val scale = min(size.width / (bounds.width + 2 * MARGIN), size.height / (bounds.height + 2 * MARGIN))
        val ox = (size.width - bounds.width * scale) / 2 - bounds.left * scale
        val oy = (size.height - bounds.height * scale) / 2 + bounds.bottom * scale
        fun p(v: V3): Offset = scene.project(v).let { Offset(ox + it.x * scale, oy - it.y * scale) }

        val lit = partsFor(shown)
        for (s in scene.shapes) {
            val base = palette.base(s.part)
            val on = s.part in lit
            // Tint toward the highlight rather than replacing the color, so the material still reads.
            // Others fade toward the background but stay opaque, so overlapping parts never show through.
            val color = when {
                on -> lerp(base, palette.highlight, 0.6f * t)
                shown == null -> base
                else -> lerp(base, palette.background, 0.55f * t)
            }
            val alpha = 1f
            when (s) {
                is Block -> drawBlock(s, ::p, color, alpha, palette.edge)
                is Tube -> drawTube(s, ::p, scale, color, alpha, palette.edge)
            }
        }

        hits.clear()
        for (d in scene.dims) {
            if (d.field !in fieldsOnScreen) continue
            val on = d.field == shown
            val color = if (on) lerp(palette.ink, palette.highlight, t) else palette.ink
            val alpha = if (on || shown == null) 1f else 1f - 0.5f * t
            val stroke = (if (on) 1.2f + 0.8f * t else 1.2f).dp.toPx()
            val style = labelStyle.copy(
                color = color.copy(alpha = alpha),
                fontWeight = if (on && t > 0.5f) FontWeight.Bold else FontWeight.Medium,
            )
            val labelRect = when (d) {
                is Linear -> drawLinear(d, ::p, color.copy(alpha = alpha), stroke)
                is Radial -> drawRadial(d, ::p, scale, color.copy(alpha = alpha), stroke)
            }.let { anchor -> labelRect(measurer.measure(d.label, style).size.let { Size(it.width.toFloat(), it.height.toFloat()) }, anchor) }
            drawRoundRect(
                palette.labelBg.copy(alpha = 0.75f * alpha),
                topLeft = labelRect.topLeft - Offset(4.dp.toPx(), 0f),
                size = Size(labelRect.width + 8.dp.toPx(), labelRect.height),
                cornerRadius = CornerRadius(6.dp.toPx()),
            )
            drawText(measurer, d.label, topLeft = labelRect.topLeft, style = style)
            hits += labelRect to d.field
        }
    }
}

/** Where a label goes: [Anchor.at] is the attachment point, [Anchor.dir] points away from the drawing. */
private data class Anchor(val at: Offset, val dir: Offset)

private fun DrawScope.labelRect(textSize: Size, anchor: Anchor): Rect {
    val gap = 4.dp.toPx()
    // Push the label out along dir far enough that its box clears the anchor point.
    val reach = (abs(anchor.dir.x) * textSize.width + abs(anchor.dir.y) * textSize.height) / 2 + gap
    val c = anchor.at + anchor.dir * reach
    return Rect(c.x - textSize.width / 2, c.y - textSize.height / 2, c.x + textSize.width / 2, c.y + textSize.height / 2)
}

private fun Offset.unit(): Offset = getDistance().let { if (it == 0f) this else this / it }

private fun DrawScope.arrow(tip: Offset, dir: Offset, color: Color) {
    val len = 7.dp.toPx(); val half = 3.2.dp.toPx()
    val n = Offset(-dir.y, dir.x)
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        val back = tip - dir * len
        lineTo(back.x + n.x * half, back.y + n.y * half)
        lineTo(back.x - n.x * half, back.y - n.y * half)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawLinear(d: Linear, p: (V3) -> Offset, color: Color, stroke: Float): Anchor {
    val a0 = p(d.a); val b0 = p(d.b)
    val a = p(d.a + d.off); val b = p(d.b + d.off)
    if (d.centerMarks) {
        val m = 5.dp.toPx()
        for (c in listOf(a0, b0)) {
            drawLine(color, c - Offset(m, 0f), c + Offset(m, 0f), strokeWidth = stroke * 0.8f)
            drawLine(color, c - Offset(0f, m), c + Offset(0f, m), strokeWidth = stroke * 0.8f)
        }
    }
    val ext = (a - a0).unit() * 4.dp.toPx()
    if (a != a0) {
        drawLine(color, a0, a + ext, strokeWidth = stroke * 0.6f)
        drawLine(color, b0, b + ext, strokeWidth = stroke * 0.6f)
    }
    val dir = (b - a).unit()
    val len = (b - a).getDistance()
    if (len > 22.dp.toPx()) {
        drawLine(color, a, b, strokeWidth = stroke)
        arrow(a, -dir, color); arrow(b, dir, color)
    } else {
        // Too short for arrows inside: point them in from outside.
        val tail = 12.dp.toPx()
        drawLine(color, a - dir * tail, b + dir * tail, strokeWidth = stroke)
        arrow(a, dir, color); arrow(b, -dir, color)
    }
    val mid = (a + b) / 2f
    val labelDir = (p(d.a + d.labelDir) - a0).unit()
    return Anchor(mid, labelDir)
}

private fun DrawScope.drawRadial(d: Radial, p: (V3) -> Offset, scale: Float, color: Color, stroke: Float): Anchor {
    val c = p(d.center)
    val rad = Math.toRadians(d.angleDeg.toDouble())
    val dir = Offset(cos(rad).toFloat(), -sin(rad).toFloat())
    val end = c + dir * (d.r * scale)
    drawCircle(color, 2.5.dp.toPx(), c)
    drawLine(color, c, end, strokeWidth = stroke)
    arrow(end, dir, color)
    // Label just past the arrow tip, continuing outward.
    return Anchor(end + dir * 4.dp.toPx(), dir)
}

private fun DrawScope.face(points: List<Offset>, color: Color, alpha: Float, edge: Color) {
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (pt in points.drop(1)) lineTo(pt.x, pt.y)
        close()
    }
    drawPath(path, color, alpha = alpha)
    drawPath(path, edge, alpha = alpha, style = Stroke(width = 0.8.dp.toPx()))
}

private fun DrawScope.drawBlock(b: Block, p: (V3) -> Offset, color: Color, alpha: Float, edge: Color) {
    val front = listOf(V3(b.x0, b.y0, b.z0), V3(b.x1, b.y0, b.z0), V3(b.x1, b.y1, b.z0), V3(b.x0, b.y1, b.z0)).map(p)
    if (!b.frontOnly) {
        val side = listOf(V3(b.x1, b.y0, b.z0), V3(b.x1, b.y0, b.z1), V3(b.x1, b.y1, b.z1), V3(b.x1, b.y1, b.z0)).map(p)
        val top = listOf(V3(b.x0, b.y1, b.z0), V3(b.x1, b.y1, b.z0), V3(b.x1, b.y1, b.z1), V3(b.x0, b.y1, b.z1)).map(p)
        face(side, lerp(color, Color.Black, 0.22f), alpha, edge)
        face(top, lerp(color, Color.White, 0.28f), alpha, edge)
    }
    face(front, color, alpha, edge)
}

private fun DrawScope.drawTube(t: Tube, p: (V3) -> Offset, scale: Float, color: Color, alpha: Float, edge: Color) {
    val front = p(V3(t.cx, t.cy, t.z0))
    val back = p(V3(t.cx, t.cy, t.z1))
    val r = t.r * scale
    val n = (back - front).unit().let { Offset(-it.y, it.x) } * r
    val sideColor = lerp(color, Color.Black, 0.18f)
    drawCircle(sideColor, r, back, alpha = alpha)
    drawCircle(edge, r, back, alpha = alpha, style = Stroke(0.8.dp.toPx()))
    face(listOf(front + n, back + n, back - n, front - n), sideColor, alpha, sideColor)
    drawLine(edge, front + n, back + n, strokeWidth = 0.8.dp.toPx(), alpha = alpha)
    drawLine(edge, front - n, back - n, strokeWidth = 0.8.dp.toPx(), alpha = alpha)
    drawCircle(color, r, front, alpha = alpha)
    drawCircle(edge, r, front, alpha = alpha, style = Stroke(0.8.dp.toPx()))
}

/** Material colors for the drawing, tuned for light and dark themes. */
private data class Palette(
    val copper: Color, val dielectric: Color, val highlight: Color,
    val ink: Color, val edge: Color, val labelBg: Color, val background: Color,
) {
    fun base(part: Part) = when (part) {
        Part.SIGNAL, Part.GROUND -> copper
        Part.DIELECTRIC -> dielectric
    }

    companion object {
        fun of(dark: Boolean, primary: Color, ink: Color, surface: Color, background: Color) = if (dark) Palette(
            copper = Color(0xFFD99A3E), dielectric = Color(0xFF5E7F68), highlight = primary,
            ink = ink, edge = Color.Black.copy(alpha = 0.45f), labelBg = surface, background = background,
        ) else Palette(
            copper = Color(0xFFE8A33D), dielectric = Color(0xFFAFCDB6), highlight = primary,
            ink = ink, edge = Color.Black.copy(alpha = 0.22f), labelBg = surface, background = background,
        )
    }
}
