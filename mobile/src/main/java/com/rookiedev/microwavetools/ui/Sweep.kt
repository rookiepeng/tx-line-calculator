package com.rookiedev.microwavetools.ui

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/** One frequency of a sweep. Each list holds one value, or even then odd for coupled lines; empty when unavailable. */
data class SweepPoint(val f: Double, val z0: List<Double>, val eeff: List<Double>, val loss: List<Double>)

enum class SweepQuantity(@param:StringRes val label: Int) {
    Z0(R.string.sweep_z0),
    EEFF(R.string.sweep_eeff),
    LOSS(R.string.sweep_loss),
}

private fun SweepPoint.valuesOf(q: SweepQuantity) = when (q) {
    SweepQuantity.Z0 -> z0
    SweepQuantity.EEFF -> eeff
    SweepQuantity.LOSS -> loss
}

/**
 * Analyzes the line in [base] (SI) at [points] evenly spaced frequencies from [start] to [stop] Hz.
 * Frequencies where the analysis fails are skipped. Loss is in dB/m.
 */
fun sweep(line: LineType, base: Map<FieldId, Double>, start: Double, stop: Double, points: Int): List<SweepPoint> {
    require(points >= 2 && stop > start)
    val engine = line.engine()
    return (0 until points).mapNotNull { i ->
        val f = start + (stop - start) * i / (points - 1)
        val v = base + (FieldId.FREQ to f)
        val r = engine.analyze(v)
        if (r.errors.isNotEmpty()) return@mapNotNull null
        val o = lossOutputs(line, v)
        if (line.coupled) {
            SweepPoint(
                f,
                listOfNotNull(r.values[FieldId.Z0E], r.values[FieldId.Z0O]),
                listOfNotNull(o[Output.EEFF_E], o[Output.EEFF_O]),
                listOfNotNull(o[Output.ALPHA_E], o[Output.ALPHA_O]),
            )
        } else {
            SweepPoint(
                f,
                listOfNotNull(r.values[FieldId.Z0]),
                listOfNotNull(o[Output.EEFF]),
                listOfNotNull(o[Output.ALPHA]),
            )
        }
    }
}

/**
 * Plots Z0, εeff or loss against frequency for the last calculated design.
 *
 * @param freqUnit unit of the frequency field, used for the inputs and the x axis.
 * @param perLength scale from dB/m and its label, for the loss plot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SweepDialog(
    line: LineType,
    base: Map<FieldId, Double>,
    freqUnit: String,
    perLength: Pair<Double, String>,
    onDismiss: () -> Unit,
) {
    val fScale = Constants.value2Hz(1.0, freqUnit)
    val f0 = base.getValue(FieldId.FREQ) / fScale
    var startText by rememberSaveable { mutableStateOf(resultNumber(f0 / 10)) }
    var stopText by rememberSaveable { mutableStateOf(resultNumber(f0 * 2)) }
    var pointsText by rememberSaveable { mutableStateOf("101") }
    var quantity by rememberSaveable { mutableStateOf(SweepQuantity.Z0) }

    val start = startText.toDoubleOrNull()?.takeIf { it > 0.0 }
    val stop = stopText.toDoubleOrNull()?.takeIf { start != null && it > start }
    val points = pointsText.toIntOrNull()?.takeIf { it in 2..1001 }
    val data by produceState<List<SweepPoint>?>(null, start, stop, points) {
        value = if (start != null && stop != null && points != null) {
            withContext(Dispatchers.Default) { sweep(line, base, start * fScale, stop * fScale, points) }
        } else null
    }
    var cursor by remember(data, quantity) { mutableStateOf<Int?>(null) }

    val nSeries = if (line.coupled) 2 else 1
    val hasLoss = data?.any { it.loss.size == nSeries } == true
    val shown = if (quantity == SweepQuantity.LOSS && !hasLoss) SweepQuantity.Z0 else quantity
    val (yScale, yUnit) = when (shown) {
        SweepQuantity.Z0 -> 1.0 to "Ω"
        SweepQuantity.EEFF -> 1.0 to ""
        SweepQuantity.LOSS -> perLength
    }
    val usable = data.orEmpty().filter { it.valuesOf(shown).size == nSeries }
    val xs = usable.map { it.f / fScale }
    val series = (0 until nSeries).map { k -> usable.map { it.valuesOf(shown)[k] * yScale } }
    val colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    val seriesNames = if (line.coupled) listOf(stringResource(R.string.sweep_even), stringResource(R.string.sweep_odd))
    else listOf("")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 720.dp),
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(stringResource(R.string.sweep_title), style = MaterialTheme.typography.headlineSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SweepField(stringResource(R.string.sweep_start), startText, { startText = it }, start == null, freqUnit, Modifier.weight(1f))
                    SweepField(stringResource(R.string.sweep_stop), stopText, { stopText = it }, stop == null, freqUnit, Modifier.weight(1f))
                    SweepField(stringResource(R.string.sweep_points), pointsText, { pointsText = it }, points == null, null, Modifier.weight(0.8f))
                }
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SweepQuantity.entries.forEachIndexed { i, q ->
                        SegmentedButton(
                            selected = shown == q,
                            onClick = { quantity = q },
                            enabled = q != SweepQuantity.LOSS || hasLoss,
                            shape = SegmentedButtonDefaults.itemShape(i, SweepQuantity.entries.size),
                        ) { Text(stringResource(q.label)) }
                    }
                }

                val readout = cursor?.takeIf { it < xs.size }?.let { i ->
                    val values = series.indices.joinToString(" · ") { k ->
                        "${seriesNames[k]} ${resultNumber(series[k][i])} $yUnit".trim()
                    }
                    "f = ${resultNumber(xs[i])} $freqUnit   $values"
                } ?: stringResource(R.string.sweep_hint)
                Text(readout, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (xs.size >= 2) {
                    SweepChart(
                        xs = xs,
                        series = series,
                        colors = colors,
                        cursor = cursor,
                        onCursor = { cursor = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    )
                    Text(
                        "${stringResource(R.string.sweep_x_axis)} ($freqUnit)" + if (yUnit.isNotEmpty()) "  ·  y: $yUnit" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (line.coupled) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            seriesNames.forEachIndexed { k, name ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(10.dp).background(colors[k], CircleShape))
                                    Spacer(Modifier.width(6.dp))
                                    Text(name, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                } else if (data != null) {
                    Text(stringResource(R.string.sweep_no_data), color = MaterialTheme.colorScheme.error)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val context = LocalContext.current
                    TextButton(
                        onClick = {
                            val csv = sweepCsv(data.orEmpty(), line.coupled, freqUnit, perLength)
                            shareCsv(context, csv, context.getString(R.string.sweep_title))
                        },
                        enabled = !data.isNullOrEmpty(),
                    ) { Text(stringResource(R.string.sweep_export)) }
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_presets)) }
                }
            }
        }
    }
}

/**
 * Sweep points as CSV: frequency in [freqUnit], then Z0, εeff and loss (in [perLength]'s unit)
 * per mode. Values a point lacks are left empty. Numbers use '.' whatever the locale.
 */
fun sweepCsv(points: List<SweepPoint>, coupled: Boolean, freqUnit: String, perLength: Pair<Double, String>): String {
    val modes = if (coupled) listOf("even", "odd") else listOf("")
    fun col(name: String, unit: String) = modes.map { m -> listOf(name, m).filter { it.isNotEmpty() }.joinToString(" ") + if (unit.isEmpty()) "" else " ($unit)" }
    val header = listOf("f ($freqUnit)") + col("Z0", "Ohm") + col("eeff", "") + col("loss", perLength.second)
    val fScale = Constants.value2Hz(1.0, freqUnit)
    fun num(x: Double) = String.format(java.util.Locale.US, "%.6g", x)
    fun cells(values: List<Double>, scale: Double = 1.0) = modes.indices.map { i -> values.getOrNull(i)?.let { num(it * scale) } ?: "" }
    val rows = points.map { p ->
        listOf(num(p.f / fScale)) + cells(p.z0) + cells(p.eeff) + cells(p.loss, perLength.first)
    }
    return (listOf(header) + rows).joinToString("\n") { it.joinToString(",") } + "\n"
}

/** Writes [csv] to the app cache and opens the share sheet for it. */
private fun shareCsv(context: Context, csv: String, subject: String) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "sweep.csv").apply { writeText(csv) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("text/csv")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, subject))
}

@Composable
private fun SweepField(
    label: String,
    text: String,
    onChange: (String) -> Unit,
    isError: Boolean,
    suffix: String?,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        label = { Text(label, maxLines = 1) },
        suffix = suffix?.let { { Text(it) } },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}

/** A "nice" tick spacing (1, 2 or 5 × 10ⁿ) giving about [target] intervals over [range]. */
internal fun niceStep(range: Double, target: Int): Double {
    val raw = range / target
    val mag = 10.0.pow(floor(log10(raw)))
    val n = raw / mag
    return mag * when {
        n < 1.5 -> 1.0
        n < 3.0 -> 2.0
        n < 7.0 -> 5.0
        else -> 10.0
    }
}

/**
 * A tick value with just enough decimals to tell neighbouring ticks apart: a fixed number of
 * significant digits fails on a nearly flat curve, where every tick would print the same.
 */
internal fun tickLabel(t: Double, step: Double): String {
    if (abs(t) < step * 1e-6) return "0"
    val decimals = ceil(-log10(step) - 1e-9).toInt().coerceIn(0, 12)
    return java.math.BigDecimal(t).setScale(decimals, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
}

/**
 * Line chart of [series] over evenly spaced [xs], with a grid, tick labels and a cursor
 * the user places by tapping or dragging.
 */
@Composable
private fun SweepChart(
    xs: List<Double>,
    series: List<List<Double>>,
    colors: List<Color>,
    cursor: Int?,
    onCursor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val cursorColor = MaterialTheme.colorScheme.onSurface
    val currentOnCursor by rememberUpdatedState(onCursor)

    val xMin = xs.first()
    val xMax = xs.last()
    val all = series.flatten()
    var yMin = all.min()
    var yMax = all.max()
    if (yMax - yMin < 1e-9 * max(1.0, abs(yMax))) {
        val pad = max(abs(yMax) * 0.01, 1e-6)
        yMin -= pad; yMax += pad
    }
    val yStep = niceStep(yMax - yMin, 4)
    val yLo = floor(yMin / yStep) * yStep
    val yHi = ceil(yMax / yStep) * yStep
    val yTicks = generateSequence(yLo) { it + yStep }.takeWhile { it <= yHi + yStep * 1e-6 }.toList()
    val xStep = niceStep(xMax - xMin, 4)
    val xTicks = generateSequence(ceil(xMin / xStep) * xStep) { it + xStep }
        .takeWhile { it <= xMax + xStep * 1e-6 }.toList()

    Canvas(
        modifier.pointerInput(xs.size) {
            val left = 56.dp.toPx()
            val right = 12.dp.toPx()
            fun pick(x: Float) {
                val fr = ((x - left) / (size.width - left - right)).coerceIn(0f, 1f)
                currentOnCursor((fr * (xs.size - 1)).roundToInt())
            }
            awaitEachGesture {
                val down = awaitFirstDown()
                pick(down.position.x)
                drag(down.id) { pick(it.position.x); it.consume() }
            }
        },
    ) {
        val left = 56.dp.toPx()
        val right = 12.dp.toPx()
        val top = 8.dp.toPx()
        val bottom = 24.dp.toPx()
        val w = size.width - left - right
        val h = size.height - top - bottom
        fun px(x: Double) = left + ((x - xMin) / (xMax - xMin)).toFloat() * w
        fun py(y: Double) = top + (1f - ((y - yLo) / (yHi - yLo)).toFloat()) * h
        val gap = 6.dp.toPx()

        for (t in yTicks) {
            val y = py(t)
            drawLine(gridColor, Offset(left, y), Offset(left + w, y), 1.dp.toPx())
            val label = measurer.measure(tickLabel(t, yStep), labelStyle)
            drawText(label, topLeft = Offset(left - label.size.width - gap, y - label.size.height / 2f))
        }
        for (t in xTicks) {
            val x = px(t)
            drawLine(gridColor, Offset(x, top), Offset(x, top + h), 1.dp.toPx())
            val label = measurer.measure(tickLabel(t, xStep), labelStyle)
            val lx = (x - label.size.width / 2f).coerceIn(0f, size.width - label.size.width)
            drawText(label, topLeft = Offset(lx, top + h + gap / 2))
        }

        series.forEachIndexed { k, s ->
            val path = Path()
            s.forEachIndexed { i, v ->
                val x = px(xs[i]); val y = py(v)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, colors[k % colors.size], style = Stroke(width = 2.dp.toPx()))
        }

        cursor?.takeIf { it < xs.size }?.let { i ->
            val x = px(xs[i])
            drawLine(cursorColor.copy(alpha = 0.6f), Offset(x, top), Offset(x, top + h), 1.dp.toPx())
            series.forEachIndexed { k, s -> drawCircle(colors[k % colors.size], 4.dp.toPx(), Offset(x, py(s[i]))) }
        }
    }
}
