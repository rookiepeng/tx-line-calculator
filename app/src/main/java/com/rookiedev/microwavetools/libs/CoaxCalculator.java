package com.rookiedev.microwavetools.libs;

public class CoaxCalculator {

    public CoaxCalculator() {
    }

    private CoaxModel Analysis(CoaxModel line) {
        double x;
        double v;

        // qualify the inputs some
        if (line.getSubRadius() <= line.getCoreRadius()) {
            /*alert("Error: b (%g) must be > a (%g)\r\n"
                    "for a coax_fragment line\r\n");
            return -1;*/
        }
        if (line.getCoreOffset() >= line.getSubRadius() - line.getCoreRadius()) {
            /*alert("Error:  c (%g)  must be < b - a (%g)\r\n"
                    "for a coax_fragment line\r\n",line->c,line->b-line->a);
            return -1;*/
        }

  /*
   * find characteristic impedance (from Rosloniec)
   *
   * Note that Rosloniec deals in diameters while I deal
   * in radii.  Also, Rosloniec swaps a and b from the notation I'm
   * using here.
   * x = (b + [a^2 - 4c^2]/b)/(2a) is on p. 184 of Rosloniec.
   *
   * substituting b = 2a, a=2b gives
   *
   * x = (2a + [4b^2 - 4c^2]/2a)/(4b)
   *   = ( a + [2b^2 - 2c^2]/2a)/(2b)
   *   = ( a + [ b^2 -  c^2]/ a)/(2b)
   */
        x = (line.getCoreRadius() + (Math.pow(line.getSubRadius(), 2.0) - Math.pow(line.getCoreOffset(), 2.0)) / line.getCoreRadius()) /
                (2 * line.getSubRadius());

        line.setImpedance((1 / (2 * Math.PI)) * Math.sqrt(Constant.FREESPACE_MU0 / (Constant.FREESPACE_E0 * line.getSubEpsilon())) * Math.log(x + Math.sqrt(x * x - 1)));

        // find velocity (meters/second)
        v = 1.0 / Math.sqrt(Constant.FREESPACE_MU0 * Constant.FREESPACE_E0 * line.getSubEpsilon());

        // electrical length 2*pi*f*(180/pi) = 360*f
        line.setElectricalLength(360.0 * line.getFrequency() * line.getMetalLength() / v);

        return line;
    }

    private CoaxModel Synthesize(CoaxModel line, int flag) {
        double Ro;
        double v;
        double elen;

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
            case Constant.Synthesize_CoreRadius:
                varmax = 0.999 * line.getSubRadius();
                varmin = 0.001 * line.getSubRadius();
                var = 0.2 * line.getSubRadius();
                break;
            case Constant.Synthesize_Height:
                varmax = 1000.0 * line.getCoreRadius();
                varmin = 1.001 * line.getCoreRadius();
                var = 5 * line.getCoreRadius();
                break;
            case Constant.Synthesize_CoreOffset:
                varmax = 0.999 * (line.getSubRadius() - line.getCoreRadius());
                varmin = 0;
                var = 0.1 * varmax;
                break;
            case Constant.Synthesize_Er:
                varmax = 100.0;
                varmin = 1.0;
                var = 5.0;
                break;
            default:
                //fprintf(stderr,"coax_syn():  illegal flag=%d\n",flag);
                //exit(1);
                break;
        }


        // read values from the input line structure
        Ro = line.getImpedance();
        elen = line.getElectricalLength();

        // temp value for len used while synthesizing the other header_parameters.
        line.setMetalLength(1.0, Constant.LengthUnit_m);

        if (!done) {
            // Initialize the various error values
            line.setSynthesizeParameter(varmin, flag);
            line = Analysis(line);
            errmin = line.getImpedance() - Ro;

            line.setSynthesizeParameter(varmax, flag);
            line = Analysis(line);
            errmax = line.getImpedance() - Ro;

            line.setSynthesizeParameter(var, flag);
            line = Analysis(line);
            err = line.getImpedance() - Ro;

            varold = 0.99 * var;
            line.setSynthesizeParameter(varold, flag);
            line = Analysis(line);
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
            line = Analysis(line);
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
                        "%d iterations\n", maxiters);
                return -1;*/
            }
            // done with iteration
        }

        // velocity on line
        line = Analysis(line);

        v = Constant.LIGHTSPEED / Math.sqrt(line.getSubEpsilon());
        line.setMetalLength((elen / 360) * (v / line.getFrequency()), Constant.LengthUnit_m);

        return line;
    }

    public CoaxModel getAnaResult(CoaxModel line) {
        return Analysis(line);
    }

    public CoaxModel getSynResult(CoaxModel line, int flag) {
        return Synthesize(line, flag);
    }
}
