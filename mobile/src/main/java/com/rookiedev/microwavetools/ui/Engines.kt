package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.CmlinCalculator
import com.rookiedev.microwavetools.libs.CmlinModel
import com.rookiedev.microwavetools.libs.CoaxCalculator
import com.rookiedev.microwavetools.libs.CoaxModel
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
