package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.R
import com.rookiedev.microwavetools.libs.RfMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignsAndToolsTest {

    @Test
    fun mismatchConversions() {
        val gamma = RfMath.gammaFromVswr(2.0)!!
        assertEquals(1.0 / 3.0, gamma, 1e-12)
        assertEquals(2.0, RfMath.vswrFromGamma(gamma), 1e-12)
        assertEquals(9.542, RfMath.returnLossFromGamma(gamma), 1e-3)
        assertEquals(gamma, RfMath.gammaFromReturnLoss(RfMath.returnLossFromGamma(gamma))!!, 1e-12)
        assertEquals(0.512, RfMath.mismatchLoss(gamma), 1e-3)
        assertNull(RfMath.gammaFromVswr(0.5))
        assertNull(RfMath.gammaValid(1.0))
        assertTrue(RfMath.returnLossFromGamma(0.0).isInfinite())
    }

    @Test
    fun powerConversions() {
        assertEquals(1e-3, RfMath.wattsFromDbm(0.0), 1e-15)
        assertEquals(30.0, RfMath.dbmFromWatts(1.0), 1e-12)
        // 0 dBm across 50 Ω is 0.2236 V RMS.
        assertEquals(0.2236, RfMath.vrmsFromWatts(1e-3), 1e-4)
        assertEquals(70.71, RfMath.quarterWave(50.0, 100.0), 0.01)
    }

    private val design = SavedDesign(
        id = "abc",
        name = "Feed line, 50 Ω",
        line = LineType.MLIN,
        fields = mapOf("W" to ("2.9" to "mm"), "ER" to ("4.6" to ""), "Z0" to ("50" to "Ω")),
        target = FieldId.W,
        useZ0k = false,
        savedAt = 1_700_000_000_000L,
    )

    @Test
    fun designJsonRoundTrips() {
        assertEquals(listOf(design), decodeDesigns(encodeDesigns(listOf(design))))
    }

    @Test
    fun badStoredDataIsSkipped() {
        assertEquals(emptyList<SavedDesign>(), decodeDesigns(null))
        assertEquals(emptyList<SavedDesign>(), decodeDesigns("not json"))
        // An unknown line type (from a newer version, say) drops just that design.
        val json = encodeDesigns(listOf(design, design.copy(id = "x"))).replaceFirst("\"MLIN\"", "\"FUTURE\"")
        assertEquals(1, decodeDesigns(json).size)
    }

    @Test
    fun summaryListsFieldsAndResults() {
        val line = LineType.MLIN
        val state = CalculatorState(
            fields = line.fields.associateWith { id -> line.defaultOf(id).let { FieldState(it.text, it.unit) } },
            target = FieldId.W,
            useZ0k = true,
            extras = mapOf(Output.EEFF to 3.441, Output.ALPHA to 3.356),
        )
        val text = designSummary(line, state) { "s$it" }
        assertTrue(text.startsWith("s${R.string.full_name_mlin}\n"))
        assertTrue(text, text.contains("  s${R.string.label_w}: 2.9 mm"))
        assertTrue(text, text.contains("  s${R.string.label_z0}: 50.0 Ω"))
        assertTrue(text, text.contains("  s${R.string.out_eeff}: 3.441"))
        // L is in mm, so loss is per cm: 3.356 dB/m → 0.03356 dB/cm.
        assertTrue(text, text.contains("  s${R.string.out_alpha}: 0.03356 dB/cm"))
    }

    /** A nearly flat curve (Z0 within 0.01 Ω) must still get distinct tick labels. */
    @Test
    fun tickLabelsStayDistinct() {
        val step = niceStep(25.1892 - 25.1886, 4) // 0.0001 or 0.0002; both need four decimals
        assertEquals(listOf("25.1886", "25.1888", "25.189"), listOf(25.1886, 25.1888, 25.1890).map { tickLabel(it, step) })
        assertEquals(listOf("0.5", "1", "1.5"), listOf(0.5, 1.0, 1.5).map { tickLabel(it, 0.5) })
        assertEquals("200", tickLabel(200.0, 50.0))
        assertEquals("0", tickLabel(1e-17, 0.1))
    }

    @Test
    fun sweepCsvFormat() {
        val pts = listOf(
            SweepPoint(1e9, listOf(62.0, 41.0), listOf(3.2, 2.9), listOf(2.0, 3.0)),
            SweepPoint(2e9, listOf(61.5, 40.8), listOf(3.25, 2.95), emptyList()),
        )
        val csv = sweepCsv(pts, coupled = true, freqUnit = "GHz", perLength = 0.01 to "dB/cm")
        val lines = csv.trimEnd().lines()
        assertEquals(
            "f (GHz),Z0 even (Ohm),Z0 odd (Ohm),eeff even,eeff odd,loss even (dB/cm),loss odd (dB/cm)",
            lines[0],
        )
        assertEquals("1.00000,62.0000,41.0000,3.20000,2.90000,0.0200000,0.0300000", lines[1])
        assertEquals("2.00000,61.5000,40.8000,3.25000,2.95000,,", lines[2])
    }
}
