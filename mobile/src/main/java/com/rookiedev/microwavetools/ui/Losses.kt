package com.rookiedev.microwavetools.ui

import androidx.annotation.StringRes
import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import com.rookiedev.microwavetools.libs.Loss
import com.rookiedev.microwavetools.libs.Waveguide
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/** How a read-only result is displayed. Values are stored in SI: dB/m for per-length loss. */
enum class OutputUnit {
    NONE,
    DB_PER_LENGTH,
    DB,
    /** Metres, shown in L's unit. */
    LENGTH,
    /** Seconds per metre, shown as ps per inch or per cm. */
    DELAY_PER_LENGTH,
    /** Metres, shown in µm. */
    MICRONS,
    /** Hz, shown in the frequency field's unit. */
    FREQUENCY,
    OHMS,
}

/** Read-only quantities shown in the Results card after a calculation. */
enum class Output(@param:StringRes val label: Int, val unit: OutputUnit) {
    FC(R.string.out_fc, OutputUnit.FREQUENCY),
    FC_NEXT(R.string.out_fc_next, OutputUnit.FREQUENCY),
    EEFF(R.string.out_eeff, OutputUnit.NONE),
    LAMBDA_G(R.string.out_lambda_g, OutputUnit.LENGTH),
    DELAY(R.string.out_delay, OutputUnit.DELAY_PER_LENGTH),
    ALPHA_C(R.string.out_alpha_c, OutputUnit.DB_PER_LENGTH),
    ALPHA_D(R.string.out_alpha_d, OutputUnit.DB_PER_LENGTH),
    ALPHA(R.string.out_alpha, OutputUnit.DB_PER_LENGTH),
    LOSS(R.string.out_loss, OutputUnit.DB),

    Z_DIFF(R.string.out_zdiff, OutputUnit.OHMS),
    Z_CM(R.string.out_zcm, OutputUnit.OHMS),
    EEFF_E(R.string.out_eeff_e, OutputUnit.NONE),
    EEFF_O(R.string.out_eeff_o, OutputUnit.NONE),
    DELAY_E(R.string.out_delay_e, OutputUnit.DELAY_PER_LENGTH),
    DELAY_O(R.string.out_delay_o, OutputUnit.DELAY_PER_LENGTH),
    ALPHA_E(R.string.out_alpha_e, OutputUnit.DB_PER_LENGTH),
    ALPHA_O(R.string.out_alpha_o, OutputUnit.DB_PER_LENGTH),
    LOSS_E(R.string.out_loss_e, OutputUnit.DB),
    LOSS_O(R.string.out_loss_o, OutputUnit.DB),

    SKIN_DEPTH(R.string.out_skin_depth, OutputUnit.MICRONS),
}

/** Conductor presets, conductivity in MS/m. */
enum class Metal(@param:StringRes val label: Int, val msPerM: Double) {
    COPPER(R.string.metal_copper, 58.0),
    SILVER(R.string.metal_silver, 63.0),
    GOLD(R.string.metal_gold, 41.0),
    ALUMINUM(R.string.metal_aluminum, 37.7),
}

/** Impedance and (for single lines) phase per metre of one propagating mode. */
private data class Mode(val z0: Double, val degPerMeter: Double?)

/** Analyzes the line with dielectric constant [er] and returns its modes (even then odd for coupled lines). */
private fun LineType.modes(v: Map<FieldId, Double>, er: Double): List<Mode>? {
    val r = engine().analyze(v + (FieldId.ER to er) + (FieldId.L to 1.0))
    if (r.errors.isNotEmpty()) return null
    val modes = if (coupled) {
        listOf(Mode(r.values[FieldId.Z0E] ?: return null, null), Mode(r.values[FieldId.Z0O] ?: return null, null))
    } else {
        listOf(Mode(r.values[FieldId.Z0] ?: return null, r.values[FieldId.PHS]))
    }
    return modes.takeIf { m -> m.all { it.z0.isFinite() && it.z0 > 0.0 } }
}

/**
 * The geometry with every metal surface receded by [d] metres into the metal (negative
 * advances it), as Wheeler's incremental inductance rule needs. A zero-thickness strip
 * has no top or bottom surface to recede.
 */
private fun LineType.recede(v: Map<FieldId, Double>, d: Double): Map<FieldId, Double> {
    val m = v.toMutableMap()
    fun add(id: FieldId, delta: Double) { m[id] = m.getValue(id) + delta }
    val thick = (v[FieldId.T] ?: 0.0) > 0.0
    when (this) {
        LineType.MLIN, LineType.CMLIN, LineType.GCPW -> {
            // Ground plane recedes down; the strip's bottom face recedes up.
            add(FieldId.W, -2 * d)
            if (FieldId.S in v) add(FieldId.S, 2 * d)
            add(FieldId.H, if (thick) 2 * d else d)
            if (thick) add(FieldId.T, -2 * d)
        }
        LineType.SLIN, LineType.CSLIN -> {
            // H is the ground-to-ground spacing; both grounds recede.
            add(FieldId.W, -2 * d)
            if (FieldId.S in v) add(FieldId.S, 2 * d)
            add(FieldId.H, 2 * d)
            if (thick) add(FieldId.T, -2 * d)
        }
        LineType.CPW -> {
            // No ground plane under the substrate.
            add(FieldId.W, -2 * d)
            add(FieldId.S, 2 * d)
            if (thick) add(FieldId.T, -2 * d)
        }
        LineType.COAX -> {
            add(FieldId.A, -d)
            add(FieldId.B, d)
        }
        LineType.TWP -> {
            // Each wire's surface recedes: the diameter shrinks, the centers stay put.
            add(FieldId.A, -2 * d)
        }
        LineType.ECPW -> {
            // Zero-thickness strips: both edges of each strip recede, widening both gaps.
            add(FieldId.W, -2 * d)
            add(FieldId.S, 2 * d)
            add(FieldId.G2, 2 * d)
        }
        LineType.ASLIN -> {
            // H and H2 each grow as their ground recedes and, for a thick strip, as the strip face recedes.
            add(FieldId.W, -2 * d)
            add(FieldId.H, if (thick) 2 * d else d)
            add(FieldId.H2, if (thick) 2 * d else d)
            if (thick) add(FieldId.T, -2 * d)
        }
        LineType.WG -> error("Waveguide loss uses its own closed form")
    }
    return m
}

/** A finite-difference step small against every metal dimension. */
private fun step(v: Map<FieldId, Double>): Double {
    val sizes = listOf(FieldId.W, FieldId.S, FieldId.G2, FieldId.H, FieldId.H2, FieldId.T, FieldId.A, FieldId.B)
        .mapNotNull { v[it] }.filter { it > 0.0 }
    return (sizes.minOrNull() ?: return 0.0) * 1e-4
}

/**
 * Effective dielectric constant and losses for a line whose geometry, εr and frequency are
 * all in [v] (SI). Conductor loss needs σ (and optionally roughness), dielectric loss needs
 * tanδ; outputs whose inputs are missing are left out. Total loss over the line needs L.
 */
fun lossOutputs(line: LineType, v: Map<FieldId, Double>): Map<Output, Double> {
    if (line == LineType.WG) return waveguideOutputs(v)
    val f = v[FieldId.FREQ] ?: return emptyMap()
    val er = v[FieldId.ER] ?: return emptyMap()
    val real = line.modes(v, er) ?: return emptyMap()
    val air = line.modes(v, 1.0) ?: return emptyMap()

    val tand = v[FieldId.TAND]
    val sigma = v[FieldId.SIGMA]?.takeIf { it > 0.0 }
    val rs = sigma?.let { Loss.surfaceResistance(f, it, v[FieldId.ROUGH] ?: 0.0) }
    val d = step(v)
    val slopes: List<Double>? = if (rs != null && d > 0.0) {
        val plus = line.modes(line.recede(v, d), 1.0)
        val minus = line.modes(line.recede(v, -d), 1.0)
        if (plus != null && minus != null) plus.indices.map { max(0.0, (plus[it].z0 - minus[it].z0) / (2 * d)) } else null
    } else null
    val length = v[FieldId.L]?.takeIf { it > 0.0 }

    data class ModeLoss(val eeff: Double, val ac: Double?, val ad: Double?) {
        val total: Double? get() = if (ac == null && ad == null) null else (ac ?: 0.0) + (ad ?: 0.0)
    }

    val perMode = real.indices.map { i ->
        val m = real[i]
        // Single lines: from the analyzed phase, which includes dispersion. Coupled modes: from Z0,air/Z0.
        val eeff = m.degPerMeter?.let { (it * Constants.LIGHTSPEED / (360.0 * f)).pow(2) }
            ?: (air[i].z0 / m.z0).pow(2)
        ModeLoss(
            eeff = eeff,
            ac = if (rs != null && slopes != null) Loss.conductor(rs, m.z0, slopes[i]) * Loss.NP_TO_DB else null,
            ad = tand?.let { Loss.dielectric(f, er, eeff, it) * Loss.NP_TO_DB },
        )
    }

    val c = Constants.LIGHTSPEED
    return buildMap {
        sigma?.let { put(Output.SKIN_DEPTH, Loss.skinDepth(f, it)) }
        if (line.coupled) {
            val (e, o) = perMode
            put(Output.Z_DIFF, 2 * real[1].z0); put(Output.Z_CM, real[0].z0 / 2)
            put(Output.EEFF_E, e.eeff); put(Output.EEFF_O, o.eeff)
            put(Output.DELAY_E, sqrt(e.eeff) / c); put(Output.DELAY_O, sqrt(o.eeff) / c)
            e.total?.let { put(Output.ALPHA_E, it); if (length != null) put(Output.LOSS_E, it * length) }
            o.total?.let { put(Output.ALPHA_O, it); if (length != null) put(Output.LOSS_O, it * length) }
        } else {
            val m = perMode.single()
            put(Output.EEFF, m.eeff)
            put(Output.LAMBDA_G, c / (f * sqrt(m.eeff)))
            put(Output.DELAY, sqrt(m.eeff) / c)
            m.ac?.let { put(Output.ALPHA_C, it) }
            m.ad?.let { put(Output.ALPHA_D, it) }
            m.total?.let { put(Output.ALPHA, it); if (length != null) put(Output.LOSS, it * length) }
        }
    }.filterValues { it.isFinite() }
}

/**
 * TE10 cutoffs, εeff = (β/k0)², guided wavelength, group delay and wall/dielectric loss of a
 * rectangular waveguide. Empty below cutoff or when b >= a.
 */
private fun waveguideOutputs(v: Map<FieldId, Double>): Map<Output, Double> {
    val a = v[FieldId.A] ?: return emptyMap()
    val b = v[FieldId.B] ?: return emptyMap()
    val er = v[FieldId.ER] ?: return emptyMap()
    val f = v[FieldId.FREQ] ?: return emptyMap()
    if (b >= a || f <= Waveguide.cutoff(a, er)) return emptyMap()
    val beta = Waveguide.beta(f, a, er)
    val k0 = Waveguide.k(f, 1.0)
    val sigma = v[FieldId.SIGMA]?.takeIf { it > 0.0 }
    val ac = sigma?.let {
        Waveguide.conductorLoss(f, a, b, er, Loss.surfaceResistance(f, it, v[FieldId.ROUGH] ?: 0.0)) * Loss.NP_TO_DB
    }
    val ad = v[FieldId.TAND]?.let { Waveguide.dielectricLoss(f, a, er, it) * Loss.NP_TO_DB }
    val total = if (ac == null && ad == null) null else (ac ?: 0.0) + (ad ?: 0.0)
    val length = v[FieldId.L]?.takeIf { it > 0.0 }
    return buildMap {
        put(Output.FC, Waveguide.cutoff(a, er))
        put(Output.FC_NEXT, Waveguide.nextCutoff(a, b, er))
        put(Output.EEFF, (beta / k0).pow(2))
        put(Output.LAMBDA_G, 2 * Math.PI / beta)
        put(Output.DELAY, Waveguide.groupDelay(f, a, er))
        ac?.let { put(Output.ALPHA_C, it) }
        ad?.let { put(Output.ALPHA_D, it) }
        total?.let { put(Output.ALPHA, it); if (length != null) put(Output.LOSS, it * length) }
        sigma?.let { put(Output.SKIN_DEPTH, Loss.skinDepth(f, it)) }
    }.filterValues { it.isFinite() }
}
