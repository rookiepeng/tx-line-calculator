package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.libs.CoupledCpw
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class CoupledCpwTest {

    private val engine = LineType.ECPW.engine()
    private val base = mapOf(FieldId.H to 1.6e-3, FieldId.G2 to 0.2e-3, FieldId.ER to 4.6, FieldId.FREQ to 1e9)

    @Test
    fun ellipticRatio() {
        // K(k)/K(k') is 1 at k = 1/√2 and inverts under k ↔ k'.
        assertEquals(1.0, CoupledCpw.ellipticRatio(1 / sqrt(2.0)), 1e-12)
        val k = 0.3
        assertEquals(1.0, CoupledCpw.ellipticRatio(k) * CoupledCpw.ellipticRatio(sqrt(1 - k * k)), 1e-12)
        // K(0.5) = 1.685750354812596, K(√0.75) = 2.156515647499643.
        assertEquals(1.685750354812596 / 2.156515647499643, CoupledCpw.ellipticRatio(0.5), 1e-12)
    }

    /**
     * As S → 0 the two strips merge into one CPW of width 2W, so the even mode (each strip carrying
     * half the current) has twice that CPW's impedance. The existing finite-substrate CPW model must agree.
     */
    @Test
    fun mergedStripsMatchSingleCpw() {
        val (even, _) = CoupledCpw.analyze(0.5e-3, 1e-9, 0.2e-3, 1.6e-3, 4.6)!!
        val single = LineType.CPW.engine().analyze(
            mapOf(FieldId.W to 1.0e-3, FieldId.S to 0.2e-3, FieldId.H to 1.6e-3, FieldId.T to 0.0, FieldId.ER to 4.6, FieldId.FREQ to 1e9),
        ).values.getValue(FieldId.Z0)
        assertEquals(2 * single, even.z0, 2 * single * 0.01)
    }

    /**
     * Far apart, each strip sees ground on its outer side only: a strip beside a semi-infinite ground,
     * k = √(W / (W + G)), Z = η0 / (2√εeff) · K(k')/K(k) on a thick substrate.
     */
    @Test
    fun weakCouplingIsStripBesideGround() {
        val w = 0.5e-3; val g = 0.2e-3
        val (even, odd) = CoupledCpw.analyze(w, 10.0, g, 100.0, 4.6)!!
        val expected = 376.730313 / (2 * sqrt(2.8)) / CoupledCpw.ellipticRatio(sqrt(w / (w + g)))
        assertEquals(expected, even.z0, expected * 1e-4)
        assertEquals(expected, odd.z0, expected * 1e-4)
    }

    /** On a very thick substrate εeff tends to (εr + 1)/2 for both modes. */
    @Test
    fun thickSubstrateLimit() {
        val (even, odd) = CoupledCpw.analyze(0.5e-3, 0.2e-3, 0.2e-3, 1.0, 4.6)!!
        assertEquals(2.8, even.eeff, 1e-6)
        assertEquals(2.8, odd.eeff, 1e-6)
        assertTrue(odd.z0 < even.z0)
    }

    @Test
    fun synthesizeRoundTrips() {
        val z0k = engine.synthesize(base + mapOf(FieldId.Z0 to 70.0, FieldId.K to 0.3, FieldId.PHS to 90.0), null, true)
        val w = z0k.values.getValue(FieldId.W)
        val s = z0k.values.getValue(FieldId.S)
        val back = engine.analyze(base + mapOf(FieldId.W to w, FieldId.S to s)).values
        assertEquals(70.0, back.getValue(FieldId.Z0), 1e-6)
        assertEquals(0.3, back.getValue(FieldId.K), 1e-8)

        val zeo = engine.synthesize(base + mapOf(FieldId.Z0E to 100.0, FieldId.Z0O to 55.0), null, false)
        val back2 = engine.analyze(base + mapOf(FieldId.W to zeo.values.getValue(FieldId.W), FieldId.S to zeo.values.getValue(FieldId.S))).values
        assertEquals(100.0, back2.getValue(FieldId.Z0E), 1e-6)
        assertEquals(55.0, back2.getValue(FieldId.Z0O), 1e-6)

        assertEquals(setOf(FieldId.Z0E, FieldId.Z0O), engine.synthesize(base + mapOf(FieldId.Z0E to 40.0, FieldId.Z0O to 50.0), null, false).errors.keys)
    }

    @Test
    fun differentialOutputs() {
        val v = base + mapOf(FieldId.W to 0.5e-3, FieldId.S to 0.2e-3, FieldId.L to 1.0)
        val a = engine.analyze(v).values
        val out = lossOutputs(LineType.ECPW, v)
        assertEquals(2 * a.getValue(FieldId.Z0O), out.getValue(Output.Z_DIFF), 1e-9)
        assertEquals(a.getValue(FieldId.Z0E) / 2, out.getValue(Output.Z_CM), 1e-9)
    }
}
