package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 8/21/13.
 */
public class CSLIN {
    /* free space speed of light, meters/second */
    private static double LIGHTSPEED = 2.99792458e8;
    /* free space permitivitty (Henries/meter) */
    private static double FREESPACE_MU0 = 4.0 * Math.PI * 1.0e-7;
    /* free space permitivitty (Farads/meter) */
    private static double FREESPACE_E0 = 1.0 / (LIGHTSPEED * LIGHTSPEED * FREESPACE_MU0);
    /* free space impedance, Ohms */
    private static double FREESPACEZ0 = FREESPACE_MU0 * LIGHTSPEED;
    private double W, S, L, Z0, k, Z0o, Z0e, Eeff, f, er, H, T;
    private boolean use_z0k;

    public CSLIN(double width, double space, double length, double impedance, double waveNumber, double impedanceOdd, double impedanceEven, double electricalLength, double frequency, double epsilon, double height, double thick, boolean usez0k) {
        W = width;
        S = space;
        L = length;
        Z0 = impedance;
        k = waveNumber;
        Z0o = impedanceOdd;
        Z0e = impedanceEven;
        Eeff = electricalLength;
        f = frequency;
        er = epsilon;
        H = height;
        T = thick;
        use_z0k = usez0k;
    }

    public double getZ0e() {
        return Z0e_calc(W, S, H, T, f);
    }

    public double getZ0o() {
        return Z0o_calc(W, S, H, T, f);
    }

    public double getEeff() {
        return Eeff_calc(L, f);
    }

    public double getW() {
        return W;
    }

    public double getS() {
        return S;
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

    private double z0e_zerot(double W, double S, double H) {
        double ke;

		/* (3) from Cohn */
        ke = Math.tanh(Math.PI * W / (2.0 * H))
                * Math.tanh(Math.PI * (W + S) / (2.0 * H));

		/* (2) from Cohn */
        return ((FREESPACEZ0 / 4.0) * Math.sqrt(1.0 / er) / k_over_kp(ke));
    }

    private double z0o_zerot(double W, double S, double H) {
        double ko;

		/* (6) from Cohn */
        ko = Math.tanh(Math.PI * W / (2.0 * H))
                / Math.tanh(Math.PI * (W + S) / (2.0 * H));

		/* (5) from Cohn */
        return ((FREESPACEZ0 / 4.0) * Math.sqrt(1.0 / er) / k_over_kp(ko));
    }

    private double Z0_calc(double W, double H, double T) {
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

    private double Z0e_calc(double W, double S, double H, double T, double f) {
		/* zero thickness even and odd impedances */
        double z0e_0t;
        double z0e;
        double z0s, z0s_0t;
        double cf_t, cf_0;
        z0e_0t = z0e_zerot(W, S, H);

        if (T == 0.0) {
            z0e = z0e_0t;
            // z0o = z0o_0t;
        } else {
            // single.subs = stripline_subs_new();
            // *(single.subs) = *(line->subs);
            // single.w = line->w;
            // single.l = line->l;
            // single.freq = line->freq;

            // rslt = stripline_calc(&single, line->freq);
            // if( rslt != 0 ) {
            // alert ("%s():  stripline_calc failed (%d)", __FUNCTION__);
            // }
            z0s = Z0_calc(W, H, T);

            // single.subs->tmet = 0.0;
            // rslt = stripline_calc(&single, line->freq);
            // if( rslt != 0 ) {
            // alert ("%s():  stripline_calc failed (%d)", __FUNCTION__);
            // }
            z0s_0t = Z0_calc(W, H, 0);

			/* fringing capacitance */
            cf_t = (FREESPACE_E0 * er / Math.PI)
                    * ((2.0 / (1.0 - T / H))
                    * Math.log((1.0 / (1.0 - T / H)) + 1.0) - (1.0 / (1.0 - T
                    / H) - 1.0)
                    * Math.log((1.0 / Math.pow(1.0 - T / H, 2.0)) - 1.0));

			/* zero thickness fringing capacitance */
            cf_0 = (FREESPACE_E0 * er / Math.PI) * 2.0 * Math.log(2.0);

			/* (18) from Cohn, (4.6.5.1) in Wadell */
            z0e = 1.0 / ((1.0 / z0s) - (cf_t / cf_0)
                    * ((1.0 / z0s_0t) - (1.0 / z0e_0t)));
        }
        return z0e;
    }

    private double Z0o_calc(double W, double S, double H, double T, double f) {
		/* zero thickness even and odd impedances */
        double z0o_0t;
        double z0o;
        double z0s, z0s_0t;
        double cf_t, cf_0;
        z0o_0t = z0o_zerot(W, S, H);

        if (T == 0.0) {
            // z0e = z0e_0t;
            z0o = z0o_0t;
        } else {
            // single.subs = stripline_subs_new();
            // *(single.subs) = *(line->subs);
            // single.w = line->w;
            // single.l = line->l;
            // single.freq = line->freq;

            // rslt = stripline_calc(&single, line->freq);
            // if( rslt != 0 ) {
            // alert ("%s():  stripline_calc failed (%d)", __FUNCTION__);
            // }
            z0s = Z0_calc(W, H, T);

            // single.subs->tmet = 0.0;
            // rslt = stripline_calc(&single, line->freq);
            // if( rslt != 0 ) {
            // alert ("%s():  stripline_calc failed (%d)", __FUNCTION__);
            // }
            z0s_0t = Z0_calc(W, H, 0);

			/* fringing capacitance */
            cf_t = (FREESPACE_E0 * er / Math.PI)
                    * ((2.0 / (1.0 - T / H))
                    * Math.log((1.0 / (1.0 - T / H)) + 1.0) - (1.0 / (1.0 - T
                    / H) - 1.0)
                    * Math.log((1.0 / Math.pow(1.0 - T / H, 2.0)) - 1.0));

			/* zero thickness fringing capacitance */
            cf_0 = (FREESPACE_E0 * er / Math.PI) * 2.0 * Math.log(2.0);

            if (S >= 5.0 * T) {
				/*
				 * (20) from Cohn, (4.6.5.2) in Wadell -- note, Wadell has a
				 * sign error in the equation
				 */
                z0o = 1.0 / ((1.0 / z0s) + (cf_t / cf_0)
                        * ((1.0 / z0o_0t) - (1.0 / z0s_0t)));

            } else {
				/*
				 * (22) from Cohn, (4.6.5.3) in Wadell -- note, Wadell has a
				 * couple of errors in the transcription from the original
				 * (Cohn)
				 */
                z0o = 1.0 / ((1.0 / z0o_0t) + ((1.0 / z0s) - (1.0 / z0s_0t))
                        - (2.0 / FREESPACEZ0)
                        * (cf_t / FREESPACE_E0 - cf_0 / FREESPACE_E0) + (2.0 * T)
                        / (FREESPACEZ0 * S));
            }
        }
        return z0o;
    }

    private double Eeff_calc(double L, double f) {
        return (360.0 * L * f / LIGHTSPEED * Math.sqrt(er));
    }

    public void cslin_syn() {
        // double wmin, wmax, abstol, reltol;
        int maxiters;
        int iters;
        int done = 0;
        double len;

        // double smin, smax;
        double delta, cval, err, d;
        // double loss, kev, kodd;

        double AW, F1, F2, F3;

        double ai[] = {1, -0.301, 3.209, -27.282, 56.609, -37.746};
        double bi[] = {0.020, -0.623, 17.192, -68.946, 104.740, -16.148};
        double ci[] = {0.002, -0.347, 7.171, -36.910, 76.132, -51.616};

        int i;
        double dw, ds;
        double ze0 = 0, ze1, ze2, dedw, deds;
        double zo0 = 0, zo1, zo2, dodw, dods;
        double l, w, s;

        len = Eeff;

		/* Substrate dielectric thickness (m) */
        // h = line->subs->h;

		/* Substrate relative permittivity */
        // er = line->subs->er;

		/* impedance and coupling */
        // z0 = line->z0;
        // k = line->k;

		/* even/odd mode impedances */
        // z0e = line->z0e;
        // z0o = line->z0o;

        if (use_z0k) {
			/* use z0 and k to calculate z0e and z0o */
            Z0o = Z0 * Math.sqrt((1.0 - k) / (1.0 + k));
            Z0e = Z0 * Math.sqrt((1.0 + k) / (1.0 - k));
        } else {
			/* use z0e and z0o to calculate z0 and k */
            Z0 = Math.sqrt(Z0e * Z0o);
            k = (Z0e - Z0o) / (Z0e + Z0o);
        }

		/* temp value for l used while finding w and s */
        l = 1000.0;
        // line->l=l;

		/* limits on the allowed range for w */
        // wmin = MIL2M(0.5);
        // wmax = MIL2M(1000);

		/* limits on the allowed range for s */
        // smin = MIL2M(0.5);
        // smax = MIL2M(1000);

		/* impedance convergence tolerance (ohms) */
        // abstol = 1e-6;

		/* width relative convergence tolerance (mils) (set to 0.1 micron) */
        // reltol = MICRON2MIL(0.1);

        maxiters = 50;

		/*
		 * Initial guess at a solution
		 */
        AW = Math.exp(Z0 * Math.sqrt(er + 1.0) / 42.4) - 1.0;
        F1 = 8.0
                * Math.sqrt(AW * (7.0 + 4.0 / er) / 11.0 + (1.0 + 1.0 / er)
                / 0.81) / AW;

        F2 = 0;
        for (i = 0; i <= 5; i++) {
            F2 = F2 + ai[i] * Math.pow(k, i);
        }

        F3 = 0;
        for (i = 0; i <= 5; i++) {
            F3 = F3 + (bi[i] - ci[i] * (9.6 - er))
                    * Math.pow((0.6 - k), (double) (i));
        }

        w = H * Math.abs(F1 * F2);
        s = H * Math.abs(F1 * F3);

        // l = 100;
        // loss = 0;
        // kev = 1;
        // kodd = 1;

        iters = 0;
        done = 0;
        if (w < s)
            delta = 1e-3 * w;
        else
            delta = 1e-3 * s;

        delta = MIL2M(1e-5);

        cval = 1e-12 * Z0e * Z0o;

		/*
		 * We should never need anything anywhere near maxiters iterations. This
		 * limit is just to prevent going to lala land if something breaks.
		 */
        while ((done == 0) && (iters < maxiters)) {
            iters++;
            // line->w = w;
            // line->s = s;
			/* don't bother with loss calculations while we are iterating */
            // coupled_microstrip_calc_int(line, line->freq, 0);
            ze0 = Z0e_calc(w, s, H, T, f);
            zo0 = Z0o_calc(w, s, H, T, f);

			/* check for convergence */
            err = Math.pow((ze0 - Z0e), 2.0) + Math.pow((zo0 - Z0o), 2.0);
            if (err < cval) {
                done = 1;
            } else {
				/* approximate the first jacobian */
                // line->w = w + delta;
                // line->s = s;
                // coupled_microstrip_calc_int (line, line->freq, 0);
                ze1 = Z0e_calc(w + delta, s, H, T, f);
                zo1 = Z0o_calc(w + delta, s, H, T, f);

                // line->w = w;
                // line->s = s + delta;
                // coupled_microstrip_calc_int (line, line->freq, 0);
                ze2 = Z0e_calc(w, s + delta, H, T, f);
                zo2 = Z0o_calc(w, s + delta, H, T, f);

                dedw = (ze1 - ze0) / delta;
                dodw = (zo1 - zo0) / delta;
                deds = (ze2 - ze0) / delta;
                dods = (zo2 - zo0) / delta;

				/* find the determinate */
                d = dedw * dods - deds * dodw;

				/* estimate the new solution */
                dw = -1.0 * ((ze0 - Z0e) * dods - (zo0 - Z0o) * deds) / d;
                if (Math.abs(dw) > 0.1 * w) {
                    if (dw > 0.0)
                        dw = 0.1 * w;
                    else
                        dw = -0.1 * w;
                }
                w = Math.abs(w + dw);

                ds = ((ze0 - Z0e) * dodw - (zo0 - Z0o) * dedw) / d;
                if (Math.abs(ds) > 0.1 * s) {
                    if (ds > 0.0)
                        ds = 0.1 * s;
                    else
                        ds = -0.1 * s;
                }
                s = Math.abs(s + ds);
            }
        }

        // Z0e = Z0_e_f_calc(W, L, S, H, T, er, f);
        // Z0o = Z0_o_f_calc(W, L, S, H, T, er, f);

        W = w;
        S = s;
        // coupled_microstrip_calc_int (line, line->freq, 0);
        L = l * len / Eeff_calc(l, f);
    }

    private double MIL2M(double x) {
        return (x * 25.4e-6);
    }

	/*
	 * private double MICRON2MIL(double x) { return (x / 25.4); }
	 */
}
