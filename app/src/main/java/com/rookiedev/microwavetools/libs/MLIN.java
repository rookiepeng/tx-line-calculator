package com.rookiedev.microwavetools.libs;

public class MLIN {
    private Line MLINLine;

    public MLIN(Line mlinLine) {
        MLINLine = mlinLine;
    }

    /*
    * flag=1 enables loss calculations
    * flag=0 disables loss calculations
    */
    private int microstrip_calc_int(double f, int flag) {
        int rslt = 0;
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

  /* Substrate dielectric thickness */
        //h = line->subs->h;
        h = MLINLine.getSubHeight();

  /* Substrate relative permittivity */
        //er = line->subs->er;
        er = MLINLine.getSubEpsilon();

  /* Metal resistivity */
        rho = line -> subs -> rho;

  /* Loss tangent of the dielectric material */
        tand = line -> subs -> tand;

  /* Metal thickness */
        //t = line->subs->tmet;
        t = MLINLine.getMetalThick();

  /*   subs(6) = Metalization roughness */
        rough = line -> subs -> rough;

        //#ifdef DEBUG_CALC
        //printf("starting microstrip_calc_int() with %g MHz and ",f/1.0e6);
        //if(flag == NOLOSS)
        //    printf("NOLOSS\n");
        //else
        //    printf("WITHLOSS\n");

        //printf("L = %g mil\n", M2MIL(l));
        //printf("W = %g mil\n", M2MIL(w));
        //printf("Tmet = %g mil\n", M2MIL(t));
        //printf("er = %g\n", er);
        //printf("\n");

        //#endif

  /*
   * Start of microstrip calculations
   */


  /* Find u and correction factor for nonzero metal thickness */
        u = w / h;

        if (t > 0.0) {
    /* find normalized metal thickness */
            T = t / h;

    /* (6) from Hammerstad and Jensen */
            deltau1 = (T / M_PI)
                    * log(1.0 + 4.0 * exp(1.0) / (T * pow(coth(sqrt(6.517 * u)), 2.0)));

    /* (7) from Hammerstad and Jensen */
            deltaur = 0.5 * (1.0 + 1.0 / cosh(sqrt(er - 1.0))) * deltau1;

            deltau = deltaur;

            //#ifdef DEBUG_CALC
            //printf("microstrip.c: microstrip_calc():  deltau1 = %g \n",deltau1);
            //printf("                                  deltaur = %g \n",deltaur);
            //printf("                                  t/h     = %g \n",T);
            //#endif
        } else {
            deltau = 0.0;
            deltau1 = 0.0;
            deltaur = 0.0;
        }


  /*
   * some test stuff to compare with Kobayashi
   * u = 2;
   * er=16;
   * hl0 = 1;
   * f=10e9;
   * l0 = 3e8/f;
   * h=hl0*l0;
   */

  /*
   * relative permittivity at f=0
   *  (Hammerstad and Jensen)
   */

        u1 = u + deltau1;
        ur = u + deltaur;

        //#ifdef DEBUG_CALC
        //printf("microstrip.c: microstrip_calc():  u  = %g \n",u);
        //printf("                                  u1 = %g \n",u1);
        //printf("                                  ur = %g \n",ur);
        //#endif


        E0 = ee_HandJ(ur, er);
        //#ifdef DEBUG_CALC
        //printf("microstrip.c: microstrip_calc():  E0 = %g \n",E0);
        //printf("    This is (3) from Hammerstad & Jensen and Y from\n");
        //printf("    the Rogers Corp. paper\n");
        //#endif

  /*
   * zero frequency characteristic impedance
   * (8) from Hammerstad and Jensen
   */
        z0 = z0_HandJ(ur) / sqrt(E0);

        //#ifdef DEBUG_CALC
        //printf("microstrip.c: microstrip_calc():  z0(0) = %g \n",z0);
        //printf("              This is (8) from Hammerstad & Jensen\n");
        //#endif

  /*
   * zero frequency effective permitivity.
   * (9) from Hammerstad and Jensen
   */
        EFF0 = E0 * pow(z0_HandJ(u1) / z0_HandJ(ur), 2.0);

        //#ifdef DEBUG_CALC
        //printf("microstrip.c: microstrip_calc():  EFF0 = %g \n",EFF0);
        //printf("              This is (9) from Hammerstad & Jensen\n");
        //#endif

  /*
   * relative permittivity including dispersion
   *  (Kirschning and Jansen)
   */

  /* normalized frequency (GHz-cm)*/
        fn = 1e-7 * f * h;

  /* (2) from Kirschning and Jansen */
        P1 = 0.27488 +
                (0.6315 + (0.525 / (pow((1.0 + 0.157 * fn), 20.0)))) * u
                - 0.065683 * exp(-8.7513 * u);
        P2 = 0.33622 * (1.0 - exp(-0.03442 * er));
        P3 = 0.0363 * exp(-4.6 * u) * (1.0 - exp(-pow((fn / 3.87), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - exp(-pow((er / 15.916), 8.0)));
        P = P1 * P2 * pow(((0.1844 + P3 * P4) * 10.0 * fn), 1.5763);

  /* (1) from Kirschning and Jansen */
        EF = (EFF0 + er * P) / (1.0 + P);


   /*
    * Characteristic Impedance
    *  (Jansen and Kirschning)
    */

   /* normalized frequency (GHz-mm) */
        fn = 1.0e-6 * f * h;


   /* (1) from Jansen and Kirschning */
        R1 = 0.03891 * pow(er, 1.4);
        R2 = 0.267 * pow(u, 7.0);
        R3 = 4.766 * exp(-3.228 * pow(u, 0.641));
        R4 = 0.016 + pow((0.0514 * er), 4.524);
        R5 = pow((fn / 28.843), 12.0);
        R6 = 22.20 * pow(u, 1.92);

   /* (2) from Jansen and Kirschning */
        R7 = 1.206 - 0.3144 * exp(-R1) * (1.0 - exp(-R2));
        R8 = 1.0 + 1.275 * (1.0 -
                exp(-0.004625 * R3 *
                        pow(er, 1.674) *
                        pow(fn / 18.365, 2.745)));
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4)) * (exp(-R6) / (1.0 + 1.2992 * R5));
        R9 = R9 * pow((er - 1.0), 6.0) / (1.0 + 10.0 * pow((er - 1), 6.0));

   /* (3) from Jansen and Kirschning */
        R10 = 0.00044 * pow(er, 2.136) + 0.0184;
        R11 = pow((fn / 19.47), 6.0) / (1.0 + 0.0962 * pow((fn / 19.47), 6.0));
        R12 = 1.0 / (1.0 + 0.00245 * u * u);

   /* (4) from Jansen and Kirschning */
        R13 = 0.9408 * pow(EF, R8) - 0.9603;
        R14 = (0.9408 - R9) * pow(EFF0, R8) - 0.9603;
        R15 = 0.707 * R10 * pow((fn / 12.3), 1.097);
        R16 = 1.0 + 0.0503 * er * er * R11 * (1.0 - exp(-pow((u / 15), 6.0)));
        R17 = R7 * (1.0 - 1.1241 * (R12 / R16) * exp(-0.026 * pow(fn, 1.15656) - R15));


        //#ifdef DEBUG_CALC
        //printf("microstrip.c: microstrip_calc()  R13 = %g, R14 = %g, R17=%g\n",
        //        R13,R14,R17);
        //#endif

   /* (5) from Jansen and Kirschning */
        z0 = z0 * pow((R13 / R14), R17);

   /*
    * propagation velocity (meters/sec)
    */
        v = LIGHTSPEED / sqrt(EF);

   /*
    * delay on line
    */
        delay = line -> l / v;

   /*
    * End correction
    *  (Kirschning, Jansen, and Koster)
    */
   /* DAN should decide what to do about this */
        z1 = 0.434907 * ((pow(EF, 0.81) + 0.26) / (pow(EF, 0.81) - 0.189))
                * (pow(u, 0.8544) + 0.236) / (pow(u, 0.8544) + 0.87);
        z2 = 1.0 + (pow(u, 0.371)) / (2.358 * er + 1.0);
        z3 = 1.0 + (0.5274 * atan(0.084 * (pow(u, (1.9413 / z2))))) / (pow(EF, 0.9236));
        z4 = 1.0
                + 0.0377 * atan(0.067 * (pow(u, 1.456))) * (6.0 - 5.0 * exp(0.036 * (1.0 - er)));
        z5 = 1.0 - 0.218 * exp(-7.5 * u);

        deltal = h * z1 * z3 * z5 / z4;


   /* find the incremental circuit model */
   /*
    * find L and C from the impedance and velocity
    *
    * z0 = sqrt(L/C), v = 1/sqrt(LC)
    *
    * this gives the result below
    */
        L = z0 / v;
        C = 1.0 / (z0 * v);

   /* resistance and conductance will be updated below */
        R = 0.0;
        G = 0.0;

        if (flag == WITHLOSS) {
       /* length in wavelengths */
            if (f > 0.0)
                len = (l) / (v / f);
            else
                len = 0.0;

       /* convert to degrees */
            len = 360.0 * len;



       /* effective relative permittivity */
            eeff = EF;


            line -> keff = eeff;
            line -> len = len;

       /* calculate loss */


       /*
    * Dielectric Losses
	*/

       /* loss in nepers/meter */

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
                ld = (M_PI * f / v) * (er / EF) * ((EF - 1.0) / (er - 1.0)) * tand;
            } else {
	 /* if er == 1, then this is probably a vacuum */
                ld = 0.0;
            }

            G = 2.0 * ld / z0;
            line -> alpha_d = ld;

       /* loss in dB/meter */
            ld = 20.0 * log10(exp(1.0)) * ld;

       /* loss in dB */
            ld = ld * l;

       /*
	* Conduction Losses
	*/


       /* calculate skin depth */

       /* conductivity */
            sigma = 1.0 / rho;

       /* permeability of free space */
            mu = 4.0 * M_PI * 1e-7;

       /* skin depth in meters */
            delta = sqrt(1.0 / (M_PI * f * mu * sigma));
            depth = delta;


       /* warn the user if the loss calc is suspect. */
            if (t < 3.0 * depth) {
                alert("Warning:  The metal thickness is less than\n"
                        "three skin depths.  Use the loss results with\n"
                        "caution.\n");
            }

       /*
	* if the skinDepth is greater than Tmet, assume current
	* flows uniformly through  the conductor.  Then loss
	* is just calculated from the dc resistance of the
	* trace.  This is somewhat
	* suspect, but I dont have time right now to come up
	* with a better result.
	*/
            if (depth <= t) {

	   /* store the substrate parameters */
	   /* XXX */
	   /* subsl = subs; */

                line -> subs -> er = 1.0;
                rslt = microstrip_calc_int(line, f, NOLOSS);
                if (rslt)
                    return rslt;
                z2 = line -> z0;
                //#ifdef DEBUG_CALC
                //printf("%s(): z2 = %g Ohms (er = 1.0, nom dimensions)\n",
                //        __FUNCTION__, z2);
                //#endif

                line -> subs -> h = h + delta;
                line -> subs -> tmet = t - delta;
                line -> w = w - delta;
                rslt = microstrip_calc_int(line, f, NOLOSS);
                if (rslt)
                    return rslt;
                z1 = line -> z0;
                //#ifdef DEBUG_CALC
                //printf("%s(): z1 = %g Ohms (er = 1.0, w=%g %s, h=%g %s, t=%g %s)\n",
                //        __FUNCTION__, z1,
                //        line->w/line->units_lwht->sf, line->units_lwht->name,
                //        line->subs->h/line->units_lwht->sf, line->units_lwht->name,
                //        line->subs->tmet/line->units_lwht->sf, line->units_lwht->name);
                //printf("%s(): delta = %g m (%g %s)\n", __FUNCTION__,
                //        delta, delta/line->units_lwht->sf, line->units_lwht->name);

                //printf("%s(): z1 - z2 = %g Ohms\n", __FUNCTION__, z1 - z2);
                //#endif

                line -> subs -> er = er;
                line -> subs -> h = h;
                line -> subs -> tmet = t;
                line -> w = w;

	   /* conduction losses, nepers per meter */
                lc = (M_PI * f / LIGHTSPEED) * (z1 - z2) / z0;

                R = lc * 2 * z0;
            }

	   /* "dc" case  */
            else if (t > 0.0) {
	   /* resistance per meter = 1/(Area*conductivity) */
                R = 1 / (line -> w * line -> subs -> tmet * sigma);

	   /* resistance per meter = 1/(Area*conductivity) */
                Res = 1 / (w * t * sigma);

	   /* conduction losses, nepers per meter */
                lc = Res / (2.0 * z0);

	   /*
	    * change delta to be equal to the metal thickness for
	    * use in surface roughness correction
	    */
                delta = t;

	   /* no conduction loss case */
            } else {
                lc = 0.0;
            }



       /* factor due to surface roughness
	* note that the equation in Fooks and Zakarevicius is slightly
	* errored.
	* the correct equation is penciled in my copy and was
	* found in Hammerstad and Bekkadal
	*/
            lc = lc * (1.0 + (2.0 / M_PI) * atan(1.4 * pow((rough / delta), 2.0)));

            line -> alpha_c = lc;

       /*
	* recalculate R now that we have the surface roughness in
	* place
	*/
            R = lc * 2.0 * z0;

            //#ifdef DEBUG_CALC
            //printf ("R (%g) = alpha_c (%g) * 2.0 * z0 (%g)\n", R, lc, z0);
            //#endif

       /* loss in dB/meter */
            lc = 20.0 * log10(exp(1.0)) * lc;

       /* loss in dB */
            lc = lc * l;

       /*
	* Total Loss
	*/

            loss = ld + lc;

        } else {
            loss = 0.0;
            depth = 0.0;
        }

   /*  store results */
        line -> z0 = z0;

        line -> loss = loss;
        line -> losslen = loss / line -> l;
        line -> skindepth = depth;

        line -> deltal = deltal;
        line -> delay = delay;

        line -> Ls = L;
        line -> Rs = R;
        line -> Cs = C;
        line -> Gs = G;


        return (rslt);
    }

    /*
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

        double fu = 6 + (2 * Constant.Pi - 6)
                * Math.pow(Constant.Exp, -Math.pow(30.666 / U, 0.7528));
        return (n0 / 2 / Constant.Pi * Math.log(fu / U
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
                / Constant.Pi
                * Math.log(1 + 4 * Constant.Exp * H / T
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
                / Constant.Pi
                * Math.log(1 + 4 * Constant.Exp * H / T
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
                - 0.065683 * Math.pow(Constant.Exp, -8.7513 * U);
        double P2 = 0.33622 * (1 - Math.pow(Constant.Exp, -0.03442 * er));
        double P3 = 0.0363 * Math.pow(Constant.Exp, -4.6 * U)
                * (1 - Math.pow(Constant.Exp, -Math.pow(fn / 38.7, 4.97)));
        double P4 = 1 + 2.751 * (1 - Math.pow(Constant.Exp, -Math.pow(er / 15.916, 8)));
        double Pf = P1 * P2 * Math.pow((0.1844 + P3 * P4) * fn, 1.5763);
        if (T != 0) {
            return (er - (er - Er_eff_thickness(W, H, T)) / (1 + Pf));
        } else {
            return (er - (er - Er_eff(U)) / (1 + Pf));
        }
    }

    private double ZL_f(double W, double H, double T, double f) {
        double U = W / H;
        double fn = H / 39.37007874 * f;
        double R1 = 0.03891 * Math.pow(er, 1.4);
        double R2 = 0.267 * Math.pow(U, 7);
        double R3 = 4.766 * Math.pow(Constant.Exp, -3.228 * Math.pow(U, 0.641));
        double R4 = 0.016 + Math.pow(0.0514 * er, 4.524);
        double R5 = Math.pow(fn / 28.843, 12);
        double R6 = 22.2 * Math.pow(U, 1.92);
        double R7 = 1.206 - 0.3144 * Math.pow(Constant.Exp, -R1) * (1 - Math.pow(Constant.Exp, -R2));
        double R8 = 1.0 + 1.275 * (1.0 - Math.exp(-0.004625 * R3
                * Math.pow(er, 1.674) * Math.pow(fn / 18.365, 2.745)));
        double R9 = 5.086 * R4 * R5 / (0.3838 + 0.386 * R4) * Math.pow(Constant.Exp, -R6)
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
                * (1 - Math.pow(Constant.Exp, -Math.pow(U / 15, 6)));
        double R17 = R7
                * (1 - 1.1241 * R12 / R16
                * Math.pow(Constant.Exp, -0.026 * Math.pow(fn, 1.15656) - R15));
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
    }*/


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

        double A = ((er - 1) / (er + 1)) * (0.226 + 0.121 / er) + (Constant.Pi / 377)
                * Math.sqrt(2 * (er + 1)) * zd;

        double w_h = 4 / (0.5 * Math.pow(Constant.Exp, A) - Math.pow(Constant.Exp, -A));
        if (w_h > 2) {
            double B = Constant.Pi * 377 / (2 * zd * Math.sqrt(er));
            w_h = (2 / Constant.Pi)
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
