package com.rookiedev.microwavetools.libs;

public class SlinCalculator {

    public SlinCalculator() {
    }

    private SlinModel Analysis(SlinModel line) {
        // calculation variables
        double k, kp, r, kf, z0, m, deltaW, A, v;

        double frequency;

        frequency = line.getFrequency();

        // Characteristic Impedance
        if (line.getMetalThick() < line.getSubHeight() / 1000) {

            // Thin strip case:
            k = 1 / Math.cosh(Math.PI * line.getMetalWidth() / (2 * line.getSubHeight()));

            //  compute K(k)/K'(k) where K is the complete elliptic integral of the first kind, K' is the complementary complete elliptic integral of the first kind
            kp = Math.sqrt(1.0 - Math.pow(k, 2.0));
            r = 1.0;
            kf = (1.0 + k) / (1.0 + kp);
            while (kf != 1.0) {
                r = r * kf;
                k = 2.0 * Math.sqrt(k) / (1.0 + k);
                kp = 2.0 * Math.sqrt(kp) / (1.0 + kp);
                kf = (1.0 + k) / (1.0 + kp);
            }

            z0 = 29.976 * Math.PI * r / Math.sqrt(line.getSubEpsilon());
        } else {
            // Finite strip case:
            m = 6.0 * (line.getSubHeight() - line.getMetalThick()) / (3.0 * line.getSubHeight() - line.getMetalThick());
            deltaW = (line.getMetalThick() / Math.PI) *
                    (1.0 - 0.5 * Math.log(
                            Math.pow((line.getMetalThick() / (2 * line.getSubHeight() - line.getMetalThick())), 2.0) +
                                    Math.pow((0.0796 * line.getMetalThick() / (line.getMetalWidth() + 1.1 * line.getMetalThick())), m)));
            A = 4.0 * (line.getSubHeight() - line.getMetalThick()) / (Math.PI * (line.getMetalWidth() + deltaW));
            z0 = (30 / Math.sqrt(line.getSubEpsilon())) * Math.log(1.0 + A * (2.0 * A + Math.sqrt(4.0 * A * A + 6.27)));
        }

        // Electrical Length
        // propagation velocity (meters/sec)
        v = Constant.LIGHTSPEED / Math.sqrt(line.getSubEpsilon());
        line.setElectricalLength(360 * line.getMetalLength() * frequency / v);

        //  store results
        line.setImpedance(z0);
        return line;
    }

    /*
 *  The dielectric thickness above the stripline is assumed
 *  to be the same as below the stripline
 *
 *   /////////////////ground///////////////////////
 *   ----------------------------------------------
 *  (  dielectric,er         \/           /|\     (
 *   )             -------   --            |       )
 *  (             | metal | Tmet           | H    (
 *   )             -------   --            |       )
 *  (             <---W--->  /\           \|/     (
 *   ----------------------------------------------
 *   /////////////////ground///////////////////////
 *
 */


    private SlinModel Synthesize(SlinModel line, int flag) {
        double l;
        double v, len;
        double impedance;

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
            case Constant.Synthesize_Length:
                varmax = 100.0;
                varmin = 1.0;
                var = 5.0;
                //done = 1;
                break;
            default:
                //fprintf(stderr,"stripline_synth():  illegal flag=%d\n",flag);
                //exit(1);
                break;
        }

        // read values from the input line structure
        len = line.getElectricalLength();
        impedance = line.getImpedance();

        // temp value for l used while synthesizing the other header_parameters.
        l = 1000.0;
        line.setMetalLength(l, Constant.LengthUnit_m);

        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varmin, flag);
            line = Analysis(line);
            errmin = line.getImpedance() - impedance;

            line.setSynthesizeParameter(varmax, flag);
            line = Analysis(line);
            errmax = line.getImpedance() - impedance;

            line.setSynthesizeParameter(var, flag);
            line = Analysis(line);
            err = line.getImpedance() - impedance;

            varold = 0.99 * var;
            line.setSynthesizeParameter(varold, flag);
            line = Analysis(line);
            errold = line.getImpedance() - impedance;

            // see if we've actually been able to bracket the solution
            if (errmax * errmin > 0) {
                //alert("Could not bracket the solution.\n"
                //        "Synthesis failed.\n"MLINLine);
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
            line = Analysis(line);
            err = line.getImpedance() - impedance;

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
                //alert("Synthesis failed to converge in\n"
                //        "%d iterations\n", maxiters);
                //return -1;
            }
            // done with iteration
        }

        // velocity on line
        line = Analysis(line);

        v = Constant.LIGHTSPEED / Math.sqrt(line.getSubEpsilon());
        line.setMetalLength((len / 360) * (v / line.getFrequency()), Constant.LengthUnit_m);

        return line;
    }

    public SlinModel getAnaResult(SlinModel line) {
        return Analysis(line);
    }

    public SlinModel getSynResult(SlinModel line, int flag) {
        return Synthesize(line, flag);
    }

}
