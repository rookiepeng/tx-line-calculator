package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.Constants
import com.rookiedev.microwavetools.libs.Loss
import com.rookiedev.microwavetools.libs.Materials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.acosh
import kotlin.math.sqrt

class TwistedPairTest {

    private val engine = LineType.TWP.engine()
    private val d = 0.511e-3

    private fun v(spacing: Double, er: Double = 1.0, twist: Double = 0.0) = mapOf(
        FieldId.A to d, FieldId.B to spacing, FieldId.ER to er, FieldId.TWIST to twist, FieldId.FREQ to 100e6,
    )

    /** Straight bare wires in air: the textbook twin-lead impedance. */
    @Test
    fun parallelWiresInAir() {
        val z = engine.analyze(v(1.5e-3)).values.getValue(FieldId.Z0)
        assertEquals(Constants.FREESPACEZ0 / PI * acosh(1.5e-3 / d), z, 1e-9)
    }

    /** Wheeler's rule must reproduce the twin-lead resistance, proximity effect included. */
    @Test
    fun conductorLossMatchesTwinLead() {
        val s = 1.5e-3
        val f = 100e6
        val out = lossOutputs(LineType.TWP, v(s) + (FieldId.SIGMA to 5.8e7))
        val z0 = Constants.FREESPACEZ0 / PI * acosh(s / d)
        val rs = Loss.surfaceResistance(f, 5.8e7)
        val ratio = s / d
        val r = 2 * rs / (PI * d) * ratio / sqrt(ratio * ratio - 1)
        assertEquals(r / (2 * z0) * Loss.NP_TO_DB, out.getValue(Output.ALPHA_C), r / (2 * z0) * Loss.NP_TO_DB * 1e-5)
        assertEquals(1.0, out.getValue(Output.EEFF), 1e-12)
    }

    /** Insulation fills a quarter of the field when straight; twisting packs the wires and raises εeff. */
    @Test
    fun twistRaisesEffectivePermittivity() {
        val straight = lossOutputs(LineType.TWP, v(1e-3, er = 2.25)).getValue(Output.EEFF)
        assertEquals(1 + 0.25 * 1.25, straight, 1e-9)
        val twisted = lossOutputs(LineType.TWP, v(1e-3, er = 2.25, twist = 66.0)).getValue(Output.EEFF)
        assertTrue(twisted > straight)
    }

    @Test
    fun synthesizeRoundTrips() {
        val base = v(0.0, er = 2.25, twist = 66.0) - FieldId.B
        val synB = engine.synthesize(base + mapOf(FieldId.Z0 to 100.0, FieldId.PHS to 90.0), FieldId.B, false)
        val s = synB.values.getValue(FieldId.B)
        assertEquals(100.0, engine.analyze(base + (FieldId.B to s)).values.getValue(FieldId.Z0), 1e-6)

        val synA = engine.synthesize(v(s, 2.25, 66.0) - FieldId.A + (FieldId.Z0 to 120.0), FieldId.A, false)
        val dA = synA.values.getValue(FieldId.A)
        assertEquals(120.0, engine.analyze(v(s, 2.25, 66.0) + (FieldId.A to dA)).values.getValue(FieldId.Z0), 1e-6)
        assertEquals(mapOf(FieldId.B to R.string.twp_spacing_too_small), engine.analyze(v(d)).errors)
    }

    @Test
    fun awg24Is0511mm() {
        assertEquals(0.511e-3, Materials.wireGauges.toMap().getValue(24), 0.001e-3)
    }
}
