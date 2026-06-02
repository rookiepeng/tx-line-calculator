package com.rookiedev.microwavetools.libs

import android.util.Log
import com.rookiedev.microwavetools.libs.Constants.ERROR
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tanh

class CpwCalculator {
    /**
     * Calculates the complete elliptic integral of the first kind.
     * 
     * @param k The modulus of the elliptic integral.
     * @return The value of the complete elliptic integral of the first kind.
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
     * Analyzes the coplanar waveguide (CPW) model.
     * 
     * @param line The CPW model to analyze.
     * @param withGround Whether the CPW has a ground plane.
     * @return The analyzed CPW model with updated parameters.
     */
    private fun Analysis(line: CpwModel, withGround: Boolean): CpwModel {
        // calculation variables

        val k: Double
        val k1: Double
        val kt: Double
        val z0: Double
        val v: Double
        var loss: Double
        val k_kp: Double
        val k_kp1: Double
        val k_kpt: Double
        val a: Double
        var at: Double
        val b: Double
        var bt: Double
        val eeff: Double

        val keff: Double

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

        if (!withGround) {
            // These equations are _without_ the bottom side ground plane.
            // match the notation in Wadell
            a = line.metalWidth
            b = line.metalWidth + 2.0 * line.metalSpace

            // Wadell (3.4.1.8), (3.4.1.9) but avoid issues with tmet = 0
            if (line.metalThick > 0.0) {
                at = (a + (1.25 * line.metalThick / Math.PI)
                        * (1.0 + ln(4.0 * Math.PI * a / line.metalThick)))
                bt = (b - (1.25 * line.metalThick / Math.PI)
                        * (1.0 + ln(4.0 * Math.PI * a / line.metalThick)))
            } else {
                at = a
                bt = b
            }

            /*
             * we have to be a bit careful here. If we're not careful, we can end up with b
             * < a which is nonsense and it will cause the calculation of the elliptic
             * integral ratios to produce NaN
             */
            if (bt <= at) {
                at = a
                bt = b
                // alert("Warning: bt <= at so I am reverting to zero thickness equations\n");
            }

            // Wadell (3.4.1.6)
            k1 =
                sinh(Math.PI * at / (4.0 * line.subHeight)) / sinh(Math.PI * bt / (4.0 * line.subHeight))

            // Wadell (3.4.1.4), (3.4.1.5)
            k = a / b
            kt = at / bt

            k_kp = k_over_kp(k)
            k_kp1 = k_over_kp(k1)
            k_kpt = k_over_kp(kt)

            // Wadell (3.4.1.3)
            eeff = 1.0 + 0.5 * (line.subEpsilon - 1.0) * k_kp1 / k_kp

            // Wadell (3.4.1.2)
            keff =
                eeff - (eeff - 1.0) / ((0.5 * (b - a) / (0.7 * line.metalThick)) * k_kp + 1.0)

            // for coplanar waveguide (ground signal ground)
            z0 = Constants.FREESPACEZ0 / (4.0 * sqrt(keff) * k_kpt)
        } else {
            /*
             * These equations are _with_ the bottom side ground plane.
             *
             * See Wadell, eq 3.4.3.1 through 3.4.3.6 on p. 79
             */

            // FIXME -- surely these are not accurate without accounting for metal
            // thickness...

            k = line.metalWidth / (line.metalWidth + 2.0 * line.metalSpace)
            k1 =
                tanh(Math.PI * line.metalWidth / (4.0 * line.subHeight)) / tanh(Math.PI * (line.metalWidth + 2.0 * line.metalSpace) / (4.0 * line.subHeight))
            k_kp = k_over_kp(k)
            k_kp1 = k_over_kp(k1)

            keff = (1.0 + line.subEpsilon * k_kp1 / k_kp) / (1.0 + k_kp1 / k_kp)

            z0 = (Constants.FREESPACEZ0 / (2.0 * sqrt(keff))) / (k_kp + k_kp1)
        }

        // Electrical Length
        // propagation velocity (meters/sec)
        v = Constants.LIGHTSPEED / sqrt(keff)
        line.phase = (360 * line.metalLength * line.frequency / v)

        // store results
        line.impedance = (z0)
        line.errorCode = (ERROR.NO_ERROR)
        return line
    }

    /**
     * Synthesizes the coplanar waveguide (CPW) model.
     * 
     * @param line The CPW model to synthesize.
     * @param flag The parameter to synthesize (e.g., width, gap, height, or dielectric constant).
     * @param withGround Whether the CPW has a ground plane.
     * @return The synthesized CPW model with updated parameters.
     */
    private fun Synthesize(line: CpwModel, flag: Int, withGround: Boolean): CpwModel {
        var line = line
        val Ro: Double
        var Xo: Double
        var v: Double
        val len: Double

        // the optimization variables, current, min/max, and previous values
        var `var` = 0.0
        var varmax = 0.0
        var varmin = 0.0
        var varold = 0.0

        // errors due to the above values for the optimization variable
        var err = 0.0
        var errmax = 0.0
        var errmin = 0.0
        var errold = 0.0

        // derivative
        var deriv: Double

        // the sign of the slope of the function being optimized
        var sign = 0.0

        // number of iterations so far, and max number allowed
        var iters = 0
        val maxiters = 100

        // convergence header_parameters
        val abstol = 0.1e-6
        val reltol = 0.01e-6

        // flag to end optimization
        var done = false

        /*
         * figure out what parameter we're synthesizing and set up the various
         * optimization header_parameters.
         *
         * Basically what we need to know are 1) min/max values for the parameter 2) how
         * to access the parameter 3) an initial guess for the parameter
         */
        when (flag) {
            Constants.Synthesize_Width -> {
                varmax = 100.0 * line.subHeight
                varmin = 0.01 * line.subHeight
                `var` = line.subHeight
            }

            Constants.Synthesize_Gap -> {
                varmax = 100.0 * line.subHeight
                varmin = 0.001 * line.subHeight
                `var` = line.subHeight
                Log.v("TAG", "Synthesize_Gap")
            }

            Constants.Synthesize_Height -> {
                varmax = 100.0 * line.metalWidth
                varmin = 0.01 * line.metalWidth
                `var` = line.metalWidth
            }

            Constants.Synthesize_Er -> {
                varmax = 100.0
                varmin = 1.0
                `var` = 5.0
            }

            else -> {}
        }

        // read values from the input line structure
        Ro = line.impedance

        /*
         * temp value for l used while synthesizing the other header_parameters. We'll
         * correct l later.
         */
        len = line.phase /* remember what electrical length we want */
        line.setMetalLength(1.0, "m")

        Log.v("TAG", "1")
        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varmin, flag)
            line = Analysis(line, withGround)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            errmin = line.impedance - Ro
            Log.v("MIN Imp", line.impedance.toString())
            Log.v("TAG", "2")
            line.setSynthesizeParameter(varmax, flag)
            line = Analysis(line, withGround)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            errmax = line.impedance - Ro
            Log.v("MAX Imp", line.impedance.toString())
            Log.v("TAG", "3")
            line.setSynthesizeParameter(`var`, flag)
            line = Analysis(line, withGround)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            err = line.impedance - Ro
            Log.v("TAG", "4")
            varold = 0.99 * `var`
            line.setSynthesizeParameter(varold, flag)
            line = Analysis(line, withGround)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            errold = line.impedance - Ro
            Log.v("TAG", "5")

            Log.v("MAX", errmax.toString())
            Log.v("MIN", errmin.toString())
            // see if we've actually been able to bracket the solution
            if (errmax * errmin > 0) {
                /*
                 * alert("Could not bracket the solution.\n" "Synthesis failed.\n"); return -1;
                 */
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            Log.v("TAG", "6")
            // figure out the slope of the error vs variable
            if (errmax > 0) sign = 1.0
            else sign = -1.0

            iters = 0
        }

        // the actual iterations
        while (!done) {
            // update the interation count
            iters = iters + 1

            Log.v("TAG", iters.toString())
            // calculate an estimate of the derivative
            deriv = (err - errold) / (`var` - varold)

            // copy over the current estimate to the previous one
            varold = `var`
            errold = err

            // try a quasi-newton iteration
            `var` = `var` - err / deriv

            /*
             * see if the new guess is within our bracketed range. If so, accept the new
             * estimate. If not, toss it out and do a bisection step to reduce the bracket.
             */
            if ((`var` > varmax) || (`var` < varmin)) {
                `var` = (varmin + varmax) / 2.0
            }

            // update the error value
            line.setSynthesizeParameter(`var`, flag)
            line = Analysis(line, withGround)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            err = line.impedance - Ro

            // update our bracket of the solution.
            if (sign * err > 0) varmax = `var`
            else varmin = `var`

            // check to see if we've converged
            if (abs(err) < abstol) {
                done = true
            } else if (abs((`var` - varold) / `var`) < reltol) {
                done = true
            } else if (iters >= maxiters) {
                /*
                 * alert("Synthesis failed to converge in\n"
                 * "%d iterations.  Final optimization header_parameters:\n" "  min = %g\n"
                 * "  val = %g\n" "  max = %g\n", maxiters, varmin, var, varmax); return -1;
                 */
                line.errorCode = ERROR.MAX_ITERATIONS
                return line
            }
            // done with iteration
        }

        // velocity on line
        line = Analysis(line, withGround)

        // v = Constants.LIGHTSPEED / Math.sqrt(line.getSubEpsilon());
        line.setMetalLength(line.metalLength * len / line.phase, "m")

        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Gets the analysis result of the CPW model.
     * 
     * @param line The CPW model to analyze.
     * @param withGround Whether the CPW has a ground plane.
     * @return The analyzed CPW model with updated parameters.
     */
    fun getAnaResult(line: CpwModel, withGround: Boolean): CpwModel {
        return Analysis(line, withGround)
    }

    /**
     * Gets the synthesis result of the CPW model.
     * 
     * @param line The CPW model to synthesize.
     * @param flag The parameter to synthesize (e.g., width, gap, height, or dielectric constant).
     * @param withGround Whether the CPW has a ground plane.
     * @return The synthesized CPW model with updated parameters.
     */
    fun getSynResult(line: CpwModel, flag: Int, withGround: Boolean): CpwModel {
        return Synthesize(line, flag, withGround)
    }
}
