package com.rookiedev.microwavetools.libs;

public class CpwCalculator {

    public CpwCalculator() {

    }

    private double k_over_kp(double k) {
        double kp, r, kf;
        int i = 0;

        kp = Math.sqrt(1.0 - Math.pow(k, 2.0));
        r = 1.0;
        do {
            kf = (1.0 + k) / (1.0 + kp);
            r = r * kf;
            k = 2.0 * Math.sqrt(k) / (1.0 + k);
            kp = 2.0 * Math.sqrt(kp) / (1.0 + kp);
            i++;
        } while ((Math.abs(kf - 1.0) > 1e-15) && (i < 20));

/* alternate approach
  if( k < sqrt(0.5) ) {
    kp = sqrt(1.0 - k*k);
    r = M_PI / log(2.0 * (1.0 + sqrt(kp)) / (1.0 - sqrt(kp)) );
  } else {
    r = log(2.0 * (1.0 + sqrt(k)) / (1.0 - sqrt(k)) ) / M_PI;
  }
*/
        return r;
    }

    private CpwModel Analysis(CpwModel line, boolean withGround) {

        // calculation variables
        double k, k1, kt, z0, v, loss;
        double k_kp, k_kp1, k_kpt;
        double a, at, b, bt, eeff;

        double keff;

        if (!withGround) {
            // These equations are _without_ the bottom side ground plane.
            // match the notation in Wadell
            a = line.getMetalWidth();
            b = line.getMetalWidth() + 2.0 * line.getMetalSpace();

            // Wadell (3.4.1.8), (3.4.1.9) but avoid issues with tmet = 0
            if (line.getMetalThick() > 0.0) {
                at = a + (1.25 * line.getMetalThick() / Math.PI) * (1.0 + Math.log(4.0 * Math.PI * a / line.getMetalThick()));
                bt = b - (1.25 * line.getMetalThick() / Math.PI) * (1.0 + Math.log(4.0 * Math.PI * a / line.getMetalThick()));
            } else {
                at = a;
                bt = b;
            }

    /*
     * we have to be a bit careful here.  If we're not careful, we can
     * end up with b < a which is nonsense and it will cause the
     * calculation of the elliptic integral ratios to produce NaN
     */
            if (bt <= at) {
                at = a;
                bt = b;
                //alert("Warning:  bt <= at so I am reverting to zero thickness equations\n");
            }

            // Wadell (3.4.1.6)
            k1 = Math.sinh(Math.PI * at / (4.0 * line.getSubHeight())) / Math.sinh(Math.PI * bt / (4.0 * line.getSubHeight()));

            // Wadell (3.4.1.4), (3.4.1.5)
            k = a / b;
            kt = at / bt;

            k_kp = k_over_kp(k);
            k_kp1 = k_over_kp(k1);
            k_kpt = k_over_kp(kt);

            // Wadell (3.4.1.3)
            eeff = 1.0 + 0.5 * (line.getSubEpsilon() - 1.0) * k_kp1 / k_kp;

            // Wadell (3.4.1.2)
            keff = eeff - (eeff - 1.0) / ((0.5 * (b - a) / (0.7 * line.getMetalThick())) * k_kp + 1.0);

            // for coplanar waveguide (ground signal ground)
            z0 = Constant.FREESPACEZ0 / (4.0 * Math.sqrt(keff) * k_kpt);
        } else {
    /*
     * These equations are _with_ the bottom side ground plane.
     *
     * See Wadell, eq 3.4.3.1 through 3.4.3.6 on p. 79
     */

            // FIXME -- surely these are not accurate without accounting for metal thickness...
            k = line.getMetalWidth() / (line.getMetalWidth() + 2.0 * line.getMetalSpace());
            k1 = Math.tanh(Math.PI * line.getMetalWidth() / (4.0 * line.getSubHeight())) /
                    Math.tanh(Math.PI * (line.getMetalWidth() + 2.0 * line.getMetalSpace()) / (4.0 * line.getSubHeight()));
            k_kp = k_over_kp(k);
            k_kp1 = k_over_kp(k1);

            keff = (1.0 + line.getSubEpsilon() * k_kp1 / k_kp) /
                    (1.0 + k_kp1 / k_kp);

            z0 = (Constant.FREESPACEZ0 / (2.0 * Math.sqrt(keff))) / (k_kp + k_kp1);
        }

        // Electrical Length
        // propagation velocity (meters/sec)
        v = Constant.LIGHTSPEED / Math.sqrt(keff);
        line.setElectricalLength(360 * line.getMetalLength() * line.getFrequency() / v);

        //  store results
        line.setImpedance(z0);
        return line;
    }

    private CpwModel Synthesize(CpwModel line, int flag, boolean withGround) {
        double Ro, Xo;
        double v, len;

        // the optimization variables, current, min/max, and previous values
        double var = 0, varmax = 0, varmin = 0, varold = 0;

        // errors due to the above values for the optimization variable
        double err = 0, errmax = 0, errmin = 0, errold = 0;

        // derivative
        double deriv;

        // the sign of the slope of the function being optimized
        double sign = 0;

        // number of iterations so far, and max number allowed
        int iters = 0;
        int maxiters = 100;

        // convergence header_parameters
        double abstol = 0.1e-6;
        double reltol = 0.01e-6;

        // flag to end optimization
        boolean done = false;

  /*
   * figure out what parameter we're synthesizing and set up the
   * various optimization header_parameters.
   *
   * Basically what we need to know are
   *    1)  min/max values for the parameter
   *    2)  how to access the parameter
   *    3)  an initial guess for the parameter
   */

        switch (flag) {
            case Constant.Synthesize_Width:
                varmax = 100.0 * line.getSubHeight();
                varmin = 0.01 * line.getSubHeight();
                var = line.getSubHeight();
                break;
            case Constant.Synthesize_Gap:
                varmax = 100.0 * line.getSubHeight();
                varmin = 0.01 * line.getSubHeight();
                var = line.getSubHeight();
                break;
            case Constant.Synthesize_Height:
                varmax = 100.0 * line.getMetalWidth();
                varmin = 0.01 * line.getMetalWidth();
                var = line.getMetalWidth();
                break;
            case Constant.Synthesize_Er:
                varmax = 100.0;
                varmin = 1.0;
                var = 5.0;
                break;
            default:
                //fprintf(stderr,"coplanar_synth():  illegal flag=%d\n",flag);
                //exit(1);
                break;
        }

        // read values from the input line structure
        Ro = line.getImpedance();

  /*
   * temp value for l used while synthesizing the other header_parameters.
   * We'll correct l later.
   */
        len = line.getElectricalLength();  /* remember what electrical length we want */
        line.setMetalLength(1.0, Constant.LengthUnit_m);

        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varmin, flag);
            line = Analysis(line, withGround);
            errmin = line.getImpedance() - Ro;

            line.setSynthesizeParameter(varmax, flag);
            line = Analysis(line, withGround);
            errmax = line.getImpedance() - Ro;

            line.setSynthesizeParameter(var, flag);
            line = Analysis(line, withGround);
            err = line.getImpedance() - Ro;

            varold = 0.99 * var;
            line.setSynthesizeParameter(varold, flag);
            line = Analysis(line, withGround);
            errold = line.getImpedance() - Ro;

            // see if we've actually been able to bracket the solution
            if (errmax * errmin > 0) {
                /*alert("Could not bracket the solution.\n"
                        "Synthesis failed.\n");
                return -1;*/
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
            deriv = (err - errold) / (var - varold);

            // copy over the current estimate to the previous one
            varold = var;
            errold = err;

            // try a quasi-newton iteration
            var = var - err / deriv;

    /*
     * see if the new guess is within our bracketed range.  If so,
     * accept the new estimate.  If not, toss it out and do a
     * bisection step to reduce the bracket.
     */

            if ((var > varmax) || (var < varmin)) {
                var = (varmin + varmax) / 2.0;
            }

            // update the error value
            line.setSynthesizeParameter(var, flag);
            line = Analysis(line, withGround);
            err = line.getImpedance() - Ro;

            // update our bracket of the solution.
            if (sign * err > 0)
                varmax = var;
            else
                varmin = var;

            // check to see if we've converged
            if (Math.abs(err) < abstol) {
                done = true;
            } else if (Math.abs((var - varold) / var) < reltol) {
                done = true;
            } else if (iters >= maxiters) {
                /*alert("Synthesis failed to converge in\n"
                        "%d iterations.  Final optimization header_parameters:\n"
                        "  min = %g\n"
                        "  val = %g\n"
                        "  max = %g\n", maxiters, varmin, var, varmax);
                return -1;*/
            }
            // done with iteration
        }

        // velocity on line
        line = Analysis(line, withGround);

        //v = Constant.LIGHTSPEED / Math.sqrt(line.getSubEpsilon());
        line.setMetalLength(line.getMetalLength() * len / line.getElectricalLength(), Constant.LengthUnit_m);

        return line;
    }

    public CpwModel getAnaResult(CpwModel line, boolean withGround) {
        return Analysis(line, withGround);
    }

    public CpwModel getSynResult(CpwModel line, int flag, boolean withGround) {
        return Synthesize(line, flag, withGround);
    }

}
