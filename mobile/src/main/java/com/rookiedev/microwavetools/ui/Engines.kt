package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.CmlinCalculator
import com.rookiedev.microwavetools.libs.CmlinModel
import com.rookiedev.microwavetools.libs.CoaxCalculator
import com.rookiedev.microwavetools.libs.CoaxModel
import com.rookiedev.microwavetools.libs.CoupledCpw
import com.rookiedev.microwavetools.libs.Constants
import com.rookiedev.microwavetools.libs.Constants.ERROR
import com.rookiedev.microwavetools.libs.CpwCalculator
import com.rookiedev.microwavetools.libs.CpwModel
import com.rookiedev.microwavetools.libs.CslinCalculator
import com.rookiedev.microwavetools.libs.CslinModel
import com.rookiedev.microwavetools.libs.MlinCalculator
import com.rookiedev.microwavetools.libs.MlinModel
import com.rookiedev.microwavetools.libs.SlinCalculator
import com.rookiedev.microwavetools.libs.SlinModel
import com.rookiedev.microwavetools.libs.Waveguide
import kotlin.math.PI
import kotlin.math.acosh
import kotlin.math.atan
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Result of a calculation. [values] are in SI units (m, Hz, Ω, degrees);
 * [errors] map a field to the string resource to show under it.
 */
data class EngineResult(
    val values: Map<FieldId, Double> = emptyMap(),
    val errors: Map<FieldId, Int> = emptyMap(),
)

/**
 * Runs a calculator. Input values are in SI units and already validated;
 * optional fields (L for analysis, phase for synthesis) are absent when left blank.
 */
interface LineEngine {
    fun analyze(v: Map<FieldId, Double>): EngineResult
    fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult
}

fun LineType.engine(): LineEngine = when (this) {
    LineType.MLIN -> MlinEngine
    LineType.SLIN -> SlinEngine
    LineType.CPW -> CpwEngine(withGround = false)
    LineType.GCPW -> CpwEngine(withGround = true)
    LineType.COAX -> CoaxEngine
    LineType.CMLIN -> CoupledEngine(stripline = false)
    LineType.CSLIN -> CoupledEngine(stripline = true)
    LineType.WG -> WgEngine
    LineType.ASLIN -> AslinEngine
    LineType.ECPW -> EcpwEngine
    LineType.TWP -> TwpEngine
}

/** The calculator flag for a synthesis target (also the value saved in preferences). */
internal fun synthFlag(target: FieldId): Int = when (target) {
    FieldId.W -> Constants.Synthesize_Width
    FieldId.H -> Constants.Synthesize_Height
    FieldId.S -> Constants.Synthesize_Gap
    FieldId.A -> Constants.Synthesize_CoreRadius
    FieldId.B -> Constants.Synthesize_SubRadius
    FieldId.C -> Constants.Synthesize_CoreOffset
    else -> error("$target is not a synthesis target")
}

private fun Double.isUsable() = !isNaN() && !isInfinite()

/** Maps a calculator error code to field errors, falling back to "synthesis failed" on [failed]. */
private fun errorsFor(code: ERROR?, failed: Collection<FieldId>): Map<FieldId, Int> = when (code) {
    ERROR.WIDTH_MINIMAL_LIMIT -> mapOf(FieldId.W to R.string.unreasonable_value)
    ERROR.HEIGHT_MINIMAL_LIMIT -> mapOf(FieldId.H to R.string.unreasonable_value)
    ERROR.SPACE_MINIMAL_LIMIT -> mapOf(FieldId.S to R.string.unreasonable_value)
    ERROR.ER_MINIMAL_LIMIT -> mapOf(FieldId.ER to R.string.unreasonable_value)
    ERROR.SUBSTRATE_TOO_LARGE -> mapOf(FieldId.A to R.string.substrate_too_large, FieldId.B to R.string.substrate_too_large)
    ERROR.OFFSET_TOO_LARGE -> mapOf(FieldId.C to R.string.offset_too_large)
    ERROR.K_OUT_OF_RANGE -> mapOf(FieldId.K to R.string.k_out_of_range)
    ERROR.Z0E_Z0O_MISTAKE -> mapOf(FieldId.Z0E to R.string.Z0e_larger_Z0o, FieldId.Z0O to R.string.Z0e_larger_Z0o)
    else -> failed.associateWith { R.string.synthesize_failed }
}

/** Builds a synthesis result, failing when any solved value is not a finite number. */
private fun solved(code: ERROR?, values: Map<FieldId, Double>, solvedFields: List<FieldId>): EngineResult {
    if (code != ERROR.NO_ERROR) return EngineResult(errors = errorsFor(code, solvedFields))
    if (solvedFields.any { values[it]?.isUsable() != true }) return EngineResult(errors = errorsFor(null, solvedFields))
    return EngineResult(values = values)
}

private fun analysisResult(code: ERROR?, values: Map<FieldId, Double>): EngineResult =
    if (code == null || code == ERROR.NO_ERROR) EngineResult(values = values)
    else EngineResult(errors = errorsFor(code, emptyList()))

/** Phase output only when a length was given; length output only when a phase was given. */
private fun MutableMap<FieldId, Double>.putIfGiven(given: Boolean, id: FieldId, value: Double) {
    if (given) put(id, value)
}

private object MlinEngine : LineEngine {
    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val line = MlinModel().apply {
            setMetalWidth(v.getValue(FieldId.W), "m")
            setMetalLength(v[FieldId.L] ?: 0.0, "m")
            setMetalThick(v.getValue(FieldId.T), "m")
            setSubHeight(v.getValue(FieldId.H), "m")
            subEpsilon = v.getValue(FieldId.ER)
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
        }.let { MlinCalculator().getAnaResult(it) }
        return analysisResult(line.errorCode, buildMap {
            put(FieldId.Z0, line.impedance)
            putIfGiven(FieldId.L in v, FieldId.PHS, line.phase)
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val t = requireNotNull(target)
        val line = MlinModel().apply {
            impedance = v.getValue(FieldId.Z0)
            phase = v[FieldId.PHS] ?: 0.0
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
            subEpsilon = v.getValue(FieldId.ER)
            setMetalThick(v.getValue(FieldId.T), "m")
            setSubHeight(if (t == FieldId.H) 0.0 else v.getValue(FieldId.H), "m")
            setMetalWidth(if (t == FieldId.W) 0.0 else v.getValue(FieldId.W), "m")
        }.let { MlinCalculator().getSynResult(it, synthFlag(t)) }
        val values = buildMap {
            put(t, if (t == FieldId.W) line.metalWidth else line.subHeight)
            putIfGiven(FieldId.PHS in v, FieldId.L, line.metalLength)
        }
        return solved(line.errorCode, values, listOf(t))
    }
}

private object SlinEngine : LineEngine {
    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val line = SlinModel().apply {
            setMetalWidth(v.getValue(FieldId.W), "m")
            setMetalLength(v[FieldId.L] ?: 0.0, "m")
            setMetalThick(v.getValue(FieldId.T), "m")
            setSubHeight(v.getValue(FieldId.H), "m")
            subEpsilon = v.getValue(FieldId.ER)
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
        }.let { SlinCalculator().getAnaResult(it) }
        return analysisResult(line.errorCode, buildMap {
            put(FieldId.Z0, line.impedance)
            putIfGiven(FieldId.L in v, FieldId.PHS, line.phase)
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val t = requireNotNull(target)
        val line = SlinModel().apply {
            impedance = v.getValue(FieldId.Z0)
            phase = v[FieldId.PHS] ?: 0.0
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
            subEpsilon = v.getValue(FieldId.ER)
            setMetalThick(v.getValue(FieldId.T), "m")
            setSubHeight(if (t == FieldId.H) 0.0 else v.getValue(FieldId.H), "m")
            setMetalWidth(if (t == FieldId.W) 0.0 else v.getValue(FieldId.W), "m")
        }.let { SlinCalculator().getSynResult(it, synthFlag(t)) }
        val values = buildMap {
            put(t, if (t == FieldId.W) line.metalWidth else line.subHeight)
            putIfGiven(FieldId.PHS in v, FieldId.L, line.metalLength)
        }
        return solved(line.errorCode, values, listOf(t))
    }
}

/** Coplanar waveguide; the grounded variant has no thickness field and uses zero thickness. */
private class CpwEngine(private val withGround: Boolean) : LineEngine {
    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val line = CpwModel().apply {
            setMetalWidth(v.getValue(FieldId.W), "m")
            setMetalSpace(v.getValue(FieldId.S), "m")
            setMetalLength(v[FieldId.L] ?: 0.0, "m")
            setMetalThick(v[FieldId.T] ?: 0.0, "m")
            setSubHeight(v.getValue(FieldId.H), "m")
            subEpsilon = v.getValue(FieldId.ER)
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
        }.let { CpwCalculator().getAnaResult(it, withGround) }
        return analysisResult(line.errorCode, buildMap {
            put(FieldId.Z0, line.impedance)
            putIfGiven(FieldId.L in v, FieldId.PHS, line.phase)
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val t = requireNotNull(target)
        val line = CpwModel().apply {
            impedance = v.getValue(FieldId.Z0)
            phase = v[FieldId.PHS] ?: 0.0
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
            subEpsilon = v.getValue(FieldId.ER)
            setMetalThick(v[FieldId.T] ?: 0.0, "m")
            setMetalWidth(if (t == FieldId.W) 0.0 else v.getValue(FieldId.W), "m")
            setMetalSpace(if (t == FieldId.S) 0.0 else v.getValue(FieldId.S), "m")
            setSubHeight(if (t == FieldId.H) 0.0 else v.getValue(FieldId.H), "m")
        }.let { CpwCalculator().getSynResult(it, synthFlag(t), withGround) }
        val values = buildMap {
            put(t, when (t) {
                FieldId.W -> line.metalWidth
                FieldId.S -> line.metalSpace
                else -> line.subHeight
            })
            putIfGiven(FieldId.PHS in v, FieldId.L, line.metalLength)
        }
        return solved(line.errorCode, values, listOf(t))
    }
}

private object CoaxEngine : LineEngine {
    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val line = CoaxModel().apply {
            setCoreRadius(v.getValue(FieldId.A), "m")
            setSubRadius(v.getValue(FieldId.B), "m")
            setCoreOffset(v.getValue(FieldId.C), "m")
            setMetalLength(v[FieldId.L] ?: 0.0, "m")
            subEpsilon = v.getValue(FieldId.ER)
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
        }.let { CoaxCalculator().getAnaResult(it) }
        return analysisResult(line.errorCode, buildMap {
            put(FieldId.Z0, line.impedance)
            putIfGiven(FieldId.L in v, FieldId.PHS, line.phase)
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val t = requireNotNull(target)
        val line = CoaxModel().apply {
            impedance = v.getValue(FieldId.Z0)
            phase = v[FieldId.PHS] ?: 0.0
            setFrequency(v.getValue(FieldId.FREQ), "Hz")
            subEpsilon = v.getValue(FieldId.ER)
            setCoreRadius(if (t == FieldId.A) 0.0 else v.getValue(FieldId.A), "m")
            setSubRadius(if (t == FieldId.B) 0.0 else v.getValue(FieldId.B), "m")
            setCoreOffset(if (t == FieldId.C) 0.0 else v.getValue(FieldId.C), "m")
        }.let { CoaxCalculator().getSynResult(it, synthFlag(t)) }
        val values = buildMap {
            put(t, when (t) {
                FieldId.A -> line.coreRadius
                FieldId.B -> line.subRadius
                else -> line.coreOffset
            })
            putIfGiven(FieldId.PHS in v, FieldId.L, line.metalLength)
        }
        return solved(line.errorCode, values, listOf(t))
    }
}

/**
 * Asymmetric stripline. Each side of the strip holds half the capacitance of a centered stripline
 * with ground spacing 2·h + t, so Z0 = 2·Z1·Z2 / (Z1 + Z2) (Wadell §3.5.3). Exact when H = H2.
 */
private object AslinEngine : LineEngine {
    /** Symmetric stripline impedance for ground spacing [b], or null if the model rejects it. */
    private fun symmetric(w: Double, t: Double, b: Double, er: Double, f: Double): Double? =
        SlinModel().apply {
            setMetalWidth(w, "m"); setMetalLength(0.0, "m"); setMetalThick(t, "m")
            setSubHeight(b, "m"); subEpsilon = er; setFrequency(f, "Hz")
        }.let { SlinCalculator().getAnaResult(it) }
            .takeIf { it.errorCode == ERROR.NO_ERROR && it.impedance.isUsable() && it.impedance > 0.0 }?.impedance

    fun impedance(w: Double, t: Double, h1: Double, h2: Double, er: Double, f: Double): Double? {
        val z1 = symmetric(w, t, 2 * h1 + t, er, f) ?: return null
        val z2 = symmetric(w, t, 2 * h2 + t, er, f) ?: return null
        return 2 * z1 * z2 / (z1 + z2)
    }

    /** Homogeneous dielectric: phase depends only on εr. */
    private fun degPerMeter(er: Double, f: Double) = 360.0 * f * sqrt(er) / Constants.LIGHTSPEED

    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val er = v.getValue(FieldId.ER)
        val f = v.getValue(FieldId.FREQ)
        val z = impedance(v.getValue(FieldId.W), v.getValue(FieldId.T), v.getValue(FieldId.H), v.getValue(FieldId.H2), er, f)
            ?: return EngineResult(errors = mapOf(FieldId.W to R.string.unreasonable_value))
        return EngineResult(buildMap {
            put(FieldId.Z0, z)
            v[FieldId.L]?.let { put(FieldId.PHS, it * degPerMeter(er, f)) }
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val t = requireNotNull(target)
        val er = v.getValue(FieldId.ER)
        val f = v.getValue(FieldId.FREQ)
        val zTarget = v.getValue(FieldId.Z0)
        val thick = v.getValue(FieldId.T)
        val z: (Double) -> Double? = { x ->
            if (t == FieldId.W) impedance(x, thick, v.getValue(FieldId.H), v.getValue(FieldId.H2), er, f)
            else impedance(v.getValue(FieldId.W), thick, x, v.getValue(FieldId.H2), er, f)
        }
        // Z0 falls as W grows and rises as H grows.
        val x = bisectLog(1e-7, 1.0, increasing = t == FieldId.H, target = zTarget, f = z)
            ?: return EngineResult(errors = mapOf(t to R.string.synthesize_failed))
        return EngineResult(buildMap {
            put(t, x)
            v[FieldId.PHS]?.let { put(FieldId.L, it / degPerMeter(er, f)) }
        })
    }
}

/**
 * Finds x in [lo, hi] (bisecting in log space) where the monotonic [f] equals [target],
 * or null when the target is outside the bracket.
 */
private fun bisectLog(lo: Double, hi: Double, increasing: Boolean, target: Double, f: (Double) -> Double?): Double? {
    var a = ln(lo); var b = ln(hi)
    val fa = f(lo) ?: return null
    val fb = f(hi) ?: return null
    if ((fa - target) * (fb - target) > 0) return null
    repeat(100) {
        val m = (a + b) / 2
        val fm = f(exp(m)) ?: return null
        if ((fm < target) == increasing) a = m else b = m
    }
    return exp((a + b) / 2)
}

/** Edge-coupled CPW (zero thickness). Synthesize solves W and S with G2 and H fixed. */
private object EcpwEngine : LineEngine {
    private data class Result(val z0e: Double, val z0o: Double, val eeffE: Double, val eeffO: Double) {
        val z0 get() = sqrt(z0e * z0o)
        val k get() = (z0e - z0o) / (z0e + z0o)
    }

    private fun run(w: Double, s: Double, v: Map<FieldId, Double>): Result? =
        CoupledCpw.analyze(w, s, v.getValue(FieldId.G2), v.getValue(FieldId.H), v.getValue(FieldId.ER))
            ?.let { (e, o) -> Result(e.z0, o.z0, e.eeff, o.eeff) }
            ?.takeIf { listOf(it.z0e, it.z0o, it.eeffE, it.eeffO).all { x -> x.isUsable() } }

    /** Same convention as the other coupled lines: phase from the geometric mean of the modal εeff. */
    private fun degPerMeter(r: Result, f: Double) = 360.0 * f * sqrt(sqrt(r.eeffE * r.eeffO)) / Constants.LIGHTSPEED

    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val r = run(v.getValue(FieldId.W), v.getValue(FieldId.S), v)
            ?: return EngineResult(errors = mapOf(FieldId.W to R.string.unreasonable_value))
        return EngineResult(buildMap {
            put(FieldId.Z0, r.z0); put(FieldId.K, r.k); put(FieldId.Z0O, r.z0o); put(FieldId.Z0E, r.z0e)
            v[FieldId.L]?.let { put(FieldId.PHS, it * degPerMeter(r, v.getValue(FieldId.FREQ))) }
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val (z0, k) = if (useZ0k) {
            v.getValue(FieldId.Z0) to v.getValue(FieldId.K)
        } else {
            val ze = v.getValue(FieldId.Z0E)
            val zo = v.getValue(FieldId.Z0O)
            if (ze <= zo) {
                return EngineResult(errors = mapOf(FieldId.Z0E to R.string.Z0e_larger_Z0o, FieldId.Z0O to R.string.Z0e_larger_Z0o))
            }
            sqrt(ze * zo) to (ze - zo) / (ze + zo)
        }
        if (k <= 0.0 || k >= 1.0) return EngineResult(errors = mapOf(FieldId.K to R.string.k_out_of_range))
        val failed = EngineResult(errors = mapOf(FieldId.W to R.string.synthesize_failed, FieldId.S to R.string.synthesize_failed))

        // For a spacing S, the width that gives Z0 (Z0 falls as W grows).
        fun widthFor(s: Double): Double? = bisectLog(1e-7, 1.0, increasing = false, target = z0) { w -> run(w, s, v)?.z0 }
        // Coupling falls as the strips move apart.
        val s = bisectLog(1e-7, 1.0, increasing = false, target = k) { s -> widthFor(s)?.let { run(it, s, v)?.k } }
            ?: return failed
        val w = widthFor(s) ?: return failed
        val r = run(w, s, v) ?: return failed
        return EngineResult(buildMap {
            put(FieldId.W, w); put(FieldId.S, s)
            v[FieldId.PHS]?.let { put(FieldId.L, it / degPerMeter(r, v.getValue(FieldId.FREQ))) }
            if (useZ0k) { put(FieldId.Z0O, r.z0o); put(FieldId.Z0E, r.z0e) } else { put(FieldId.Z0, r.z0); put(FieldId.K, r.k) }
        })
    }
}

/**
 * Twisted pair: parallel wires, Z0 = (η0 / π√εeff)·acosh(D/d), with Lefferson's effective
 * permittivity εeff = 1 + q(εr − 1), q = 0.25 + 0.0004·θ² and twist angle θ = atan(T·π·D) in
 * degrees (Wadell §3.3.4). T is twists per metre.
 */
private object TwpEngine : LineEngine {
    fun eeff(er: Double, twist: Double, spacing: Double): Double {
        val theta = Math.toDegrees(atan(twist * PI * spacing))
        return 1.0 + (0.25 + 0.0004 * theta * theta) * (er - 1.0)
    }

    fun impedance(d: Double, spacing: Double, er: Double, twist: Double): Double =
        Constants.FREESPACEZ0 / (PI * sqrt(eeff(er, twist, spacing))) * acosh(spacing / d)

    private fun degPerMeter(er: Double, twist: Double, spacing: Double, f: Double) =
        360.0 * f * sqrt(eeff(er, twist, spacing)) / Constants.LIGHTSPEED

    private val tooClose = EngineResult(errors = mapOf(FieldId.B to R.string.twp_spacing_too_small))

    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val d = v.getValue(FieldId.A)
        val s = v.getValue(FieldId.B)
        if (s <= d) return tooClose
        val er = v.getValue(FieldId.ER)
        val twist = v.getValue(FieldId.TWIST)
        return EngineResult(buildMap {
            put(FieldId.Z0, impedance(d, s, er, twist))
            v[FieldId.L]?.let { put(FieldId.PHS, it * degPerMeter(er, twist, s, v.getValue(FieldId.FREQ))) }
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val t = requireNotNull(target)
        val z0 = v.getValue(FieldId.Z0)
        val er = v.getValue(FieldId.ER)
        val twist = v.getValue(FieldId.TWIST)
        val (d, s) = if (t == FieldId.A) {
            // εeff does not depend on the diameter, so invert acosh directly.
            val s = v.getValue(FieldId.B)
            s / cosh(z0 * PI * sqrt(eeff(er, twist, s)) / Constants.FREESPACEZ0) to s
        } else {
            // The twist angle depends on the spacing, so solve numerically; Z0 grows with spacing.
            val d = v.getValue(FieldId.A)
            val s = bisectLog(d * (1 + 1e-9), 100.0, increasing = true, target = z0) { impedance(d, it, er, twist) }
                ?: return EngineResult(errors = mapOf(FieldId.B to R.string.synthesize_failed))
            d to s
        }
        if (s <= d) return tooClose
        return EngineResult(buildMap {
            put(t, if (t == FieldId.A) d else s)
            v[FieldId.PHS]?.let { put(FieldId.L, it / degPerMeter(er, twist, s, v.getValue(FieldId.FREQ))) }
        })
    }
}

/** Rectangular waveguide, TE10: Z0 is the wave impedance and Synthesize solves for the broad wall a. */
private object WgEngine : LineEngine {
    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val a = v.getValue(FieldId.A)
        val er = v.getValue(FieldId.ER)
        val f = v.getValue(FieldId.FREQ)
        if (v.getValue(FieldId.B) >= a) return EngineResult(errors = mapOf(FieldId.B to R.string.wg_b_too_large))
        if (f <= Waveguide.cutoff(a, er)) return EngineResult(errors = mapOf(FieldId.FREQ to R.string.wg_below_cutoff))
        return EngineResult(buildMap {
            put(FieldId.Z0, Waveguide.waveImpedance(f, a, er))
            v[FieldId.L]?.let { put(FieldId.PHS, Math.toDegrees(Waveguide.beta(f, a, er) * it)) }
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val er = v.getValue(FieldId.ER)
        val f = v.getValue(FieldId.FREQ)
        val a = Waveguide.widthFor(v.getValue(FieldId.Z0), f, er)
        if (a.isNaN()) return EngineResult(errors = mapOf(FieldId.Z0 to R.string.wg_z_too_low))
        if (v.getValue(FieldId.B) >= a) return EngineResult(errors = mapOf(FieldId.B to R.string.wg_b_too_large))
        return EngineResult(buildMap {
            put(FieldId.A, a)
            v[FieldId.PHS]?.let { put(FieldId.L, Math.toRadians(it) / Waveguide.beta(f, a, er)) }
        })
    }
}

/** Coupled microstrip (zero thickness) or coupled stripline. */
private class CoupledEngine(private val stripline: Boolean) : LineEngine {
    override fun analyze(v: Map<FieldId, Double>): EngineResult {
        val w = v.getValue(FieldId.W)
        val s = v.getValue(FieldId.S)
        val l = v[FieldId.L] ?: 0.0
        val h = v.getValue(FieldId.H)
        val t = v[FieldId.T] ?: 0.0
        val er = v.getValue(FieldId.ER)
        val f = v.getValue(FieldId.FREQ)
        val out = if (stripline) {
            CslinModel().apply {
                setMetalWidth(w, "m"); setMetalSpace(s, "m"); setMetalLength(l, "m"); setMetalThick(t, "m")
                setSubHeight(h, "m"); subEpsilon = er; setFrequency(f, "Hz")
            }.let { CslinCalculator().getAnaResult(it) }
                .let { Coupled(it.errorCode, it.impedance, it.couplingFactor, it.impedanceOdd, it.impedanceEven, it.phase) }
        } else {
            CmlinModel().apply {
                setMetalWidth(w, "m"); setMetalSpace(s, "m"); setMetalLength(l, "m"); setMetalThick(t, "m")
                setSubHeight(h, "m"); subEpsilon = er; setFrequency(f, "Hz")
            }.let { CmlinCalculator().getAnaResult(it) }
                .let { Coupled(it.errorCode, it.impedance, it.couplingFactor, it.impedanceOdd, it.impedanceEven, it.phase) }
        }
        return analysisResult(out.code, buildMap {
            put(FieldId.Z0, out.z0); put(FieldId.K, out.k); put(FieldId.Z0O, out.z0o); put(FieldId.Z0E, out.z0e)
            putIfGiven(FieldId.L in v, FieldId.PHS, out.phase)
        })
    }

    override fun synthesize(v: Map<FieldId, Double>, target: FieldId?, useZ0k: Boolean): EngineResult {
        val z0 = if (useZ0k) v.getValue(FieldId.Z0) else 0.0
        val k = if (useZ0k) v.getValue(FieldId.K) else 0.0
        val z0e = if (useZ0k) 0.0 else v.getValue(FieldId.Z0E)
        val z0o = if (useZ0k) 0.0 else v.getValue(FieldId.Z0O)
        val phs = v[FieldId.PHS] ?: 0.0
        val h = v.getValue(FieldId.H)
        val t = v[FieldId.T] ?: 0.0
        val er = v.getValue(FieldId.ER)
        val f = v.getValue(FieldId.FREQ)
        val (out, dims) = if (stripline) {
            CslinModel().apply {
                impedance = z0; couplingFactor = k; impedanceEven = z0e; impedanceOdd = z0o; phase = phs
                setFrequency(f, "Hz"); subEpsilon = er; setSubHeight(h, "m"); setMetalThick(t, "m")
            }.let { CslinCalculator().getSynResult(it, useZ0k) }.let {
                Coupled(it.errorCode, it.impedance, it.couplingFactor, it.impedanceOdd, it.impedanceEven, it.phase) to
                    Triple(it.metalWidth, it.metalSpace, it.metalLength)
            }
        } else {
            CmlinModel().apply {
                impedance = z0; couplingFactor = k; impedanceEven = z0e; impedanceOdd = z0o; phase = phs
                setFrequency(f, "Hz"); subEpsilon = er; setSubHeight(h, "m"); setMetalThick(t, "m")
            }.let { CmlinCalculator().getSynResult(it, useZ0k) }.let {
                Coupled(it.errorCode, it.impedance, it.couplingFactor, it.impedanceOdd, it.impedanceEven, it.phase) to
                    Triple(it.metalWidth, it.metalSpace, it.metalLength)
            }
        }
        val values = buildMap {
            put(FieldId.W, dims.first)
            put(FieldId.S, dims.second)
            putIfGiven(FieldId.PHS in v, FieldId.L, dims.third)
            if (useZ0k) { put(FieldId.Z0O, out.z0o); put(FieldId.Z0E, out.z0e) }
            else { put(FieldId.Z0, out.z0); put(FieldId.K, out.k) }
        }
        return solved(out.code, values, listOf(FieldId.W, FieldId.S))
    }

    private data class Coupled(
        val code: ERROR?, val z0: Double, val k: Double, val z0o: Double, val z0e: Double, val phase: Double,
    )
}
