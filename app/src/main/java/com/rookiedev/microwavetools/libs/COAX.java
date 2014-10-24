package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 8/21/13.
 */
public class COAX {
    private static double LIGHTSPEED = 2.99792458e8;
    private static double mu0 = 4 * Math.PI * 1.0e-7;
    private static double e0 = 1.0 / (mu0 * LIGHTSPEED * LIGHTSPEED);
    private double a, b, c, er, L, Z0, Eeff, f, T;
    private int flag;

    public COAX(double _a, double _b, double _c, double epsilon, double length, double impedance, double electricalLength, double frequency, double thick, int synFLag) {
        a = _a;
        b = _b;
        c = _c;
        er = epsilon;
        L = length;
        Z0 = impedance;
        Eeff = electricalLength;
        f = frequency;
        T = thick;
        flag = synFLag;
    }

    public double getZ0() {
        return Z0_calc(a, b, c, er);
    }

    public double getEeff() {
        return Eeff(L, er, f);
    }

    public double getL() {
        return L;
    }

    public double geta() {
        return a;
    }

    public double getb() {
        return b;
    }

    public double getc() {
        return c;
    }

    public double geter() {
        return er;
    }

    private double Z0_calc(double a, double b, double c, double er) {
        if (b <= a) {
            return -1;
        }
        if (c >= (b - a)) {
            return -1;
        }

		/*
         * find characteristic impedance (from Rosloniec)
		 *
		 * Note that Rosloniec deals in diameters while I deal in radii. Also,
		 * Rosloniec swaps a and b from the notation I'm using here. x = (b +
		 * [a^2 - 4c^2]/b)/(2a) is on p. 184 of Rosloniec.
		 *
		 * substituting b = 2a, a=2b gives
		 *
		 * x = (2a + [4b^2 - 4c^2]/2a)/(4b) = ( a + [2b^2 - 2c^2]/2a)/(2b) = ( a
		 * + [ b^2 - c^2]/ a)/(2b)
		 */
        double x = (a + (Math.pow(b, 2.0) - Math.pow(c, 2.0)) / a) / (2 * b);

        double z0 = (1 / (2 * Math.PI)) * Math.sqrt(mu0 / (e0 * er))
                * Math.log(x + Math.sqrt(x * x - 1));

        return z0;
    }

    private double L_calc(double a, double b, double c, double er) {
        /*
		 * find velocity (meters/second)
		 */
        double v = 1.0 / Math.sqrt(mu0 * e0 * er);

		/*
		 * find L and C from the impedance and velocity
		 *
		 * z0 = sqrt(L/C), v = 1/sqrt(LC)
		 *
		 * this gives the result below
		 */
        return Z0_calc(a, b, c, er) / v;
    }

    private double C_calc(double a, double b, double c, double er) {
		/*
		 * find velocity (meters/second)
		 */
        double v = 1.0 / Math.sqrt(mu0 * e0 * er);
        return 1 / (Z0_calc(a, b, c, er) * v);
    }

    private double delay(double L, double er) {
        double v = 1.0 / Math.sqrt(mu0 * e0 * er);
        return L / v; // second
    }

    private double Eeff(double L, double er, double Freq) {
        return 360 * Freq * delay(L, er);
    }

    public int coax_syn() {
        // int rslt = 0;
        double Ro;
        double v;
        double elen;

		/* the optimization variables, current, min/max, and previous values */
        double var = 0, varmax = 0, varmin = 0, varold = 0;

		/* errors due to the above values for the optimization variable */
        double err = 0, errmax = 0, errold = 0;

		/* derivative */
        double deriv;

		/* the sign of the slope of the function being optimized */
        double sign = 0;

		/* pointer to which parameter of the line is being optimized */
        // double *optpar;

		/* number of iterations so far, and max number allowed */
        int iters = 0;
        int maxiters = 100;

		/* convergence parameters */
        double abstol = 0.1e-6;
        double reltol = 0.01e-6;

		/* flag to end optimization */
        int done = 0;

		/*
		 * figure out what parameter we're synthesizing and set up the various
		 * optimization parameters.
		 *
		 * Basically what we need to know are 1) min/max values for the
		 * parameter 2) how to access the parameter 3) an initial guess for the
		 * parameter
		 */

		/*
		 * read values from the input line structure
		 */

        Ro = Z0;
        elen = Eeff;

		/*
		 * temp value for len used while synthesizing the other parameters.
		 * We'll correct len later.
		 */

        // line->len=1.0;

        // line->freq = f;

        if (flag == 0) {
            varmax = 0.999 * b;
            varmin = 0.001 * b;
            var = 0.2 * b;
            if (done == 0) {
				/* Initialize the various error values */
                a = varmin;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(a, b, c, er) - Ro;

                a = varmax;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(a, b, c, er) - Ro;

                a = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                err = Z0_calc(a, b, c, er) - Ro;

                varold = 0.99 * var;
                a = varold;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(a, b, c, er) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0){
				 * alert("Could not bracket the solution.\n"
				 * "Synthesis failed.\n"); return -1; }
				 */

				/* figure out the slope of the error vs variable */
                if (errmax > 0)
                    sign = 1.0;
                else
                    sign = -1.0;

                iters = 0;
            }

			/* the actual iterations */
            while (done == 0) {

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
				 * see if the new guess is within our bracketed range. If so,
				 * accept the new estimate. If not, toss it out and do a
				 * bisection step to reduce the bracket.
				 */

                if ((var > varmax) || (var < varmin)) {
                    var = (varmin + varmax) / 2.0;
                }

				/* update the error value */
                a = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                err = Z0_calc(a, b, c, er) - Ro;
                // if (rslt)
                // return rslt;

				/* update our bracket of the solution. */

                if (sign * err > 0)
                    varmax = var;
                else
                    varmin = var;

				/* check to see if we've converged */
                if (Math.abs(err) < abstol) {
                    done = 1;
                } else if (Math.abs((var - varold) / var) < reltol) {
                    done = 1;
                } else if (iters >= maxiters) {
                    // alert("Synthesis failed to converge in\n"
                    // "%d iterations\n", maxiters);
                    return -1;
                }
				/* done with iteration */
            }
        } else if (flag == 1) {
            varmax = 1000.0 * a;
            varmin = 1.001 * a;
            var = 5 * a;
            if (done == 0) {
				/* Initialize the various error values */
                b = varmin;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(a, b, c, er) - Ro;

                b = varmax;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(a, b, c, er) - Ro;

                b = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                err = Z0_calc(a, b, c, er) - Ro;

                varold = 0.99 * var;
                b = varold;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(a, b, c, er) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0){
				 * alert("Could not bracket the solution.\n"
				 * "Synthesis failed.\n"); return -1; }
				 */

				/* figure out the slope of the error vs variable */
                if (errmax > 0)
                    sign = 1.0;
                else
                    sign = -1.0;

                iters = 0;
            }

			/* the actual iterations */
            while (done == 0) {

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
				 * see if the new guess is within our bracketed range. If so,
				 * accept the new estimate. If not, toss it out and do a
				 * bisection step to reduce the bracket.
				 */

                if ((var > varmax) || (var < varmin)) {
                    var = (varmin + varmax) / 2.0;
                }

				/* update the error value */
                b = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                err = Z0_calc(a, b, c, er) - Ro;
                // if (rslt)
                // return rslt;

				/* update our bracket of the solution. */

                if (sign * err > 0)
                    varmax = var;
                else
                    varmin = var;

				/* check to see if we've converged */
                if (Math.abs(err) < abstol) {
                    done = 1;
                } else if (Math.abs((var - varold) / var) < reltol) {
                    done = 1;
                } else if (iters >= maxiters) {
                    // alert("Synthesis failed to converge in\n"
                    // "%d iterations\n", maxiters);
                    return -1;
                }
				/* done with iteration */
            }
        } else if (flag == 2) {
            varmax = 0.999 * (b - a);
            varmin = 0;
            var = 0.1 * varmax;
            if (done == 0) {
				/* Initialize the various error values */
                c = varmin;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(a, b, c, er) - Ro;

                c = varmax;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(a, b, c, er) - Ro;

                c = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                err = Z0_calc(a, b, c, er) - Ro;

                varold = 0.99 * var;
                c = varold;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(a, b, c, er) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0){
				 * alert("Could not bracket the solution.\n"
				 * "Synthesis failed.\n"); return -1; }
				 */

				/* figure out the slope of the error vs variable */
                if (errmax > 0)
                    sign = 1.0;
                else
                    sign = -1.0;

                iters = 0;
            }

			/* the actual iterations */
            while (done == 0) {

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
				 * see if the new guess is within our bracketed range. If so,
				 * accept the new estimate. If not, toss it out and do a
				 * bisection step to reduce the bracket.
				 */

                if ((var > varmax) || (var < varmin)) {
                    var = (varmin + varmax) / 2.0;
                }

				/* update the error value */
                c = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                err = Z0_calc(a, b, c, er) - Ro;
                // if (rslt)
                // return rslt;

				/* update our bracket of the solution. */

                if (sign * err > 0)
                    varmax = var;
                else
                    varmin = var;

				/* check to see if we've converged */
                if (Math.abs(err) < abstol) {
                    done = 1;
                } else if (Math.abs((var - varold) / var) < reltol) {
                    done = 1;
                } else if (iters >= maxiters) {
                    // alert("Synthesis failed to converge in\n"
                    // "%d iterations\n", maxiters);
                    return -1;
                }
				/* done with iteration */
            }
        } else if (flag == 3) {
            varmax = 100.0;
            varmin = 1.0;
            var = 5.0;
            if (done == 0) {
				/* Initialize the various error values */
                er = varmin;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(a, b, c, er) - Ro;

                er = varmax;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(a, b, c, er) - Ro;

                er = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                err = Z0_calc(a, b, c, er) - Ro;

                varold = 0.99 * var;
                er = varold;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(a, b, c, er) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0){
				 * alert("Could not bracket the solution.\n"
				 * "Synthesis failed.\n"); return -1; }
				 */

				/* figure out the slope of the error vs variable */
                if (errmax > 0)
                    sign = 1.0;
                else
                    sign = -1.0;

                iters = 0;
            }

			/* the actual iterations */
            while (done == 0) {

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
				 * see if the new guess is within our bracketed range. If so,
				 * accept the new estimate. If not, toss it out and do a
				 * bisection step to reduce the bracket.
				 */

                if ((var > varmax) || (var < varmin)) {
                    var = (varmin + varmax) / 2.0;
                }

				/* update the error value */
                er = var;
                // rslt = coax_calc_int(line,f,CALC_MIN);
                err = Z0_calc(a, b, c, er) - Ro;
                // if (rslt)
                // return rslt;

				/* update our bracket of the solution. */

                if (sign * err > 0)
                    varmax = var;
                else
                    varmin = var;

				/* check to see if we've converged */
                if (Math.abs(err) < abstol) {
                    done = 1;
                } else if (Math.abs((var - varold) / var) < reltol) {
                    done = 1;
                } else if (iters >= maxiters) {
                    // alert("Synthesis failed to converge in\n"
                    // "%d iterations\n", maxiters);
                    return -1;
                }
				/* done with iteration */
            }
        }
		/* velocity on line */
        // rslt = coax_calc(line,f);
        // if (rslt)
        // return rslt;
        v = 1 / Math.sqrt(mu0 * e0 * er);
        L = (elen / 360) * (v / f);

		/* recalculate using real length to find loss */

        return 0;
    }
}
