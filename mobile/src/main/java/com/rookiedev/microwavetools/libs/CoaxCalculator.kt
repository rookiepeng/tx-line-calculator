package com.rookiedev.microwavetools.libs

import com.rookiedev.microwavetools.libs.Constants.ERROR
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class CoaxCalculator {
    /**
     * Analyzes the given CoaxModel line and calculates its impedance and phase.
     * 
     * @param line The CoaxModel line to be analyzed.
     * @return The analyzed CoaxModel line with updated impedance and phase.
     */
    private fun Analysis(line: CoaxModel): CoaxModel {
        val x: Double
        val v: Double

        // qualify the inputs some
        if (line.subRadius <= line.coreRadius) {
            /*
             * alert("Error: b (%g) must be > a (%g)\r\n" "for a coax_fragment line\r\n");
             * return -1;
             */
            line.errorCode = ERROR.SUBSTRATE_TOO_LARGE
            return line
        }
        if (line.coreOffset >= (line.subRadius - 2 * line.coreRadius)) {
            /*
             * alert("Error:  c (%g)  must be < b - a (%g)\r\n"
             * "for a coax_fragment line\r\n",line->c,line->b-line->a); return -1;
             */
            line.errorCode = ERROR.OFFSET_TOO_LARGE
            return line
        }

        /*
         * find characteristic impedance (from Rosloniec)
         *
         * Note that Rosloniec deals in diameters while I deal in radii. Also, Rosloniec
         * swaps a and b from the notation I'm using here. x = (b + [a^2 - 4c^2]/b)/(2a)
         * is on p. 184 of Rosloniec.
         *
         * substituting b = 2a, a=2b gives
         *
         * x = (2a + [4b^2 - 4c^2]/2a)/(4b) = ( a + [2b^2 - 2c^2]/2a)/(2b) = ( a + [ b^2
         * - c^2]/ a)/(2b)
         */
        x = ((line.coreRadius
                + (line.subRadius.pow(2.0) - line.coreOffset
            .pow(2.0)) / line.coreRadius)
                / (2 * line.subRadius))

        line.impedance =
            ((1 / (2 * Math.PI))
                    * sqrt(Constants.FREESPACE_MU0 / (Constants.FREESPACE_E0 * line.subEpsilon)) * ln(
                x + sqrt(x * x - 1)
            ))

        // find velocity (meters/second)
        v = 1.0 / sqrt(Constants.FREESPACE_MU0 * Constants.FREESPACE_E0 * line.subEpsilon)

        // electrical length 2*pi*f*(180/pi) = 360*f
        line.phase = 360.0 * line.frequency * line.metalLength / v

        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Synthesizes the given CoaxModel line based on the specified flag.
     * 
     * @param line The CoaxModel line to be synthesized.
     * @param flag The parameter to be synthesized.
     * @return The synthesized CoaxModel line with updated parameters.
     */
    private fun Synthesize(line: CoaxModel, flag: Int): CoaxModel {
        var line = line
        val Ro: Double
        val v: Double
        val elen: Double

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
            Constants.Synthesize_CoreRadius -> {
                // varmax = 0.999 * line.getSubRadius();
                varmin = 0.001 * line.subRadius
                varmax = (line.subRadius - line.coreOffset) / 2 * 0.999
                `var` = 0.2 * line.subRadius
            }

            Constants.Synthesize_SubRadius -> {
                varmax = 1000.0 * line.coreRadius
                // varmin = 1.001 * line.getCoreRadius();
                varmin = (2 * line.coreRadius + line.coreOffset) * 1.001
                `var` = 5 * line.coreRadius
            }

            Constants.Synthesize_CoreOffset -> {
                varmax = 0.999 * (line.subRadius - 2 * line.coreRadius)
                varmin = 0.0
                `var` = 0.1 * varmax
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
        elen = line.phase

        // temp value for len used while synthesizing the other header_parameters.
        line.setMetalLength(1.0, "m")

        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varmin, flag)
            line = Analysis(line)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            errmin = line.impedance - Ro

            line.setSynthesizeParameter(varmax, flag)
            line = Analysis(line)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            errmax = line.impedance - Ro

            line.setSynthesizeParameter(`var`, flag)
            line = Analysis(line)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            err = line.impedance - Ro

            varold = 0.99 * `var`
            line.setSynthesizeParameter(varold, flag)
            line = Analysis(line)
            if (line.errorCode != ERROR.NO_ERROR) {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }
            errold = line.impedance - Ro

            // see if we've actually been able to bracket the solution
            if (errmax * errmin > 0) {
                /*
                 * alert("Could not bracket the solution.\n" "Synthesis failed.\n"); return -1;
                 */
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
                 * alert("Synthesis failed to converge in\n" "%d iterations\n", maxiters);
                 * return -1;
                 */
                line.errorCode = ERROR.MAX_ITERATIONS
                return line
            }
            // done with iteration
        }

        // velocity on line
        line = Analysis(line)

        v = Constants.LIGHTSPEED / sqrt(line.subEpsilon)
        line.setMetalLength((elen / 360) * (v / line.frequency), "m")
        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Returns the analysis result of the given CoaxModel line.
     * 
     * @param line The CoaxModel line to be analyzed.
     * @return The analyzed CoaxModel line.
     */
    fun getAnaResult(line: CoaxModel): CoaxModel {
        return Analysis(line)
    }

    /**
     * Returns the synthesis result of the given CoaxModel line based on the specified flag.
     * 
     * @param line The CoaxModel line to be synthesized.
     * @param flag The parameter to be synthesized.
     * @return The synthesized CoaxModel line.
     */
    fun getSynResult(line: CoaxModel, flag: Int): CoaxModel {
        return Synthesize(line, flag)
    }
}
