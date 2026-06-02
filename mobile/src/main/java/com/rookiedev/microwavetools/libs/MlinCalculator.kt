package com.rookiedev.microwavetools.libs

import com.rookiedev.microwavetools.libs.Constants.ERROR
import kotlin.math.abs
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.sqrt

class MlinCalculator {
    private var effectiveEr = 0.0

    /**
     * Analyzes the given microstrip line model and calculates its properties.
     * 
     * @param line The microstrip line model to analyze.
     * @return The analyzed microstrip line model with updated properties.
     */
    private fun Analysis(line: MlinModel): MlinModel {
        val width: Double
        val length: Double
        val height: Double
        val epsilon: Double
        val thickness: Double
        var impedance: Double
        var phase: Double
        val thicknessToHeight: Double
        val widthToHeight: Double

        val u1: Double
        val ur: Double
        val deltau1: Double
        val deltaur: Double
        val E0: Double
        val EFF0: Double
        var fn: Double // normalized frequency
        val P1: Double
        val P2: Double
        val P3: Double
        val P4: Double
        val P: Double
        val EF: Double
        val R1: Double
        val R2: Double
        val R3: Double
        val R4: Double
        val R5: Double
        val R6: Double
        val R7: Double
        val R8: Double
        var R9: Double
        val R10: Double
        val R11: Double
        val R12: Double
        val R13: Double
        val R14: Double
        val R15: Double
        val R16: Double
        val R17: Double
        val v: Double

        width = line.metalWidth
        if (width < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.WIDTH_MINIMAL_LIMIT
            return line
        }
        length = line.metalLength
        // SubstrateModel dielectric thickness
        height = line.subHeight
        if (height < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.HEIGHT_MINIMAL_LIMIT
            return line
        }

        // SubstrateModel relative permittivity
        epsilon = line.subEpsilon
        if (epsilon < 1) {
            line.errorCode = ERROR.ER_MINIMAL_LIMIT
            return line
        }

        // MetalModel thickness
        thickness = line.metalThick

        // starting microstrip_calc_int() with %f/1.0e6 MHz and Find u and correction
        // factor for nonzero metal thickness
        widthToHeight = width / height
        if (thickness > 0.0) {
            // find normalized metal thickness
            thicknessToHeight = thickness / height
            // (6) from Hammerstad and Jensen
            deltau1 = (thicknessToHeight / Constants.Pi) * ln(
                1.0 + 4.0 * exp(1.0)
                        / (thicknessToHeight * (cosh(sqrt(6.517 * widthToHeight)) / sinh(sqrt(6.517 * widthToHeight))).pow(
                    2.0
                ))
            )
            // (7) from Hammerstad and Jensen
            deltaur = 0.5 * (1.0 + 1.0 / cosh(sqrt(epsilon - 1.0))) * deltau1
        } else {
            deltau1 = 0.0
            deltaur = 0.0
        }

        // relative permittivity at f=0 (Hammerstad and Jensen)
        // (3) from Hammerstad & Jensen and Y from the Rogers Corp. paper
        u1 = widthToHeight + deltau1
        ur = widthToHeight + deltaur
        E0 = EffectiveDielectricConstant_Effer(ur, epsilon)

        // zero frequency characteristic impedance
        // (8) from Hammerstad and Jensen
        impedance = CharacteristicImpedance_Z0(ur) / sqrt(E0)

        // zero frequency effective permitivity.
        // (9) from Hammerstad and Jensen
        EFF0 = E0 * (CharacteristicImpedance_Z0(u1) / CharacteristicImpedance_Z0(ur)).pow(2.0)

        // relative permittivity including dispersion (Kirschning and Jansen)
        // normalized frequency (GHz-cm)
        fn = 1e-7 * line.frequency * height

        // (2) from Kirschning and Jansen
        P1 = (0.27488 + (0.6315 + (0.525 / ((1.0 + 0.157 * fn).pow(20.0)))) * widthToHeight
                - 0.065683 * exp(-8.7513 * widthToHeight))
        P2 = 0.33622 * (1.0 - exp(-0.03442 * epsilon))
        P3 = 0.0363 * exp(-4.6 * widthToHeight) * (1.0 - exp(-(fn / 3.87).pow(4.97)))
        P4 = 1.0 + 2.751 * (1.0 - exp(-(epsilon / 15.916).pow(8.0)))
        P = P1 * P2 * ((0.1844 + P3 * P4) * 10.0 * fn).pow(1.5763)

        // (1) from Kirschning and Jansen
        EF = (EFF0 + epsilon * P) / (1.0 + P)

        // Characteristic Impedance (Jansen and Kirschning)
        // normalized frequency (GHz-mm)
        fn = 1.0e-6 * line.frequency * height

        // (1) from Jansen and Kirschning
        R1 = 0.03891 * epsilon.pow(1.4)
        R2 = 0.267 * widthToHeight.pow(7.0)
        R3 = 4.766 * exp(-3.228 * widthToHeight.pow(0.641))
        R4 = 0.016 + (0.0514 * epsilon).pow(4.524)
        R5 = (fn / 28.843).pow(12.0)
        R6 = 22.20 * widthToHeight.pow(1.92)

        // (2) from Jansen and Kirschning
        R7 = 1.206 - 0.3144 * exp(-R1) * (1.0 - exp(-R2))
        R8 =
            1.0 + 1.275 * (1.0 - exp(-0.004625 * R3 * epsilon.pow(1.674) * (fn / 18.365).pow(2.745)))
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4)) * (exp(-R6) / (1.0 + 1.2992 * R5))
        R9 = R9 * (epsilon - 1.0).pow(6.0) / (1.0 + 10.0 * (epsilon - 1).pow(6.0))

        // (3) from Jansen and Kirschning
        R10 = 0.00044 * epsilon.pow(2.136) + 0.0184
        R11 = (fn / 19.47).pow(6.0) / (1.0 + 0.0962 * (fn / 19.47).pow(6.0))
        R12 = 1.0 / (1.0 + 0.00245 * widthToHeight * widthToHeight)

        // (4) from Jansen and Kirschning
        R13 = 0.9408 * EF.pow(R8) - 0.9603
        R14 = (0.9408 - R9) * EFF0.pow(R8) - 0.9603
        R15 = 0.707 * R10 * (fn / 12.3).pow(1.097)
        R16 = 1.0 + 0.0503 * epsilon * epsilon * R11 * (1.0 - exp(-(widthToHeight / 15).pow(6.0)))
        R17 = R7 * (1.0 - 1.1241 * (R12 / R16) * exp(-0.026 * fn.pow(1.15656) - R15))

        // (5) from Jansen and Kirschning
        impedance = impedance * (R13 / R14).pow(R17)

        // propagation velocity (meters/sec)
        v = Constants.LIGHTSPEED / sqrt(EF)

        // length in wavelengths
        if (line.frequency > 0.0) {
            phase = (length) / (v / line.frequency)
        } else {
            phase = 0.0
        }

        // convert to degrees
        phase = 360.0 * phase

        // effective relative permittivity
        effectiveEr = EF

        // this.effectiveEr = effectiveEr;
        // MLINLine.setkEff(eeff);
        line.phase = phase

        // store results
        line.impedance = impedance
        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Synthesizes the given microstrip line model based on the specified flag.
     * 
     * @param line The microstrip line model to synthesize.
     * @param flag The flag indicating which parameter to synthesize.
     * @return The synthesized microstrip line model with updated properties.
     */
    private fun Synthesize(line: MlinModel, flag: Int): MlinModel {
        var line = line
        var length: Double
        val impedance: Double
        val v: Double
        val phase: Double

        // the optimization variables, current, min/max, and previous values
        var `var` = 0.0
        var varMax = 0.0
        var varMin = 0.0
        var varOld = 0.0

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
                varMax = 100.0 * line.subHeight
                varMin = 0.01 * line.subHeight
                `var` = line.subHeight
            }

            Constants.Synthesize_Height -> {
                varMax = 100.0 * line.metalWidth
                varMin = 0.01 * line.metalWidth
                `var` = line.metalWidth
            }

            Constants.Synthesize_Er -> {
                varMax = 100.0
                varMin = 1.0
                `var` = 5.0
            }

            Constants.Synthesize_Length -> {
                varMax = 100.0
                varMin = 1.0
                `var` = 5.0
                done = true
            }

            else -> {}
        }

        // read values from the input line structure
        phase = line.phase
        impedance = line.impedance

        // temp value for l used while synthesizing the other header_parameters. We'll
        // correct l later.
        length = 1000.0
        line.setMetalLength(length, "m")

        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varMin, flag)
            line = Analysis(line)
            if (line.errorCode == ERROR.NO_ERROR) {
                errmin = line.impedance - impedance
            } else {
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
            }

            line.setSynthesizeParameter(varMax, flag)
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

            varOld = 0.99 * `var`
            line.setSynthesizeParameter(varOld, flag)
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
                // "Synthesis failed.\n");
                line.errorCode = ERROR.COULD_NOT_BRACKET_SOLUTION
                return line
                // return -1;
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
            deriv = (err - errold) / (`var` - varOld)

            // copy over the current estimate to the previous one
            varOld = `var`
            errold = err

            // try a quasi-newton iteration
            `var` = `var` - err / deriv

            /*
             * see if the new guess is within our bracketed range. If so, accept the new
             * estimate. If not, toss it out and do a bisection step to reduce the bracket.
             */
            if ((`var` > varMax) || (`var` < varMin)) {
                `var` = (varMin + varMax) / 2.0
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
                varMax = `var`
            } else {
                varMin = `var`
            }

            // check to see if we've converged
            if (abs(err) < abstol) {
                done = true
            } else if (abs((`var` - varOld) / `var`) < reltol) {
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
        v = Constants.LIGHTSPEED / sqrt(effectiveEr)
        length = (phase / 360) * (v / line.frequency)
        line.setMetalLength(length, "m")

        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Returns the analysis result for the given microstrip line model.
     * 
     * @param MLINLine The microstrip line model to analyze.
     * @return The analyzed microstrip line model with updated properties.
     */
    fun getAnaResult(MLINLine: MlinModel): MlinModel {
        return Analysis(MLINLine)
    }

    /**
     * Returns the synthesis result for the given microstrip line model based on the specified flag.
     * 
     * @param MLINLine The microstrip line model to synthesize.
     * @param flag The flag indicating which parameter to synthesize.
     * @return The synthesized microstrip line model with updated properties.
     */
    fun getSynResult(MLINLine: MlinModel, flag: Int): MlinModel {
        return Synthesize(MLINLine, flag)
    }

    companion object {
        /**
         * Calculates the effective dielectric constant for the given width-to-height ratio and dielectric constant.
         * 
         * @param widthToHeight The width-to-height ratio of the microstrip line.
         * @param dielectricConstant The dielectric constant of the substrate.
         * @return The effective dielectric constant.
         */
        private fun EffectiveDielectricConstant_Effer(
            widthToHeight: Double,
            dielectricConstant: Double
        ): Double {
            val A: Double
            val B: Double
            val E0: Double

            // (4) from Hammerstad and Jensen
            A = (1.0 + (1.0 / 49.0) * ln(
                (widthToHeight.pow(4.0) + (widthToHeight / 52.0).pow(2.0))
                        / (widthToHeight.pow(4.0) + 0.432)
            ) + (1.0 / 18.7) * ln(1.0 + (widthToHeight / 18.1).pow(3.0)))

            // (5) from Hammerstad and Jensen
            B = 0.564 * ((dielectricConstant - 0.9) / (dielectricConstant + 3.0)).pow(0.053)

            // zero frequency effective permitivity.
            // (3) from Hammerstad and Jensen.
            // This is ee(ur,er) that is used by (9) in Hammerstad and Jensen.
            E0 = ((dielectricConstant + 1.0) / 2.0
                    + ((dielectricConstant - 1.0) / 2.0) * (1.0 + 10.0 / widthToHeight).pow((-A * B)))

            return E0
        }

        /**
         * Calculates the characteristic impedance for the given width-to-height ratio.
         * 
         * @param widthToHeight The width-to-height ratio of the microstrip line.
         * @return The characteristic impedance.
         */
        private fun CharacteristicImpedance_Z0(widthToHeight: Double): Double {
            val F: Double
            val z01: Double

            // (2) from Hammerstad and Jensen. 'u' is the normalized width
            F = 6.0 + (2.0 * Constants.Pi - 6.0) * exp(-(30.666 / widthToHeight).pow(0.7528))

            // (1) from Hammerstad and Jensen
            z01 = ((Constants.FREESPACEZ0 / (2 * Constants.Pi))
                    * ln(F / widthToHeight + sqrt(1.0 + (2 / widthToHeight).pow(2.0))))

            return z01
        }
    }
}
