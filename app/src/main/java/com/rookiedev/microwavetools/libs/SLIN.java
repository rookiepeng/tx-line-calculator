package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 8/21/13.
 */
public class SLIN {
    private double W, H, er, L, Z0, Eeff, f, T;
    private int flag;

    public SLIN(double width, double height, double epsilon, double length, double impedance,
                double electricalLength, double frequency, double thick, int positionFlag) {
        W = width;
        H = height;
        er = epsilon;
        L = length;
        Z0 = impedance;
        Eeff = electricalLength;
        f = frequency;
        T = thick;
        flag = positionFlag;
    }

    public double getZ0() {
        return Z0_calc(W, H, T, er);
    }

    public double getEeff() {
        return Eeff_calc(L, f);
    }

    public double getW() {
        return W;
    }

    public double getH() {
        return H;
    }

    public double geter() {
        return er;
    }

    public double getL() {
        return L;
    }

    private double Z0_calc(double W, double H, double T, double er) {
        /* calculation variables */
        double k, kp, r, kf, z0, m, deltaW, A;
		/*
		 * Characteristic Impedance
		 */

        if (T < H / 1000) {
			/*
			 * Thin strip case:
			 */
            k = 1 / Math.cosh(Math.PI * W / (2 * H));

			/*
			 * compute K(k)/K'(k) where K is the complete elliptic integral of
			 * the first kind, K' is the complementary complete elliptic
			 * integral of the first kind
			 */

            kp = Math.sqrt(1.0 - Math.pow(k, 2.0));
            r = 1.0;
            kf = (1.0 + k) / (1.0 + kp);
            while (kf != 1.0) {
                r = r * kf;
                k = 2.0 * Math.sqrt(k) / (1.0 + k);
                kp = 2.0 * Math.sqrt(kp) / (1.0 + kp);
                kf = (1.0 + k) / (1.0 + kp);
            }

            z0 = 29.976 * Math.PI * r / Math.sqrt(er);
        } else {

			/*
			 * Finite strip case:
			 */

            m = 6.0 * (H - T) / (3.0 * H - T);
            deltaW = (T / Math.PI)
                    * (1.0 - 0.5 * Math.log(Math.pow((T / (2 * H - T)), 2.0)
                    + Math.pow((0.0796 * T / (W + 1.1 * T)), m)));
            A = 4.0 * (H - T) / (Math.PI * (W + deltaW));
            z0 = (30 / Math.sqrt(er))
                    * Math.log(1.0 + A
                    * (2.0 * A + Math.sqrt(4.0 * A * A + 6.27)));
        }

		/* done with Z0 calculation */
        return z0;
    }

    private double Eeff_calc(double L, double f) {
		/*
		 * Electrical Length
		 */

		/* propagation velocity (meters/sec) */
        double v = Constant.LIGHTSPEED / Math.sqrt(er);
        return (360 * L * f / v);
    }

    public void stripline_syn() {
        // int rslt;
        // double l;
        double Ro;
        double v;

		/* the parameters which define the structure */
        double w = 0;
        double h = 0;
        double e = 0;
        // double tmet;
        // double h,es,tand;

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
        // int maxiters = 100;

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
        // Xo = line->Xo;
        // len = line->len;

		/* Metal width, length, and thickness */
        // w = line->w;
        // l = line->l;
        // tmet = line->subs->tmet;

		/* Substrate thickness, relative permitivity, and loss tangent */
        // h = line->subs->h;
        // es = line->subs->er;
        // tand = line->subs->tand;

		/*
		 * temp value for l used while synthesizing the other parameters. We'll
		 * correct l later.
		 */
        // l = 1000.0;

        if (flag == 0) {
            varmax = 100.0 * H;
            varmin = 0.01 * H;
            var = H;
            if (done == 0) {
				/* Initialize the various error values */
                w = varmin;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(w, H, T, er) - Ro;

                w = varmax;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(w, H, T, er) - Ro;

                w = var;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                err = Z0_calc(w, H, T, er) - Ro;

                varold = 0.99 * var;
                w = varold;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(w, H, T, er) - Ro;

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
                w = var;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                err = Z0_calc(w, H, T, er) - Ro;
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
                }
				/*
				 * else if (iters >= maxiters){
				 * alert("Synthesis failed to converge in\n" "%d iterations\n",
				 * maxiters); return -1; }
				 */

				/* done with iteration */
            }
            W = w;
        } else if (flag == 1) {
            varmax = 100.0 * W;
            varmin = 0.01 * W;
            var = W;
            if (done == 0) {
				/* Initialize the various error values */
                h = varmin;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(W, h, T, er) - Ro;

                h = varmax;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(W, h, T, er) - Ro;

                h = var;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                err = Z0_calc(W, h, T, er) - Ro;

                varold = 0.99 * var;
                h = varold;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(W, h, T, er) - Ro;

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
                h = var;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                err = Z0_calc(W, h, T, er) - Ro;
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
                }
				/*
				 * else if (iters >= maxiters){
				 * alert("Synthesis failed to converge in\n" "%d iterations\n",
				 * maxiters); return -1; }
				 */

				/* done with iteration */
            }
            H = h;
        } else if (flag == 2) {
            varmax = 100.0;
            varmin = 1.0;
            var = 5.0;
            if (done == 0) {
				/* Initialize the various error values */
                e = varmin;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(W, H, T, e) - Ro;

                e = varmax;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(W, H, T, e) - Ro;

                e = var;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                err = Z0_calc(W, H, T, e) - Ro;

                varold = 0.99 * var;
                e = varold;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(W, H, T, e) - Ro;

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
                e = var;
                // rslt = stripline_calc_int(line,f,NOLOSS);
                err = Z0_calc(W, H, T, e) - Ro;
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
                }
				/*
				 * else if (iters >= maxiters){
				 * alert("Synthesis failed to converge in\n" "%d iterations\n",
				 * maxiters); return -1; }
				 */

				/* done with iteration */
            }
            er = e;
        }

		/* velocity on line */
        v = Constant.LIGHTSPEED / Math.sqrt(er);
        L = (Eeff / 360) * (v / f);
    }
}
