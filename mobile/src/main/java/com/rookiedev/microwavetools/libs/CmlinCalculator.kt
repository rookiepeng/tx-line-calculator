package com.rookiedev.microwavetools.libs

import com.rookiedev.microwavetools.libs.Constants.ERROR
import com.rookiedev.microwavetools.libs.Constants.WARNING
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class CmlinCalculator {
    /**
     * Analyzes the given CmlinModel to calculate various parameters.
     * 
     * @param line The CmlinModel to analyze.
     * @return The analyzed CmlinModel with updated parameters.
     */
    private fun Analysis(line: CmlinModel): CmlinModel {
        // input physical dimensions
        val width: Double
        val length: Double
        val space: Double
        // substrate header_parameters
        val height: Double
        val dielectric: Double
        val frequency: Double

        val widthToHeight: Double
        val spaceToHeight: Double
        val V: Double
        val EFE0: Double
        val AO: Double
        val BO: Double
        val CO: Double
        val DO: Double
        val EFO0: Double
        var frequencyToHeight: Double
        val EF: Double // 0 frequency (static) relative dielectric constant for a single microstrip.
        val EFF: Double // frequency dependent relative dielectric constant for a single microstrip
        // given in Kirschning and Jansen (EL)
        val ZL0: Double // static single strip, T=0, characteristic impedance (f=0) (Hammerstad and
        // Jensen)
        val ZLF: Double // frequency dependent single microstrip characteristic impedance from Jansen
        // and Kirschning.
        val z0ef: Double
        val z0of: Double // even/odd mode impedance at the frequency of interest
        var electricalLength: Double // degree

        val P1: Double
        val P2: Double
        val P3: Double
        val P4: Double
        val P5: Double
        val P6: Double
        val P7: Double
        val P8: Double
        val P9: Double
        val P10: Double
        val P11: Double
        val P12: Double
        val P13: Double
        val P14: Double
        val P15: Double
        val FEF: Double
        val FOF: Double
        val EFEF: Double
        val EFOF: Double
        val Q0: Double
        val Q1: Double
        val Q2: Double
        val Q3: Double
        val Q4: Double
        val Q5: Double
        val Q6: Double
        val Q7: Double
        val Q8: Double
        val Q9: Double
        val Q10: Double
        val Q11: Double
        val Q12: Double
        val Q13: Double
        val Q14: Double
        val Q15: Double
        val Q16: Double
        val Q17: Double
        val Q18: Double
        val Q19: Double
        val Q20: Double
        val Q21: Double
        val Q22: Double
        val Q23: Double
        val Q24: Double
        val Q25: Double
        val Q26: Double
        val Q27: Double
        val Q28: Double
        val Q29: Double
        val z0e0: Double
        val z0o0: Double
        val xP1: Double
        val xP2: Double
        val xP3: Double
        val xP4: Double
        val xP: Double
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
        val RE: Double
        val QE: Double
        val PE: Double
        val DE: Double
        val CE: Double

        val v: Double
        var fnold: Double

        line.warningCode = WARNING.NO_WARNING

        width = line.metalWidth
        if (width < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.WIDTH_MINIMAL_LIMIT
            return line
        }
        length = line.metalLength
        space = line.metalSpace
        if (space < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.SPACE_MINIMAL_LIMIT
            return line
        }
        frequency = line.frequency
        // SubstrateModel dielectric thickness
        height = line.subHeight
        if (height < Constants.MINI_LIMIT) {
            line.errorCode = ERROR.HEIGHT_MINIMAL_LIMIT
            return line
        }
        // SubstrateModel relative permittivity
        dielectric = line.subEpsilon
        if (dielectric < 1) {
            line.errorCode = ERROR.ER_MINIMAL_LIMIT
            return line
        }

        // Find widthToHeight and correction factor for nonzero metal thickness
        widthToHeight = width / height
        spaceToHeight = space / height

        // verify that the geometry is in a range where the accuracy is good. This is
        // given by (1) in Kirschning and Jansen (MTT)
        if ((widthToHeight < 0.1) || (widthToHeight > 10.0)) {
            // Warning: u=w/h is outside the range for highly accurate results.
            // For best accuracy 0.1 < u < 10.0
            line.warningCode = WARNING.WIDTH2HEIGHT_OUT_OF_RANGE
        }
        if ((spaceToHeight < 0.1) || (spaceToHeight > 10.0)) {
            // Warning: g=s/h is outside the range for highly accurate results
            // For best accuracy 0.1 < g < 10.0
            line.warningCode = WARNING.SPACE2HEIGHT_OUT_OF_RANGE
        }
        if (dielectric > 18.0) {
            // Warning: er is outside the range for highly accurate results
            // For best accuracy 1.0 < er < 18.0
            line.warningCode = WARNING.DIELECTRIC_OUT_OF_RANGE
        }

        // static even mode relative permittivity (f=0) (3) from Kirschning and Jansen
        // (MTT). Accurate to 0.7 % over "accurate" range
        V = (widthToHeight * (20.0 + spaceToHeight.pow(2.0)) / (10.0 + spaceToHeight.pow(2.0))
                + spaceToHeight * exp(-spaceToHeight))

        // note: The equations listed in (3) in Kirschning and Jansen (MTT) are the same
        // as in Hammerstad and Jensen but with u in H&J replaced with V from K&J.
        EFE0 = EffectiveDielectricConstant_Effer(V, dielectric)

        // static single strip, T=0, relative permittivity (f=0), width=w from
        // Hammerstad and Jensen.
        EF = EffectiveDielectricConstant_Effer(widthToHeight, dielectric)

        // static odd mode relative permittivity (f=0) This is (4) from Kirschning and
        // Jansen (MTT). Accurate to 0.5%.
        AO = 0.7287 * (EF - (dielectric + 1.0) / 2.0) * (1.0 - exp(-0.179 * widthToHeight))
        BO = 0.747 * dielectric / (0.15 + dielectric)
        CO = BO - (BO - 0.207) * exp(-0.414 * widthToHeight)
        DO = 0.593 + 0.694 * exp(-0.562 * widthToHeight)

        // Note, this includes the published correction
        EFO0 = ((dielectric + 1.0) / 2.0 + AO - EF) * exp(-CO * (spaceToHeight.pow(DO))) + EF

        // normalized frequency (2) from Kirschning and Jansen (MTT)
        frequencyToHeight = 1e-6 * frequency * height

        // check for range of validity for the dispersion equations. p. 85 of Kirschning
        // and Jansen (MTT) says fn <= 25 gives 1.4% accuracy.
        if (frequencyToHeight > 25.0) {
            // Warning: Frequency is higher than the range
            // over which the dispersion equations are accurate.
            line.warningCode = WARNING.FREQUENCY2HEIGHT_OUT_OF_RANGE
        }

        // even/odd mode relative permittivity including dispersion
        // even mode dispersion. (6) from Kirschning and Jansen (MTT)
        P1 =
            (0.27488 + (0.6315 + 0.525 / ((1.0 + 0.0157 * frequencyToHeight).pow(20.0))) * widthToHeight
                    - 0.065683 * exp(-8.7513 * widthToHeight))
        P2 = 0.33622 * (1.0 - exp(-0.03442 * dielectric))
        P3 = 0.0363 * exp(-4.6 * widthToHeight) * (1.0 - exp(-(frequencyToHeight / 38.7).pow(4.97)))
        P4 = 1.0 + 2.751 * (1.0 - exp(-(dielectric / 15.916).pow(8.0)))
        P5 = 0.334 * exp(-3.3 * (dielectric / 15.0).pow(3.0)) + 0.746
        P6 = P5 * exp(-(frequencyToHeight / 18.0).pow(0.368))
        P7 = 1.0 + (4.069 * P6 * (spaceToHeight.pow(0.479))
                * exp(-1.347 * (spaceToHeight.pow(0.595)) - 0.17 * (spaceToHeight.pow(2.5))))
        FEF = P1 * P2 * ((P3 * P4 + 0.1844 * P7) * frequencyToHeight).pow(1.5763)

        // odd mode dispersion. (7) from Kirschning and Jansen (MTT)
        P8 = 0.7168 * (1.0 + 1.076 / (1.0 + 0.0576 * (dielectric - 1.0)))
        P9 = P8 - (0.7913 * (1.0 - exp(-(frequencyToHeight / 20.0).pow(1.424)))
                * atan(2.481 * (dielectric / 8.0).pow(0.946)))
        P10 = 0.242 * (dielectric - 1.0).pow(0.55)
        P11 = (0.6366 * (exp(-0.3401 * frequencyToHeight) - 1.0)
                * atan(1.263 * (widthToHeight / 3.0).pow(1.629)))
        P12 = P9 + (1.0 - P9) / (1.0 + 1.183 * widthToHeight.pow(1.376))
        P13 = 1.695 * P10 / (0.414 + 1.605 * P10)
        P14 = 0.8928 + 0.1072 * (1.0 - exp(-0.42 * (frequencyToHeight / 20.0).pow(3.215)))
        P15 = abs(1.0 - 0.8928 * (1.0 + P11) * P12 * exp(-P13 * (spaceToHeight.pow(1.092))) / P14)
        FOF = P1 * P2 * ((P3 * P4 + 0.1844) * frequencyToHeight * P15).pow(1.5763)

        // relative permittivities including dispersion via generalization of
        // Getsinger's dispersion relation. This is (5) in Kirschning and Jansen (MTT).
        EFEF = dielectric - (dielectric - EFE0) / (1.0 + FEF)
        EFOF = dielectric - (dielectric - EFO0) / (1.0 + FOF)

        // static single strip, T=0, characteristic impedance (f=0) (Hammerstad and
        // Jensen)
        ZL0 = CharacteristicImpedance_Z0(widthToHeight) / sqrt(EF)

        // static even mode characteristic impedance (f=0) (8) from Kirschning and
        // Jansen (MTT) 0.6% accurate
        Q1 = 0.8695 * (widthToHeight.pow(0.194))
        Q2 = 1.0 + 0.7519 * spaceToHeight + 0.189 * (spaceToHeight.pow(2.31))
        Q3 = (0.1975 + (16.6 + (8.4 / spaceToHeight).pow(6.0)).pow(-0.387) + ln(
            (spaceToHeight.pow(10.0)) / (1.0 + (spaceToHeight / 3.4).pow(10.0))
        ) / 241.0)
        Q4 = (2.0 * Q1 / Q2) / (exp(-spaceToHeight) * (widthToHeight.pow(Q3))
                + (2.0 - exp(-spaceToHeight)) * (widthToHeight.pow(-Q3)))
        z0e0 = ZL0 * sqrt(EF / EFE0) / (1.0 - (ZL0 / 377.0) * sqrt(EF) * Q4)

        // static odd mode characteristic impedance (f=0) (9) from Kirschning and Jansen
        // (MTT) 0.6% accurate
        Q5 = 1.794 + 1.14 * ln(1.0 + 0.638 / (spaceToHeight + 0.517 * (spaceToHeight.pow(2.43))))
        Q6 =
            (0.2305 + ln((spaceToHeight.pow(10.0)) / (1.0 + (spaceToHeight / 5.8).pow(10.0))) / 281.3 + ln(
                1.0 + 0.598 * (spaceToHeight.pow(1.154))
            ) / 5.1)
        Q7 = (10.0 + 190.0 * spaceToHeight * spaceToHeight) / (1.0 + 82.3 * spaceToHeight.pow(3.0))
        Q8 = exp(-6.5 - 0.95 * ln(spaceToHeight) - (spaceToHeight / 0.15).pow(5.0))
        Q9 = ln(Q7) * (Q8 + 1.0 / 16.5)
        Q10 = (Q2 * Q4 - Q5 * exp(ln(widthToHeight) * Q6 * (widthToHeight.pow(-Q9)))) / Q2
        z0o0 = ZL0 * sqrt(EF / EFO0) / (1.0 - (ZL0 / 377.0) * sqrt(EF) * Q10)

        // relative permittivity including dispersion of single microstrip of width W,
        // Tmet=0. Kirschning and Jansen (EL) **/
        // normalized frequency (GHz-cm)
        // save fn
        fnold = frequencyToHeight
        frequencyToHeight = 1.0e-7 * frequency * height

        // (2) from Kirschning and Jansen (EL)
        xP1 =
            (0.27488 + (0.6315 + (0.525 / ((1.0 + 0.157 * frequencyToHeight).pow(20.0)))) * widthToHeight
                    - 0.065683 * exp(-8.7513 * widthToHeight))
        xP2 = 0.33622 * (1.0 - exp(-0.03442 * dielectric))
        xP3 =
            0.0363 * exp(-4.6 * widthToHeight) * (1.0 - exp(-(frequencyToHeight / 3.87).pow(4.97)))
        xP4 = 1.0 + 2.751 * (1.0 - exp(-(dielectric / 15.916).pow(8.0)))
        xP = xP1 * xP2 * ((0.1844 + xP3 * xP4) * 10.0 * frequencyToHeight).pow(1.5763)

        // (1) from Kirschning and Jansen (EL)
        EFF = (EF + dielectric * xP) / (1.0 + xP)
        // recall fn
        frequencyToHeight = fnold

        // Characteristic Impedance of single strip, Width=W, Tmet=0. Jansen and
        // Kirschning **/
        // normalized frequency (GHz-mm)
        // save fn
        fnold = frequencyToHeight
        frequencyToHeight = 1e-6 * frequency * height

        // (1) from Jansen and Kirschning
        R1 = 0.03891 * (dielectric.pow(1.4))
        R2 = 0.267 * (widthToHeight.pow(7.0))
        R3 = 4.766 * exp(-3.228 * (widthToHeight.pow(0.641)))
        R4 = 0.016 + (0.0514 * dielectric).pow(4.524)
        R5 = (frequencyToHeight / 28.843).pow(12.0)
        R6 = 22.20 * (widthToHeight.pow(1.92))

        // (2) from Jansen and Kirschning
        R7 = 1.206 - 0.3144 * exp(-R1) * (1.0 - exp(-R2))
        R8 = 1.0 + 1.275 * (1.0
                - exp(
            -0.004625 * R3 * dielectric.pow(1.674) * (frequencyToHeight / 18.365).pow(
                2.745
            )
        ))
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4)) * (exp(-R6) / (1.0 + 1.2992 * R5))
        R9 = R9 * ((dielectric - 1.0).pow(6.0)) / (1.0 + 10.0 * (dielectric - 1.0).pow(6.0))

        // (3) from Jansen and Kirschning
        R10 = 0.00044 * (dielectric.pow(2.136)) + 0.0184
        R11 = (((frequencyToHeight / 19.47).pow(6.0))
                / (1.0 + 0.0962 * ((frequencyToHeight / 19.47).pow(6.0))))
        R12 = 1.0 / (1.0 + 0.00245 * widthToHeight * widthToHeight)

        // (4) from Jansen and Kirschning
        R13 = 0.9408 * (EFF.pow(R8)) - 0.9603
        R14 = (0.9408 - R9) * EF.pow(R8) - 0.9603
        R15 = 0.707 * R10 * ((frequencyToHeight / 12.3).pow(1.097))
        R16 =
            1.0 + 0.0503 * dielectric * dielectric * R11 * (1.0 - exp(-((widthToHeight / 15).pow(6.0))))
        R17 =
            R7 * (1.0 - 1.1241 * (R12 / R16) * exp(-0.026 * (frequencyToHeight.pow(1.15656)) - R15))

        // ZLF = zero thickness single strip characteristic impedance including
        // dispersion
        ZLF = ZL0 * (R13 / R14).pow(R17)

        // Q0 = R17 from zero thickness, single microstrip
        Q0 = R17

        // recall fn
        frequencyToHeight = fnold

        // even mode characteristic impedance including dispersion, this is (10) from
        // Kirschning and Jansen (MTT)
        Q11 = 0.893 * (1.0 - 0.3 / (1.0 + 0.7 * (dielectric - 1.0)))
        Q12 = (2.121
                * (((frequencyToHeight / 20.0).pow(4.91))
                / (1 + Q11 * (frequencyToHeight / 20.0).pow(4.91)))
                * exp(-2.87 * spaceToHeight) * spaceToHeight.pow(0.902))
        Q13 = 1.0 + 0.038 * (dielectric / 8.0).pow(5.1)
        Q14 = 1.0 + 1.203 * ((dielectric / 15.0).pow(4.0)) / (1.0 + (dielectric / 15.0).pow(4.0))
        Q15 = (1.887 * exp(-1.5 * (spaceToHeight.pow(0.84))) * (spaceToHeight.pow(Q14))
                / (1.0 + 0.41 * ((frequencyToHeight / 15.0).pow(3.0)) * (widthToHeight.pow((2.0 / Q13)))
                / (0.125 + widthToHeight.pow((1.626 / Q13)))))
        Q16 = (1.0 + 9.0 / (1.0 + 0.403 * (dielectric - 1.0).pow(2.0))) * Q15
        Q17 = (0.394 * (1.0 - exp(-1.47 * (widthToHeight / 7.0).pow(0.672)))
                * (1.0 - exp(-4.25 * (frequencyToHeight / 20.0).pow(1.87))))
        Q18 = (0.61 * (1.0 - exp(-2.13 * (widthToHeight / 8).pow(1.593)))
                / (1.0 + 6.544 * spaceToHeight.pow(4.17)))
        Q19 = 0.21 * (spaceToHeight.pow(4.0)) / (((1.0 + 0.18 * (spaceToHeight.pow(4.9)))
                * (1.0 + 0.1 * widthToHeight * widthToHeight) * (1 + (frequencyToHeight / 24.0).pow(
            3.0
        ))))
        Q20 = (0.09 + 1.0 / (1.0 + 0.1 * (dielectric - 1.0).pow(2.7))) * Q19
        Q21 = abs(
            1.0 - (42.54 * spaceToHeight.pow(0.133) * exp(-0.812 * spaceToHeight) * widthToHeight.pow(
                2.5
            )) / (1.0 + 0.033 * widthToHeight.pow(2.5))
        )

        RE = (frequencyToHeight / 28.843).pow(12.0)
        QE = 0.016 + (0.0514 * dielectric * Q21).pow(4.524)
        PE = 4.766 * exp(-3.228 * widthToHeight.pow(0.641))
        DE = (5.086 * QE * (RE / (0.3838 + 0.386 * QE))
                * (exp(-22.2 * widthToHeight.pow(1.92)) / (1.0 + 1.2992 * RE))
                * (((dielectric - 1.0).pow(6.0)) / (1.0 + 10.0 * (dielectric - 1.0).pow(6.0))))
        CE = (1.0 + 1.275 * (1.0 - exp(
            -0.004625 * PE * dielectric.pow(1.674) * (frequencyToHeight / 18.365).pow(2.745)
        ))
                - Q12 + Q16 - Q17) + Q18 + Q20

        // Note: This line contains one of the published corrections. The second EFF
        // from the original paper is replaced by EF.
        if (dielectric > 1.0) {
            z0ef = (z0e0 * ((0.9408 * EFF.pow(CE) - 0.9603).pow(Q0))
                    / (((0.9408 - DE) * EF.pow(CE) - 0.9603).pow(Q0)))
        } else {
            // no dispersion for er = 1.0
            z0ef = z0e0
        }

        // odd mode characteristic impedance including dispersion. This is (11) from
        // Kirschning and Jansen (MTT)
        if (dielectric > 1.0) {
            Q29 = 15.16 / (1.0 + 0.196 * (dielectric - 1.0).pow(2.0))
            Q28 =
                0.149 * ((dielectric - 1.0).pow(3.0)) / (94.5 + 0.038 * (dielectric - 1.0).pow(3.0))
            Q27 =
                (0.4 * spaceToHeight.pow(0.84) * (1.0 + 2.5 * ((dielectric - 1.0).pow(1.5)) / (5.0 + (dielectric - 1.0).pow(
                    1.5
                ))))
            Q26 = 30.0 - 22.2 * ((((dielectric - 1.0) / 13.0).pow(12.0))
                    / (1.0 + 3.0 * ((dielectric - 1.0) / 13.0).pow(12.0))) - Q29
            Q25 =
                ((0.3 * frequencyToHeight * frequencyToHeight / (10.0 + frequencyToHeight * frequencyToHeight))
                        * (1.0 + 2.333 * ((dielectric - 1.0).pow(2.0)) / (5.0 + (dielectric - 1.0).pow(
                    2.0
                ))))
        } else {
            Q29 = 15.16 / (1.0 + 0.196)
            Q28 = 0.149 / (94.5 + 0.038)
            Q27 = 0.4 * spaceToHeight.pow(0.84) * (1.0 + 2.5 / (5.0 + 1.0))
            Q26 = 30.0 - 22.2 * ((1.0) / (1.0 + 3.0)) - Q29
            Q25 =
                ((0.3 * frequencyToHeight * frequencyToHeight / (10.0 + frequencyToHeight * frequencyToHeight))
                        * (1.0 + 2.333 / (5.0 + 1.0)))
        }
        Q24 =
            (2.506 * Q28 * widthToHeight.pow(0.894) * (((1.0 + 1.3 * widthToHeight) * frequencyToHeight / 99.25).pow(
                4.29
            ))
                    / (3.575 + widthToHeight.pow(0.894)))
        Q23 =
            1.0 + 0.005 * frequencyToHeight * Q27 / ((1.0 + 0.812 * (frequencyToHeight / 15.0).pow(
                1.9
            ))
                    * (1.0 + 0.025 * widthToHeight * widthToHeight))
        Q22 = (0.925 * ((frequencyToHeight / Q26).pow(1.536))
                / (1.0 + 0.3 * (frequencyToHeight / 30.0).pow(1.536)))

        // in this final expression, ZLF is the frequency dependent single microstrip
        // characteristic impedance from Jansen and Kirschning.
        if (dielectric > 1.0) {
            z0of = (ZLF + (z0o0 * (EFOF / EFO0).pow(Q22) - ZLF * Q23)
                    / (1.0 + Q24 + ((0.46 * spaceToHeight).pow(2.2)) * Q25))
        } else {
            z0of = z0o0
        }

        // accuracy check from page 87 in Kirschning and Jansen (MTT)
        if (frequencyToHeight > 20.0) {
            // Warning: Normalized frequency is higher than the
            // maximum for < 2.5 %% error in even/odd mode impedance.
            line.warningCode = WARNING.FREQUENCY2HEIGHT_OUT_OF_RANGE
        }

        // electrical length
        // propagation velocity (meters/sec)
        v = Constants.LIGHTSPEED / sqrt(sqrt(EFEF * EFOF))
        // length in wavelengths
        electricalLength = length / (v / frequency)
        // convert to degrees
        electricalLength = 360.0 * electricalLength

        // copy over the results
        line.impedanceEven = z0ef
        line.impedanceOdd = z0of
        line.impedance = sqrt(z0ef * z0of)
        // coupling coefficient
        line.couplingFactor = (z0ef - z0of) / (z0ef + z0of)
        // electrical length
        line.phase = electricalLength

        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Synthesizes the given CmlinModel to calculate width and space based on impedance and coupling factor.
     * 
     * @param line The CmlinModel to synthesize.
     * @param use_z0k Flag to indicate whether to use z0 and k for calculations.
     * @return The synthesized CmlinModel with updated parameters.
     */
    private fun Synthesize(line: CmlinModel, use_z0k: Boolean): CmlinModel {
        var line = line
        val h: Double
        val er: Double
        val l: Double
        val maxiters: Int
        var z0: Double
        var w: Double
        var iters: Int
        var done: Boolean
        val electricalLength: Double // degree

        var s: Double
        var z0e: Double
        var z0o: Double
        var k: Double
        var delta: Double
        val cval: Double
        var err: Double
        var d: Double

        val AW: Double
        val F1: Double
        var F2: Double
        var F3: Double

        val ai = doubleArrayOf(1.0, -0.301, 3.209, -27.282, 56.609, -37.746)
        val bi = doubleArrayOf(0.020, -0.623, 17.192, -68.946, 104.740, -16.148)
        val ci = doubleArrayOf(0.002, -0.347, 7.171, -36.910, 76.132, -51.616)

        var i: Int
        var dw: Double
        var ds: Double
        var ze0: Double
        var ze1: Double
        var ze2: Double
        var dedw: Double
        var deds: Double
        var zo0: Double
        var zo1: Double
        var zo2: Double
        var dodw: Double
        var dods: Double

        electricalLength = line.phase

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
            /** use z0 and k to calculate z0e and z0o */
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
            //Log.v("CmlinCalculator", "k=" + Double.toString(k));
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

        // width relative convergence tolerance (mils) (set to 0.1 micron)
        // reltol = Constants.MICRON2MIL(0.1);
        maxiters = 50

        // Initial guess at a solution
        AW = exp(z0 * sqrt(er + 1.0) / 42.4) - 1.0
        F1 = 8.0 * sqrt(AW * (7.0 + 4.0 / er) / 11.0 + (1.0 + 1.0 / er) / 0.81) / AW

        F2 = 0.0
        i = 0
        while (i <= 5) {
            F2 = F2 + ai[i] * k.pow(i.toDouble())
            i++
        }

        F3 = 0.0
        i = 0
        while (i <= 5) {
            F3 = F3 + (bi[i] - ci[i] * (9.6 - er)) * (0.6 - k).pow(i.toDouble())
            i++
        }

        w = h * abs(F1 * F2)
        s = h * abs(F1 * F3)

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

                // estimate the new solution
                dw = -1.0 * ((ze0 - z0e) * dods - (zo0 - z0o) * deds) / d
                w = abs(w + dw)

                ds = ((ze0 - z0e) * dodw - (zo0 - z0o) * dedw) / d
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
        line.setMetalLength(
            line.metalLength * electricalLength / line.phase,
            "m"
        )

        /*
         * one last calculation and this time we find the loss too.
         */
        // line = Analysis(line);
        line.errorCode = ERROR.NO_ERROR
        return line
    }

    /**
     * Gets the analysis result for the given CmlinModel.
     * 
     * @param line The CmlinModel to analyze.
     * @return The analyzed CmlinModel with updated parameters.
     */
    fun getAnaResult(line: CmlinModel): CmlinModel {
        return Analysis(line)
    }

    /**
     * Gets the synthesis result for the given CmlinModel.
     * 
     * @param line The CmlinModel to synthesize.
     * @param use_z0k Flag to indicate whether to use z0 and k for calculations.
     * @return The synthesized CmlinModel with updated parameters.
     */
    fun getSynResult(line: CmlinModel, use_z0k: Boolean): CmlinModel {
        return Synthesize(line, use_z0k)
    }

    companion object {
        /**
         * Calculates the effective dielectric constant using Hammerstad and Jensen's method.
         * 
         * @param widthToHeight The width to height ratio.
         * @param dielectricConstant The dielectric constant.
         * @return The effective dielectric constant.
         */
        private fun EffectiveDielectricConstant_Effer(
            widthToHeight: Double,
            dielectricConstant: Double
        ): Double {
            val A: Double
            val B: Double
            // (4) from Hammerstad and Jensen
            A = (1.0 + (1.0 / 49.0) * ln(
                (widthToHeight.pow(4.0) + (widthToHeight / 52.0).pow(2.0))
                        / (widthToHeight.pow(4.0) + 0.432)
            ) + (1.0 / 18.7) * ln(1.0 + (widthToHeight / 18.1).pow(3.0)))

            // (5) from Hammerstad and Jensen
            B = 0.564 * ((dielectricConstant - 0.9) / (dielectricConstant + 3.0)).pow(0.053)

            // zero frequency effective permitivity. (3) from Hammerstad and Jensen. This is
            // ee(ur,er) thats used by (9) in Hammerstad and Jensen.
            return ((dielectricConstant + 1.0) / 2.0
                    + ((dielectricConstant - 1.0) / 2.0) * (1.0 + 10.0 / widthToHeight).pow((-A * B)))
        }

        /**
         * Calculates the characteristic impedance using Hammerstad and Jensen's method.
         * 
         * @param widthToHeight The width to height ratio.
         * @return The characteristic impedance.
         */
        private fun CharacteristicImpedance_Z0(widthToHeight: Double): Double {
            val F: Double
            val z01: Double

            // (2) from Hammerstad and Jensen. 'u' is the normalized width
            F = 6.0 + (2.0 * Constants.Pi - 6.0) * exp(-(30.666 / widthToHeight).pow(0.7528))

            // (1) from Hammerstad and Jensen
            // TODO XXX decide on which to use here
            // z01 = (Constants.FREESPACEZ0 / (2 * Constants.Pi)) * Math.log(F / u +
            // Math.sqrt(1.0 + Math.pow((2 / u), 2.0)));
            z01 = ((377.0 / (2 * Constants.Pi))
                    * ln(F / widthToHeight + sqrt(1.0 + (2 / widthToHeight).pow(2.0))))
            return z01
        }
    }
}
