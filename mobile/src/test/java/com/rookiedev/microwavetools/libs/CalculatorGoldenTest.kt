package com.rookiedev.microwavetools.libs

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks in calculator outputs for each line type using the app's default values,
 * so UI refactors can't silently change the math.
 */
class CalculatorGoldenTest {

    private fun mm(v: Double) = v * 1000

    @Test
    fun mlin() {
        val ana = MlinModel().apply {
            setMetalWidth(2.9, "mm"); setMetalLength(40.0, "mm"); setMetalThick(0.035, "mm")
            setSubHeight(1.6, "mm"); subEpsilon = 4.6; setFrequency(1.0, "GHz")
        }.let { MlinCalculator().getAnaResult(it) }
        GOLDEN.check("mlin.ana.z0", ana.impedance)
        GOLDEN.check("mlin.ana.phase", ana.phase)

        for ((target, name) in listOf(Constants.Synthesize_Width to "w", Constants.Synthesize_Height to "h")) {
            val syn = MlinModel().apply {
                impedance = 50.0; phase = 90.0; setFrequency(1.0, "GHz"); subEpsilon = 4.6
                setMetalThick(0.035, "mm")
                if (target == Constants.Synthesize_Width) setSubHeight(1.6, "mm") else setMetalWidth(2.9, "mm")
            }.let { MlinCalculator().getSynResult(it, target) }
            assertEquals(Constants.ERROR.NO_ERROR, syn.errorCode)
            GOLDEN.check("mlin.syn.$name", mm(if (name == "w") syn.metalWidth else syn.subHeight))
            GOLDEN.check("mlin.syn.$name.l", mm(syn.metalLength))
        }
    }

    @Test
    fun slin() {
        val ana = SlinModel().apply {
            setMetalWidth(0.6, "mm"); setMetalLength(34.9, "mm"); setMetalThick(0.035, "mm")
            setSubHeight(1.6, "mm"); subEpsilon = 4.6; setFrequency(1.0, "GHz")
        }.let { SlinCalculator().getAnaResult(it) }
        GOLDEN.check("slin.ana.z0", ana.impedance)
        GOLDEN.check("slin.ana.phase", ana.phase)

        for ((target, name) in listOf(Constants.Synthesize_Width to "w", Constants.Synthesize_Height to "h")) {
            val syn = SlinModel().apply {
                impedance = 50.0; phase = 90.0; setFrequency(1.0, "GHz"); subEpsilon = 4.6
                setMetalThick(0.035, "mm")
                if (target == Constants.Synthesize_Width) setSubHeight(1.6, "mm") else setMetalWidth(0.6, "mm")
            }.let { SlinCalculator().getSynResult(it, target) }
            assertEquals(Constants.ERROR.NO_ERROR, syn.errorCode)
            GOLDEN.check("slin.syn.$name", mm(if (name == "w") syn.metalWidth else syn.subHeight))
            GOLDEN.check("slin.syn.$name.l", mm(syn.metalLength))
        }
    }

    private fun cpw(ground: Boolean, w: Double, s: Double, l: Double, t: Double, prefix: String) {
        val ana = CpwModel().apply {
            setMetalWidth(w, "mm"); setMetalSpace(s, "mm"); setMetalLength(l, "mm"); setMetalThick(t, "mm")
            setSubHeight(1.6, "mm"); subEpsilon = 4.6; setFrequency(1.0, "GHz")
        }.let { CpwCalculator().getAnaResult(it, ground) }
        GOLDEN.check("$prefix.ana.z0", ana.impedance)
        GOLDEN.check("$prefix.ana.phase", ana.phase)

        for ((target, name) in listOf(
            Constants.Synthesize_Width to "w", Constants.Synthesize_Gap to "s", Constants.Synthesize_Height to "h"
        )) {
            val syn = CpwModel().apply {
                impedance = 50.0; phase = 90.0; setFrequency(1.0, "GHz"); subEpsilon = 4.6
                setMetalThick(t, "mm")
                if (target != Constants.Synthesize_Width) setMetalWidth(w, "mm")
                if (target != Constants.Synthesize_Gap) setMetalSpace(s, "mm")
                if (target != Constants.Synthesize_Height) setSubHeight(1.6, "mm")
            }.let { CpwCalculator().getSynResult(it, target, ground) }
            assertEquals(Constants.ERROR.NO_ERROR, syn.errorCode)
            val v = when (name) { "w" -> syn.metalWidth; "s" -> syn.metalSpace; else -> syn.subHeight }
            GOLDEN.check("$prefix.syn.$name", mm(v))
            GOLDEN.check("$prefix.syn.$name.l", mm(syn.metalLength))
        }
    }

    @Test
    fun cpw() = cpw(false, 1.0, 0.244, 44.464, 0.035, "cpw")

    @Test
    fun gcpw() = cpw(true, 1.0, 0.15, 44.38, 0.0, "gcpw")

    @Test
    fun coax() {
        val ana = CoaxModel().apply {
            setCoreRadius(0.167, "mm"); setSubRadius(1.0, "mm"); setCoreOffset(0.0, "mm")
            setMetalLength(34.945, "mm"); subEpsilon = 4.6; setFrequency(1.0, "GHz")
        }.let { CoaxCalculator().getAnaResult(it) }
        assertEquals(Constants.ERROR.NO_ERROR, ana.errorCode)
        GOLDEN.check("coax.ana.z0", ana.impedance)
        GOLDEN.check("coax.ana.phase", ana.phase)

        for ((target, name) in listOf(
            Constants.Synthesize_CoreRadius to "a", Constants.Synthesize_SubRadius to "b"
        )) {
            val syn = CoaxModel().apply {
                impedance = 50.0; phase = 90.0; setFrequency(1.0, "GHz"); subEpsilon = 4.6
                setCoreOffset(0.0, "mm")
                if (target != Constants.Synthesize_CoreRadius) setCoreRadius(0.167, "mm")
                if (target != Constants.Synthesize_SubRadius) setSubRadius(1.0, "mm")
            }.let { CoaxCalculator().getSynResult(it, target) }
            assertEquals(Constants.ERROR.NO_ERROR, syn.errorCode)
            GOLDEN.check("coax.syn.$name", mm(if (name == "a") syn.coreRadius else syn.subRadius))
            GOLDEN.check("coax.syn.$name.l", mm(syn.metalLength))
        }
    }

    @Test
    fun cmlin() {
        val ana = CmlinModel().apply {
            setMetalWidth(2.7, "mm"); setMetalSpace(0.8, "mm"); setMetalLength(40.9, "mm"); setMetalThick(0.0, "mm")
            setSubHeight(1.6, "mm"); subEpsilon = 4.6; setFrequency(1.0, "GHz")
        }.let { CmlinCalculator().getAnaResult(it) }
        GOLDEN.check("cmlin.ana.z0", ana.impedance)
        GOLDEN.check("cmlin.ana.k", ana.couplingFactor)
        GOLDEN.check("cmlin.ana.z0e", ana.impedanceEven)
        GOLDEN.check("cmlin.ana.z0o", ana.impedanceOdd)
        GOLDEN.check("cmlin.ana.phase", ana.phase)

        val syn = CmlinModel().apply {
            impedance = 50.0; couplingFactor = 0.2; phase = 90.0; setFrequency(1.0, "GHz")
            subEpsilon = 4.6; setSubHeight(1.6, "mm"); setMetalThick(0.0, "mm")
        }.let { CmlinCalculator().getSynResult(it, true) }
        assertEquals(Constants.ERROR.NO_ERROR, syn.errorCode)
        GOLDEN.check("cmlin.syn.w", mm(syn.metalWidth))
        GOLDEN.check("cmlin.syn.s", mm(syn.metalSpace))
        GOLDEN.check("cmlin.syn.l", mm(syn.metalLength))
    }

    @Test
    fun cslin() {
        val ana = CslinModel().apply {
            setMetalWidth(0.59, "mm"); setMetalSpace(0.36, "mm"); setMetalLength(34.9, "mm")
            setMetalThick(0.035, "mm"); setSubHeight(1.6, "mm"); subEpsilon = 4.6; setFrequency(1.0, "GHz")
        }.let { CslinCalculator().getAnaResult(it) }
        GOLDEN.check("cslin.ana.z0", ana.impedance)
        GOLDEN.check("cslin.ana.k", ana.couplingFactor)
        GOLDEN.check("cslin.ana.z0e", ana.impedanceEven)
        GOLDEN.check("cslin.ana.z0o", ana.impedanceOdd)
        GOLDEN.check("cslin.ana.phase", ana.phase)

        val syn = CslinModel().apply {
            impedance = 50.0; couplingFactor = 0.2; phase = 90.0; setFrequency(1.0, "GHz")
            subEpsilon = 4.6; setSubHeight(1.6, "mm"); setMetalThick(0.035, "mm")
        }.let { CslinCalculator().getSynResult(it, true) }
        assertEquals(Constants.ERROR.NO_ERROR, syn.errorCode)
        GOLDEN.check("cslin.syn.w", mm(syn.metalWidth))
        GOLDEN.check("cslin.syn.s", mm(syn.metalSpace))
        GOLDEN.check("cslin.syn.l", mm(syn.metalLength))
    }

    private object GOLDEN {
        val values: Map<String, Double> = mapOf(
            "cpw.ana.z0" to 50.00086336980642,
            "cpw.ana.phase" to 90.00062642086027,
            "cpw.syn.w" to 1.0000697305755968,
            "cpw.syn.w.l" to 44.463721375330245,
            "cpw.syn.s" to 0.24399060766150793,
            "cpw.syn.s.l" to 44.46363522708446,
            "cpw.syn.h" to 1.6013835847505953,
            "cpw.syn.h.l" to 44.46292276351442,
            "coax.ana.z0" to 50.0341972412833,
            "coax.ana.phase" to 90.00067331192508,
            "coax.syn.a" to 0.16720440966005168,
            "coax.syn.a.l" to 34.94473856989779,
            "coax.syn.b" to 0.9987774864283838,
            "coax.syn.b.l" to 34.94473856989779,
            "gcpw.ana.z0" to 49.77960252031052,
            "gcpw.ana.phase" to 89.99433062877165,
            "gcpw.syn.w" to 0.9858457688526493,
            "gcpw.syn.w.l" to 44.390682570436745,
            "gcpw.syn.s" to 0.1528052235608996,
            "gcpw.syn.s.l" to 44.37923723246604,
            "gcpw.syn.h" to 1.7074843524264232,
            "gcpw.syn.h.l" to 44.42891977113677,
            "mlin.ana.z0" to 50.14580526506941,
            "mlin.ana.phase" to 89.10075683574648,
            "mlin.syn.w" to 2.9144534191055644,
            "mlin.syn.w.l" to 40.3943025150707,
            "mlin.syn.h" to 1.592163310014993,
            "mlin.syn.h.l" to 40.39572378433004,
            "slin.ana.z0" to 51.21650332364802,
            "slin.ana.phase" to 89.8847760362337,
            "slin.syn.w" to 0.6313804737708161,
            "slin.syn.w.l" to 34.94473856989779,
            "slin.syn.h" to 1.5278234611665793,
            "slin.syn.h.l" to 34.94473856989779,
            "cmlin.ana.z0" to 50.584208606099715,
            "cmlin.ana.k" to 0.20599752999360502,
            "cmlin.ana.z0e" to 62.3414992274174,
            "cmlin.ana.z0o" to 41.044283374886895,
            "cmlin.ana.phase" to 89.97673994907669,
            "cmlin.syn.w" to 2.7649710686592,
            "cmlin.syn.s" to 0.831006249555381,
            "cmlin.syn.l" to 40.85647855466745,
            "cslin.ana.z0" to 49.97078990310951,
            "cslin.ana.k" to 0.19916631900124723,
            "cslin.ana.z0e" to 61.1483524930422,
            "cslin.ana.z0o" to 40.836420634960575,
            "cslin.ana.phase" to 89.8847760362337,
            "cslin.syn.w" to 0.5889366212121305,
            "cslin.syn.s" to 0.35833681603434486,
            "cslin.syn.l" to 34.94473856989779
        )

        fun check(key: String, actual: Double) {
            val expected = values[key]
            requireNotNull(expected) { "No golden value for $key (actual $actual)" }
            assertEquals(key, expected, actual, kotlin.math.abs(expected) * 1e-9 + 1e-12)
        }
    }
}
