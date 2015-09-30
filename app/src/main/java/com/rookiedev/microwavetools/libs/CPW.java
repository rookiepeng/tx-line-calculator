package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 8/21/13.
 */
public class CPW {
    /* free space speed of light, meters/second */
    private static double LIGHTSPEED = 2.99792458e8;
    /* free space permitivitty (Henries/meter) */
    private static double FREESPACE_MU0 = 4.0 * Math.PI * 1.0e-7;

    /* free space permitivitty (Farads/meter) */
    // private static double FREESPACE_E0 = 1.0 / (LIGHTSPEED * LIGHTSPEED *
    // FREESPACE_MU0);
    /* free space impedance, Ohms */
    private static double FREESPACEZ0 = FREESPACE_MU0 * LIGHTSPEED;
    private double W, S, H, er, L, Z0, Eeff, f, T;
    private int flag;

    public CPW(double width, double space, double height, double epsilon, double length,
               double impedance, double electricalLength, double frequency, double thick,
               int positionFlag) {
        W = width;
        S = space;
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
        return Z0_calc(W, S, T, H, er);
    }

    public double getEeff() {
        return Eeff_calc(W, S, T, H, L, er, f);
    }

    public double getW() {
        return W;
    }

    public double getS() {
        return S;
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

    /*
     * compute K(k)/K'(k) where K is the complete elliptic integral of the first
	 * kind, K' is the complementary complete elliptic integral of the first
	 * kind
	 */

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

		/*
         * alternate approach if( k < sqrt(0.5) ) { kp = sqrt(1.0 - k*k); r =
		 * M_PI / log(2.0 * (1.0 + sqrt(kp)) / (1.0 - sqrt(kp)) ); } else { r =
		 * log(2.0 * (1.0 + sqrt(k)) / (1.0 - sqrt(k)) ) / M_PI; }
		 */
        return r;
    }

    private double Z0_calc(double W, double S, double T, double H, double er) {

		/* calculation variables */
        double k, k1, kt, z0;
        double k_kp, k_kp1, k_kpt;
        double a, at, b, bt, eeff, keff;

		/*
		 * Characteristic Impedance
		 */

		/*
		 * These equations are _without_ the bottom side ground plane.
		 */

		/* match the notation in Wadell */
        a = W;
        b = W + 2.0 * S;

		/* Wadell (3.4.1.8), (3.4.1.9) but avoid issues with tmet = 0 */
        if (T > 0.0) {
            at = a + (1.25 * T / Math.PI)
                    * (1.0 + Math.log(4.0 * Math.PI * a / T));
            bt = b - (1.25 * T / Math.PI)
                    * (1.0 + Math.log(4.0 * Math.PI * a / T));
        } else {
            at = a;
            bt = b;
        }

		/*
		 * we have to be a bit careful here. If we're not careful, we can end up
		 * with b < a which is nonsense and it will cause the calculation of the
		 * elliptic integral ratios to produce NaN
		 */
        if (bt <= at) {
            at = a;
            bt = b;
            // alert("Warning:  bt <= at so I am reverting to zero thickness equations\n");
        }

		/* Wadell (3.4.1.6) */
        k1 = Math.sinh(Math.PI * at / (4.0 * H))
                / Math.sinh(Math.PI * bt / (4.0 * H));

		/* Wadell (3.4.1.4), (3.4.1.5) */
        k = a / b;
        kt = at / bt;
        k_kp = k_over_kp(k);
        k_kp1 = k_over_kp(k1);
        k_kpt = k_over_kp(kt);

		/* Wadell (3.4.1.3) */
        eeff = 1.0 + 0.5 * (er - 1.0) * k_kp1 / k_kp;

		/* Wadell (3.4.1.2) */
        keff = eeff - (eeff - 1.0) / ((0.5 * (b - a) / (0.7 * T)) * k_kp + 1.0);

		/* for coplanar waveguide (ground signal ground) */
        z0 = FREESPACEZ0 / (4.0 * Math.sqrt(keff) * k_kpt);
        return z0;
    }

    private double Eeff_calc(double W, double S, double T, double H, double L,
                             double er, double f) {
		/* calculation variables */
        double k, k1;
        double k_kp, k_kp1;
        double a, at, b, bt, eeff, keff;

		/*
		 * Characteristic Impedance
		 */

		/*
		 * These equations are _without_ the bottom side ground plane.
		 */

		/* match the notation in Wadell */
        a = W;
        b = W + 2.0 * S;

		/* Wadell (3.4.1.8), (3.4.1.9) but avoid issues with tmet = 0 */
        if (T > 0.0) {
            at = a + (1.25 * T / Math.PI)
                    * (1.0 + Math.log(4.0 * Math.PI * a / T));
            bt = b - (1.25 * T / Math.PI)
                    * (1.0 + Math.log(4.0 * Math.PI * a / T));
        } else {
            at = a;
            bt = b;
        }

		/*
		 * we have to be a bit careful here. If we're not careful, we can end up
		 * with b < a which is nonsense and it will cause the calculation of the
		 * elliptic integral ratios to produce NaN
		 */
        if (bt <= at) {
            at = a;
            bt = b;
            // alert("Warning:  bt <= at so I am reverting to zero thickness equations\n");
        }

		/* Wadell (3.4.1.6) */
        k1 = Math.sinh(Math.PI * at / (4.0 * H))
                / Math.sinh(Math.PI * bt / (4.0 * H));

		/* Wadell (3.4.1.4), (3.4.1.5) */
        k = a / b;
        // kt = at / bt;
        k_kp = k_over_kp(k);
        k_kp1 = k_over_kp(k1);
        // k_kpt = k_over_kp(kt);

		/* Wadell (3.4.1.3) */
        eeff = 1.0 + 0.5 * (er - 1.0) * k_kp1 / k_kp;

		/* Wadell (3.4.1.2) */
        keff = eeff - (eeff - 1.0) / ((0.5 * (b - a) / (0.7 * T)) * k_kp + 1.0);
		/*
		 * Electrical Length
		 */

		/* propagation velocity (meters/sec) */
        double v = LIGHTSPEED / Math.sqrt(keff);
        return (360 * L * f / v);
    }

    public int coplanar_syn() {
        double w = 0, s = 0, h = 0, e = 0;

        double Ro;
        double v;

		/* permeability and permitivity of free space */
        // double mu0;

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

		/* permeability and permitivitty of free space (H/m and F/m) */
        // mu0 = 4.0 * Math.PI * 1.0e-7;
        // e0 = 1.0 / (mu0 * LIGHTSPEED * LIGHTSPEED);

		/*
		 * figure out what parameter we're synthesizing and set up the various
		 * optimization parameters.
		 *
		 * Basically what we need to know are 1) min/max values for the
		 * parameter 2) how to access the parameter 3) an initial guess for the
		 * parameter
		 */

		/*
		 * case CPWSYN_ER: optpar = &(line->subs->er); varmax = 100.0; varmin =
		 * 1.0; var = 5.0; break;
		 *
		 * default: fprintf(stderr,"coplanar_synth():  illegal flag=%d\n",flag);
		 * exit(1); break; }
		 */

		/*
		 * read values from the input line structure
		 */

        Ro = Z0;

		/*
		 * temp value for l used while synthesizing the other parameters. We'll
		 * correct l later.
		 */
        // len = Eeff; /* remember what electrical length we want */
        // l = 1.0;

        if (flag == 0) {
            varmax = 100.0 * H;
            varmin = 0.01 * H;
            var = H;

            if (done == 0) {
				/* Initialize the various error values */
                w = varmin;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(w, S, T, H, er) - Ro;

                w = varmax;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(w, S, T, H, er) - Ro;

                w = var;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                err = Z0_calc(w, S, T, H, er) - Ro;

                varold = 0.99 * var;
                w = varold;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(w, S, T, H, er) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0) {
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
                // rslt = coplanar_calc_int(line, f, NOLOSS);
                err = Z0_calc(w, S, T, H, er) - Ro;
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
				 * alert("Synthesis failed to converge in\n"
				 * "%d iterations.  Final optimization parameters:\n"
				 * "  min = %g\n" "  val = %g\n" "  max = %g\n", maxiters,
				 * varmin, var, varmax); return -1; }
				 */
				/* done with iteration */
            }
            W = w;
        }
        if (flag == 1) {
            varmax = 100.0 * H;
            varmin = 0.01 * H;
            var = H;

            if (done == 0) {
				/* Initialize the various error values */
                s = varmin;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(W, s, T, H, er) - Ro;

                s = varmax;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(W, s, T, H, er) - Ro;

                s = var;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                err = Z0_calc(W, s, T, H, er) - Ro;

                varold = 0.99 * var;
                s = varold;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(W, s, T, H, er) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0) {
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
                s = var;
                // rslt = coplanar_calc_int(line, f, NOLOSS);
                err = Z0_calc(W, s, T, H, er) - Ro;
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
				 * alert("Synthesis failed to converge in\n"
				 * "%d iterations.  Final optimization parameters:\n"
				 * "  min = %g\n" "  val = %g\n" "  max = %g\n", maxiters,
				 * varmin, var, varmax); return -1; }
				 */
				/* done with iteration */
            }
            S = s;
        }
        if (flag == 2) {
            varmax = 100.0 * W;
            varmin = 0.01 * W;
            var = W;

            if (done == 0) {
				/* Initialize the various error values */
                h = varmin;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(W, S, T, h, er) - Ro;

                h = varmax;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(W, S, T, h, er) - Ro;

                h = var;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                err = Z0_calc(W, S, T, h, er) - Ro;

                varold = 0.99 * var;
                h = varold;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(W, S, T, h, er) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0) {
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
                // rslt = coplanar_calc_int(line, f, NOLOSS);
                err = Z0_calc(W, S, T, h, er) - Ro;
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
				 * alert("Synthesis failed to converge in\n"
				 * "%d iterations.  Final optimization parameters:\n"
				 * "  min = %g\n" "  val = %g\n" "  max = %g\n", maxiters,
				 * varmin, var, varmax); return -1; }
				 */
				/* done with iteration */
            }
            H = h;
        }
        if (flag == 3) {
            varmax = 100.0;
            varmin = 1.0;
            var = 5.0;

            if (done == 0) {
				/* Initialize the various error values */
                e = varmin;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                // errmin = Z0_calc(W, S, T, H, e) - Ro;

                e = varmax;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errmax = Z0_calc(W, S, T, H, e) - Ro;

                e = var;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                err = Z0_calc(W, S, T, H, e) - Ro;

                varold = 0.99 * var;
                e = varold;
                // rslt = coplanar_calc_int(line,f,NOLOSS);
                // if (rslt)
                // return rslt;
                errold = Z0_calc(W, S, T, H, e) - Ro;

				/* see if we've actually been able to bracket the solution */
				/*
				 * if (errmax*errmin > 0) {
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
                // rslt = coplanar_calc_int(line, f, NOLOSS);
                err = Z0_calc(W, S, T, H, e) - Ro;
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
				 * alert("Synthesis failed to converge in\n"
				 * "%d iterations.  Final optimization parameters:\n"
				 * "  min = %g\n" "  val = %g\n" "  max = %g\n", maxiters,
				 * varmin, var, varmax); return -1; }
				 */
				/* done with iteration */
            }
            er = e;
        }

		/* velocity on line */
        // coplanar_calc(line, f);
        v = LIGHTSPEED / Math.sqrt(er);
        L = (Eeff / 360.0) * (v / f);
        return 0;
    }
}
