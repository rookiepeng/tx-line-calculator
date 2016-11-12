package com.rookiedev.microwavetools.libs;

import android.util.Log;

public class MLIN {
    private Line MLINLine;

    public MLIN(Line mlinLine) {
        MLINLine = mlinLine;
    }

    private Line microstripAna(Line MLINLine, int flag) {
        // flag=1 enables loss calculations
        // flag=0 disables loss calculations
        double w, l;
        double h, er, rho, tand, t, rough;
        double T;
        double u, deltau;
        double u1, ur, deltau1, deltaur;
        double E0, EFF0;
        double fn;
        double P1, P2, P3, P4, P, EF;
        double z0;
        double R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16, R17;
        double v;
        double z1, z2, z3, z4, z5, deltal, len;
        double eeff;
        double ld, mu, delta, depth;
        double lc, Res, loss;
        double sigma;
        double L, R, C, G;
        double delay;

        w = MLINLine.getMetalWidth();
        l = MLINLine.getMetalLength();
        // Substrate dielectric thickness
        h = MLINLine.getSubHeight();
        // Substrate relative permittivity
        er = MLINLine.getSubEpsilon();
        // Metal resistivity
        rho = MLINLine.getRho();
        // Loss tangent of the dielectric material
        tand = MLINLine.getTand();
        // Metal thickness
        t = MLINLine.getMetalThick();
        // subs(6) = Metalization roughness
        rough = MLINLine.getRough();

        // starting microstrip_calc_int() with %f/1.0e6 MHz and
        // Find u and correction factor for nonzero metal thickness
        u = w / h;
        if (t > 0.0) {
            // find normalized metal thickness
            T = t / h;
            // (6) from Hammerstad and Jensen
            deltau1 = (T / Constant.Pi)
                    * Math.log(1.0 + 4.0 * Math.exp(1.0)
                    / (T * Math.pow(Math.cosh(Math.sqrt(6.517 * u))
                    / Math.sinh(Math.sqrt(6.517 * u)), 2.0)));
            // (7) from Hammerstad and Jensen
            deltaur = 0.5 * (1.0 + 1.0 / Math.cosh(Math.sqrt(er - 1.0))) * deltau1;
            deltau = deltaur;
        } else {
            deltau = 0.0;
            deltau1 = 0.0;
            deltaur = 0.0;
        }

        // relative permittivity at f=0 (Hammerstad and Jensen)
        // (3) from Hammerstad & Jensen and Y from the Rogers Corp. paper
        u1 = u + deltau1;
        ur = u + deltaur;
        E0 = ee_HandJ(ur, er);

        // zero frequency characteristic impedance
        // (8) from Hammerstad and Jensen
        z0 = z0_HandJ(ur) / Math.sqrt(E0);

        // zero frequency effective permitivity.
        // (9) from Hammerstad and Jensen
        EFF0 = E0 * Math.pow(z0_HandJ(u1) / z0_HandJ(ur), 2.0);

        // relative permittivity including dispersion (Kirschning and Jansen)
        // normalized frequency (GHz-cm)
        fn = 1e-7 * MLINLine.getFrequency() * h;

        // (2) from Kirschning and Jansen
        P1 = 0.27488 +
                (0.6315 + (0.525 / (Math.pow((1.0 + 0.157 * fn), 20.0)))) * u
                - 0.065683 * Math.exp(-8.7513 * u);
        P2 = 0.33622 * (1.0 - Math.exp(-0.03442 * er));
        P3 = 0.0363 * Math.exp(-4.6 * u) * (1.0 - Math.exp(-Math.pow((fn / 3.87), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((er / 15.916), 8.0)));
        P = P1 * P2 * Math.pow(((0.1844 + P3 * P4) * 10.0 * fn), 1.5763);

        // (1) from Kirschning and Jansen
        EF = (EFF0 + er * P) / (1.0 + P);

        // Characteristic Impedance (Jansen and Kirschning)
        // normalized frequency (GHz-mm)
        fn = 1.0e-6 * MLINLine.getFrequency() * h;

        // (1) from Jansen and Kirschning
        R1 = 0.03891 * Math.pow(er, 1.4);
        R2 = 0.267 * Math.pow(u, 7.0);
        R3 = 4.766 * Math.exp(-3.228 * Math.pow(u, 0.641));
        R4 = 0.016 + Math.pow((0.0514 * er), 4.524);
        R5 = Math.pow((fn / 28.843), 12.0);
        R6 = 22.20 * Math.pow(u, 1.92);

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
        R12 = 1.0 / (1.0 + 0.00245 * u * u);

        // (4) from Jansen and Kirschning
        R13 = 0.9408 * Math.pow(EF, R8) - 0.9603;
        R14 = (0.9408 - R9) * Math.pow(EFF0, R8) - 0.9603;
        R15 = 0.707 * R10 * Math.pow((fn / 12.3), 1.097);
        R16 = 1.0 + 0.0503 * er * er * R11 * (1.0 - Math.exp(-Math.pow((u / 15), 6.0)));
        R17 = R7 * (1.0 - 1.1241 * (R12 / R16) * Math.exp(-0.026 * Math.pow(fn, 1.15656) - R15));

        // (5) from Jansen and Kirschning
        z0 = z0 * Math.pow((R13 / R14), R17);

        // propagation velocity (meters/sec)
        v = Constant.LIGHTSPEED / Math.sqrt(EF);

        // delay on line
        delay = MLINLine.getMetalLength() / v;
        // End correction (Kirschning, Jansen, and Koster)

        /* DAN should decide what to do about this */
        z1 = 0.434907 * ((Math.pow(EF, 0.81) + 0.26) / (Math.pow(EF, 0.81) - 0.189))
                * (Math.pow(u, 0.8544) + 0.236) / (Math.pow(u, 0.8544) + 0.87);
        z2 = 1.0 + (Math.pow(u, 0.371)) / (2.358 * er + 1.0);
        z3 = 1.0 + (0.5274 * Math.atan(0.084 * (Math.pow(u, (1.9413 / z2))))) / (Math.pow(EF, 0.9236));
        z4 = 1.0
                + 0.0377 * Math.atan(0.067 * (Math.pow(u, 1.456))) * (6.0 - 5.0 * Math.exp(0.036 * (1.0 - er)));
        z5 = 1.0 - 0.218 * Math.exp(-7.5 * u);
        deltal = h * z1 * z3 * z5 / z4;

        // find the incremental circuit model
        // find L and C from the impedance and velocity
        // z0 = sqrt(L/C), v = 1/sqrt(LC)
        // this gives the result below
        L = z0 / v;
        C = 1.0 / (z0 * v);

        // resistance and conductance will be updated below
        R = 0.0;
        G = 0.0;

        // length in wavelengths
        if (MLINLine.getFrequency() > 0.0)
            len = (l) / (v / MLINLine.getFrequency());
        else
            len = 0.0;

        // convert to degrees
        len = 360.0 * len;

        // effective relative permittivity
        eeff = EF;
        Log.v("MLIN", "eeff=" + Double.toString(eeff));
        Log.v("MLIN", "l=" + Double.toString(l));
        Log.v("MLIN", "v=" + Double.toString(v));
        Log.v("MLIN", "freq=" + Double.toString(len));
        MLINLine.setkEff(eeff);
        MLINLine.setElectricalLength(len);

        // TODO Move this out
        if (flag == Constant.LOSSY) {
            // calculate loss
            // Dielectric Losses
            // loss in nepers/meter
        /*
        * The dielectric loss here matches equation (1) in the
	    * Denlinger paper although the form is slightly different.  In
	    * the Denlinger paper it is in dB/m.  Note that the 27.3 in
	    * the Denlinger paper is equal to pi * 20*log10( e ).
	    *
	    * See also equation (4.21) in Fooks and Zakarevicius.  The
	    * difference in form there is (4.21) uses c/sqrt(EF) in place
	    * of 'v' in our equation here.
	    *
	    *
	    * With a uniform dielectric, we would have this:
	    *
	    * G = 2 * M_PI * f * C * line->subs->tand;
	    *
	    * alpha_d = (G * Z0 / 2) * tand     (nepers/meter)
	    *
	    * but for the mixed air/dielectric that we have, the loss is
	    * less by a factor qd which is the filling factor.
	    * bu
	    */

            if (er > 1.0) {
                ld = (Constant.Pi * MLINLine.getFrequency() / v) * (er / EF) * ((EF - 1.0) / (er - 1.0)) * tand;
            } else {
                // if er == 1, then this is probably a vacuum
                ld = 0.0;
            }
            G = 2.0 * ld / z0;
            MLINLine.setAlphaD(ld);
            // loss in dB/meter
            ld = 20.0 * Math.log10(Math.exp(1.0)) * ld;
            // loss in dB
            ld = ld * l;
            // Conduction Losses
            // calculate skin depth
            // conductivity
            sigma = 1.0 / rho;
            // permeability of free space
            mu = 4.0 * Constant.Pi * 1e-7;
            // skin depth in meters
            delta = Math.sqrt(1.0 / (Constant.Pi * MLINLine.getFrequency() * mu * sigma));
            depth = delta;
            // warn the user if the loss calc is suspect.
            if (t < 3.0 * depth) {
                //alert("Warning:  The metal thickness is less than\n"
                //        "three skin depths.  Use the loss results with\n"
                //        "caution.\n");
            }

        /*
        * if the skinDepth is greater than Tmet, assume current
	    * flows uniformly through  the conductor.  Then loss
	    * is just calculated from the dc resistance of the
	    * trace.  This is somewhat
	    * suspect, but I dont have time right now to come up
	    * with a better result.
	    */


            Line MLINLineStore = MLINLine;
            //MLINLineStore=MLINLine;

            if (depth <= t) {
                MLINLineStore.setSubEpsilon(1.0);
                MLINLineStore = microstripAna(MLINLineStore, Constant.LOSSLESS);
                z2 = MLINLineStore.getImpedance();

                MLINLineStore.setSubHeight(h + delta, Line.LUnitm);
                MLINLineStore.setMetalThick(t - delta, Line.LUnitm);
                MLINLineStore.setMetalWidth(w - delta, Line.LUnitm);
                MLINLineStore = microstripAna(MLINLineStore, Constant.LOSSLESS);
                z1 = MLINLineStore.getImpedance();

                //MLINLine.setSubEpsilon(er);
                //MLINLine.setSubHeight(h, Line.LUnitm);
                //MLINLine.setMetalThick(t, Line.LUnitm);
                //MLINLine.setMetalWidth(w, Line.LUnitm);

                // conduction losses, nepers per meter
                lc = (Constant.Pi * MLINLineStore.getFrequency() / Constant.LIGHTSPEED) * (z1 - z2) / z0;

                R = lc * 2 * z0;
            }

            // "dc" case
            else if (t > 0.0) {
                // resistance per meter = 1/(Area*conductivity)
                R = 1 / (MLINLineStore.getMetalWidth() * MLINLineStore.getMetalThick() * sigma);
                // resistance per meter = 1/(Area*conductivity)
                Res = 1 / (w * t * sigma);
                // conduction losses, nepers per meter
                lc = Res / (2.0 * z0);
                // change delta to be equal to the metal thickness for use in surface roughness correction
                delta = t;
                // no conduction loss case
            } else {
                lc = 0.0;
            }

            /* factor due to surface roughness
            * note that the equation in Fooks and Zakarevicius is slightly
	        * errored.
	        * the correct equation is penciled in my copy and was
	        * found in Hammerstad and Bekkadal
	        */
            lc = lc * (1.0 + (2.0 / Constant.Pi) * Math.atan(1.4 * Math.pow((rough / delta), 2.0)));

            MLINLine.setAlphaC(lc);

            // recalculate R now that we have the surface roughness in place
            R = lc * 2.0 * z0;

            // loss in dB/meter
            lc = 20.0 * Math.log10(Math.exp(1.0)) * lc;

            // loss in dB
            lc = lc * l;

            // Total Loss
            loss = ld + lc;
        } else {
            loss = 0.0;
            depth = 0.0;
        }
        MLINLine.setLoss(loss);
        MLINLine.setLossLen(loss / MLINLine.getMetalLength());
        MLINLine.setSkinDepth(depth);
        //  store results
        MLINLine.setImpedance(z0);

        MLINLine.setDeltal(deltal);
        MLINLine.setDelay(delay);
        MLINLine.setLs(L);
        MLINLine.setRs(R);
        MLINLine.setCs(C);
        MLINLine.setGs(G);

        return MLINLine;
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
    public int microstrip_syn(int flag) {
        int rslt = 0;
        double l;
        //double Ro;
        double Xo, Z0;
        double v, len;
        double eeff;

        /* the parameters which define the structure */
        double w;
        double tmet;
        double h, es, tand;

        /* permeability and permitivity of free space */
        double mu0, e0;


        /* the optimization variables, current, min/max, and previous values */
        double var = 0, varmax = 0, varmin = 0, varold = 0;

        /* errors due to the above values for the optimization variable */
        double err = 0, errmax = 0, errmin = 0, errold = 0;

        /* derivative */
        double deriv;

        /* the sign of the slope of the function being optimized */
        double sign = 0;

        /* number of iterations so far, and max number allowed */
        int iters = 0;
        int maxiters = 100;

        /* convergence parameters */
        double abstol = 0.1e-6;
        double reltol = 0.01e-6;

        /* flag to end optimization */
        boolean done = false;

        /* permeability and permitivitty of free space (H/m and F/m) */
        mu0 = 4 * Constant.Pi * 1.0e-7;
        e0 = 1.0 / (mu0 * Constant.LIGHTSPEED * Constant.LIGHTSPEED);

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
            case Line.SYN_W:
                varmax = 100.0 * MLINLine.getSubHeight();
                varmin = 0.01 * MLINLine.getSubHeight();
                var = MLINLine.getSubHeight();
                break;

            case Line.SYN_H:
                varmax = 100.0 * MLINLine.getMetalWidth();
                varmin = 0.01 * MLINLine.getMetalWidth();
                var = MLINLine.getMetalWidth();
                break;

            case Line.SYN_Er:
                varmax = 100.0;
                varmin = 1.0;
                var = 5.0;
                break;

            case Line.SYN_L:
                varmax = 100.0;
                varmin = 1.0;
                var = 5.0;
                done = true;
                break;

            default:
                break;
        }

        // read values from the input line structure

        //Ro = MLINLine.getRo();
        //Log.v("MLIN", "Ro=" + Double.toString(Ro));
        Xo = MLINLine.getXo();
        len = MLINLine.getElectricalLength();
        Z0 = MLINLine.getImpedance();

        // Metal width, length, and thickness
        w = MLINLine.getMetalWidth();
        l = MLINLine.getMetalLength();
        tmet = MLINLine.getMetalThick();

        // Substrate thickness, relative permitivity, and loss tangent
        h = MLINLine.getSubHeight();
        es = MLINLine.getSubEpsilon();
        tand = MLINLine.getTand();

        //temp value for l used while synthesizing the other parameters. We'll correct l later.
        l = 1000.0;
        MLINLine.setMetalLength(l, Line.LUnitm);


        if (!done) {
            // Initialize the various error values
            MLINLine.setParameter(varmin, flag);
            MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);
            errmin = MLINLine.getImpedance() - Z0;

            MLINLine.setParameter(varmax, flag);
            MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);
            errmax = MLINLine.getImpedance() - Z0;

            MLINLine.setParameter(var, flag);
            MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);
            err = MLINLine.getImpedance() - Z0;

            varold = 0.99 * var;
            MLINLine.setParameter(varold, flag);
            MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);
            errold = MLINLine.getImpedance() - Z0;

            // see if we've actually been able to bracket the solution
            if (errmax * errmin > 0) {
                //alert("Could not bracket the solution.\n"
                //        "Synthesis failed.\n");
                return -1;
            }

            // figure out the slope of the error vs variable
            if (errmax > 0)
                sign = 1.0;
            else
                sign = -1.0;

            iters = 0;
        }

        /* the actual iterations */
        while (!done) {

            /* update the interation count */
            iters = iters + 1;

            /* calculate an estimate of the derivative */
            deriv = (err - errold) / (var - varold);

            /* copy over the current estimate to the previous one */
            varold = var;
            errold = err;

            /* try a quasi-newton iteration */
            var = var - err / deriv;

            /*
            * see if the new guess is within our bracketed range.  If so,
            * accept the new estimate.  If not, toss it out and do a
            * bisection step to reduce the bracket.
            */

            if ((var > varmax) || (var < varmin)) {
                //#ifdef DEBUG_SYN
                //printf("microstrip_syn():  Taking a bisection step\n");
                //#endif
                var = (varmin + varmax) / 2.0;
            }

            /* update the error value */
            MLINLine.setParameter(var, flag);
            MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);
            err = MLINLine.getImpedance() - Z0;
            //if (rslt)
            //    return rslt;


            /* update our bracket of the solution. */
            if (sign * err > 0) {
                varmax = var;
            } else {
                varmin = var;
            }

            /* check to see if we've converged */
            if (Math.abs(err) < abstol) {
                done = true;
                //printf("microstrip_syn():  abstol converged after iteration #%d\n",
                //        iters);
            } else if (Math.abs((var - varold) / var) < reltol) {
                done = true;
                //printf("microstrip_syn():  reltol converged after iteration #%d\n",
                //        iters);
            } else if (iters >= maxiters) {
                //alert("Synthesis failed to converge in\n"
                //        "%d iterations\n", maxiters);
                return -1;
            }
            // done with iteration
        }

        // velocity on line
        MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);

        eeff = MLINLine.getkEff();

        v = Constant.LIGHTSPEED / Math.sqrt(eeff);

        l = (len / 360) * (v / MLINLine.getFrequency());
        //Log.v("MLIN", "eeff=" + Double.toString(eeff));

        MLINLine.setMetalLength(l, Line.LUnitm);

        // recalculate using real length to find loss
        MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);

        return 0;
    }

    private static double ee_HandJ(double u, double er) {
        double A, B, E0;

        // (4) from Hammerstad and Jensen
        A = 1.0 + (1.0 / 49.0)
                * Math.log((Math.pow(u, 4.0) + Math.pow((u / 52.0), 2.0)) / (Math.pow(u, 4.0) + 0.432))
                + (1.0 / 18.7) * Math.log(1.0 + Math.pow((u / 18.1), 3.0));

        // (5) from Hammerstad and Jensen
        B = 0.564 * Math.pow(((er - 0.9) / (er + 3.0)), 0.053);

        // zero frequency effective permitivity.
        // (3) from Hammerstad and Jensen.
        // This is ee(ur,er) that is used by (9) in Hammerstad and Jensen.
        E0 = (er + 1.0) / 2.0 + ((er - 1.0) / 2.0) * Math.pow((1.0 + 10.0 / u), (-A * B));

        return E0;
    }

    /*
     * Characteristic impedance from (1) and (2) in Hammerstad and Jensen
     */
    private static double z0_HandJ(double u) {
        double F, z01;

        // (2) from Hammerstad and Jensen.  'u' is the normalized width
        F = 6.0 + (2.0 * Constant.Pi - 6.0) * Math.exp(-Math.pow((30.666 / u), 0.7528));

        // (1) from Hammerstad and Jensen
        z01 = (Constant.FREESPACEZ0 / (2 * Constant.Pi)) * Math.log(F / u + Math.sqrt(1.0 + Math.pow((2 / u), 2.0)));

        return z01;
    }

    public Line getAnaResult() {
        MLINLine = microstripAna(MLINLine, Constant.LOSSLESS);
        return MLINLine;
    }

    public Line getSynResult(int flag) {
        microstrip_syn(flag);
        return MLINLine;
    }
}
