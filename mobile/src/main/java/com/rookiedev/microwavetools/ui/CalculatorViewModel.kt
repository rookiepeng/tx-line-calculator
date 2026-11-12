package com.rookiedev.microwavetools.ui

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class FieldState(val text: String, val unit: String, val error: Int? = null)

enum class Action { ANALYZE, SYNTHESIZE }

data class CalculatorState(
    val fields: Map<FieldId, FieldState>,
    /** Selected synthesis target; null for coupled lines. */
    val target: FieldId?,
    /** Coupled lines: synthesize from Z0 & k (true) or from Z0e & Z0o (false). */
    val useZ0k: Boolean,
    /** Fields written by the last calculation, for a brief highlight. */
    val updated: Set<FieldId> = emptySet(),
    /** Increments on every calculation so the UI can replay the highlight. */
    val runId: Int = 0,
    /** The action behind [runId], so the UI knows which card plays its reveal. */
    val lastAction: Action? = null,
    /** Read-only results (εeff, losses) of the last successful calculation. */
    val extras: Map<Output, Double> = emptyMap(),
    /** SI inputs and solved values of the last successful calculation; the base for a frequency sweep. */
    val lastInputs: Map<FieldId, Double>? = null,
) {
    fun field(id: FieldId) = fields.getValue(id)
}

/**
 * Holds one calculator screen's inputs, runs Analyze/Synthesize, and saves values
 * under the same SharedPreferences keys used by earlier app versions.
 */
class CalculatorViewModel(
    val line: LineType,
    private val prefs: SharedPreferences,
) : ViewModel() {

    private val engine = line.engine()

    var state by mutableStateOf(load())
        private set

    fun onTextChange(id: FieldId, text: String) = update {
        copy(fields = fields + (id to field(id).copy(text = text, error = null)))
    }

    fun onUnitChange(id: FieldId, unit: String) = update {
        copy(fields = fields + (id to field(id).copy(unit = unit)))
    }

    fun onTargetChange(target: FieldId) = update { copy(target = target) }

    fun onModeChange(useZ0k: Boolean) = update { copy(useZ0k = useZ0k) }

    fun reset() = update { defaults() }

    /** Fills εr and tanδ from a preset and, when given and the line has one, the substrate height (m). */
    fun applySubstrate(er: Double, lossTangent: Double, height: Double?) = update {
        var f = fields + (FieldId.ER to field(FieldId.ER).copy(text = presetText(er), error = null)) +
            (FieldId.TAND to field(FieldId.TAND).copy(text = presetText(lossTangent), error = null))
        if (height != null && FieldId.H in line.fields) {
            val h = f.getValue(FieldId.H)
            f = f + (FieldId.H to h.copy(text = presetText(fromSi(FieldId.H, height, h.unit)), error = null))
        }
        copy(fields = f)
    }

    fun applyMetal(metal: Metal) = update {
        copy(fields = fields + (FieldId.SIGMA to field(FieldId.SIGMA).copy(text = presetText(metal.msPerM), error = null)))
    }

    /** Sets the waveguide's inner dimensions (m) in their current units. */
    fun applyWaveguide(a: Double, b: Double) = update {
        val fa = field(FieldId.A)
        val fb = field(FieldId.B)
        copy(
            fields = fields +
                (FieldId.A to fa.copy(text = presetText(fromSi(FieldId.A, a, fa.unit)), error = null)) +
                (FieldId.B to fb.copy(text = presetText(fromSi(FieldId.B, b, fb.unit)), error = null)),
        )
    }

    /** Sets the metal thickness (m) in its current unit. */
    fun applyThickness(thickness: Double) = applyLength(FieldId.T, thickness)

    /** Sets a length field from metres, in its current unit. */
    fun applyLength(id: FieldId, meters: Double) = update {
        val f = field(id)
        copy(fields = fields + (id to f.copy(text = presetText(fromSi(id, meters, f.unit)), error = null)))
    }

    fun analyze() = run(Action.ANALYZE)

    fun synthesize() = run(Action.SYNTHESIZE)

    /** Which fields the given action fills in (and so does not need as input). */
    fun outputsOf(action: Action): Set<FieldId> = when (action) {
        Action.ANALYZE -> line.analysisOutputs
        Action.SYNTHESIZE -> line.synthesisOutputs(state.target, state.useZ0k)
    }

    private fun run(action: Action) {
        val outputs = outputsOf(action)
        // L is optional for analysis and phase is optional for synthesis; loss inputs are always optional.
        val optional = MATERIAL_FIELDS.toSet() + if (action == Action.ANALYZE) FieldId.L else FieldId.PHS
        val inputs = line.fields.filter { it !in outputs }

        val errors = mutableMapOf<FieldId, Int>()
        val si = mutableMapOf<FieldId, Double>()
        for (id in inputs) {
            val f = state.field(id)
            val text = f.text.trim()
            if (text.isEmpty()) {
                if (id !in optional) errors[id] = id.emptyError
                continue
            }
            val value = text.toDoubleOrNull()
            val problem = when {
                value == null || value.isNaN() || value.isInfinite() -> R.string.unreasonable_value
                else -> validate(id, value, f.unit)
            }
            if (problem != null) errors[id] = problem else si[id] = toSi(id, value!!, f.unit)
        }

        val result = if (errors.isEmpty()) {
            if (action == Action.ANALYZE) engine.analyze(si)
            else engine.synthesize(si, state.target, state.useZ0k)
        } else {
            EngineResult(errors = errors)
        }

        update {
            val newFields = fields.toMutableMap()
            // Clear every output first; a blank optional input leaves its paired output blank.
            for (id in outputs) newFields[id] = newFields.getValue(id).copy(text = "", error = null)
            for ((id, value) in result.values) {
                val f = newFields.getValue(id)
                newFields[id] = f.copy(text = format(fromSi(id, value, f.unit)), error = null)
            }
            for ((id, msg) in result.errors) newFields[id] = newFields.getValue(id).copy(error = msg)
            val solved = if (result.errors.isEmpty()) si + result.values else null
            val extras = solved?.let { lossOutputs(line, it) } ?: emptyMap()
            copy(
                fields = newFields, updated = result.values.keys, runId = runId + 1, lastAction = action,
                extras = extras, lastInputs = solved,
            )
        }
    }

    private fun validate(id: FieldId, value: Double, unit: String): Int? = when {
        id == FieldId.ER -> if (value < 1) R.string.unreasonable_value else null
        id == FieldId.FREQ -> if (value <= 0.0) R.string.error_zero_frequency else null
        id == FieldId.K -> if (value <= 0.0) R.string.unreasonable_value else null
        id == FieldId.TAND -> if (value < 0.0) R.string.unreasonable_value else null
        id == FieldId.SIGMA -> if (value <= 0.0) R.string.unreasonable_value else null
        id.unitKind == UnitKind.IMPEDANCE -> if (value <= 0.0) R.string.unreasonable_value else null
        id.unitKind == UnitKind.TWIST -> if (value < 0.0) R.string.unreasonable_value else null
        id.mustBePositive -> if (Constants.value2meter(value, unit) < Constants.MINI_LIMIT) R.string.unreasonable_value else null
        id.unitKind == UnitKind.LENGTH || id.unitKind == UnitKind.ROUGHNESS ->
            if (value < 0.0) R.string.unreasonable_value else null
        else -> null
    }

    private fun toSi(id: FieldId, value: Double, unit: String): Double = when (id.unitKind) {
        UnitKind.LENGTH, UnitKind.ROUGHNESS -> Constants.value2meter(value, unit)
        UnitKind.FREQUENCY -> Constants.value2Hz(value, unit)
        UnitKind.CONDUCTIVITY -> value * 1e6
        UnitKind.TWIST -> if (unit == "/in") value / 0.0254 else value * 100.0
        else -> value
    }

    private fun fromSi(id: FieldId, value: Double, unit: String): Double = when (id.unitKind) {
        UnitKind.LENGTH, UnitKind.ROUGHNESS -> Constants.meter2others(value, unit)
        UnitKind.CONDUCTIVITY -> value / 1e6
        else -> value
    }

    private fun format(value: Double): String =
        BigDecimal.valueOf(value).setScale(Constants.DecimalLength, RoundingMode.HALF_UP).toDouble().toString()

    /** Preset values keep up to six significant digits, so e.g. 17.5 µm in mm is not rounded away. */
    private fun presetText(value: Double): String =
        BigDecimal.valueOf(value).round(MathContext(6)).stripTrailingZeros().toPlainString()

    private inline fun update(change: CalculatorState.() -> CalculatorState) {
        state = state.change()
        save(state)
    }

    // --- persistence -------------------------------------------------------------------------

    private fun key(id: FieldId) = "${line.prefPrefix}_${id.prefName}"
    private fun unitKey(id: FieldId) = "${key(id)}_UNIT"
    private val targetKey = "${line.prefPrefix}_TARGET"
    private val modeKey = "${line.prefPrefix}_USEZ0K"

    private fun defaults() = CalculatorState(
        fields = line.fields.associateWith { id ->
            val d = line.defaultOf(id)
            FieldState(d.text, d.unit)
        },
        target = line.defaultTarget,
        useZ0k = true,
    )

    private fun load(): CalculatorState {
        val d = defaults()
        val fields = line.fields.associateWith { id ->
            val def = d.field(id)
            val unit = prefs.getString(unitKey(id), def.unit)?.takeIf { it in id.unitKind.choices } ?: def.unit
            FieldState(prefs.getString(key(id), def.text) ?: def.text, unit)
        }
        val savedTarget = prefs.getString(targetKey, null)?.toIntOrNull()
        val target = line.targets.firstOrNull { savedTarget != null && synthFlag(it) == savedTarget } ?: d.target
        val useZ0k = prefs.getString(modeKey, "true") == "true"
        return CalculatorState(fields, target, useZ0k)
    }

    private fun save(s: CalculatorState) = prefs.edit {
        for ((id, f) in s.fields) {
            putString(key(id), f.text)
            if (id.unitKind != UnitKind.NONE) putString(unitKey(id), f.unit)
        }
        s.target?.let { putString(targetKey, synthFlag(it).toString()) }
        if (line.coupled) putString(modeKey, s.useZ0k.toString())
    }
}
