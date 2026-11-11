package com.rookiedev.microwavetools.ui

import androidx.annotation.StringRes
import com.rookiedev.microwavetools.R

/** Units a field can be entered in. The first entry of [choices] is never assumed as a default. */
enum class UnitKind(val choices: List<String>, val suffix: String? = null) {
    LENGTH(listOf("mil", "mm", "cm")),
    FREQUENCY(listOf("MHz", "GHz")),
    IMPEDANCE(listOf("Ω"), suffix = "Ω"),
    PHASE(listOf("Deg"), suffix = "°"),
    NONE(emptyList());

    /** True when the user can pick between several units. */
    val selectable: Boolean get() = choices.size > 1
}

/**
 * Every quantity shown on any calculator screen.
 *
 * @param prefName suffix of the SharedPreferences key, e.g. "W" in "MLIN_W".
 */
enum class FieldId(
    @param:StringRes val label: Int,
    val unitKind: UnitKind,
    val prefName: String,
    @param:StringRes val emptyError: Int,
    /** Dimensions that must be strictly positive (vs. thickness/offset, which may be 0). */
    val mustBePositive: Boolean = false,
) {
    T(R.string.label_t, UnitKind.LENGTH, "T", R.string.Error_T_empty),
    H(R.string.label_h, UnitKind.LENGTH, "H", R.string.Error_H_empty, mustBePositive = true),
    W(R.string.label_w, UnitKind.LENGTH, "W", R.string.Error_W_empty, mustBePositive = true),
    S(R.string.label_s, UnitKind.LENGTH, "S", R.string.Error_S_empty, mustBePositive = true),
    A(R.string.label_a, UnitKind.LENGTH, "A", R.string.Error_a_empty, mustBePositive = true),
    B(R.string.label_b, UnitKind.LENGTH, "B", R.string.Error_b_empty, mustBePositive = true),
    C(R.string.label_c, UnitKind.LENGTH, "C", R.string.Error_c_empty),
    L(R.string.label_l, UnitKind.LENGTH, "L", 0),
    Z0(R.string.label_z0, UnitKind.IMPEDANCE, "Z0", R.string.error_Z0_empty),
    K(R.string.label_k, UnitKind.NONE, "K", R.string.Error_k_empty),
    Z0O(R.string.label_z0o, UnitKind.IMPEDANCE, "Z0O", R.string.error_Z0o_empty),
    Z0E(R.string.label_z0e, UnitKind.IMPEDANCE, "Z0E", R.string.error_Z0e_empty),
    PHS(R.string.label_phs, UnitKind.PHASE, "PHS", 0),
    ER(R.string.label_er, UnitKind.NONE, "ER", R.string.error_er_empty),
    FREQ(R.string.label_freq, UnitKind.FREQUENCY, "FREQ", R.string.Error_Freq_empty),
}

/** Default value and unit for a field. */
data class Default(val text: String, val unit: String = "")

private fun mm(v: String) = Default(v, "mm")
private val GHz = Default("1.00", "GHz")
private fun ohm(v: String) = Default(v, "Ω")
private fun deg(v: String) = Default(v, "Deg")
private fun plain(v: String) = Default(v)

/** The seven transmission-line calculators. Order matches the saved drawer position. */
enum class LineType(
    @param:StringRes val title: Int,
    val navIcon: Int,
    val diagram: Int,
    /** SharedPreferences key prefix used by earlier app versions; kept so saved values survive. */
    val prefPrefix: String,
    val dimensions: List<FieldId>,
    val parameters: List<FieldId>,
    /** Dimensions the user can pick as the synthesis target. Empty for coupled lines (W and S are both solved). */
    val targets: List<FieldId>,
    val defaults: Map<FieldId, Default>,
) {
    MLIN(
        R.string.full_name_mlin, R.drawable.ic_menu_mlin, R.drawable.vt_mlin, "MLIN",
        dimensions = listOf(FieldId.T, FieldId.H, FieldId.W, FieldId.L),
        parameters = listOf(FieldId.Z0, FieldId.PHS, FieldId.ER, FieldId.FREQ),
        targets = listOf(FieldId.H, FieldId.W),
        defaults = mapOf(
            FieldId.T to mm("0.035"), FieldId.H to mm("1.6"), FieldId.W to mm("2.9"), FieldId.L to mm("40"),
            FieldId.Z0 to ohm("50.0"), FieldId.PHS to deg("90"), FieldId.ER to plain("4.6"), FieldId.FREQ to GHz,
        ),
    ),
    CMLIN(
        R.string.full_name_cmlin, R.drawable.ic_menu_cmlin, R.drawable.vt_cmlin, "CMLIN",
        dimensions = listOf(FieldId.H, FieldId.W, FieldId.S, FieldId.L),
        parameters = listOf(FieldId.Z0, FieldId.K, FieldId.Z0O, FieldId.Z0E, FieldId.PHS, FieldId.ER, FieldId.FREQ),
        targets = emptyList(),
        defaults = mapOf(
            FieldId.H to mm("1.6"), FieldId.W to mm("2.7"), FieldId.S to mm("0.8"), FieldId.L to mm("40.9"),
            FieldId.Z0 to ohm("50"), FieldId.K to plain("0.2"), FieldId.Z0O to ohm("41.044"),
            FieldId.Z0E to ohm("62.341"), FieldId.PHS to deg("90"), FieldId.ER to plain("4.60"), FieldId.FREQ to GHz,
        ),
    ),
    SLIN(
        R.string.full_name_slin, R.drawable.ic_menu_slin, R.drawable.vt_slin, "SLIN",
        dimensions = listOf(FieldId.T, FieldId.H, FieldId.W, FieldId.L),
        parameters = listOf(FieldId.Z0, FieldId.PHS, FieldId.ER, FieldId.FREQ),
        targets = listOf(FieldId.H, FieldId.W),
        defaults = mapOf(
            FieldId.T to mm("0.035"), FieldId.H to mm("1.6"), FieldId.W to mm("0.6"), FieldId.L to mm("34.9"),
            FieldId.Z0 to ohm("50"), FieldId.PHS to deg("90"), FieldId.ER to plain("4.60"), FieldId.FREQ to GHz,
        ),
    ),
    CSLIN(
        R.string.full_name_cslin, R.drawable.ic_menu_cslin, R.drawable.vt_cslin, "CSLIN",
        dimensions = listOf(FieldId.T, FieldId.H, FieldId.W, FieldId.S, FieldId.L),
        parameters = listOf(FieldId.Z0, FieldId.K, FieldId.Z0O, FieldId.Z0E, FieldId.PHS, FieldId.ER, FieldId.FREQ),
        targets = emptyList(),
        defaults = mapOf(
            FieldId.T to mm("0.035"), FieldId.H to mm("1.6"), FieldId.W to mm("0.59"), FieldId.S to mm("0.36"),
            FieldId.L to mm("34.9"), FieldId.Z0 to ohm("50"), FieldId.K to plain("0.2"), FieldId.Z0O to ohm("40.8"),
            FieldId.Z0E to ohm("61.2"), FieldId.PHS to deg("90"), FieldId.ER to plain("4.6"), FieldId.FREQ to GHz,
        ),
    ),
    CPW(
        R.string.full_name_cpw, R.drawable.ic_menu_cpw, R.drawable.vt_cpw, "CPW",
        dimensions = listOf(FieldId.T, FieldId.H, FieldId.W, FieldId.S, FieldId.L),
        parameters = listOf(FieldId.Z0, FieldId.PHS, FieldId.ER, FieldId.FREQ),
        targets = listOf(FieldId.H, FieldId.W, FieldId.S),
        defaults = mapOf(
            FieldId.T to mm("0.035"), FieldId.H to mm("1.6"), FieldId.W to mm("1"), FieldId.S to mm("0.244"),
            FieldId.L to mm("44.464"), FieldId.Z0 to ohm("50"), FieldId.PHS to deg("90"), FieldId.ER to plain("4.6"),
            FieldId.FREQ to GHz,
        ),
    ),
    GCPW(
        R.string.full_name_gcpw, R.drawable.ic_menu_gcpw, R.drawable.vt_cpwg, "GCPW",
        dimensions = listOf(FieldId.H, FieldId.W, FieldId.S, FieldId.L),
        parameters = listOf(FieldId.Z0, FieldId.PHS, FieldId.ER, FieldId.FREQ),
        targets = listOf(FieldId.H, FieldId.W, FieldId.S),
        defaults = mapOf(
            FieldId.H to mm("1.6"), FieldId.W to mm("1"), FieldId.S to mm("0.15"), FieldId.L to mm("44.38"),
            FieldId.Z0 to ohm("50.00"), FieldId.PHS to deg("90.00"), FieldId.ER to plain("4.6"), FieldId.FREQ to GHz,
        ),
    ),
    COAX(
        R.string.full_name_coax, R.drawable.ic_menu_coax, R.drawable.vt_coax, "COAX",
        dimensions = listOf(FieldId.A, FieldId.B, FieldId.C, FieldId.L),
        parameters = listOf(FieldId.Z0, FieldId.PHS, FieldId.ER, FieldId.FREQ),
        targets = listOf(FieldId.A, FieldId.B, FieldId.C),
        defaults = mapOf(
            FieldId.A to mm("0.167"), FieldId.B to mm("1.00"), FieldId.C to mm("0.00"), FieldId.L to mm("34.945"),
            FieldId.Z0 to ohm("50"), FieldId.PHS to deg("90"), FieldId.ER to plain("4.6"), FieldId.FREQ to GHz,
        ),
    );

    val coupled: Boolean get() = targets.isEmpty()
    val fields: List<FieldId> get() = dimensions + parameters
    val defaultTarget: FieldId? get() = targets.firstOrNull { it == FieldId.W || it == FieldId.A }

    /** Fields Analyze fills in. */
    val analysisOutputs: Set<FieldId>
        get() = if (coupled) setOf(FieldId.Z0, FieldId.K, FieldId.Z0O, FieldId.Z0E, FieldId.PHS)
        else setOf(FieldId.Z0, FieldId.PHS)

    /** Fields Synthesize fills in, given the selected target or coupled-line input mode. */
    fun synthesisOutputs(target: FieldId?, useZ0k: Boolean): Set<FieldId> = buildSet {
        if (coupled) {
            add(FieldId.W); add(FieldId.S)
            if (useZ0k) { add(FieldId.Z0O); add(FieldId.Z0E) } else { add(FieldId.Z0); add(FieldId.K) }
        } else if (target != null) {
            add(target)
        }
        add(FieldId.L)
    }
}
