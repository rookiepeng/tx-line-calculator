package com.rookiedev.microwavetools.libs

import com.rookiedev.microwavetools.libs.Constants.ERROR
import kotlin.math.abs
import kotlin.math.cosh
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class SlinCalculator {
    /**
     * Analyzes the given SlinModel to calculate characteristic impedance and phase.
     * 
     * @param line the SlinModel to analyze
     * @return the analyzed SlinModel with calculated impedance and phase
     */
    private fun Analysis(line: SlinModel): SlinModel {
        // calculation variables
        var k: Double
        var kp: Double
        var r: Double
        var kf: Double
        val z0: Double
        val m: Double
        val deltaW: Double
        val A: Double
        val v: Double

        val frequency: Double

        if (line.metalWidth < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.WIDTH_MINIMAL_LIMIT
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

        frequency = line.frequency

        // Characteristic Impedance
        if (line.metalThick < line.subHeight / 1000) {
            // Thin strip case:

            k = 1 / cosh(Math.PI * line.metalWidth / (2 * line.subHeight))

            // compute K(k)/K'(k) where K is the complete elliptic integral of the first
            // kind, K' is the complementary complete elliptic integral of the first kind
            kp = sqrt(1.0 - k.pow(2.0))
            r = 1.0
            kf = (1.0 + k) / (1.0 + kp)
            while (kf != 1.0) {
                r = r * kf
                k = 2.0 * sqrt(k) / (1.0 + k)
                kp = 2.0 * sqrt(kp) / (1.0 + kp)
                kf = (1.0 + k) / (1.0 + kp)
            }

            z0 = 29.976 * Math.PI * r / sqrt(line.subEpsilon)
        } else {
            // Finite strip case:
            m =
                6.0 * (line.subHeight - line.metalThick) / (3.0 * line.subHeight - line.metalThick)
            deltaW = (line.metalThick / Math.PI) * (1.0 - 0.5 * ln(
                (line.metalThick / (2 * line.subHeight - line.metalThick)).pow(2.0) + (0.0796 * line.metalThick / (line.metalWidth + 1.1 * line.metalThick)).pow(
                    m
                )
            ))
            A =
                4.0 * (line.subHeight - line.metalThick) / (Math.PI * (line.metalWidth + deltaW))
            z0 =
                (30 / sqrt(line.subEpsilon)) * ln(1.0 + A * (2.0 * A + sqrt(4.0 * A * A + 6.27)))
        }

        // Electrical Length
        // propagation velocity (meters/sec)
        v = Constants.LIGHTSPEED / sqrt(line.subEpsilon)
        line.phase = 360 * line.metalLength * frequency / v

        // store results
        line.impedance = z0
        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Synthesizes the given SlinModel to match the desired impedance by adjusting a specified parameter.
     * 
     * @param line the SlinModel to synthesize
     * @param flag the parameter to adjust (width, height, Er, or length)
     * @return the synthesized SlinModel with adjusted parameter
     */
    private fun Synthesize(line: SlinModel, flag: Int): SlinModel {
        var line = line
        val l: Double
        val v: Double
        val len: Double
        val impedance: Double

        // the optimization variables, current, min/max, and previous values
        var `var` = 0.0
        var varmax = 0.0
        var varmin = 0.0
        var varold = 0.0

        // errors due to the above values for the optimization variable
        var err = 0.0
        val errmax: Double
        val errmin: Double
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

            Constants.Synthesize_Height -> {
                varmax = 100.0 * line.metalWidth
                varmin = 1.01 * line.metalThick
                `var` = line.metalWidth
            }

            Constants.Synthesize_Er -> {
                varmax = 100.0
                varmin = 1.0
                `var` = 5.0
            }

            Constants.Synthesize_Length -> {
                varmax = 100.0
                varmin = 1.0
                `var` = 5.0
                done = true
            }

            else -> {}
        }

        // read values from the input line structure
        len = line.phase
        impedance = line.impedance

        // temp value for l used while synthesizing the other header_parameters.
        l = 1000.0
        line.setMetalLength(l, "m")

        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varmin, flag)
            line = Analysis(line)
            if (line.errorCode == ERROR.NO_ERROR) {
                errmin = line.impedance - impedance
            } else {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            line.setSynthesizeParameter(varmax, flag)
            line = Analysis(line)
            if (line.errorCode == ERROR.NO_ERROR) {
                errmax = line.impedance - impedance
            } else {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            line.setSynthesizeParameter(`var`, flag)
            line = Analysis(line)
            if (line.errorCode == ERROR.NO_ERROR) {
                err = line.impedance - impedance
            } else {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            varold = 0.99 * `var`
            line.setSynthesizeParameter(varold, flag)
            line = Analysis(line)
            if (line.errorCode == ERROR.NO_ERROR) {
                errold = line.impedance - impedance
            } else {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            // see if we've actually been able to bracket the solution
            if (errmax * errmin > 0) {
                // alert("Could not bracket the solution.\n"
                // "Synthesis failed.\n"MLINLine);
                // return -1;
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            // figure out the slope of the error vs variable
            if (errmax > 0) sign = 1.0
            else sign = -1.0

            iters = 0
        }

        // the actual iterations
        while (!done) {
            // update the interation count
            iters = iters + 1

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
            line = Analysis(line)
            if (line.errorCode == ERROR.NO_ERROR) {
                err = line.impedance - impedance
            } else {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            // update our bracket of the solution.
            if (sign * err > 0) {
                varmax = `var`
            } else {
                varmin = `var`
            }

            // check to see if we've converged
            if (abs(err) < abstol) {
                done = true
            } else if (abs((`var` - varold) / `var`) < reltol) {
                done = true
            } else if (iters >= maxiters) {
                // alert("Synthesis failed to converge in\n"
                // "%d iterations\n", maxiters);
                // return -1;
                line.errorCode = ERROR.MAX_ITERATIONS
                return line
            }
            // done with iteration
        }

        // velocity on line
        line = Analysis(line)

        v = Constants.LIGHTSPEED / sqrt(line.subEpsilon)
        line.setMetalLength((len / 360) * (v / line.frequency), "m")

        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Gets the analysis result for the given SlinModel.
     * 
     * @param line the SlinModel to analyze
     * @return the analyzed SlinModel
     */
    fun getAnaResult(line: SlinModel): SlinModel {
        return Analysis(line)
    }

    /**
     * Gets the synthesis result for the given SlinModel by adjusting a specified parameter.
     * 
     * @param line the SlinModel to synthesize
     * @param flag the parameter to adjust (width, height, Er, or length)
     * @return the synthesized SlinModel
     */
    fun getSynResult(line: SlinModel, flag: Int): SlinModel {
        return Synthesize(line, flag)
    }
}
