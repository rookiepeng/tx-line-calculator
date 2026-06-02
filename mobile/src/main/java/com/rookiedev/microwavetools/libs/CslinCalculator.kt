package com.rookiedev.microwavetools.libs

import android.util.Log
import com.rookiedev.microwavetools.libs.Constants.ERROR
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tanh

class CslinCalculator {
    /**
     * Calculates the ratio of k over kp.
     * 
     * @param k the input value for k
     * @return the calculated ratio
     */
    private fun k_over_kp(k: Double): Double {
        var k = k
        var kp: Double
        var r: Double
        var kf: Double
        var i = 0

        kp = sqrt(1.0 - k.pow(2.0))
        r = 1.0
        do {
            kf = (1.0 + k) / (1.0 + kp)
            r = r * kf
            k = 2.0 * sqrt(k) / (1.0 + k)
            kp = 2.0 * sqrt(kp) / (1.0 + kp)
            i++
        } while ((abs(kf - 1.0) > 1e-15) && (i < 20))

        /*
         * alternate approach if( k < sqrt(0.5) ) { kp = sqrt(1.0 - k*k); r = M_PI /
         * log(2.0 * (1.0 + sqrt(kp)) / (1.0 - sqrt(kp)) ); } else { r = log(2.0 * (1.0
         * + sqrt(k)) / (1.0 - sqrt(k)) ) / M_PI; }
         */
        return r
    }

    /**
     * Calculates the zero thickness characteristic impedance.
     * 
     * @param line the CslinModel object containing line parameters
     * @return the updated CslinModel object with calculated impedances
     */
    private fun impedance_zeroThickness(line: CslinModel): CslinModel {
        val ke: Double
        val ko: Double

        // (3) from Cohn
        ke =
            tanh(Math.PI * line.metalWidth / (2.0 * line.subHeight)) * tanh(Math.PI * (line.metalWidth + line.metalSpace) / (2.0 * line.subHeight))
        // (6) from Cohn
        ko =
            tanh(Math.PI * line.metalWidth / (2.0 * line.subHeight)) / tanh(Math.PI * (line.metalWidth + line.metalSpace) / (2.0 * line.subHeight))
        // (2) from Cohn
        line.impedanceEven =
            (Constants.FREESPACEZ0 / 4.0) * sqrt(1.0 / line.subEpsilon) / k_over_kp(
                ke
            )
        // (5) from Cohn
        line.impedanceOdd =
            (Constants.FREESPACEZ0 / 4.0) * sqrt(1.0 / line.subEpsilon) / k_over_kp(
                ko
            )
        return line
    }

    /**
     * Analyzes the given CslinModel line and calculates its properties.
     * 
     * @param line the CslinModel object containing line parameters
     * @return the updated CslinModel object with calculated properties
     */
    private fun Analysis(line: CslinModel): CslinModel {
        // zero thickness even and odd impedances
        var line = line
        val z0e_0t: Double
        val z0o_0t: Double

        // single stripline
        var lineSlin = SlinModel()

        val z0s: Double
        val z0s_0t: Double
        val cf_t: Double
        val cf_0: Double

        if (line.metalWidth < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.WIDTH_MINIMAL_LIMIT
            return line
        }
        if (line.metalSpace < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.SPACE_MINIMAL_LIMIT
            return line
        }
        if (line.subHeight < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.HEIGHT_MINIMAL_LIMIT
            return line
        }
        if (line.subEpsilon < 1) {
            line.errorCode = ERROR.ER_MINIMAL_LIMIT
            return line
        }
        if (line.metalThick >= line.subHeight) {
            line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
            return line
        }

        // Start of coupled stripline calculations
        // zero thickness coupled line
        line = impedance_zeroThickness(line)
        z0e_0t = line.impedanceEven
        z0o_0t = line.impedanceOdd
        if (line.metalThick != 0.0) {
            lineSlin.substrate = line.substrate
            lineSlin.setMetalThick(line.metalThick, "m")
            lineSlin.setMetalWidth(line.metalWidth, "m")
            lineSlin.setMetalLength(line.metalLength, "m")
            lineSlin.setFrequency(line.frequency, "Hz")

            Log.v("TAG", "1")
            val slin = SlinCalculator()
            lineSlin = slin.getAnaResult(lineSlin)
            if (lineSlin.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            z0s = lineSlin.impedance
            Log.v("TAG", "2")
            lineSlin.setMetalThick(0.0, "m")
            lineSlin = slin.getAnaResult(lineSlin)
            if (lineSlin.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            z0s_0t = lineSlin.impedance

            if ((1.0 / (1.0 - line.metalThick / line.subHeight).pow(2.0)) - 1.0 < 0) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            Log.v("TAG", "3")
            // fringing capacitance
            cf_t = ((Constants.FREESPACE_E0 * line.subEpsilon / Math.PI)
                    * ((2.0 / (1.0 - line.metalThick / line.subHeight))
                    * ln((1.0 / (1.0 - line.metalThick / line.subHeight)) + 1.0)
                    - (1.0 / (1.0 - line.metalThick / line.subHeight) - 1.0) * ln(
                (1.0 / (1.0 - line.metalThick / line.subHeight).pow(2.0)) - 1.0
            )))

            Log.v("TAG", "4")
            // zero thickness fringing capacitance
            cf_0 = (Constants.FREESPACE_E0 * line.subEpsilon / Math.PI) * 2.0 * ln(2.0)

            Log.v("TAG", "5")
            // (18) from Cohn, (4.6.5.1) in Wadell
            line.impedanceEven = 1.0 / ((1.0 / z0s) - (cf_t / cf_0) * ((1.0 / z0s_0t) - (1.0 / z0e_0t)))

            if (line.metalSpace >= 5.0 * line.metalThick) {
                // (20) from Cohn, (4.6.5.2) in Wadell -- note, Wadell has a sign error in the
                // equation
                line.impedanceOdd = 1.0 / ((1.0 / z0s) + (cf_t / cf_0) * ((1.0 / z0o_0t) - (1.0 / z0s_0t)))
            } else {
                // (22) from Cohn, (4.6.5.3) in Wadell -- note, Wadell has a couple of errors in
                // the transcription from the original (Cohn)
                line.impedanceOdd =
                    1.0 / ((1.0 / z0o_0t) + ((1.0 / z0s) - (1.0 / z0s_0t))
                            - (2.0 / Constants.FREESPACEZ0)
                            * (cf_t / Constants.FREESPACE_E0 - cf_0 / Constants.FREESPACE_E0)
                            + (2.0 * line.metalThick) / (Constants.FREESPACEZ0 * line.metalSpace))
            }
            Log.v("TAG", "6")
        }

        // find impedance and coupling coefficient
        line.impedance = sqrt(line.impedanceEven * line.impedanceOdd)

        // coupling coefficient
        line.couplingFactor =
            (line.impedanceEven - line.impedanceOdd) / (line.impedanceEven + line.impedanceOdd)

        /*
         * electrical length = 360 degrees * physical length / wavelength
         *
         * freq * wavelength = velocity => 1/wavelength = freq / velocity
         *
         * 1/wavelength = freq * LIGHTSPEED/sqrt(keff)
         */
        line.phase =
            360.0 * line.metalLength * line.frequency / (Constants.LIGHTSPEED / sqrt(line.subEpsilon))
        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Synthesizes the given CslinModel line based on the provided parameters.
     * 
     * @param line the CslinModel object containing line parameters
     * @param use_z0k flag indicating whether to use z0 and k for calculations
     * @return the updated CslinModel object with synthesized properties
     */
    private fun Synthesize(line: CslinModel, use_z0k: Boolean): CslinModel {
        var line = line
        val h: Double
        val er: Double
        var l: Double
        var wmin: Double
        var wmax: Double
        var abstol: Double
        var reltol: Double
        val maxiters: Int
        var z0: Double
        var w: Double
        var iters: Int
        var done: Boolean
        val len: Double

        var s: Double
        var smin: Double
        var smax: Double
        var z0e: Double
        var z0o: Double
        var k: Double
        var loss: Double
        var delta: Double
        val cval: Double
        var err: Double
        var d: Double

        val AW: Double
        val F1: Double
        var F2: Double
        var F3: Double

        val ai: DoubleArray? = doubleArrayOf(1.0, -0.301, 3.209, -27.282, 56.609, -37.746)
        val bi: DoubleArray? = doubleArrayOf(0.020, -0.623, 17.192, -68.946, 104.740, -16.148)
        val ci: DoubleArray? = doubleArrayOf(0.002, -0.347, 7.171, -36.910, 76.132, -51.616)

        var i: Int
        var dw: Double
        var ds: Double
        var ze0 = 0.0
        var ze1: Double
        var ze2: Double
        var dedw: Double
        var deds: Double
        var zo0 = 0.0
        var zo1: Double
        var zo2: Double
        var dodw: Double
        var dods: Double

        len = line.phase

        // SubstrateModel dielectric thickness (m)
        h = line.subHeight
        // SubstrateModel relative permittivity
        er = line.subEpsilon
        // impedance and coupling
        z0 = line.impedance
        k = line.couplingFactor
        // even/odd mode impedances
        z0e = line.impedanceEven
        z0o = line.impedanceOdd

        if (use_z0k) {
            // use z0 and k to calculate z0e and z0o
            z0o = z0 * sqrt((1.0 - k) / (1.0 + k))
            z0e = z0 * sqrt((1.0 + k) / (1.0 - k))
            line.impedanceEven = z0e
            line.impedanceOdd = z0e
            if (k >= 1) {
                line.errorCode = ERROR.K_OUT_OF_RANGE
                return line
            }
        } else {
            // use z0e and z0o to calculate z0 and k
            z0 = sqrt(z0e * z0o)
            k = (z0e - z0o) / (z0e + z0o)
            line.impedance = z0
            line.couplingFactor = k
            if (k <= 0 || k >= 1) {
                line.errorCode = ERROR.Z0E_Z0O_MISTAKE
                return line
            }
        }

        // temp value for l used while finding w and s
        l = 1000.0
        line.setMetalLength(l, "m")

        // FIXME - change limits to be normalized to substrate thickness limits on the
        // allowed range for w
        // wmin = MIL2M(0.5);
        // wmax = MIL2M(1000);

        // limits on the allowed range for s
        // smin = MIL2M(0.5);
        // smax = MIL2M(1000);

        // impedance convergence tolerance (ohms)
        // abstol = 1e-6;

        // width relative convergence tolerance (mils) (set to 0.1 micron)
        // reltol = MICRON2MIL(0.1);
        maxiters = 50

        // Initial guess at a solution -- FIXME: This is an initial guess for coupled
        // microstrip _not_ coupled stripline.
        AW = exp(z0 * sqrt(er + 1.0) / 42.4) - 1.0
        F1 = 8.0 * sqrt(AW * (7.0 + 4.0 / er) / 11.0 + (1.0 + 1.0 / er) / 0.81) / AW
        F2 = 0.0
        i = 0
        while (i <= 5) {
            F2 = F2 + ai!![i] * k.pow(i.toDouble())
            i++
        }
        F3 = 0.0
        i = 0
        while (i <= 5) {
            F3 = F3 + (bi!![i] - ci!![i] * (9.6 - er)) * (0.6 - k).pow((i).toDouble())
            i++
        }
        w = h * abs(F1 * F2)
        s = h * abs(F1 * F3)
        l = 100.0

        iters = 0
        done = false
        if (w < s) delta = 1e-3 * w
        else delta = 1e-3 * s
        delta = Constants.value2meter(1e-5, "mil")
        cval = 1e-12 * z0e * z0o

        /*
         * We should never need anything anywhere near maxiters iterations. This limit
         * is just to prevent going to lala land if something breaks.
         */
        while (!done) {
            iters++
            line.setMetalWidth(w, "m")
            line.setMetalSpace(s, "m")
            line = Analysis(line)
            if (line.errorCode == ERROR.NO_ERROR) {
                ze0 = line.impedanceEven
                zo0 = line.impedanceOdd
            } else {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            // check for convergence
            err = (ze0 - z0e).pow(2.0) + (zo0 - z0o).pow(2.0)
            if (err < cval) {
                done = true
            } else {
                // approximate the first jacobian
                line.setMetalWidth(w + delta, "m")
                line.setMetalSpace(s, "m")
                line = Analysis(line)
                if (line.errorCode == ERROR.NO_ERROR) {
                    ze1 = line.impedanceEven
                    zo1 = line.impedanceOdd
                } else {
                    line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                    return line
                }

                line.setMetalWidth(w, "m")
                line.setMetalSpace(s + delta, "m")
                line = Analysis(line)
                if (line.errorCode == ERROR.NO_ERROR) {
                    ze2 = line.impedanceEven
                    zo2 = line.impedanceOdd
                } else {
                    line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                    return line
                }

                dedw = (ze1 - ze0) / delta
                dodw = (zo1 - zo0) / delta
                deds = (ze2 - ze0) / delta
                dods = (zo2 - zo0) / delta

                // find the determinate
                d = dedw * dods - deds * dodw

                /*
                 * estimate the new solution, but don't change by more than 10% at a time to
                 * avoid convergence problems
                 */
                dw = -1.0 * ((ze0 - z0e) * dods - (zo0 - z0o) * deds) / d
                if (abs(dw) > 0.1 * w) {
                    if (dw > 0.0) dw = 0.1 * w
                    else dw = -0.1 * w
                }
                w = abs(w + dw)

                ds = ((ze0 - z0e) * dodw - (zo0 - z0o) * dedw) / d
                if (abs(ds) > 0.1 * s) {
                    if (ds > 0.0) ds = 0.1 * s
                    else ds = -0.1 * s
                }
                s = abs(s + ds)
            }

            if (iters >= maxiters) {
                line.errorCode = ERROR.MAX_ITERATIONS
                return line
            }
        }

        line.setMetalWidth(w, "m")
        line.setMetalSpace(s, "m")
        line = Analysis(line)

        // scale the line length to get the desired electrical length
        line.setMetalLength(line.metalLength * len / line.phase, "m")
        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Gets the analysis result for the given CslinModel line.
     * 
     * @param line the CslinModel object containing line parameters
     * @return the updated CslinModel object with analysis results
     */
    fun getAnaResult(line: CslinModel): CslinModel {
        return Analysis(line)
    }

    /**
     * Gets the synthesis result for the given CslinModel line.
     * 
     * @param line the CslinModel object containing line parameters
     * @param use_z0k flag indicating whether to use z0 and k for calculations
     * @return the updated CslinModel object with synthesis results
     */
    fun getSynResult(line: CslinModel, use_z0k: Boolean): CslinModel {
        return Synthesize(line, use_z0k)
    }
}
