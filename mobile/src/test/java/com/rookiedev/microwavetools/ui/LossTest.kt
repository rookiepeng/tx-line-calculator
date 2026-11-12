package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.libs.Constants
import com.rookiedev.microwavetools.libs.Loss
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sqrt

class LossTest {

    private fun assertRel(expected: Double, actual: Double?, rel: Double, msg: String = "") {
        requireNotNull(actual) { "missing $msg" }
        assertEquals(msg, expected, actual, kotlin.math.abs(expected) * rel)
    }

    private val f = 10e9
    private val copper = 5.8e7

    @Test
    fun coaxMatchesClosedForm() {
        val a = 0.5e-3; val b = 1.6e-3; val er = 2.1; val tand = 2e-4
        val out = lossOutputs(
            LineType.COAX,
            mapOf(
                FieldId.A to a, FieldId.B to b, FieldId.C to 0.0, FieldId.ER to er, FieldId.FREQ to f,
                FieldId.TAND to tand, FieldId.SIGMA to copper, FieldId.L to 0.1,
            ),
        )
        val z0 = Constants.FREESPACEZ0 / (2 * PI * sqrt(er)) * ln(b / a)
        val rs = Loss.surfaceResistance(f, copper)
        val ac = rs * (1 / a + 1 / b) / (4 * PI * z0) * Loss.NP_TO_DB
        val ad = PI * f * sqrt(er) * tand / Constants.LIGHTSPEED * Loss.NP_TO_DB
        assertRel(er, out[Output.EEFF], 1e-6, "eeff")
        assertRel(ac, out[Output.ALPHA_C], 1e-4, "alpha_c")
        assertRel(ad, out[Output.ALPHA_D], 1e-9, "alpha_d")
        assertRel((ac + ad) * 0.1, out[Output.LOSS], 1e-4, "loss")
    }

    @Test
    fun striplineIsHomogeneous() {
        val er = 3.0; val tand = 1e-3
        val out = lossOutputs(
            LineType.SLIN,
            mapOf(
                FieldId.W to 0.4e-3, FieldId.H to 1.0e-3, FieldId.T to 17.5e-6, FieldId.ER to er,
                FieldId.FREQ to f, FieldId.TAND to tand, FieldId.SIGMA to copper,
            ),
        )
        assertRel(er, out[Output.EEFF], 1e-6, "eeff")
        assertRel(PI * f * sqrt(er) * tand / Constants.LIGHTSPEED * Loss.NP_TO_DB, out[Output.ALPHA_D], 1e-6, "alpha_d")
        assertTrue(out.getValue(Output.ALPHA_C) > 0.0)
        assertFalse("no L, no insertion loss", Output.LOSS in out)
    }

    /**
     * 50 Ω microstrip on 20 mil RO4350B, 1 oz copper, 10 GHz. Wheeler's rule applied by hand to the
     * Hammerstad wide-strip Z0 gives ≈0.017 dB/cm before the thickness correction; dielectric ≈0.05 dB/cm.
     */
    @Test
    fun microstripOnRo4350bIsPlausible() {
        val v = mapOf(
            FieldId.W to 1.1e-3, FieldId.H to 0.508e-3, FieldId.T to 35e-6, FieldId.ER to 3.66,
            FieldId.FREQ to f, FieldId.TAND to 0.0037, FieldId.SIGMA to copper,
        )
        val out = lossOutputs(LineType.MLIN, v)
        val perCm = { o: Output -> out.getValue(o) / 100 }
        assertTrue("eeff ${out[Output.EEFF]}", out.getValue(Output.EEFF) in 2.6..3.1)
        assertTrue("ac ${perCm(Output.ALPHA_C)}", perCm(Output.ALPHA_C) in 0.015..0.035)
        assertTrue("ad ${perCm(Output.ALPHA_D)}", perCm(Output.ALPHA_D) in 0.04..0.06)

        // Roughness well above the skin depth (0.66 µm here) nearly doubles conductor loss, never more.
        val rough = lossOutputs(LineType.MLIN, v + (FieldId.ROUGH to 3e-6))
        val ratio = rough.getValue(Output.ALPHA_C) / out.getValue(Output.ALPHA_C)
        assertTrue("roughness ratio $ratio", ratio in 1.8..2.0)
    }

    @Test
    fun missingInputsLeaveOutputsOut() {
        val v = mapOf(
            FieldId.W to 1.1e-3, FieldId.H to 0.508e-3, FieldId.T to 35e-6, FieldId.ER to 3.66, FieldId.FREQ to f,
        )
        assertEquals(setOf(Output.EEFF), lossOutputs(LineType.MLIN, v).keys)
        assertEquals(
            setOf(Output.EEFF, Output.ALPHA_D, Output.ALPHA),
            lossOutputs(LineType.MLIN, v + (FieldId.TAND to 0.002)).keys,
        )
    }

    @Test
    fun coupledMicrostripModes() {
        val out = lossOutputs(
            LineType.CMLIN,
            mapOf(
                FieldId.W to 2.7e-3, FieldId.S to 0.8e-3, FieldId.H to 1.6e-3, FieldId.ER to 4.6,
                FieldId.FREQ to 1e9, FieldId.TAND to 0.02, FieldId.SIGMA to copper, FieldId.L to 0.04,
            ),
        )
        // The even mode keeps more field in the substrate.
        assertTrue(out.getValue(Output.EEFF_E) > out.getValue(Output.EEFF_O))
        assertTrue(out.getValue(Output.ALPHA_E) > 0.0 && out.getValue(Output.ALPHA_O) > 0.0)
        assertTrue(Output.LOSS_E in out && Output.LOSS_O in out)
    }

    /** Every line type's default design yields a full, finite set of results. */
    @Test
    fun everyLineTypeDefaults() {
        for (line in LineType.entries) {
            val v = line.fields.associateWith { id ->
                val d = line.defaultOf(id)
                val x = d.text.toDouble()
                when (id.unitKind) {
                    UnitKind.LENGTH, UnitKind.ROUGHNESS -> Constants.value2meter(x, d.unit)
                    UnitKind.FREQUENCY -> Constants.value2Hz(x, d.unit)
                    UnitKind.CONDUCTIVITY -> x * 1e6
                    else -> x
                }
            }
            val out = lossOutputs(line, v)
            val expected = if (line.coupled) Output.entries.filter { it.name.endsWith("_E") || it.name.endsWith("_O") }
            else listOf(Output.EEFF, Output.ALPHA_C, Output.ALPHA_D, Output.ALPHA, Output.LOSS)
            assertEquals(line.name, expected.toSet(), out.keys)
            assertTrue("${line.name}: $out", out.values.all { it.isFinite() && it > 0.0 })
            val eeff = out[Output.EEFF] ?: out.getValue(Output.EEFF_O)
            assertTrue("${line.name} eeff $eeff", eeff in 1.0..4.6 + 1e-9)
        }
    }
}
