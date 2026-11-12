package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import com.rookiedev.microwavetools.libs.Materials
import com.rookiedev.microwavetools.libs.Waveguide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveguideTest {

    private val wr90 = Materials.waveguides.single { it.name == "WR-90" }
    private val base = mapOf(
        FieldId.A to wr90.a, FieldId.B to wr90.b, FieldId.ER to 1.0, FieldId.FREQ to 10e9,
    )
    private val engine = LineType.WG.engine()

    @Test
    fun wr90Cutoffs() {
        // WR-90 TE10 cutoff is 6.557 GHz; the next mode (TE20) is at twice that.
        assertEquals(6.557e9, Waveguide.cutoff(wr90.a, 1.0), 1e6)
        assertEquals(13.114e9, Waveguide.nextCutoff(wr90.a, wr90.b, 1.0), 2e6)
    }

    @Test
    fun analyzeAndSynthesizeRoundTrip() {
        val ana = engine.analyze(base + (FieldId.L to 0.05))
        val z = ana.values.getValue(FieldId.Z0)
        // η0 / √(1 − (6.557/10)²) ≈ 499 Ω.
        assertEquals(499.0, z, 0.5)
        val lambdaG = Constants.LIGHTSPEED / 10e9 / kotlin.math.sqrt(1 - (6.557 / 10.0) * (6.557 / 10.0))
        assertEquals(360.0 * 0.05 / lambdaG, ana.values.getValue(FieldId.PHS), 0.5)

        val syn = engine.synthesize(
            mapOf(FieldId.Z0 to z, FieldId.PHS to 90.0, FieldId.B to wr90.b, FieldId.ER to 1.0, FieldId.FREQ to 10e9),
            FieldId.A, false,
        )
        assertEquals(wr90.a, syn.values.getValue(FieldId.A), 1e-9)
        assertEquals(lambdaG / 4, syn.values.getValue(FieldId.L), 1e-5)
    }

    @Test
    fun errors() {
        assertEquals(
            mapOf(FieldId.FREQ to R.string.wg_below_cutoff),
            engine.analyze(base + (FieldId.FREQ to 6e9)).errors,
        )
        assertEquals(mapOf(FieldId.B to R.string.wg_b_too_large), engine.analyze(base + (FieldId.B to wr90.a)).errors)
        assertEquals(
            mapOf(FieldId.Z0 to R.string.wg_z_too_low),
            engine.synthesize(base + (FieldId.Z0 to 300.0), FieldId.A, false).errors,
        )
    }

    /** Copper WR-90 at 10 GHz loses about 0.11 dB/m (Pozar's TE10 formula). */
    @Test
    fun wr90CopperLoss() {
        val out = lossOutputs(LineType.WG, base + (FieldId.SIGMA to 5.8e7) + (FieldId.TAND to 0.0) + (FieldId.L to 1.0))
        val ac = out.getValue(Output.ALPHA_C)
        assertTrue("alpha_c $ac dB/m", ac in 0.10..0.12)
        assertEquals(0.0, out.getValue(Output.ALPHA_D), 0.0)
        assertEquals(ac, out.getValue(Output.LOSS), 1e-12)
        // Group delay is slower than light: 1/vg = 1/(c·0.7550).
        assertEquals(1 / (Constants.LIGHTSPEED * 0.7550), out.getValue(Output.DELAY), 1e-12)
    }

    @Test
    fun sweepStartsAtCutoff() {
        val pts = sweep(LineType.WG, base + (FieldId.SIGMA to 5.8e7), 5e9, 15e9, 11)
        // 5 and 6 GHz are below the 6.557 GHz cutoff and are skipped.
        assertEquals(9, pts.size)
        assertTrue(pts.zipWithNext().all { (p, q) -> q.z0.single() < p.z0.single() })
    }
}
