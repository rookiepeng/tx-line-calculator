package com.rookiedev.microwavetools.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AsymmetricStriplineTest {

    private val engine = LineType.ASLIN.engine()
    private val t = 35e-6
    private val base = mapOf(
        FieldId.T to t, FieldId.H to 0.4e-3, FieldId.H2 to 1.2e-3, FieldId.ER to 4.6, FieldId.FREQ to 1e9,
    )

    private fun z(v: Map<FieldId, Double>) = engine.analyze(v).values.getValue(FieldId.Z0)

    /** A centered strip must match the symmetric stripline calculator exactly. */
    @Test
    fun centeredMatchesStripline() {
        val h = (1.6e-3 - t) / 2
        val asym = z(mapOf(FieldId.W to 0.6e-3, FieldId.T to t, FieldId.H to h, FieldId.H2 to h, FieldId.ER to 4.6, FieldId.FREQ to 1e9))
        val sym = LineType.SLIN.engine().analyze(
            mapOf(FieldId.W to 0.6e-3, FieldId.T to t, FieldId.H to 1.6e-3, FieldId.ER to 4.6, FieldId.FREQ to 1e9),
        ).values.getValue(FieldId.Z0)
        assertEquals(sym, asym, 1e-9)
    }

    /** Moving the strip toward one ground, with the total spacing fixed, lowers Z0. */
    @Test
    fun offsetLowersImpedance() {
        val total = 1.6e-3 - t
        val centered = z(base + mapOf(FieldId.W to 0.4e-3, FieldId.H to total / 2, FieldId.H2 to total / 2))
        val offset = z(base + mapOf(FieldId.W to 0.4e-3, FieldId.H to total / 4, FieldId.H2 to 3 * total / 4))
        assertTrue("$offset < $centered", offset < centered)
    }

    @Test
    fun synthesizeRoundTrips() {
        val synW = engine.synthesize(base + mapOf(FieldId.Z0 to 50.0, FieldId.PHS to 90.0), FieldId.W, false)
        val w = synW.values.getValue(FieldId.W)
        assertEquals(50.0, z(base + (FieldId.W to w)), 1e-6)

        val synH = engine.synthesize(base + mapOf(FieldId.Z0 to 45.0, FieldId.W to w), FieldId.H, false)
        val h = synH.values.getValue(FieldId.H)
        assertTrue(h < 0.4e-3)
        assertEquals(45.0, z(base + mapOf(FieldId.W to w, FieldId.H to h)), 1e-6)

        val impossible = engine.synthesize(base + mapOf(FieldId.Z0 to 5000.0), FieldId.W, false)
        assertEquals(setOf(FieldId.W), impossible.errors.keys)
    }

    @Test
    fun lossIsHomogeneous() {
        val out = lossOutputs(
            LineType.ASLIN,
            base + mapOf(FieldId.W to 0.3e-3, FieldId.TAND to 0.02, FieldId.SIGMA to 5.8e7),
        )
        assertEquals(4.6, out.getValue(Output.EEFF), 1e-9)
        assertTrue(out.getValue(Output.ALPHA_C) > 0.0)
        // A 0.3 mm copper strip at 1 GHz: a few thousandths of a dB per cm.
        assertTrue(out.getValue(Output.ALPHA_C) / 100 in 0.001..0.02)
    }
}
