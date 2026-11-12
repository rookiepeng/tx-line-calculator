package com.rookiedev.microwavetools.ui

import com.rookiedev.microwavetools.libs.Constants
import org.junit.AfterClass
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acosh
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Checks every calculator against theory that is coded here independently of the app: exact
 * conformal-mapping solutions, the published closed-form models the calculators cite, textbook
 * worked examples, physical limits, and analysis ↔ synthesis round trips.
 *
 * Each comparison is recorded; after the run the table is written to
 * `mobile/build/reports/theory-validation.md`.
 */
class TheoryValidationTest {

    // --- independent reference theory -----------------------------------------------------------

    private object Ref {
        /** Free-space impedance μ0·c (the SI definition the app also uses), Ω. */
        val ETA0 = 4e-7 * PI * 299_792_458.0
        const val C = 299_792_458.0

        private fun agm(a0: Double, b0: Double): Double {
            var a = a0; var b = b0
            while (abs(a - b) > 1e-15 * a) { val m = (a + b) / 2; b = sqrt(a * b); a = m }
            return a
        }

        /** Complete elliptic integral of the first kind, K(k) = π / (2·AGM(1, k')). */
        fun ellipK(k: Double) = PI / (2 * agm(1.0, sqrt(1 - k * k)))

        /** K(k)/K(k'). */
        fun kk(k: Double) = ellipK(k) / ellipK(sqrt(1 - k * k))

        /** Coax with core offset c (radii), Z = η/(2π)·acosh((a² + b² − c²)/(2ab)). */
        fun coax(a: Double, b: Double, c: Double, er: Double) =
            ETA0 / (2 * PI * sqrt(er)) * acosh((a * a + b * b - c * c) / (2 * a * b))

        /** Cohn's exact zero-thickness stripline: Z = η0/(4√εr)·K(k)/K(k'), k = sech(πW/2b). */
        fun stripline(w: Double, b: Double, er: Double) = ETA0 / (4 * sqrt(er)) * kk(1 / cosh(PI * w / (2 * b)))

        /** Cohn's exact zero-thickness coupled stripline, (even, odd). */
        fun coupledStripline(w: Double, s: Double, b: Double, er: Double): Pair<Double, Double> {
            val t1 = tanh(PI * w / (2 * b))
            val t2 = tanh(PI * (w + s) / (2 * b))
            fun z(k: Double) = ETA0 / (4 * sqrt(er)) / kk(k)
            return z(t1 * t2) to z(t1 / t2)
        }

        /** Hammerstad–Jensen (1980) air impedance of a zero-thickness microstrip, eq. (1)–(2). */
        fun hjZ01(u: Double): Double {
            val f = 6 + (2 * PI - 6) * exp(-(30.666 / u).pow(0.7528))
            return ETA0 / (2 * PI) * ln(f / u + sqrt(1 + (2 / u).pow(2)))
        }

        /** Hammerstad–Jensen static effective permittivity, eq. (3)–(5). */
        fun hjEeff(u: Double, er: Double): Double {
            val a = 1 + ln((u.pow(4) + (u / 52).pow(2)) / (u.pow(4) + 0.432)) / 49 + ln(1 + (u / 18.1).pow(3)) / 18.7
            val b = 0.564 * ((er - 0.9) / (er + 3)).pow(0.053)
            return (er + 1) / 2 + (er - 1) / 2 * (1 + 10 / u).pow(-a * b)
        }

        /** Hammerstad–Jensen static microstrip with the thickness correction, eq. (6)–(9): (Z0, εeff). */
        fun microstrip(w: Double, h: Double, t: Double, er: Double): Pair<Double, Double> {
            val u = w / h
            var u1 = u; var ur = u
            if (t > 0) {
                val tn = t / h
                val coth = 1 / tanh(sqrt(6.517 * u))
                val du1 = tn / PI * ln(1 + 4 * E / (tn * coth * coth))
                u1 = u + du1
                ur = u + 0.5 * (1 + 1 / cosh(sqrt(er - 1))) * du1
            }
            val z0 = hjZ01(ur) / sqrt(hjEeff(ur, er))
            val eeff = hjEeff(ur, er) * (hjZ01(u1) / hjZ01(ur)).pow(2)
            return z0 to eeff
        }

        /** Kirschning–Jansen (1982) dispersive εeff, with fn in GHz·mm. */
        fun kjEeff(u: Double, er: Double, eeff0: Double, fGHz: Double, hMm: Double): Double {
            val fn = fGHz * hMm
            val p1 = 0.27488 + (0.6315 + 0.525 / (1 + 0.0157 * fn).pow(20)) * u - 0.065683 * exp(-8.7513 * u)
            val p2 = 0.33622 * (1 - exp(-0.03442 * er))
            val p3 = 0.0363 * exp(-4.6 * u) * (1 - exp(-(fn / 38.7).pow(4.97)))
            val p4 = 1 + 2.751 * (1 - exp(-(er / 15.916).pow(8)))
            val p = p1 * p2 * ((0.1844 + p3 * p4) * fn).pow(1.5763)
            return er - (er - eeff0) / (1 + p)
        }

        /** Coplanar waveguide on a substrate of height h, no ground below, zero thickness (Wadell §3.4.1). */
        fun cpw(w: Double, s: Double, h: Double, er: Double): Pair<Double, Double> {
            val a = w; val b = w + 2 * s
            val k = a / b
            val k1 = sinh(PI * a / (4 * h)) / sinh(PI * b / (4 * h))
            val eeff = 1 + (er - 1) / 2 * kk(k1) / kk(k)
            return ETA0 / (4 * sqrt(eeff)) / kk(k) to eeff
        }

        /** Conductor-backed CPW, zero thickness (Wadell §3.4.3). */
        fun gcpw(w: Double, s: Double, h: Double, er: Double): Pair<Double, Double> {
            val a = w; val b = w + 2 * s
            val r = kk(a / b)
            val r3 = kk(tanh(PI * a / (4 * h)) / tanh(PI * b / (4 * h)))
            val eeff = (r + er * r3) / (r + r3)
            return ETA0 / (2 * sqrt(eeff)) / (r + r3) to eeff
        }

        /** Electrical length in degrees of a TEM line. */
        fun phase(l: Double, eeff: Double, f: Double) = 360 * l * sqrt(eeff) * f / C
    }

    // --- recording ----------------------------------------------------------------------------

    private data class Row(
        val line: String, val check: String, val case: String,
        val app: Double, val theory: Double, val tol: Double, val source: String,
    ) {
        val err = if (theory == 0.0) abs(app) else abs(app - theory) / abs(theory)
        val pass get() = err <= tol
    }

    companion object {
        private val rows = mutableListOf<Row>()

        /** Numbered in the report in this order; [refsFor] maps each row's source to these numbers. */
        private val REFERENCES = listOf(
            "S. B. Cohn, \"Characteristic impedance of the shielded-strip transmission line,\" " +
                "*IRE Trans. Microwave Theory Tech.*, vol. MTT-2, no. 2, pp. 52–57, Jul. 1954.",
            "S. B. Cohn, \"Shielded coupled-strip transmission line,\" " +
                "*IRE Trans. Microwave Theory Tech.*, vol. MTT-3, no. 5, pp. 29–38, Oct. 1955.",
            "E. Hammerstad and Ø. Jensen, \"Accurate models for microstrip computer-aided design,\" " +
                "*IEEE MTT-S Int. Microwave Symp. Dig.*, pp. 407–409, May 1980.",
            "M. Kirschning and R. H. Jansen, \"Accurate model for effective dielectric constant of microstrip " +
                "with validity up to millimetre-wave frequencies,\" *Electronics Letters*, vol. 18, no. 6, " +
                "pp. 272–273, Mar. 1982.",
            "M. Kirschning and R. H. Jansen, \"Accurate wide-range design equations for the frequency-dependent " +
                "characteristic of parallel coupled microstrip lines,\" *IEEE Trans. Microwave Theory Tech.*, " +
                "vol. MTT-32, no. 1, pp. 83–90, Jan. 1984.",
            "B. C. Wadell, *Transmission Line Design Handbook*. Norwood, MA: Artech House, 1991 " +
                "(§3.4.1 coplanar waveguide, §3.4.3 conductor-backed CPW, eccentric coax, parallel wires).",
            "D. M. Pozar, *Microwave Engineering*, 4th ed. Hoboken, NJ: Wiley, 2012 " +
                "(§2.7 and §3.1 TEM lines and loss, §3.3 rectangular waveguide, eq. (3.96), Examples 3.5 and 3.7).",
            "H. A. Wheeler, \"Transmission-line properties of a strip line between parallel planes,\" " +
                "*IEEE Trans. Microwave Theory Tech.*, vol. MTT-26, no. 11, pp. 866–876, Nov. 1978.",
            "EIA RS-261-B, *Rectangular Waveguides (WR3 to WR2300)*, Electronic Industries Association, 1979 " +
                "(WR-90: 22.86 × 10.16 mm, TE10 cutoff 6.557 GHz).",
        )

        private val ALSO_USED = listOf(
            "H. A. Wheeler, \"Formulas for the skin effect,\" *Proc. IRE*, vol. 30, no. 9, pp. 412–424, " +
                "Sep. 1942 (incremental-inductance rule for conductor loss).",
            "E. Hammerstad and Ø. Bekkadal, *Microstrip Handbook*, ELAB Report STF44 A74169, " +
                "University of Trondheim, 1975 (surface-roughness correction).",
            "P. Lefferson, \"Twisted magnet wire transmission line,\" *IEEE Trans. Parts, Hybrids, and Packaging*, " +
                "vol. PHP-7, no. 4, pp. 148–154, Dec. 1971 (twisted-pair effective permittivity).",
        )

        /** 1-based indices into [REFERENCES] for a row's source text. */
        private fun refsFor(source: String): List<Int> = when {
            source.startsWith("Round trip") -> emptyList()
            source.startsWith("Wheeler thick-strip") -> listOf(8, 1)
            source.contains("Cohn exact (coupled)") -> listOf(2)
            source.contains("Cohn") -> listOf(1)
            source.startsWith("Limit: uncoupled strips") -> listOf(2, 1)
            source.contains("K&J vs H&J") -> listOf(5, 3)
            source.contains("Hammerstad") -> listOf(3)
            source.contains("Kirschning") -> listOf(4)
            source.contains("Wadell") || source.startsWith("Limit: one CPW") ||
                source == "Limit: (εr+1)/2" -> listOf(6)
            source.contains("eccentric") || source.contains("twin lead") -> listOf(6)
            source.contains("WR-90") -> listOf(9)
            else -> listOf(7) // Pozar: TE10, TEM loss and phase, coax, skin depth, worked examples
        }

        @JvmStatic
        @AfterClass
        fun writeReport() {
            val out = File("build/reports/theory-validation.md")
            out.parentFile.mkdirs()
            out.writeText(buildString {
                appendLine("# Theory validation")
                appendLine()
                appendLine("Each row compares the app's result with an independent implementation of the published theory")
                appendLine("(written separately in the test), an exact closed form, a textbook example, or a physical limit.")
                appendLine("Agreement shows the app implements each model correctly; it does not measure the models' own")
                appendLine("accuracy against field solvers or measurement. Numbers in the Ref. column point to the")
                appendLine("[references](#references); round trips check synthesis against the app's own analysis.")
                appendLine()
                appendLine("${rows.count { it.pass }} of ${rows.size} comparisons within tolerance.")
                appendLine()
                appendLine("| Line | Check | Case | App | Theory | Rel. error | Tolerance | Source | Ref. |")
                appendLine("|---|---|---|---|---|---|---|---|---|")
                for (r in rows.sortedBy { it.line }) {
                    val refs = refsFor(r.source).joinToString(", ") { "[$it]" }.ifEmpty { "—" }
                    appendLine("| ${r.line} | ${r.check} | ${r.case} | ${"%.6g".format(r.app)} | ${"%.6g".format(r.theory)} | " +
                        "${"%.2e".format(r.err)} | ${"%.0e".format(r.tol)} | ${r.source} | $refs |")
                }
                appendLine()
                appendLine("## References")
                appendLine()
                REFERENCES.forEachIndexed { i, ref -> appendLine("${i + 1}. $ref") }
                appendLine()
                appendLine("Also used by the app, but not checked independently here (Wheeler's rule only on the exact coax and twin-lead cases):")
                appendLine()
                ALSO_USED.forEach { appendLine("- $it") }
            })
            println("Theory validation report: ${out.absolutePath}")
        }
    }

    private fun record(line: LineType, check: String, case: String, app: Double, theory: Double, tol: Double, source: String) {
        val row = Row(line.name, check, case, app, theory, tol, source)
        synchronized(rows) { rows += row }
        assertTrue("${line.name} $check [$case]: app $app vs theory $theory (rel. error ${row.err}, tol $tol)", row.pass)
    }

    private fun analyze(line: LineType, v: Map<FieldId, Double>) = line.engine().analyze(v).also {
        assertTrue("${line.name} analysis failed for $v: ${it.errors}", it.errors.isEmpty())
    }.values

    private val mm = 1e-3
    private val lowF = 1e3 // Hz; low enough that microstrip dispersion vanishes

    // --- coax -------------------------------------------------------------------------------------

    @Test
    fun coax() {
        for ((a, b, c) in listOf(Triple(0.5, 1.75, 0.0), Triple(0.3, 2.0, 0.0), Triple(0.5, 1.75, 0.6), Triple(0.2, 3.0, 2.0))) {
            for (er in listOf(1.0, 2.1)) {
                val z = analyze(LineType.COAX, mapOf(FieldId.A to a * mm, FieldId.B to b * mm, FieldId.C to c * mm, FieldId.ER to er, FieldId.FREQ to 1e9))
                    .getValue(FieldId.Z0)
                record(LineType.COAX, "Z0", "a=$a b=$b c=$c mm εr=$er", z, Ref.coax(a, b, c, er), 1e-9, "Exact (eccentric coax)")
            }
        }
        // Air-filled 50 Ω coax has b/a = exp(2π·50/η0) ≈ 2.302.
        val syn = LineType.COAX.engine().synthesize(
            mapOf(FieldId.A to 1 * mm, FieldId.C to 0.0, FieldId.Z0 to 50.0, FieldId.ER to 1.0, FieldId.FREQ to 1e9),
            FieldId.B, false,
        ).values.getValue(FieldId.B) / mm
        record(LineType.COAX, "b/a for 50 Ω", "air", syn, 2.302, 1e-3, "Textbook: b/a = 2.302")
        val phs = analyze(LineType.COAX, mapOf(FieldId.A to 0.5 * mm, FieldId.B to 1.75 * mm, FieldId.C to 0.0, FieldId.ER to 2.1, FieldId.FREQ to 1e9, FieldId.L to 0.1))
            .getValue(FieldId.PHS)
        record(LineType.COAX, "Phase", "L=100 mm, 1 GHz, εr=2.1", phs, Ref.phase(0.1, 2.1, 1e9), 1e-9, "TEM: 360·L·√εr·f/c")
    }

    // --- stripline --------------------------------------------------------------------------------

    @Test
    fun stripline() {
        val b = 1.6 * mm
        for (wb in listOf(0.1, 0.3, 0.6, 1.0, 2.0, 4.0)) {
            for (er in listOf(2.2, 4.6)) {
                val z = analyze(LineType.SLIN, mapOf(FieldId.W to wb * b, FieldId.H to b, FieldId.T to 0.0, FieldId.ER to er, FieldId.FREQ to 1e9))
                    .getValue(FieldId.Z0)
                record(LineType.SLIN, "Z0, T=0", "W/b=$wb εr=$er", z, Ref.stripline(wb * b, b, er), 1e-6, "Cohn exact (conformal map)")
            }
        }
        // The thick-strip formula (Wheeler) must agree with the exact thin result where the app switches.
        for (wb in listOf(0.3, 1.0, 3.0)) {
            val v = mapOf(FieldId.W to wb * b, FieldId.H to b, FieldId.ER to 2.2, FieldId.FREQ to 1e9)
            val thick = analyze(LineType.SLIN, v + (FieldId.T to b / 999)).getValue(FieldId.Z0)
            record(LineType.SLIN, "Z0 branch continuity", "W/b=$wb, T=b/999 vs exact T=0", thick, Ref.stripline(wb * b, b, 2.2), 1e-2,
                "Wheeler thick-strip formula vs Cohn exact")
        }
        // Pozar, Microwave Engineering, Example 3.5: b = 0.32 cm, εr = 2.20, 50 Ω → W = 0.266 cm.
        val w = LineType.SLIN.engine().synthesize(
            mapOf(FieldId.H to 3.2 * mm, FieldId.T to 0.0, FieldId.Z0 to 50.0, FieldId.ER to 2.2, FieldId.FREQ to 1e9), FieldId.W, false,
        ).values.getValue(FieldId.W) / mm
        record(LineType.SLIN, "W for 50 Ω", "b=3.2 mm εr=2.2", w, 2.66, 2e-2, "Pozar Ex. 3.5 (approx. formula, ±1%)")
        val phs = analyze(LineType.SLIN, mapOf(FieldId.W to 1 * mm, FieldId.H to b, FieldId.T to 35e-6, FieldId.ER to 4.6, FieldId.FREQ to 2e9, FieldId.L to 0.05))
            .getValue(FieldId.PHS)
        record(LineType.SLIN, "Phase", "L=50 mm, 2 GHz, εr=4.6", phs, Ref.phase(0.05, 4.6, 2e9), 1e-9, "TEM: 360·L·√εr·f/c")
    }

    @Test
    fun coupledStripline() {
        val b = 1.6 * mm
        for ((w, s) in listOf(0.3 to 0.1, 0.6 to 0.36, 1.0 to 0.5, 0.5 to 2.0)) {
            val out = analyze(LineType.CSLIN, mapOf(FieldId.W to w * mm, FieldId.S to s * mm, FieldId.H to b, FieldId.T to 0.0, FieldId.ER to 4.6, FieldId.FREQ to 1e9))
            val (ze, zo) = Ref.coupledStripline(w * mm, s * mm, b, 4.6)
            record(LineType.CSLIN, "Z0e, T=0", "W=$w S=$s mm", out.getValue(FieldId.Z0E), ze, 1e-6, "Cohn exact (coupled)")
            record(LineType.CSLIN, "Z0o, T=0", "W=$w S=$s mm", out.getValue(FieldId.Z0O), zo, 1e-6, "Cohn exact (coupled)")
        }
        // Far apart, both modes tend to the single stripline.
        val far = analyze(LineType.CSLIN, mapOf(FieldId.W to 0.6 * mm, FieldId.S to 8 * mm, FieldId.H to b, FieldId.T to 0.0, FieldId.ER to 4.6, FieldId.FREQ to 1e9))
        val single = Ref.stripline(0.6 * mm, b, 4.6)
        record(LineType.CSLIN, "Z0e → single", "S/b=5", far.getValue(FieldId.Z0E), single, 1e-3, "Limit: uncoupled strips")
        record(LineType.CSLIN, "Z0o → single", "S/b=5", far.getValue(FieldId.Z0O), single, 1e-3, "Limit: uncoupled strips")
    }

    // --- microstrip -------------------------------------------------------------------------------

    @Test
    fun microstripStatic() {
        val h = 1 * mm
        for (u in listOf(0.1, 0.3, 1.0, 3.0, 10.0)) {
            for (er in listOf(2.2, 4.6, 9.8)) {
                for (t in listOf(0.0, 35e-6)) {
                    val v = mapOf(FieldId.W to u * h, FieldId.H to h, FieldId.T to t, FieldId.ER to er, FieldId.FREQ to lowF, FieldId.L to 1.0)
                    val z = analyze(LineType.MLIN, v).getValue(FieldId.Z0)
                    val eeff = lossOutputs(LineType.MLIN, v).getValue(Output.EEFF)
                    val (zRef, eRef) = Ref.microstrip(u * h, h, t, er)
                    val case = "W/h=$u εr=$er T=${t * 1e6} µm"
                    record(LineType.MLIN, "Z0 static", case, z, zRef, 1e-6, "Hammerstad–Jensen 1980")
                    record(LineType.MLIN, "εeff static", case, eeff, eRef, 1e-6, "Hammerstad–Jensen 1980")
                }
            }
        }
        // Pozar, Example 3.7: d = 0.158 cm, εr = 2.20, 50 Ω → W/d = 3.081, εeff = 1.87.
        val w = LineType.MLIN.engine().synthesize(
            mapOf(FieldId.H to 1.58 * mm, FieldId.T to 0.0, FieldId.Z0 to 50.0, FieldId.ER to 2.2, FieldId.FREQ to lowF), FieldId.W, false,
        ).values.getValue(FieldId.W)
        record(LineType.MLIN, "W/d for 50 Ω", "d=1.58 mm εr=2.2", w / (1.58 * mm), 3.081, 2e-2, "Pozar Ex. 3.7 (Wheeler-type formula)")
        val e = lossOutputs(LineType.MLIN, mapOf(FieldId.W to w, FieldId.H to 1.58 * mm, FieldId.T to 0.0, FieldId.ER to 2.2, FieldId.FREQ to lowF))
            .getValue(Output.EEFF)
        record(LineType.MLIN, "εeff for 50 Ω", "d=1.58 mm εr=2.2", e, 1.87, 1e-2, "Pozar Ex. 3.7")
    }

    @Test
    fun microstripDispersion() {
        // Alumina and FR-4 up to well past where dispersion matters.
        for ((er, hMm) in listOf(9.8 to 0.635, 4.6 to 1.6, 2.2 to 0.508)) {
            for (u in listOf(0.5, 1.0, 2.0)) {
                for (fGHz in listOf(1.0, 10.0, 30.0)) {
                    val v = mapOf(FieldId.W to u * hMm * mm, FieldId.H to hMm * mm, FieldId.T to 0.0, FieldId.ER to er, FieldId.FREQ to fGHz * 1e9)
                    val eeff = lossOutputs(LineType.MLIN, v).getValue(Output.EEFF)
                    val e0 = Ref.microstrip(u * hMm * mm, hMm * mm, 0.0, er).second
                    record(LineType.MLIN, "εeff(f)", "εr=$er h=$hMm mm W/h=$u $fGHz GHz", eeff, Ref.kjEeff(u, er, e0, fGHz, hMm), 1e-6,
                        "Kirschning–Jansen 1982")
                }
            }
        }
    }

    @Test
    fun coupledMicrostripLimits() {
        val h = 1.6 * mm
        val w = 2.9 * mm
        val far = analyze(LineType.CMLIN, mapOf(FieldId.W to w, FieldId.S to 10 * h, FieldId.H to h, FieldId.ER to 4.6, FieldId.FREQ to lowF))
        val single = Ref.microstrip(w, h, 0.0, 4.6).first
        record(LineType.CMLIN, "Z0e → single", "S/h=10", far.getValue(FieldId.Z0E), single, 2e-2, "Limit: uncoupled (K&J vs H&J)")
        record(LineType.CMLIN, "Z0o → single", "S/h=10", far.getValue(FieldId.Z0O), single, 2e-2, "Limit: uncoupled (K&J vs H&J)")
        val near = analyze(LineType.CMLIN, mapOf(FieldId.W to w, FieldId.S to 0.2 * h, FieldId.H to h, FieldId.ER to 4.6, FieldId.FREQ to lowF))
        val out = lossOutputs(LineType.CMLIN, mapOf(FieldId.W to w, FieldId.S to 0.2 * h, FieldId.H to h, FieldId.ER to 4.6, FieldId.FREQ to lowF))
        assertTrue("Z0e > Z0o", near.getValue(FieldId.Z0E) > near.getValue(FieldId.Z0O))
        assertTrue("εeff,e > εeff,o", out.getValue(Output.EEFF_E) > out.getValue(Output.EEFF_O))
    }

    // --- coplanar ---------------------------------------------------------------------------------

    @Test
    fun coplanar() {
        for ((w, s) in listOf(0.2 to 0.1, 1.0 to 0.244, 2.0 to 0.5, 0.5 to 1.0)) {
            for (hMm in listOf(0.254, 1.6)) {
                val v = mapOf(FieldId.W to w * mm, FieldId.S to s * mm, FieldId.H to hMm * mm, FieldId.T to 0.0, FieldId.ER to 4.6, FieldId.FREQ to 1e9)
                val case = "W=$w S=$s h=$hMm mm"
                val (z, e) = Ref.cpw(w * mm, s * mm, hMm * mm, 4.6)
                record(LineType.CPW, "Z0, T=0", case, analyze(LineType.CPW, v).getValue(FieldId.Z0), z, 1e-6, "Conformal map (Wadell §3.4.1)")
                record(LineType.CPW, "εeff, T=0", case, lossOutputs(LineType.CPW, v).getValue(Output.EEFF), e, 1e-6, "Conformal map (Wadell §3.4.1)")
                val (zg, eg) = Ref.gcpw(w * mm, s * mm, hMm * mm, 4.6)
                record(LineType.GCPW, "Z0", case, analyze(LineType.GCPW, v - FieldId.T).getValue(FieldId.Z0), zg, 1e-6, "Conformal map (Wadell §3.4.3)")
                record(LineType.GCPW, "εeff", case, lossOutputs(LineType.GCPW, v - FieldId.T).getValue(Output.EEFF), eg, 1e-6, "Conformal map (Wadell §3.4.3)")
            }
        }
        // Thick substrate: εeff → (εr + 1)/2.
        val thick = lossOutputs(LineType.CPW, mapOf(FieldId.W to 0.5 * mm, FieldId.S to 0.2 * mm, FieldId.H to 1.0, FieldId.T to 0.0, FieldId.ER to 4.6, FieldId.FREQ to 1e9))
        record(LineType.CPW, "εeff, h→∞", "W=0.5 S=0.2 mm", thick.getValue(Output.EEFF), 2.8, 1e-6, "Limit: (εr+1)/2")
    }

    // --- newer lines (independent re-checks for the report) ---------------------------------

    @Test
    fun newerLines() {
        // Asymmetric stripline with the strip centered is the symmetric stripline.
        val t = 0.0
        val z = analyze(LineType.ASLIN, mapOf(FieldId.W to 0.6 * mm, FieldId.T to t, FieldId.H to 0.8 * mm, FieldId.H2 to 0.8 * mm, FieldId.ER to 4.6, FieldId.FREQ to 1e9))
            .getValue(FieldId.Z0)
        record(LineType.ASLIN, "Z0, centered", "H1=H2=0.8 mm, T=0", z, Ref.stripline(0.6 * mm, 1.6 * mm, 4.6), 1e-6, "Limit: Cohn exact stripline")

        // Waveguide WR-90: fc = c/2a, Z = η0/√(1 − (fc/f)²).
        val a = 22.86 * mm
        val wg = analyze(LineType.WG, mapOf(FieldId.A to a, FieldId.B to 10.16 * mm, FieldId.ER to 1.0, FieldId.FREQ to 10e9, FieldId.L to 0.1))
        val fc = Ref.C / (2 * a)
        val root = sqrt(1 - (fc / 10e9).pow(2))
        record(LineType.WG, "Z_TE10", "WR-90 10 GHz", wg.getValue(FieldId.Z0), Ref.ETA0 / root, 1e-9, "Exact TE10")
        record(LineType.WG, "Phase", "WR-90 10 GHz L=100 mm", wg.getValue(FieldId.PHS), 360 * 0.1 * 10e9 * root / Ref.C, 1e-9, "Exact TE10: β = k·√(1−(fc/f)²)")
        record(LineType.WG, "fc", "WR-90", lossOutputs(LineType.WG, mapOf(FieldId.A to a, FieldId.B to 10.16 * mm, FieldId.ER to 1.0, FieldId.FREQ to 10e9))
            .getValue(Output.FC), 6.557e9, 1e-3, "Published WR-90 cutoff 6.557 GHz")

        // Edge-coupled CPW: merged strips form one CPW of width 2W, Z0e → 2·Z_CPW.
        val ecpw = analyze(LineType.ECPW, mapOf(FieldId.W to 0.5 * mm, FieldId.S to 1e-9, FieldId.G2 to 0.2 * mm, FieldId.H to 1.6 * mm, FieldId.ER to 4.6, FieldId.FREQ to 1e9))
        record(LineType.ECPW, "Z0e, S→0", "W=0.5 G=0.2 h=1.6 mm", ecpw.getValue(FieldId.Z0E), 2 * Ref.cpw(1.0 * mm, 0.2 * mm, 1.6 * mm, 4.6).first, 1e-3,
            "Limit: one CPW of width 2W")

        // Bare parallel wires in air: Z = (η0/π)·acosh(D/d).
        val twp = analyze(LineType.TWP, mapOf(FieldId.A to 0.5 * mm, FieldId.B to 1.5 * mm, FieldId.ER to 1.0, FieldId.TWIST to 0.0, FieldId.FREQ to 1e8))
        record(LineType.TWP, "Z0", "d=0.5 D=1.5 mm air", twp.getValue(FieldId.Z0), Ref.ETA0 / PI * acosh(3.0), 1e-9, "Exact twin lead")
    }

    // --- losses -----------------------------------------------------------------------------------

    @Test
    fun losses() {
        val f = 1e9
        val sigma = 5.8e7
        val rs = sqrt(PI * f * 4e-7 * PI / sigma)
        val np = 20 / ln(10.0)
        // Coax: αc = Rs(1/a + 1/b)/(2η·ln(b/a)), αd = (πf√εr/c)·tanδ.
        val (a, b, er, tand) = listOf(0.5 * mm, 1.75 * mm, 2.1, 2e-4)
        val coax = lossOutputs(LineType.COAX, mapOf(FieldId.A to a, FieldId.B to b, FieldId.C to 0.0, FieldId.ER to er, FieldId.FREQ to f, FieldId.TAND to tand, FieldId.SIGMA to sigma))
        val eta = Ref.ETA0 / sqrt(er)
        record(LineType.COAX, "αc", "a=0.5 b=1.75 mm Cu 1 GHz", coax.getValue(Output.ALPHA_C), rs * (1 / a + 1 / b) / (2 * eta * ln(b / a)) * np, 1e-4, "Exact coax")
        record(LineType.COAX, "αd", "tanδ=2e-4", coax.getValue(Output.ALPHA_D), PI * f * sqrt(er) / Ref.C * tand * np, 1e-9, "Exact TEM: k·tanδ/2")
        record(LineType.COAX, "Skin depth", "Cu 1 GHz", coax.getValue(Output.SKIN_DEPTH), 1 / sqrt(PI * f * 4e-7 * PI * sigma), 1e-12, "δ = 1/√(πfμσ)")

        // Twin lead: R = (2Rs/πd)·(D/d)/√((D/d)² − 1), α = R/2Z0 (proximity effect included).
        val (d, s) = 0.5 * mm to 1.5 * mm
        val twp = lossOutputs(LineType.TWP, mapOf(FieldId.A to d, FieldId.B to s, FieldId.ER to 1.0, FieldId.TWIST to 0.0, FieldId.FREQ to f, FieldId.SIGMA to sigma))
        val r = 2 * rs / (PI * d) * (s / d) / sqrt((s / d).pow(2) - 1)
        record(LineType.TWP, "αc", "d=0.5 D=1.5 mm Cu 1 GHz", twp.getValue(Output.ALPHA_C), r / (2 * Ref.ETA0 / PI * acosh(s / d)) * np, 1e-4, "Exact twin lead")

        // WR-90 copper at 10 GHz, Pozar (3.96).
        val aw = 22.86 * mm; val bw = 10.16 * mm; val fw = 10e9
        val k = 2 * PI * fw / Ref.C
        val beta = sqrt(k * k - (PI / aw).pow(2))
        val rsw = sqrt(PI * fw * 4e-7 * PI / sigma)
        val wg = lossOutputs(LineType.WG, mapOf(FieldId.A to aw, FieldId.B to bw, FieldId.ER to 1.0, FieldId.FREQ to fw, FieldId.SIGMA to sigma))
        record(LineType.WG, "αc", "WR-90 Cu 10 GHz", wg.getValue(Output.ALPHA_C),
            rsw / (aw.pow(3) * bw * beta * k * Ref.ETA0) * (2 * bw * PI * PI + aw.pow(3) * k * k) * np, 1e-9, "Pozar (3.96)")

        // Stripline dielectric loss: homogeneous, so k·√εr·tanδ/2 exactly.
        val sl = lossOutputs(LineType.SLIN, mapOf(FieldId.W to 1 * mm, FieldId.H to 1.6 * mm, FieldId.T to 35e-6, FieldId.ER to 4.6, FieldId.FREQ to f, FieldId.TAND to 0.02))
        record(LineType.SLIN, "αd", "εr=4.6 tanδ=0.02 1 GHz", sl.getValue(Output.ALPHA_D), PI * f * sqrt(4.6) / Ref.C * 0.02 * np, 1e-9, "Exact TEM: k·tanδ/2")
    }

    // --- synthesis inverts analysis -----------------------------------------------------------

    @Test
    fun synthesisInvertsAnalysis() {
        for (line in LineType.entries) {
            val base = line.fields.filter { it !in MATERIAL_FIELDS }.associateWith { id -> toSi(line, id) }.toMutableMap()
            if (line == LineType.COAX) base[FieldId.C] = 0.1 * mm // exercise the offset target too
            val ana = analyze(line, base)
            if (line.coupled) {
                for (useZ0k in listOf(true, false)) {
                    val given = if (useZ0k) listOf(FieldId.Z0, FieldId.K) else listOf(FieldId.Z0E, FieldId.Z0O)
                    val v = base - FieldId.W - FieldId.S - FieldId.L + given.associateWith { ana.getValue(it) } + (FieldId.PHS to ana.getValue(FieldId.PHS))
                    val syn = line.engine().synthesize(v, null, useZ0k)
                    assertTrue("${line.name} synthesis failed: ${syn.errors}", syn.errors.isEmpty())
                    for (id in listOf(FieldId.W, FieldId.S, FieldId.L)) {
                        record(line, "Synthesize ${id.prefName}", if (useZ0k) "from Z0, k" else "from Z0e, Z0o",
                            syn.values.getValue(id), base.getValue(id), 1e-4, "Round trip: analysis → synthesis")
                    }
                }
            } else {
                for (target in line.targets) {
                    val v = base - target - FieldId.L + (FieldId.Z0 to ana.getValue(FieldId.Z0)) + (FieldId.PHS to ana.getValue(FieldId.PHS))
                    val syn = line.engine().synthesize(v, target, false)
                    assertTrue("${line.name} synthesis of $target failed: ${syn.errors}", syn.errors.isEmpty())
                    record(line, "Synthesize ${target.prefName}", "defaults", syn.values.getValue(target), base.getValue(target), 1e-4,
                        "Round trip: analysis → synthesis")
                    record(line, "Synthesize L", "for ${target.prefName}", syn.values.getValue(FieldId.L), base.getValue(FieldId.L), 1e-4,
                        "Round trip: analysis → synthesis")
                }
            }
        }
    }

    private fun toSi(line: LineType, id: FieldId): Double {
        val d = line.defaultOf(id)
        val x = d.text.toDouble()
        return when (id.unitKind) {
            UnitKind.LENGTH, UnitKind.ROUGHNESS -> Constants.value2meter(x, d.unit)
            UnitKind.FREQUENCY -> Constants.value2Hz(x, d.unit)
            UnitKind.CONDUCTIVITY -> x * 1e6
            UnitKind.TWIST -> if (d.unit == "/in") x / 0.0254 else x * 100
            else -> x
        }
    }
}
