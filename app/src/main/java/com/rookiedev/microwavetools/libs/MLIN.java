package com.rookiedev.microwavetools.libs;


public class MLIN {
    private static final double pi = Math.PI;
    private static final double c = 0.2997925; // speed of the light divided by 1e9
    // private static final double n0 = 120 * pi;// impedance of free space
    private static final double n0 = 4 * pi * c * 100;
    private static final double e = Math.E;// base of the natural logarithm
    private double W, L, T, er, H, f, Eeff, Z0;

    public MLIN(double width, double length, double impedance, double electricalLength, double frequency, double epsilon, double height, double thick) {
        W = width;
        L = length;
        T = thick;
        er = epsilon;
        H = height;
        f = frequency;
        Eeff = electricalLength;
        Z0 = impedance;
    }

    public double getZ0() {
        return ZL_f(W, H, T, f);
    }

    public double getEeff() {
        return Eeff(W, L, H, T, f);
    }

    public double getW() {
        return W_syn(Z0, H, T, f);
    }

    public void setW(double wide) {
        W = wide;
    }

    public double getL() {
        return L_syn(W, H, T, f, Eeff);
    }

    // Hammerstad and Jensen
    // Quasi-static characteristic impedance ZL1
    private double ZL1(double U) {

        double fu = 6 + (2 * pi - 6)
                * Math.pow(e, -Math.pow(30.666 / U, 0.7528));
        return (n0 / 2 / pi * Math.log(fu / U
                + Math.sqrt(1 + Math.pow(2 / U, 2))));
        // return n0;
    }

    // Hammerstad and Jensen
    // Quasi-static characteristic impedance ZL
    private double ZL(double U) {
        return (ZL1(U) / Math.sqrt(er));
    }

    // Hammerstad and Jensen
    // Quasi-static effective dielectric constant
    private double Er_eff(double U) {
        double a = 1
                + Math.log((Math.pow(U, 4) + Math.pow(U / 52, 2))
                / (Math.pow(U, 4) + 0.432)) / 49
                + Math.log(1 + Math.pow(U / 18.1, 3)) / 18.7;
        double b = 0.564 * Math.pow((er - 0.9) / (er + 3), 0.053);
        return ((er + 1) / 2 + (er - 1) / 2 * Math.pow(1 + 10 / U, -a * b));
    }

    // Strip thickness correction
    private double U1(double W, double H, double T) {
        double U = W / H;
        double t1 = Math.sqrt(6.517 * U);// ???
        double dW1 = T
                / H
                / pi
                * Math.log(1 + 4 * e * H / T
                / Math.pow(Math.cosh(t1) / Math.sinh(t1), 2));
        return (U + dW1);
    }

    private double Ur(double W, double H, double T) {
        double U = W / H;
        // double t2 = Math.sqrt(er - 1);
        // double sech = 2 / (Math.pow(e, t2) + Math.pow(e, -t2));
        double t1 = Math.sqrt(6.517 * U);// ???
        double dW1 = T
                / H
                / pi
                * Math.log(1 + 4 * e * H / T
                / Math.pow(Math.cosh(t1) / Math.sinh(t1), 2));
        double dWr = (1 + 1 / Math.cosh(Math.sqrt(er - 1))) * dW1 * 0.5;
        // double dWr = 1 / 2 * dW1
        // * (1 + 1 / Math.cosh(Math.sqrt(er - 1)));
        return (U + dWr);// U or W???????????
    }

    private double ZL_thickness(double W, double H, double T) {
        return (ZL1(Ur(W, H, T)) / Math.sqrt(Er_eff(Ur(W, H, T))));
    }

    private double Er_eff_thickness(double W, double H, double T) {
        return (Er_eff(Ur(W, H, T)) * ZL1(U1(W, H, T))
                * ZL1(U1(W, H, T)) / ZL1(Ur(W, H, T)) / ZL1(Ur(W, H, T)));
    }

    // Dispersion
    private double Er_f(double W, double H, double T, double f) {
        double U = W / H;
        double fn = H / 39.37007874 * f;
        double P1 = 0.27488
                + (0.6315 + 0.525 / (Math.pow((1 + 0.0157 * fn), 20))) * U
                - 0.065683 * Math.pow(e, -8.7513 * U);
        double P2 = 0.33622 * (1 - Math.pow(e, -0.03442 * er));
        double P3 = 0.0363 * Math.pow(e, -4.6 * U)
                * (1 - Math.pow(e, -Math.pow(fn / 38.7, 4.97)));
        double P4 = 1 + 2.751 * (1 - Math.pow(e, -Math.pow(er / 15.916, 8)));
        double Pf = P1 * P2 * Math.pow((0.1844 + P3 * P4) * fn, 1.5763);
        if (T != 0) {
            return (er - (er - Er_eff_thickness(W, H, T)) / (1 + Pf));
        } else {
            return (er - (er - Er_eff(U)) / (1 + Pf));
        }
    }

    //private double Mu_eff(double U, double mu) {
    //    return 2 * mu / ((1 + mu) + (1 - mu) * Math.pow(1 + 10 / U, -0.5));
    //}

    private double ZL_f(double W, double H, double T, double f) {
        double U = W / H;
        double fn = H / 39.37007874 * f;
        double R1 = 0.03891 * Math.pow(er, 1.4);
        double R2 = 0.267 * Math.pow(U, 7);
        double R3 = 4.766 * Math.pow(e, -3.228 * Math.pow(U, 0.641));
        double R4 = 0.016 + Math.pow(0.0514 * er, 4.524);
        double R5 = Math.pow(fn / 28.843, 12);
        double R6 = 22.2 * Math.pow(U, 1.92);
        double R7 = 1.206 - 0.3144 * Math.pow(e, -R1) * (1 - Math.pow(e, -R2));
        double R8 = 1.0 + 1.275 * (1.0 - Math.exp(-0.004625 * R3
                * Math.pow(er, 1.674) * Math.pow(fn / 18.365, 2.745)));
        double R9 = 5.086 * R4 * R5 / (0.3838 + 0.386 * R4) * Math.pow(e, -R6)
                / (1 + 1.2992 * R5) * Math.pow(er - 1, 6)
                / (1 + 10 * Math.pow(er - 1, 6));
        double R10 = 0.00044 * Math.pow(er, 2.136) + 0.0184;
        double R11 = Math.pow(fn / 19.47, 6)
                / (1 + 0.0962 * Math.pow(fn / 19.47, 6));
        double R12 = 1 / (1 + 0.00245 * Math.pow(U, 2));
        double R13 = 0.9408 * Math.pow(Er_f(W, H, T, f), R8) - 0.9603;
        double R14;
        if (T != 0) {
            R14 = (0.9408 - R9) * Math.pow(Er_eff_thickness(W, H, T), R8)
                    - 0.9603;
        } else {
            R14 = (0.9408 - R9) * Math.pow(Er_eff(U), R8) - 0.9603;
        }
        double R15 = 0.707 * R10 * Math.pow(fn / 12.3, 1.097);
        double R16 = 1 + 0.0505 * er * er * R11
                * (1 - Math.pow(e, -Math.pow(U / 15, 6)));
        double R17 = R7
                * (1 - 1.1241 * R12 / R16
                * Math.pow(e, -0.026 * Math.pow(fn, 1.15656) - R15));
        if (T != 0) {
            return (ZL_thickness(W, H, T) * Math.pow(R13 / R14, R17));
        } else {
            return (ZL(U) * Math.pow(R13 / R14, R17));
        }
    }

    private double Eeff(double W, double L, double H, double T, double f) {
        double U = W / H;
        double v;
        v = c / Math.sqrt(Er_f(W, H, T, f));
        return L / (v / f) * 360 / 39.37007874 / 1000;
    }

    private double W_syn(double zd, double H, double T, double f) {
        double PHYSSF = 0.0000254;
        // double rel = 1000000;
        /*
         * temp value for l used while finding w
		 */
        // double lx = 1000;

		/*
         * limits on the allowed range for w (in mils) - should limit w/h, not w
		 */
        double wmin = 0.01;
        // var wmax = 1000;
        double wmax = 499;

        wmin = wmin * 25.4e-6 / PHYSSF;
        wmax = wmax * 25.4e-6 / PHYSSF;

		/*
		 * impedance convergence tolerance (ohms)
		 */
        double abstol = 0.000001;

		/*
		 * width relative convergence tolerance (mils) (set to 0.1 micron)
		 */
        double reltol = 0.1 / 25.4;
        reltol = reltol * 25.4e-6 / PHYSSF;

        int maxiters = 50;

		/*
		 * take an initial guess at w and take a trial step to initialize the
		 * iteration
		 */

        double A = ((er - 1) / (er + 1)) * (0.226 + 0.121 / er) + (pi / 377)
                * Math.sqrt(2 * (er + 1)) * zd;

        double w_h = 4 / (0.5 * Math.pow(e, A) - Math.pow(e, -A));
        if (w_h > 2) {
            double B = pi * 377 / (2 * zd * Math.sqrt(er));
            w_h = (2 / pi)
                    * (B - 1 - Math.log(2 * B - 1) + ((er - 1) / (2 * er))
                    * (Math.log(B - 1) + 0.293 - 0.517 / er));
        }

        // alert("w is " + document.mstripForm.H.value*w_h);

        // var wx=50*25.4e-6/PHYSSF;
        double wx = H * w_h;
        if (wx >= wmax) {
            wx = 0.95 * wmax;
        }

        if (wx <= wmin) {
            wx = wmin;
        }
        // var wold = 1.001*wx;
        double wold = 1.01 * wx;

        double zold = ZL_f(wold, H, T, f);

		/*
		 * document.mstripForm.W.value = wold; document.mstripForm.L.value = lx;
		 * computeAnalyzeForm(); var zold = computeAnalyzeForm();
		 */

		/*
		 * check to see if we're too high or too low and bracket the value for
		 * w.
		 */
        if (zold < zd) {
            wmax = wold;
        } else {
            wmin = wold;
        }

        int iters = 0;
        int done = 0;

        while (done == 0) {

            iters = iters + 1;

            double zo = ZL_f(wx, H, T, f);

            if (zo < zd) {
                wmax = wx;
            } else {
                wmin = wx;
            }

            if (Math.abs(zo - zd) < abstol) {
                done = 1;
            } else if (Math.abs(wx - wold) < reltol) {
                done = 1;
            } else if (iters >= maxiters) {
				/* failed */
            } else {
				/* calculate approximation to the derivative */
                double dzdw = (zo - zold) / (wx - wold);
                wold = wx;
                zold = zo;

				/* take a newton iteration */
                wx = wx - (zo - zd) / dzdw;

				/*
				 * if the newton iteration takes us out of the known range for
				 * w, take a bisection step
				 */
                if ((wx > wmax) | (wx < wmin)) {
                    wx = (wmin + wmax) / 2;
                }
            }
        }
        return wx;
    }

    private double L_syn(double W, double H, double T, double f, double Eeff) {
        //double U = W / H;
        double v;
        v = c / Math.sqrt(Er_f(W, H, T, f));
        return Eeff * (v / f) / 360 * 39.37007874 * 1000;
    }

}
