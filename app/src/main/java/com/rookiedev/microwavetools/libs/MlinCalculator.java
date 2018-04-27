package com.rookiedev.microwavetools.libs;

public class MlinCalculator {
    private double effectiveEr;

    public MlinCalculator() {
    }

    private MlinModel Analysis(MlinModel line) {
        double width, length, height, er, thickness;
        double impedance, electricalLength;
        double thicknessToHeight, widthToHeight;

        double u1, ur, deltau1, deltaur;
        double E0, EFF0;
        double fn; // normalized frequency
        double P1, P2, P3, P4, P, EF;
        double R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16, R17;
        double v;

        width = line.getMetalWidth();
        length = line.getMetalLength();
        // SubstrateModel dielectric thickness
        height = line.getSubHeight();
        // SubstrateModel relative permittivity
        er = line.getSubEpsilon();
        // MetalModel thickness
        thickness = line.getMetalThick();

        // starting microstrip_calc_int() with %f/1.0e6 MHz and Find u and correction factor for nonzero metal thickness
        widthToHeight = width / height;
        if (thickness > 0.0) {
            // find normalized metal thickness
            thicknessToHeight = thickness / height;
            // (6) from Hammerstad and Jensen
            deltau1 = (thicknessToHeight / Constant.Pi)
                    * Math.log(1.0 + 4.0 * Math.exp(1.0)
                    / (thicknessToHeight * Math.pow(Math.cosh(Math.sqrt(6.517 * widthToHeight))
                    / Math.sinh(Math.sqrt(6.517 * widthToHeight)), 2.0)));
            // (7) from Hammerstad and Jensen
            deltaur = 0.5 * (1.0 + 1.0 / Math.cosh(Math.sqrt(er - 1.0))) * deltau1;
        } else {
            deltau1 = 0.0;
            deltaur = 0.0;
        }

        // relative permittivity at f=0 (Hammerstad and Jensen)
        // (3) from Hammerstad & Jensen and Y from the Rogers Corp. paper
        u1 = widthToHeight + deltau1;
        ur = widthToHeight + deltaur;
        E0 = EffectiveDielectricConstant_Effer(ur, er);

        // zero frequency characteristic impedance
        // (8) from Hammerstad and Jensen
        impedance = CharacteristicImpedance_Z0(ur) / Math.sqrt(E0);

        // zero frequency effective permitivity.
        // (9) from Hammerstad and Jensen
        EFF0 = E0 * Math.pow(CharacteristicImpedance_Z0(u1) / CharacteristicImpedance_Z0(ur), 2.0);

        // relative permittivity including dispersion (Kirschning and Jansen)
        // normalized frequency (GHz-cm)
        fn = 1e-7 * line.getFrequency() * height;

        // (2) from Kirschning and Jansen
        P1 = 0.27488 +
                (0.6315 + (0.525 / (Math.pow((1.0 + 0.157 * fn), 20.0)))) * widthToHeight
                - 0.065683 * Math.exp(-8.7513 * widthToHeight);
        P2 = 0.33622 * (1.0 - Math.exp(-0.03442 * er));
        P3 = 0.0363 * Math.exp(-4.6 * widthToHeight) * (1.0 - Math.exp(-Math.pow((fn / 3.87), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((er / 15.916), 8.0)));
        P = P1 * P2 * Math.pow(((0.1844 + P3 * P4) * 10.0 * fn), 1.5763);

        // (1) from Kirschning and Jansen
        EF = (EFF0 + er * P) / (1.0 + P);

        // Characteristic Impedance (Jansen and Kirschning)
        // normalized frequency (GHz-mm)
        fn = 1.0e-6 * line.getFrequency() * height;

        // (1) from Jansen and Kirschning
        R1 = 0.03891 * Math.pow(er, 1.4);
        R2 = 0.267 * Math.pow(widthToHeight, 7.0);
        R3 = 4.766 * Math.exp(-3.228 * Math.pow(widthToHeight, 0.641));
        R4 = 0.016 + Math.pow((0.0514 * er), 4.524);
        R5 = Math.pow((fn / 28.843), 12.0);
        R6 = 22.20 * Math.pow(widthToHeight, 1.92);

        // (2) from Jansen and Kirschning
        R7 = 1.206 - 0.3144 * Math.exp(-R1) * (1.0 - Math.exp(-R2));
        R8 = 1.0 + 1.275 * (1.0 -
                Math.exp(-0.004625 * R3 *
                        Math.pow(er, 1.674) *
                        Math.pow(fn / 18.365, 2.745)));
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4)) * (Math.exp(-R6) / (1.0 + 1.2992 * R5));
        R9 = R9 * Math.pow((er - 1.0), 6.0) / (1.0 + 10.0 * Math.pow((er - 1), 6.0));

        // (3) from Jansen and Kirschning
        R10 = 0.00044 * Math.pow(er, 2.136) + 0.0184;
        R11 = Math.pow((fn / 19.47), 6.0) / (1.0 + 0.0962 * Math.pow((fn / 19.47), 6.0));
        R12 = 1.0 / (1.0 + 0.00245 * widthToHeight * widthToHeight);

        // (4) from Jansen and Kirschning
        R13 = 0.9408 * Math.pow(EF, R8) - 0.9603;
        R14 = (0.9408 - R9) * Math.pow(EFF0, R8) - 0.9603;
        R15 = 0.707 * R10 * Math.pow((fn / 12.3), 1.097);
        R16 = 1.0 + 0.0503 * er * er * R11 * (1.0 - Math.exp(-Math.pow((widthToHeight / 15), 6.0)));
        R17 = R7 * (1.0 - 1.1241 * (R12 / R16) * Math.exp(-0.026 * Math.pow(fn, 1.15656) - R15));

        // (5) from Jansen and Kirschning
        impedance = impedance * Math.pow((R13 / R14), R17);

        // propagation velocity (meters/sec)
        v = Constant.LIGHTSPEED / Math.sqrt(EF);

        // length in wavelengths
        if (line.getFrequency() > 0.0) {
            electricalLength = (length) / (v / line.getFrequency());
        } else {
            electricalLength = 0.0;
        }

        // convert to degrees
        electricalLength = 360.0 * electricalLength;

        // effective relative permittivity
        effectiveEr = EF;

        //this.effectiveEr = effectiveEr;
        //MLINLine.setkEff(eeff);
        line.setElectricalLength(electricalLength);

        //  store results
        line.setImpedance(impedance);
        return line;
    }

    /*
    *  Synthesize microstrip transmission line from electrical parameters
    *
    *  calculates:
    *    w     = microstrip line width (mils)
    *    l     = microstrip line length (mils)
    *    loss  = insertion loss (dB)
    *    eeff  = effective relative permitivity
    *
    *  from:
    *    z0    = characteristic impedance (ohms)
    *    len   = electrical length (degrees)
    *    f     = frequency (Hz)
    *    subs  = substrate parameters.  See TRSUBS for details.
    *
    *                |<--W-->|
    *                 _______
    *                | metal |
    *   ----------------------------------------------
    *  (  dielectric,er                      /|\     (
    *   )                                 H   |       )
    *  (                                     \|/     (
    *   ----------------------------------------------
    *   /////////////////ground///////////////////////
    *
    */
    private MlinModel Synthesize(MlinModel line, int flag) {
        double length;
        double impedance;
        double v, electricalLength;

        // the optimization variables, current, min/max, and previous values
        double var = 0, varMax = 0, varMin = 0, varOld = 0;

        // errors due to the above values for the optimization variable
        double err = 0, errmax = 0, errmin = 0, errold = 0;

        // derivative
        double deriv;

        // the sign of the slope of the function being optimized
        double sign = 0;

        // number of iterations so far, and max number allowed
        int iters = 0;
        int maxiters = 100;

        // convergence parameters
        double abstol = 0.1e-6;
        double reltol = 0.01e-6;

        // flag to end optimization
        boolean done = false;

        /*
        * figure out what parameter we're synthesizing and set up the
        * various optimization parameters.
        *
        * Basically what we need to know are
        *    1)  min/max values for the parameter
        *    2)  how to access the parameter
        *    3)  an initial guess for the parameter
        */

        switch (flag) {
            case Constant.Synthesize_Width:
                varMax = 100.0 * line.getSubHeight();
                varMin = 0.01 * line.getSubHeight();
                var = line.getSubHeight();
                break;

            case Constant.Synthesize_Height:
                varMax = 100.0 * line.getMetalWidth();
                varMin = 0.01 * line.getMetalWidth();
                var = line.getMetalWidth();
                break;

            case Constant.Synthesize_Er:
                varMax = 100.0;
                varMin = 1.0;
                var = 5.0;
                break;

            case Constant.Synthesize_Length:
                varMax = 100.0;
                varMin = 1.0;
                var = 5.0;
                done = true;
                break;

            default:
                break;
        }

        // read values from the input line structure
        electricalLength = line.getElectricalLength();
        impedance = line.getImpedance();

        //temp value for l used while synthesizing the other parameters. We'll correct l later.
        length = 1000.0;
        line.setMetalLength(length, Constant.LengthUnit_m);

        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varMin, flag);
            line = Analysis(line);
            errmin = line.getImpedance() - impedance;

            line.setSynthesizeParameter(varMax, flag);
            line = Analysis(line);
            errmax = line.getImpedance() - impedance;

            line.setSynthesizeParameter(var, flag);
            line = Analysis(line);
            err = line.getImpedance() - impedance;

            varOld = 0.99 * var;
            line.setSynthesizeParameter(varOld, flag);
            line = Analysis(line);
            errold = line.getImpedance() - impedance;

            // see if we've actually been able to bracket the solution
            if (errmax * errmin > 0) {
                //alert("Could not bracket the solution.\n"
                //        "Synthesis failed.\n");
                //return -1;
            }

            // figure out the slope of the error vs variable
            if (errmax > 0)
                sign = 1.0;
            else
                sign = -1.0;

            iters = 0;
        }

        // the actual iterations
        while (!done) {

            // update the interation count
            iters = iters + 1;

            // calculate an estimate of the derivative
            deriv = (err - errold) / (var - varOld);

            // copy over the current estimate to the previous one
            varOld = var;
            errold = err;

            // try a quasi-newton iteration
            var = var - err / deriv;

            /*
            * see if the new guess is within our bracketed range.  If so,
            * accept the new estimate.  If not, toss it out and do a
            * bisection step to reduce the bracket.
            */
            if ((var > varMax) || (var < varMin)) {
                var = (varMin + varMax) / 2.0;
            }

            // update the error value
            line.setSynthesizeParameter(var, flag);
            line = Analysis(line);
            err = line.getImpedance() - impedance;

            // update our bracket of the solution.
            if (sign * err > 0) {
                varMax = var;
            } else {
                varMin = var;
            }

            // check to see if we've converged
            if (Math.abs(err) < abstol) {
                done = true;
            } else if (Math.abs((var - varOld) / var) < reltol) {
                done = true;
            } else if (iters >= maxiters) {
                //alert("Synthesis failed to converge in\n"
                //        "%d iterations\n", maxiters);
                //return -1;
            }
            // done with iteration
        }

        // velocity on line
        line = Analysis(line);
        v = Constant.LIGHTSPEED / Math.sqrt(effectiveEr);
        length = (electricalLength / 360) * (v / line.getFrequency());
        line.setMetalLength(length, Constant.LengthUnit_m);

        return line;
    }

    private static double EffectiveDielectricConstant_Effer(double widthToHeight, double dielectricConstant) {
        double A, B, E0;

        // (4) from Hammerstad and Jensen
        A = 1.0 + (1.0 / 49.0)
                * Math.log((Math.pow(widthToHeight, 4.0) + Math.pow((widthToHeight / 52.0), 2.0)) / (Math.pow(widthToHeight, 4.0) + 0.432))
                + (1.0 / 18.7) * Math.log(1.0 + Math.pow((widthToHeight / 18.1), 3.0));

        // (5) from Hammerstad and Jensen
        B = 0.564 * Math.pow(((dielectricConstant - 0.9) / (dielectricConstant + 3.0)), 0.053);

        // zero frequency effective permitivity.
        // (3) from Hammerstad and Jensen.
        // This is ee(ur,er) that is used by (9) in Hammerstad and Jensen.
        E0 = (dielectricConstant + 1.0) / 2.0 + ((dielectricConstant - 1.0) / 2.0) * Math.pow((1.0 + 10.0 / widthToHeight), (-A * B));

        return E0;
    }

    // Characteristic impedance from (1) and (2) in Hammerstad and Jensen
    private static double CharacteristicImpedance_Z0(double widthToHeight) {
        double F, z01;

        // (2) from Hammerstad and Jensen.  'u' is the normalized width
        F = 6.0 + (2.0 * Constant.Pi - 6.0) * Math.exp(-Math.pow((30.666 / widthToHeight), 0.7528));

        // (1) from Hammerstad and Jensen
        z01 = (Constant.FREESPACEZ0 / (2 * Constant.Pi)) * Math.log(F / widthToHeight + Math.sqrt(1.0 + Math.pow((2 / widthToHeight), 2.0)));

        return z01;
    }

    public MlinModel getAnaResult(MlinModel MLINLine) {
        return Analysis(MLINLine);
    }

    public MlinModel getSynResult(MlinModel MLINLine, int flag) {
        return Synthesize(MLINLine, flag);
    }
}
