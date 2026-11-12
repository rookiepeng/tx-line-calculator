package com.rookiedev.microwavetools.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.Animatable
import kotlin.math.hypot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SuggestionChip
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import com.rookiedev.microwavetools.libs.Materials
import com.rookiedev.microwavetools.libs.Substrate
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs
import com.rookiedev.microwavetools.ui.theme.LocalAccents

/**
 * One screen for every line type: a cross-section diagram, a Dimensions card (solved by
 * Synthesize) and a Parameters card (solved by Analyze), with the two actions pinned at the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    vm: CalculatorViewModel,
    onOpenDrawer: (() -> Unit)?,
    compactTopBar: Boolean = false,
) {
    val line = vm.line
    val state = vm.state
    // The header slides away while scrolling down and comes back as soon as you scroll up.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val focus = LocalFocusManager.current
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    var showSweep by rememberSaveable { mutableStateOf(false) }
    val fieldFocus = remember(line) { FieldFocus(line.fields) }
    val imeVisible = WindowInsets.isImeVisible

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val title = @Composable {
                Text(stringResource(line.title), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val navigationIcon = @Composable {
                if (onOpenDrawer != null) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(painterResource(R.drawable.ic_menu), stringResource(R.string.navigation_drawer_open))
                    }
                }
            }
            val actions: @Composable RowScope.() -> Unit = {
                IconButton(onClick = { confirmReset = true }) {
                    Icon(painterResource(R.drawable.ic_restore_white_24dp), stringResource(R.string.reset))
                }
            }
            // Semi-transparent: content scrolling behind the bar stays faintly visible
            // while the title remains readable.
            val barColors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
            )
            // A single-row bar so it can slide fully out of view; a two-row bar only collapses.
            // On short screens it also steps aside while typing, leaving room for the field.
            AnimatedVisibility(
                visible = !(compactTopBar && imeVisible),
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                TopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions,
                    colors = barColors,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            // On short screens the keyboard leaves almost no room, so give the action bar's space
            // to the field being edited; the bar returns when the keyboard closes.
            if (!(compactTopBar && imeVisible)) {
                ActionBar(
                    onSynthesize = { focus.clearFocus(); vm.synthesize() },
                    onAnalyze = { focus.clearFocus(); vm.analyze() },
                )
            }
        },
    ) { padding ->
        // Skip the top inset here and add it inside the scrolling column instead, so the cards
        // start below the header but scroll up behind the transparent bar.
        val layoutDirection = LocalLayoutDirection.current
        // Where the main diagram sits on screen, to know when it has scrolled under the header.
        var viewportTop by remember { mutableFloatStateOf(0f) }
        var diagramBottom by remember { mutableFloatStateOf(Float.MAX_VALUE) }
        var diagramHeight by remember { mutableIntStateOf(0) }
        val headerPx = with(LocalDensity.current) { padding.calculateTopPadding().toPx() }
        val diagramHidden = diagramBottom < viewportTop + headerPx + diagramHeight * 0.5f
        val trackDiagram = Modifier.onGloballyPositioned {
            diagramBottom = it.positionInRoot().y + it.size.height
            diagramHeight = it.size.height
        }
        BoxWithConstraints(
            Modifier
                .padding(
                    start = padding.calculateStartPadding(layoutDirection),
                    end = padding.calculateEndPadding(layoutDirection),
                    bottom = padding.calculateBottomPadding(),
                )
                .onGloballyPositioned { viewportTop = it.positionInRoot().y }
                // Keeps content above the keyboard when the action bar (which normally does it) is hidden.
                .consumeWindowInsets(padding)
                .imePadding()
        ) {
            val wide = maxWidth >= 720.dp
            val scroll = rememberScrollState()
            val dimensions = @Composable { DimensionsCard(vm, state, fieldFocus) }
            val parameters = @Composable {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ParametersCard(vm, state, fieldFocus)
                    if (state.extras.isNotEmpty()) ResultsCard(state, onSweep = { showSweep = true })
                    MaterialsCard(vm, state, fieldFocus)
                }
            }
            Column(
                Modifier
                    .verticalScroll(scroll)
                    .padding(top = padding.calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Diagram(line, fieldFocus, trackDiagram)
                            dimensions()
                        }
                        Column(Modifier.weight(1f)) { parameters() }
                    }
                } else {
                    Diagram(line, fieldFocus, trackDiagram)
                    dimensions()
                    parameters()
                }
                Spacer(Modifier.size(8.dp))
            }

            // Keep the status bar readable when the header has scrolled away.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            )

            // While editing with the main diagram scrolled away, pin a compact copy under the header
            // so the highlight stays in view.
            AnimatedVisibility(
                visible = fieldFocus.focused != null && diagramHidden && !compactTopBar,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = padding.calculateTopPadding() + 4.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                ) {
                    LineDiagram(
                        line = line,
                        highlight = fieldFocus.focused,
                        onSelect = { fieldFocus.requesters[it]?.requestFocus() },
                        contentDescription = stringResource(R.string.diagram_description, stringResource(line.title)),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        compact = true,
                    )
                }
            }
        }
    }

    val sweepBase = state.lastInputs
    if (showSweep && sweepBase != null) {
        SweepDialog(
            line = line,
            base = sweepBase,
            freqUnit = state.field(FieldId.FREQ).unit,
            perLength = lossUnit(state),
            onDismiss = { showSweep = false },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.reset_title)) },
            text = { Text(stringResource(R.string.reset_message)) },
            confirmButton = {
                TextButton(onClick = { vm.reset(); confirmReset = false }) { Text(stringResource(R.string.reset)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

/** Which field is being edited, and a way to move focus to any field (from the diagram). */
@Stable
private class FieldFocus(fields: List<FieldId>) {
    val requesters = fields.associateWith { FocusRequester() }
    var focused by mutableStateOf<FieldId?>(null)
}

@Composable
private fun Diagram(line: LineType, focus: FieldFocus, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth(),
    ) {
        LineDiagram(
            line = line,
            highlight = focus.focused,
            onSelect = { focus.requesters[it]?.requestFocus() },
            contentDescription = stringResource(R.string.diagram_description, stringResource(line.title)),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DimensionsCard(vm: CalculatorViewModel, state: CalculatorState, focus: FieldFocus) {
    val line = vm.line
    val outputs = vm.outputsOf(Action.SYNTHESIZE)
    SectionCard(
        icon = R.drawable.vt_ruler,
        title = stringResource(R.string.Dim),
        subtitle = stringResource(
            when {
                line == LineType.ECPW -> R.string.dim_subtitle_ecpw
                line.coupled -> R.string.dim_subtitle_coupled
                else -> R.string.dim_subtitle_pick
            },
        ),
        accent = LocalAccents.current.synthesize,
        reveal = Reveal(state.runId, state.lastAction == Action.SYNTHESIZE, fromBottomStart = true),
    ) {
        val hasRadios = line.targets.isNotEmpty()
        if (line == LineType.TWP) {
            PresetMenu(
                label = stringResource(R.string.preset_awg),
                items = Materials.wireGauges.map { (awg, d) ->
                    "AWG $awg · ${presetNumber(d * 1e3)} mm" to { vm.applyLength(FieldId.A, d) }
                },
            )
        }
        if (line == LineType.WG) {
            PresetMenu(
                label = stringResource(R.string.preset_wr),
                items = Materials.waveguides.map { wg ->
                    "${wg.name} · ${presetNumber(wg.bandLow / 1e9)}–${presetNumber(wg.bandHigh / 1e9)} GHz" to
                        { vm.applyWaveguide(wg.a, wg.b) }
                },
            )
        }
        for (id in line.dimensions) {
            FieldRow(
                vm = vm,
                id = id,
                fieldFocus = focus,
                field = state.field(id),
                solvedBy = if (id in outputs) Action.SYNTHESIZE else null,
                flash = id in state.updated,
                runId = state.runId,
                leading = if (!hasRadios) null else {
                    {
                        if (id in line.targets) {
                            val solveFor = stringResource(R.string.solve_for, stringResource(line.labelOf(id)))
                            RadioButton(
                                selected = state.target == id,
                                onClick = { vm.onTargetChange(id) },
                                colors = RadioButtonDefaults.colors(selectedColor = LocalAccents.current.synthesize),
                                modifier = Modifier.semantics {
                                    contentDescription = solveFor
                                },
                            )
                        } else {
                            Spacer(Modifier.width(48.dp))
                        }
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParametersCard(vm: CalculatorViewModel, state: CalculatorState, focus: FieldFocus) {
    val line = vm.line
    val anaOutputs = vm.outputsOf(Action.ANALYZE)
    SectionCard(
        icon = R.drawable.vt_wave,
        title = stringResource(R.string.Par),
        subtitle = stringResource(R.string.par_subtitle),
        accent = LocalAccents.current.analyze,
        reveal = Reveal(state.runId, state.lastAction == Action.ANALYZE, fromBottomStart = false),
    ) {
        if (line.coupled) {
            Text(
                stringResource(R.string.synthesize_from),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.useZ0k,
                    onClick = { vm.onModeChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text(stringResource(R.string.mode_z0k)) }
                SegmentedButton(
                    selected = !state.useZ0k,
                    onClick = { vm.onModeChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text(stringResource(R.string.mode_z0eo)) }
            }
        }
        for (id in line.parameters) {
            FieldRow(
                vm = vm,
                id = id,
                fieldFocus = focus,
                field = state.field(id),
                solvedBy = if (id in anaOutputs) Action.ANALYZE else null,
                flash = id in state.updated,
                runId = state.runId,
                leading = null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialsCard(vm: CalculatorViewModel, state: CalculatorState, focus: FieldFocus) {
    val line = vm.line
    var showSubstrates by rememberSaveable { mutableStateOf(false) }
    SectionCard(
        icon = R.drawable.vt_layers,
        title = stringResource(R.string.materials_title),
        subtitle = stringResource(R.string.materials_subtitle),
        accent = MaterialTheme.colorScheme.tertiary,
        reveal = Reveal(state.runId, active = false, fromBottomStart = false),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { showSubstrates = true }, label = { Text(stringResource(R.string.preset_substrate)) })
            PresetMenu(
                label = stringResource(R.string.preset_metal),
                items = Metal.entries.map { m ->
                    "${stringResource(m.label)} · ${presetNumber(m.msPerM)} MS/m" to { vm.applyMetal(m) }
                },
            )
            if (FieldId.T in line.fields) {
                PresetMenu(
                    label = stringResource(R.string.preset_copper),
                    items = Materials.copperWeights.map { (name, t) ->
                        "$name · ${presetNumber(t * 1e6)} µm" to { vm.applyThickness(t) }
                    },
                )
            }
        }
        for (id in MATERIAL_FIELDS) {
            FieldRow(
                vm = vm,
                id = id,
                fieldFocus = focus,
                field = state.field(id),
                solvedBy = null,
                flash = false,
                runId = state.runId,
                leading = null,
            )
        }
    }
    if (showSubstrates) {
        val hUnit = if (FieldId.H in line.fields) state.field(FieldId.H).unit else null
        SubstrateDialog(
            heightUnit = hUnit,
            onPick = { sub, h -> vm.applySubstrate(sub.er, sub.lossTangent, h); showSubstrates = false },
            onDismiss = { showSubstrates = false },
        )
    }
}

/** A chip that opens a menu of presets. */
@Composable
private fun PresetMenu(label: String, items: List<Pair<String, () -> Unit>>) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(onClick = { expanded = true }, label = { Text(label) })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for ((text, action) in items) {
                DropdownMenuItem(text = { Text(text) }, onClick = { action(); expanded = false })
            }
        }
    }
}

/** Lists substrate presets; tapping a thickness also sets H (shown in [heightUnit], or hidden when null). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubstrateDialog(heightUnit: String?, onPick: (Substrate, Double?) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.presets_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.presets_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                for (sub in Materials.substrates) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onPick(sub, null) }
                            .padding(vertical = 4.dp),
                    ) {
                        Text(sub.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(R.string.preset_values, presetNumber(sub.er), presetNumber(sub.lossTangent)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (heightUnit != null && sub.thicknesses.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (h in sub.thicknesses) {
                                SuggestionChip(
                                    onClick = { onPick(sub, h) },
                                    label = { Text("${presetNumber(Constants.meter2others(h, heightUnit))} $heightUnit") },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_presets)) } },
    )
}

/** εeff and losses from the last calculation; loss per length follows L's unit (dB/in for mil, else dB/cm). */
@Composable
private fun ResultsCard(state: CalculatorState, onSweep: () -> Unit) {
    val (scale, perLength) = lossUnit(state)
    val lUnit = state.fields[FieldId.L]?.unit ?: "mm"
    val delayPer = if (lUnit == "mil") "ps/in" else "ps/cm"
    val fUnit = state.field(FieldId.FREQ).unit
    SectionCard(
        icon = R.drawable.vt_chart,
        title = stringResource(R.string.results_title),
        subtitle = stringResource(R.string.results_subtitle),
        accent = MaterialTheme.colorScheme.tertiary,
        reveal = Reveal(state.runId, active = true, fromBottomStart = state.lastAction == Action.SYNTHESIZE),
    ) {
        for ((out, value) in state.extras.entries.sortedBy { it.key.ordinal }) {
            val (shown, unit) = when (out.unit) {
                OutputUnit.NONE -> value to ""
                OutputUnit.DB_PER_LENGTH -> value * scale to perLength
                OutputUnit.DB -> value to "dB"
                OutputUnit.LENGTH -> Constants.meter2others(value, lUnit) to lUnit
                OutputUnit.DELAY_PER_LENGTH -> value * 1e12 * scale to delayPer
                OutputUnit.MICRONS -> value * 1e6 to "µm"
                OutputUnit.FREQUENCY -> value / Constants.value2Hz(1.0, fUnit) to fUnit
                OutputUnit.OHMS -> value to "Ω"
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(out.label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Text("${resultNumber(shown)} $unit".trim(), style = MaterialTheme.typography.bodyLarge)
            }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            FilledTonalButton(onClick = onSweep) {
                Icon(painterResource(R.drawable.vt_chart), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sweep_button))
            }
        }
    }
}

/** Loss per length follows L's unit: dB/in for mil, else dB/cm. Returns (scale from per-metre, label). */
private fun lossUnit(state: CalculatorState): Pair<Double, String> =
    if (state.fields[FieldId.L]?.unit == "mil") 0.0254 to "dB/in" else 0.01 to "dB/cm"

/** Four significant digits for read-only results. */
internal fun resultNumber(v: Double): String =
    if (v == 0.0) "0" else BigDecimal(v).round(MathContext(4)).stripTrailingZeros().let {
        if (abs(v) >= 1e-4 && abs(v) < 1e7) it.toPlainString() else it.toString()
    }

/** Up to six significant digits, as preset values are filled into fields. */
private fun presetNumber(v: Double): String =
    BigDecimal.valueOf(v).round(MathContext(6)).stripTrailingZeros().toPlainString()

/** When and where a card plays its reveal animation. */
private data class Reveal(val runId: Int, val active: Boolean, val fromBottomStart: Boolean)

@Composable
private fun SectionCard(
    icon: Int,
    title: String,
    subtitle: String,
    accent: Color,
    reveal: Reveal,
    content: @Composable () -> Unit,
) {
    // Circular reveal from the original app: a circle of the action's color grows from a
    // corner across the card while fading out.
    val progress = remember { Animatable(1f) }
    LaunchedEffect(reveal.runId) {
        if (reveal.active && reveal.runId > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 450, easing = LinearEasing))
        }
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .drawWithContent {
                drawContent()
                val p = progress.value
                if (p < 1f) {
                    val origin = if (reveal.fromBottomStart) Offset(0f, size.height) else Offset(size.width, 0f)
                    drawCircle(
                        color = accent,
                        radius = hypot(size.width, size.height) * FastOutSlowInEasing.transform(p),
                        center = origin,
                        alpha = 1f - p * p,
                    )
                }
            },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(icon), contentDescription = null, tint = accent, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = accent)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}

/**
 * A value field plus its unit. Fields the chosen action will fill in are tinted with that
 * action's color; after a calculation the updated fields briefly flash.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FieldRow(
    vm: CalculatorViewModel,
    id: FieldId,
    fieldFocus: FieldFocus,
    field: FieldState,
    solvedBy: Action?,
    flash: Boolean,
    runId: Int,
    leading: (@Composable () -> Unit)?,
) {
    val accents = LocalAccents.current
    val accent = when (solvedBy) {
        Action.SYNTHESIZE -> accents.synthesize
        Action.ANALYZE -> accents.analyze
        null -> null
    }
    val flashColor = when (solvedBy) {
        Action.SYNTHESIZE -> accents.synthesizeContainer
        Action.ANALYZE -> accents.analyzeContainer
        null -> MaterialTheme.colorScheme.secondaryContainer
    }
    // Fade the highlight's alpha (not toward Color.Transparent, which would pass through grey).
    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(runId) {
        if (flash && runId > 0) {
            flashAlpha.snapTo(1f)
            flashAlpha.animateTo(0f, tween(durationMillis = 900))
        }
    }
    val containerColor = flashColor.copy(alpha = flashAlpha.value)

    val focus = LocalFocusManager.current
    val colors = if (accent != null) {
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = accent, unfocusedTextColor = accent,
            focusedBorderColor = accent, unfocusedBorderColor = accent.copy(alpha = 0.6f),
            focusedLabelColor = accent, unfocusedLabelColor = accent,
            focusedContainerColor = containerColor, unfocusedContainerColor = containerColor,
        )
    } else {
        OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor, unfocusedContainerColor = containerColor,
        )
    }

    Row(verticalAlignment = Alignment.Top) {
        if (leading != null) {
            Box(Modifier.padding(top = 8.dp)) { leading() }
        }
        OutlinedTextField(
            value = field.text,
            onValueChange = { vm.onTextChange(id, it) },
            label = { Text(stringResource(vm.line.labelOf(id)), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            suffix = id.unitKind.suffix?.let { { Text(it) } },
            isError = field.error != null,
            supportingText = field.error?.let { { Text(stringResource(it)) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            colors = colors,
            modifier = Modifier
                .weight(1f)
                .focusRequester(fieldFocus.requesters.getValue(id))
                .onFocusChanged {
                    if (it.isFocused) fieldFocus.focused = id
                    else if (fieldFocus.focused == id) fieldFocus.focused = null
                },
        )
        if (id.unitKind.selectable) {
            Spacer(Modifier.width(8.dp))
            UnitPicker(
                units = id.unitKind.choices,
                selected = field.unit,
                onSelect = { vm.onUnitChange(id, it) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitPicker(units: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.widthIn(max = 112.dp),
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.unit)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (u in units) {
                DropdownMenuItem(
                    text = { Text(u) },
                    onClick = { onSelect(u); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun ActionBar(onSynthesize: () -> Unit, onAnalyze: () -> Unit) {
    val accents = LocalAccents.current
    Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                // Sit above the keyboard when it is open, otherwise above the navigation bar.
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onSynthesize,
                colors = ButtonDefaults.buttonColors(containerColor = accents.synthesize, contentColor = accents.onSynthesize),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
            ) {
                Icon(painterResource(R.drawable.ic_arrow_upward), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.Syn_button))
            }
            Button(
                onClick = onAnalyze,
                colors = ButtonDefaults.buttonColors(containerColor = accents.analyze, contentColor = accents.onAnalyze),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
            ) {
                Icon(painterResource(R.drawable.ic_arrow_downward), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.Ana_button))
            }
        }
    }
}
