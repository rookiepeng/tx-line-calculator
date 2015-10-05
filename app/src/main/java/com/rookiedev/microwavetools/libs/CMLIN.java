package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 8/20/13.
 */
public class CMLIN {
    //private double W, S, L, Z0, k, Z0o, Z0e, Eeff, f, er, H, T;
    private boolean use_z0k;
    private Line CMLINLine;

    public CMLIN(Line CMLINLine, boolean usez0k) {
        this.CMLINLine = CMLINLine;
        use_z0k = usez0k;
    }

    /*function [z0e,z0o,len,loss,kev,kodd,deltale,deltalo]=cmlicalc(w,l,s,f,subs,flag)*/
/* CMLICALC   Analyze coupled microstrip transmission line from physical parameters
 *
 *  [z0e,z0o,len,loss,kev,kodd]=cmlicalc(w,l,s,f,subs)
 *  calculates:
 *    z0e   = even mode characteristic impedance (ohms)
 *    z0o   = odd mode characteristic impedance (ohms)
 *    len   = electrical length (degrees)
 *    loss  = insertion loss (dB)
 *    kev   = even mode effective relative permitivity
 *    kodd  = odd mode effective relative permitivity
 *
 *  from:
 *    w     = microstrip line width (mils)
 *    l     = microstrip line length (mils)
 *    s     = spacing between lines (mils)
 *    f     = frequency (Hz)
 *    subs  = substrate parameters.  See TRSUBS for details.
 *
 *          |<--W-->|<---S--->|<--W-->|
 *           _______           _______
 *          | metal |         | metal |
 *   ----------------------------------------------
 *  (  dielectric,er                      /|\     (
 *   )                                 H   |       )
 *  (                                     \|/     (
 *   ----------------------------------------------
 *   /////////////////ground///////////////////////
 *
 *  Part of the Filter Design Toolbox
 *  See Also:  MLICALC, MLISYN, CMLISYN, TRSUBS

 *  Dan McMahill, 7/17/97
 *  Copyright (C) 1997 by Dan McMahill.

 *  Reference:
 *
 *  The equations for effective permittivity and characteristic
 *  impedance for the coupled lines are from:
 *
 *    Kirschning and Jansen (MTT):
 *    Manfred Kirschning and Rolf Jansen, "Accurate Wide-Range Design Equations for
 *      the Frequency-Dependent Characteristic of Parallel Coupled Microstrip
 *      Lines", IEEE Transactions on Microwave Theory and Techniques, Vol MTT-32,
 *      No 1, January 1984, p83-90.
 *      corrections in MTT-33, No 3, March 1985, p. 288
 *
 *  Effective permittivity and characteristic impedance for a single
 *  microstrip which is used in the Kirschning and Jansen (MTT) paper
 *  is from:
 *
 *    E. Hammerstad and O. Jensen, "Accurate Models for Microstrip Computer-
 *      Aided Design" IEEE MTT-S, International Symposium Digest, Washington
 *      D.C., May 1980, pp. 407-409
 *
 *    Kirschning and Jansen (EL):
 *    M. Kirschning and R. H. Jansen, "Accurate Model for Effective Dielectric
 *      Constant of Microstrip with Validity up to Millimetre-Wave Frequencies"
 *      Electronics Letters, Vol 18, No. 6, March 18th, 1982, pp 272-273.
 *
 *  Kirschning and Jansen give a couple of references for where to go
 *  for loss equations.  Hammerstad and Jensen present equations where
 *  the conductor losses depend on a current distribution factor.  It
 *  is perhaps easier to use Wheelers incremental inductance rule.
 *  The dielectric losses use the standard dielectric fill factors
 *  calculated from the even and odd mode effective dielectric constants.
 *
 *
 *  I must acknowledge the transcalc project,
 *  http://transcalc.sourceforge.net
 *  They have an independent implementation of the same equations
 *  which provided something to compare to.  By comparing all of my
 *  intermediate results with all of theirs I found one bug in their
 *  code and one in mine.  A win for everyone!
 *
 */

    private int coupledMicrostripAna(int do_loss) {

        // input physical dimensions
        double w, l, s;
        // substrate parameters
        double h, er, rho, tand, t, rough;
        double u, g, deltau, deltau1, deltaur, T, V, EFE0, EF, AO, BO, CO, DO;
        double EFO0, fn;
        double P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15;
        double FEF, FOF, EFEF, EFOF, ZL0;
        double Q0, Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8, Q9, Q10, Q11, Q12, Q13, Q14;
        double Q15, Q16, Q17, Q18, Q19, Q20, Q21, Q22, Q23, Q24, Q25, Q26;
        double Q27, Q28, Q29;
        double z0e0, z0o0;
        double xP1, xP2, xP3, xP4, xP, EFF;
        double R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16, R17;
        double ZLF;
        double RE, QE, PE, DE, CE;
        // even/odd mode impedance at the frequency of interest
        double z0ef, z0of;
        double v, len, uold, z1, z2, z3, z4, z5, d1, d2;
        // even/odd mode open end correction lengths
        double deltale, deltalo;
        double fnold;
        // for skindepth calculation
        double mu, sigma, depth;
        // even and odd mode conductor and dielectric losses in nepers/meter
        double alpha_c_even, alpha_c_odd, alpha_d_even, alpha_d_odd;
        double Ko, Rs, lc, Res, delta, Q_c_even, Q_c_odd;
        // correction factor for surface roughness
        double rough_factor;
        double f;

        w = CMLINLine.getMetalWidth();
        l = CMLINLine.getMetalLength();
        s = CMLINLine.getMetalSpace();
        // Substrate dielectric thickness
        h = CMLINLine.getSubHeight();
        // Substrate relative permittivity
        er = CMLINLine.getSubEpsilon();
        // Metal resistivity
        rho = CMLINLine.getRho();
        // Loss tangent of the dielectric material
        tand = CMLINLine.getTand();
        // Metal thickness
        t = CMLINLine.getMetalThick();
        // subs(6) = Metalization roughness
        rough = CMLINLine.getRough();

        f=CMLINLine.getFrequency();

        // Start of coupled microstrip calculations
        // Find u and correction factor for nonzero metal thickness
        u = w / h;
        g = s / h;

        // verify that the geometry is in a range where the accuracy is good.  This is given by (1) in Kirschning and Jansen (MTT)
        if ((u < 0.1) || (u > 10.0)) {
            //alert(_("Warning:  u=w/h=%g is outside the range for highly accurate results.\n"
            //        "For best accuracy 0.1 < u < 10.0\n"), u);
        }
        if ((g < 0.1) || (g > 10.0)) {
            //alert(_("Warning:  g=s/h=%g is outside the range for highly accurate results\n"
            //        "For best accuracy 0.1 < g < 10.0 "), g);
        }
        if ((er < 1.0) || (er > 18.0)) {
            //alert(_("Warning:  er=%g is outside the range for highly accurate results\n"
            //        "For best accuracy 1.0 < er < 18.0"), er);
        }

        if (t > 0.0) {
            // find normalized metal thickness
            T = t / h;

            // (6) from Hammerstad and Jensen
            deltau1 = (T / Constant.Pi) * Math.log(1.0 + 4.0 * Math.exp(1.0) / (T * Math.pow(coth(Math.sqrt(6.517 * u)), 2.0)));

            // (7) from Hammerstad and Jensen
            deltaur = 0.5 * (1.0 + 1.0 / Math.cosh(Math.sqrt(er - 1.0))) * deltau1;

            deltau = deltaur;
        } else {
            deltau = 0.0;
            deltau1 = 0.0;
            deltaur = 0.0;
        }

        // static even mode relative permittivity (f=0)
        // (3) from Kirschning and Jansen (MTT).  Accurate to 0.7 % over
        // "accurate" range
        V = u * (20.0 + Math.pow(g, 2.0)) / (10.0 + Math.pow(g, 2.0)) + g * Math.exp(-g);

        // note:  The equations listed in (3) in Kirschning and Jansen (MTT)
        // are the same as in Hammerstad and Jensen but with u in H&J
        // replaced with V from K&J.
        EFE0 = ee_HandJ(V, er);

        // static single strip, T=0, relative permittivity (f=0), width=w
        // This is from Hammerstad and Jensen.
        EF = ee_HandJ(u, er);

        // static odd mode relative permittivity (f=0)
        // This is (4) from Kirschning and Jansen (MTT).  Accurate to 0.5%.
        AO = 0.7287 * (EF - (er + 1.0) / 2.0) * (1.0 - Math.exp(-0.179 * u));
        BO = 0.747 * er / (0.15 + er);
        CO = BO - (BO - 0.207) * Math.exp(-0.414 * u);
        DO = 0.593 + 0.694 * Math.exp(-0.562 * u);

        // Note, this includes the published correction
        EFO0 = ((er + 1.0) / 2.0 + AO - EF) * Math.exp(-CO * (Math.pow(g, DO))) + EF;

        // normalized frequency (2) from Kirschning and Jansen (MTT)
        fn = 1e-6 * f * h;

        // check for range of validity for the dispersion equations.  p. 85
        // of Kirschning and Jansen (MTT) says fn <= 25 gives 1.4% accuracy.
        if (fn > 25.0) {
            //alert(_("Warning:  Frequency is higher (by %g %%) than the range\n"
            //        "over which the dispersion equations are accurate."), 100.0 * (fn - 25.0) / 25.0);
        }

        // even/odd mode relative permittivity including dispersion
        // even mode dispersion. (6) from Kirschning and Jansen (MTT)
        P1 = 0.27488 + (0.6315 + 0.525 / (Math.pow((1.0 + 0.0157 * fn), 20.0))) * u - 0.065683 * Math.exp(-8.7513 * u);
        P2 = 0.33622 * (1.0 - Math.exp(-0.03442 * er));
        P3 = 0.0363 * Math.exp(-4.6 * u) * (1.0 - Math.exp(-Math.pow((fn / 38.7), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((er / 15.916), 8.0)));
        P5 = 0.334 * Math.exp(-3.3 * Math.pow((er / 15.0), 3.0)) + 0.746;
        P6 = P5 * Math.exp(-Math.pow((fn / 18.0), 0.368));
        P7 = 1.0 + 4.069 * P6 * (Math.pow(g, 0.479)) * Math.exp(-1.347 * (Math.pow(g, 0.595)) - 0.17 * (Math.pow(g, 2.5)));
        FEF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844 * P7) * fn), 1.5763);

        // odd mode dispersion.  (7) from Kirschning and Jansen (MTT)
        P8 = 0.7168 * (1.0 + 1.076 / (1.0 + 0.0576 * (er - 1.0)));
        P9 = P8 - 0.7913 * (1.0 - Math.exp(-Math.pow((fn / 20.0), 1.424))) * Math.atan(2.481 * Math.pow((er / 8.0), 0.946));
        P10 = 0.242 * Math.pow((er - 1.0), 0.55);
        P11 = 0.6366 * (Math.exp(-0.3401 * fn) - 1.0) * Math.atan(1.263 * Math.pow((u / 3.0), 1.629));
        P12 = P9 + (1.0 - P9) / (1.0 + 1.183 * Math.pow(u, 1.376));
        P13 = 1.695 * P10 / (0.414 + 1.605 * P10);
        P14 = 0.8928 + 0.1072 * (1.0 - Math.exp(-0.42 * Math.pow((fn / 20.0), 3.215)));
        P15 = Math.abs(1.0 - 0.8928 * (1.0 + P11) * P12 * Math.exp(-P13 * (Math.pow(g, 1.092))) / P14);
        FOF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844) * fn * P15), 1.5763);

        // relative permittivities including dispersion via generalization
        // of Getsinger's dispersion relation.  This is (5) in Kirschning and Jansen (MTT).
        EFEF = er - (er - EFE0) / (1.0 + FEF);
        EFOF = er - (er - EFO0) / (1.0 + FOF);

        // static single strip, T=0, characteristic impedance (f=0) (Hammerstad and Jensen)
        ZL0 = z0_HandJ(u) / Math.sqrt(EF);

        // static even mode characteristic impedance (f=0)
        // (8) from Kirschning and Jansen (MTT) 0.6% accurate
        Q1 = 0.8695 * (Math.pow(u, 0.194));
        Q2 = 1.0 + 0.7519 * g + 0.189 * (Math.pow(g, 2.31));
        Q3 = 0.1975 + Math.pow((16.6 + Math.pow((8.4 / g), 6.0)), -0.387)
                + Math.log((Math.pow(g, 10.0)) / (1.0 + Math.pow((g / 3.4), 10.0))) / 241.0;
        Q4 = (2.0 * Q1 / Q2) / (Math.exp(-g) * (Math.pow(u, Q3)) + (2.0 - Math.exp(-g)) * (Math.pow(u, -Q3)));
        z0e0 = ZL0 * Math.sqrt(EF / EFE0) / (1.0 - (ZL0 / 377.0) * Math.sqrt(EF) * Q4);

        // static odd mode characteristic impedance (f=0)
        // (9) from Kirschning and Jansen (MTT) 0.6% accurate
        Q5 = 1.794 + 1.14 * Math.log(1.0 + 0.638 / (g + 0.517 * (Math.pow(g, 2.43))));
        Q6 = 0.2305
                + Math.log((Math.pow(g, 10.0)) / (1.0 + Math.pow((g / 5.8), 10.0))) / 281.3
                + Math.log(1.0 + 0.598 * (Math.pow(g, 1.154))) / 5.1;
        Q7 = (10.0 + 190.0 * g * g) / (1.0 + 82.3 * Math.pow(g, 3.0));
        Q8 = Math.exp(-6.5 - 0.95 * Math.log(g) - Math.pow((g / 0.15), 5.0));
        Q9 = Math.log(Q7) * (Q8 + 1.0 / 16.5);
        Q10 = (Q2 * Q4 - Q5 * Math.exp(Math.log(u) * Q6 * (Math.pow(u, -Q9)))) / Q2;
        z0o0 = ZL0 * Math.sqrt(EF / EFO0) / (1.0 - (ZL0 / 377.0) * Math.sqrt(EF) * Q10);

  /*
   * relative permittivity including dispersion
   * of single microstrip of width W, Tmet=0
   * Kirschning and Jansen (EL)
   */

        // normalized frequency (GHz-cm)
        // save fn
        fnold = fn;

        fn = 1.0e-7 * f * h;

        // (2) from Kirschning and Jansen (EL)
        xP1 = 0.27488 + (0.6315 + (0.525 / (Math.pow((1.0 + 0.157 * fn), 20.0)))) * u
                - 0.065683 * Math.exp(-8.7513 * u);
        xP2 = 0.33622 * (1.0 - Math.exp(-0.03442 * er));
        xP3 = 0.0363 * Math.exp(-4.6 * u) * (1.0 - Math.exp(-Math.pow((fn / 3.87), 4.97)));
        xP4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((er / 15.916), 8.0)));
        xP = xP1 * xP2 * Math.pow(((0.1844 + xP3 * xP4) * 10.0 * fn), 1.5763);

        // (1) from Kirschning and Jansen (EL)
        EFF = (EF + er * xP) / (1.0 + xP);
        // recall fn
        fn = fnold;

  /*
   * Characteristic Impedance of single strip, Width=W, Tmet=0
   * Jansen and Kirschning
   */
        // normalized frequency (GHz-mm)
        // save fn
        fnold = fn;
        fn = 1e-6 * f * h;

        // (1) from Jansen and Kirschning
        R1 = 0.03891 * (Math.pow(er, 1.4));
        R2 = 0.267 * (Math.pow(u, 7.0));
        R3 = 4.766 * Math.exp(-3.228 * (Math.pow(u, 0.641)));
        R4 = 0.016 + Math.pow((0.0514 * er), 4.524);
        R5 = Math.pow((fn / 28.843), 12.0);
        R6 = 22.20 * (Math.pow(u, 1.92));

        // (2) from Jansen and Kirschning
        R7 = 1.206 - 0.3144 * Math.exp(-R1) * (1.0 - Math.exp(-R2));
        R8 = 1.0 + 1.275 * (1.0 -
                Math.exp(-0.004625 * R3 *
                        Math.pow(er, 1.674) *
                        Math.pow(fn / 18.365, 2.745)));
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4)) * (Math.exp(-R6) / (1.0 + 1.2992 * R5));
        R9 = R9 * (Math.pow((er - 1.0), 6.0)) / (1.0 + 10.0 * Math.pow((er - 1.0), 6.0));

        // (3) from Jansen and Kirschning
        R10 = 0.00044 * (Math.pow(er, 2.136)) + 0.0184;
        R11 = (Math.pow((fn / 19.47), 6.0)) / (1.0 + 0.0962 * (Math.pow((fn / 19.47), 6.0)));
        R12 = 1.0 / (1.0 + 0.00245 * u * u);

        // (4) from Jansen and Kirschning
  /*
   * EF is the 0 frequency (static) relative dielectric constant for a
   * single microstrip.
   * EFF is the frequency dependent relative dielectric constant for a
   * single microstrip given in Kirschning and Jansen (EL)
   */
        R13 = 0.9408 * (Math.pow(EFF, R8)) - 0.9603;
        R14 = (0.9408 - R9) * Math.pow(EF, R8) - 0.9603;
        R15 = 0.707 * R10 * (Math.pow((fn / 12.3), 1.097));
        R16 = 1.0 + 0.0503 * er * er * R11 * (1.0 - Math.exp(-(Math.pow((u / 15), 6.0))));
        R17 = R7 * (1.0 - 1.1241 * (R12 / R16) * Math.exp(-0.026 * (Math.pow(fn, 1.15656)) - R15));

        // ZLF = zero thickness single strip characteristic impedance including dispersion
        ZLF = ZL0 * Math.pow((R13 / R14), R17);

        // Q0 = R17 from zero thickness, single microstrip
        Q0 = R17;

        // recall fn
        fn = fnold;

        // even mode characteristic impedance including dispersion
        // this is (10) from Kirschning and Jansen (MTT)
        Q11 = 0.893 * (1.0 - 0.3 / (1.0 + 0.7 * (er - 1.0)));
        Q12 = 2.121 * ((Math.pow((fn / 20.0), 4.91))
                / (1 + Q11 * Math.pow((fn / 20.0), 4.91))) * Math.exp(-2.87 * g) * Math.pow(g, 0.902);
        Q13 = 1.0 + 0.038 * Math.pow((er / 8.0), 5.1);
        Q14 = 1.0 + 1.203 * (Math.pow((er / 15.0), 4.0)) / (1.0 + Math.pow((er / 15.0), 4.0));
        Q15 = 1.887 * Math.exp(-1.5 * (Math.pow(g, 0.84))) * (Math.pow(g, Q14))
                / (1.0 + 0.41 * (Math.pow((fn / 15.0), 3.0)) * (Math.pow(u, (2.0 / Q13))) / (0.125 + Math.pow(u, (1.626 / Q13))));
        Q16 = (1.0 + 9.0 / (1.0 + 0.403 * Math.pow((er - 1.0), 2.0))) * Q15;
        Q17 = 0.394 * (1.0 - Math.exp(-1.47 * Math.pow((u / 7.0), 0.672))) * (1.0 - Math.exp(-4.25 * Math.pow((fn / 20.0), 1.87)));
        Q18 = 0.61 * (1.0 - Math.exp(-2.13 * Math.pow((u / 8), 1.593))) / (1.0 + 6.544 * Math.pow(g, 4.17));
        Q19 = 0.21 * (Math.pow(g, 4.0)) / ((1.0 + 0.18 * (Math.pow(g, 4.9))) * (1.0 + 0.1 * u * u) * (1 + Math.pow((fn / 24.0), 3.0)));
        Q20 = (0.09 + 1.0 / (1.0 + 0.1 * Math.pow((er - 1.0), 2.7))) * Q19;
        Q21 = Math.abs(1.0 - 42.54 * Math.pow(g, 0.133) * Math.exp(-0.812 * g) * Math.pow(u, 2.5) / (1.0 + 0.033 * Math.pow(u, 2.5)));

        RE = Math.pow((fn / 28.843), 12.0);
        QE = 0.016 + Math.pow((0.0514 * er * Q21), 4.524);
        PE = 4.766 * Math.exp(-3.228 * Math.pow(u, 0.641));
        DE = 5.086 * QE * (RE / (0.3838 + 0.386 * QE))
                * (Math.exp(-22.2 * Math.pow(u, 1.92)) / (1.0 + 1.2992 * RE))
                * ((Math.pow((er - 1.0), 6.0)) / (1.0 + 10.0 * Math.pow((er - 1.0), 6.0)));
        CE = 1.0 + 1.275 * (1.0 - Math.exp(-0.004625 * PE * Math.pow(er, 1.674) * Math.pow((fn / 18.365), 2.745)))
                - Q12 + Q16 - Q17 + Q18 + Q20;

  /*
   * EFF is the single microstrip effective dielectric constant from
   * Kirschning and Jansen (EL).
   * Note:  This line contains one of the published corrections.
   * The second EFF from the original paper is replaced by EF.
   */
        if (er > 1.0) {
            z0ef = z0e0
                    * (Math.pow((0.9408 * Math.pow(EFF, CE) - 0.9603), Q0))
                    / (Math.pow(((0.9408 - DE) * Math.pow(EF, CE) - 0.9603), Q0));
        } else {
            // no dispersion for er = 1.0
            z0ef = z0e0;
        }

        // odd mode characteristic impedance including dispersion
        // This is (11) from Kirschning and Jansen (MTT)
        if (er > 1.0) {
            Q29 = 15.16 / (1.0 + 0.196 * Math.pow((er - 1.0), 2.0));
            Q28 = 0.149 * (Math.pow((er - 1.0), 3.0)) / (94.5 + 0.038 * Math.pow((er - 1.0), 3.0));
            Q27 = 0.4 * Math.pow(g, 0.84)
                    * (1.0 + 2.5 * (Math.pow((er - 1.0), 1.5)) / (5.0 + Math.pow((er - 1.0), 1.5)));
            Q26 = 30.0
                    - 22.2 * ((Math.pow(((er - 1.0) / 13.0), 12.0))
                    / (1.0 + 3.0 * Math.pow(((er - 1.0) / 13.0), 12.0)))
                    - Q29;
            Q25 = (0.3 * fn * fn / (10.0 + fn * fn))
                    * (1.0 + 2.333 * (Math.pow((er - 1.0), 2.0)) / (5.0 + Math.pow((er - 1.0), 2.0)));
        } else {
            // it seems that pow(0.0, x) gives a floating exception
            Q29 = 15.16 / (1.0 + 0.196);
            Q28 = 0.149 / (94.5 + 0.038 * 1.0);
            Q27 = 0.4 * Math.pow(g, 0.84)
                    * (1.0 + 2.5 / (5.0 + 1.0));
            Q26 = 30.0
                    - 22.2 * ((1.0)
                    / (1.0 + 3.0))
                    - Q29;
            Q25 = (0.3 * fn * fn / (10.0 + fn * fn))
                    * (1.0 + 2.333 * (1.0) / (5.0 + 1.0));
        }
        Q24 = 2.506 * Q28 * Math.pow(u, 0.894)
                * (Math.pow(((1.0 + 1.3 * u) * fn / 99.25), 4.29)) / (3.575 + Math.pow(u, 0.894));
        Q23 = 1.0 + 0.005 * fn * Q27 / ((1.0 + 0.812 * Math.pow((fn / 15.0), 1.9)) *
                (1.0 + 0.025 * u * u));
        Q22 = 0.925 * (Math.pow((fn / Q26), 1.536)) / (1.0 + 0.3 * Math.pow((fn / 30.0), 1.536));

        // in this final expression, ZLF is the frequency dependent single
        // microstrip characteristic impedance from Jansen and Kirschning.
        if (er > 1.0) {
            z0of = ZLF + (z0o0 * Math.pow((EFOF / EFO0), Q22) - ZLF * Q23) / (1.0 + Q24 + (Math.pow((0.46 * g), 2.2)) * Q25);
        } else {
            z0of = z0o0;
        }

        // accuracy check from page 87 in Kirschning and Jansen (MTT)
        if (fn > 20.0) {
            //alert(_("Warning:  Normalized frequency is higher than the\n"
            //        "maximum for < 2.5 %% error in even/odd mode impedance."));
        }

        // electrical length
        // propagation velocity (meters/sec)
        v = Constant.LIGHTSPEED / Math.sqrt(Math.sqrt(EFEF * EFOF));
        // length in wavelengths
        len = l / (v / f);
        // convert to degrees
        len = 360.0 * len;

        // Dielectric Losses
        // loss in nepers/meter
        if (er > 1.0) {
            line -> losslen_ev = (Constant.Pi * f * Math.sqrt(EFE0) / Constant.LIGHTSPEED) * (er / EFE0) * ((EFE0 - 1.0) / (er - 1.0)) * tand;
            line -> losslen_odd = (Constant.Pi * f * Math.sqrt(EFO0) / Constant.LIGHTSPEED) * (er / EFO0) * ((EFO0 - 1.0) / (er - 1.0)) * tand;
        } else {
            // XXX verify this one
            line -> losslen_ev = 0.0;
            line -> losslen_odd = 0.0;
        }
        // remember these two for later
        alpha_d_even = line -> losslen_ev;
        alpha_d_odd = line -> losslen_odd;

        // calculate skin depth
        // conductivity
        sigma = 1.0 / rho;
        // permeability of free space Henries/meter
        mu = 4.0 * Constant.Pi * 1e-7;
        // skin depth in meters
        depth = Math.sqrt(1.0 / (Constant.Pi * f * mu * sigma));
        // warn the user if the loss calc is suspect.
        if (t > 0.0 && t < 3.0 * depth) {
            //alert("Warning:  The metal thickness is less than\n"
            //        "three skin depths.  Use the loss results with\n"
            //       "caution.\n");
        }
  /*
   * if the skinDepth is greater than Tmet, assume current
   * flows uniformly through  the conductor.  Then loss
   * is just calculated from the dc resistance of the
   * trace.  This is somewhat
   * suspect, but I dont have time right now to come up
   * with a better result.
   */
        alpha_c_even = 0.0;
        alpha_c_odd = 0.0;
        delta = depth;
        lc = 0.0;
        if (do_loss) {
            if (3.0 * depth <= t &&
                    3.0 * depth <= w) {
                // Find the current distribution factor.  This is (39) from Hammerstad and Jensen.
                Ko = Math.exp(-1.2 * Math.pow(Math.sqrt(er) * (z0e0 + z0o0) / (2.0 * Constant.FREESPACEZ0), 0.7));
                // skin resistance
                Rs = 1.0 / (sigma * depth);
    /*
     * Inductive quality factor (34) from Hammerstand and Jensen
	 *
	 * FIXME -- should these be the impedances with or without
	 * dispersion?  Does is really matter that much for a loss
	 * calculation which I think in general may be a little less accurate?
	 *
	 * The impedance here is the impedance in a homogeneous
	 * dielectric.  See (1) and (8) in Hammerstad and Jensen.
	 */
                Q_c_even = (Constant.Pi * z0e0 * Math.sqrt(EFEF) * h * f / (Rs * Constant.LIGHTSPEED)) * (u / Ko);
                Q_c_odd = (Constant.Pi * z0o0 * Math.sqrt(EFOF) * h * f / (Rs * Constant.LIGHTSPEED)) * (u / Ko);

                //#ifdef DEBUG_CALC
                //printf("%s():  Q_c_even = %g\n", __FUNCTION__, Q_c_even);
                //printf("%s():  Q_c_odd = %g\n", __FUNCTION__, Q_c_odd);
                //#endif

	/*
     * (38) from Hammerstad and Jensen says (after converting to
	 * nepers/meter)
	 * alpha_c = pi * lightpeed / (Q * f * sqrt(keff)
	 *         = pi * lambda_g / Q where lambda_g is the guided
	 * wavelength
	 *
	 * However, I think they have the lambda_g on the wrong side
	 *
	 */
                alpha_c_even = Constant.Pi * f * Math.sqrt(EFEF) / (Q_c_even * Constant.LIGHTSPEED);
                alpha_c_odd = Constant.Pi * f * Math.sqrt(EFOF) / (Q_c_odd * Constant.LIGHTSPEED);

                //#ifdef DEBUG_CALC
                //printf("%s():  Even mode conduction loss = %g dB/m\n",
                //        __FUNCTION__, 20.0 * log10(exp(1.0)) * alpha_c_even);
                //printf("%s():  Odd mode conduction loss = %g dB/m\n",
                //        __FUNCTION__, 20.0 * log10(exp(1.0)) * alpha_c_odd);
                //#endif

	/*
     * Instead try out Wheelers incremental inductance rule
	 * for some reason, I'm getting more loss (almost a factor of
	 * 2) from the above formulation.  Using the incremental
	 * inductance approach below, I get an answer which is close
	 * to single microstrip when I make the spacing between traces
	 * wide.  Until I can get an electromagnetic solver going, go
	 * with the incremental inductance approach.
	 */
                coupled_microstrip_line tmp_line;
                int rslt;
                double z1e, z1o, z2e, z2o;

                // clone the line
                tmp_line =*line;
                tmp_line.subs = microstrip_subs_new();
                *(tmp_line.subs) =*(line -> subs);

                tmp_line.subs->er = 1.0;
                rslt = coupled_microstrip_calc_int( & tmp_line, f, 0);
                z1e = tmp_line.z0e;
                z1o = tmp_line.z0o;

                tmp_line.w = line -> w - depth;
                tmp_line.s = line -> s + depth;
                tmp_line.subs->tmet = line -> subs -> tmet - depth;
                tmp_line.subs->h = line -> subs -> h + depth;
                rslt = coupled_microstrip_calc_int( & tmp_line, f, 0);
                z2e = tmp_line.z0e;
                z2o = tmp_line.z0o;
                free(tmp_line.subs);

                // conduction losses, nepers per meter
                alpha_c_even = (Constant.Pi * f / Constant.LIGHTSPEED) * (z2e - z1e) / z0ef;
                alpha_c_odd = (Constant.Pi * f / Constant.LIGHTSPEED) * (z2o - z1o) / z0of;

                //#ifdef DEBUG_CALC

                //printf("%s():  Odd mode conduction loss = %g dB/m (Via Wheelers incremental inductance)\n",
                //        __FUNCTION__, 20.0 * log10(exp(1.0)) * alpha_c_odd);
                //printf("%s():  Even mode conduction loss = %g dB/m (Via Wheelers incremental inductance)\n",
                //        __FUNCTION__, 20.0 * log10(exp(1.0)) * alpha_c_even);

                if (z2e > z1e) {
                    //printf("%s():  Q_c_even (via incremental inductance) = %g\n", __FUNCTION__, sqrt(EFEF) * z0ef / (z2e - z1e));
                } else {
                    //printf("%s():  Q_c_even (via incremental inductance) = Infinite\n", __FUNCTION__);
                }
                if (z2o > z1o) {
                    //printf("%s():  Q_c_odd  (via incremental inductance) = %g\n", __FUNCTION__, sqrt(EFOF) * z0of / (z2o - z1o));
                } else {
                    //printf("%s():  Q_c_odd  (via incremental inductance) = Infinite\n", __FUNCTION__);
                }
                //#endif
            }
            // "dc" case
            else if (t > 0.0) {
                // resistance per meter = 1/(Area*conductivity)
                Res = 1 / (w * t * sigma);

                // conduction losses, nepers per meter
                alpha_c_even = Res / (2.0 * z0e0);
                ;
                alpha_c_odd = Res / (2.0 * z0o0);
                lc = Res / (2.0 * Math.sqrt(z0e0 * z0o0));

                // dB/meter
                lc = 20.0 * Math.log10(Math.exp(1.0)) * lc;

                // change delta to be equal to the metal thickness fo use in surface roughness correction
                delta = t;

                // no conduction loss case
            } else {
                lc = 0.0;
            }
        }
        // loss in dB
        lc = lc * l;

  /* factor due to surface roughness
   * note that the equation in Fooks and Zakarevicius is slightly
   * errored.
   * the correct equation is penciled in my copy and was
   * found in Hammerstad and Bekkadal as well as Hammerstad and Jensen
   */
        // XXX this was roughmil/delta double check it
        rough_factor = 1.0 + (2.0 / Constant.Pi) * Math.atan(1.4 * Math.pow((rough / delta), 2.0));
        lc = lc * rough_factor;
        alpha_c_even = alpha_c_even * rough_factor;
        alpha_c_odd = alpha_c_odd * rough_factor;

        //#ifdef DEBUG_CALC
        //printf("%s():  rough_factor = %g \n", __FUNCTION__, rough_factor);
        //#endif

        // Single Line End correction (Kirschning, Jansen, and Koster)
        // deltal(2u,er) (per Kirschning and Jansen (MTT) notation)
        uold = u;
        u = 2 * u;
        z1 = 0.434907 * ((Math.pow(EF, 0.81) + 0.26) / (Math.pow(EF, 0.81) - 0.189))
                * (Math.pow(u, 0.8544) + 0.236) / (Math.pow(u, 0.8544) + 0.87);
        z2 = 1.0 + Math.pow(u, 0.371) / (2.358 * er + 1.0);
        z3 = 1.0 + (0.5274 * Math.atan(0.084 * (Math.pow(u, (1.9413 / z2))))) / Math.pow(EF, 0.9236);
        z4 = 1.0 + 0.0377 * Math.atan(0.067 * Math.pow(u, 1.456)) * (6.0 - 5.0 * Math.exp(0.036 * (1.0 - er)));
        z5 = 1.0 - 0.218 * Math.exp(-7.5 * u);
        d2 = h * z1 * z3 * z5 / z4;

        // deltal(u,er) (per Kirschning and Jansen (MTT) notation)
        u = uold;
        z1 = 0.434907 * ((Math.pow(EF, 0.81) + 0.26) / (Math.pow(EF, 0.81) - 0.189))
                * (Math.pow(u, 0.8544) + 0.236) / (Math.pow(u, 0.8544) + 0.87);
        z2 = 1 + Math.pow(u, 0.371) / (2.358 * er + 1);
        z3 = 1 + (0.5274 * Math.atan(0.084 * (Math.pow(u, (1.9413 / z2))))) / Math.pow(EF, 0.9236);
        z4 = 1 + 0.0377 * Math.atan(0.067 * Math.pow(u, 1.456)) * (6.0 - 5.0 * Math.exp(0.036 * (1.0 - er)));
        z5 = 1 - 0.218 * Math.exp(-7.5 * u);
        d1 = h * z1 * z3 * z5 / z4;

        // Even and Odd Mode End corrections (12) and (13) from Kirschning and Jansen (MTT)
        R1 = 1.187 * (1.0 - Math.exp(-0.069 * Math.pow(u, 2.1)));
        R2 = 0.343 * Math.pow(u, 0.6187) + (0.45 * er / (1.0 + er)) * (Math.pow(u, (1.357 + 1.65 / (1.0 + 0.7 * er))));
        R3 = 0.2974 * (1.0 - Math.exp(-R2));
        R4 = (0.271 + 0.0281 * er) * (Math.pow(g, (1.167 * er / (0.66 + er))))
                + (1.025 * er / (0.687 + er)) * (Math.pow(g, (0.958 * er / (0.706 + er))));

        deltale = (d2 - d1 + 0.0198 * h * Math.pow(g, R1)) * Math.exp(-0.328 * Math.pow(g, 2.244)) + d1;
        deltalo = (d1 - h * R3) * (1.0 - Math.exp(-R4)) + h * R3;

        // [z0e,z0o,len,loss,kev,kodd]=cmlicalc(w,l,s,f,subs)
        // copy over the results
        line -> z0e = z0ef;
        line -> z0o = z0of;
        line -> z0 = Math.sqrt(z0ef * z0of);
        line -> kev = EFEF;
        line -> kodd = EFOF;

        // coupling coefficient
        line -> k = (z0ef - z0of) / (z0ef + z0of);
        line -> deltale = deltale;
        line -> deltalo = deltalo;

        // electrical length
        line -> len = len;

        // skin depth in m
        line -> skindepth = depth;

        // incremental circuit model
        line -> Lev = line -> z0e * sqrt(line -> kev) / Constant.LIGHTSPEED;
        line -> Cev = sqrt(line -> kev) / (line -> z0e * Constant.LIGHTSPEED);
        line -> Lodd = line -> z0o * sqrt(line -> kodd) / Constant.LIGHTSPEED;
        line -> Codd = sqrt(line -> kodd) / (line -> z0o * Constant.LIGHTSPEED);
        line -> Rev = alpha_c_even * 2.0 * z0ef;
        line -> Gev = 2.0 * alpha_d_even / z0ef;
        line -> Rodd = alpha_c_odd * 2.0 * z0of;
        line -> Godd = 2.0 * alpha_d_odd / z0of;

        // loss in dB/meter
        line -> losslen_ev = 20.0 * Math.log10(Math.exp(1.0)) * (alpha_c_even + alpha_d_even);
        line -> losslen_odd = 20.0 * Math.log10(Math.exp(1.0)) * (alpha_c_odd + alpha_d_odd);

        // loss in dB
        line -> loss_ev = l * line -> losslen_ev;
        line -> loss_odd = l * line -> losslen_odd;
        return 0;
    }


   /* public double getZ0o() {
        return Z0_o_f_calc(W, S, H, f);
    }

    public double getZ0e() {
        return Z0_e_f_calc(W, S, H, f);
    }

    public double getEeff() {
        return Eeff_calc(W, L, S, H, f);
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

    private double Er_eff_f_calc(double W, double H, double f) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, EF;
        double fn;
        double xP1, xP2, xP3, xP4, xP;

		*//*
         * Start of coupled microstrip calculations
		 *//*

		*//* Find u and correction factor for nonzero metal thickness *//*

        u = W / H;
        // g = S / H;

		*//*
         * static single strip, T=0, relative permittivity (f=0), width=w This
		 * is from Hammerstad and Jensen.
		 *//*
        EF = ee_HandJ(u);

		*//*
         * check for range of validity for the dispersion equations. p. 85 of
		 * Kirschning and Jansen (MTT) says fn <= 25 gives 1.4% accuracy.
		 *//*
        // if (fn > 25.0) {
        // alert(_("Warning:  Frequency is higher (by %g %%) than the range\n"
        // "over which the dispersion equations are accurate."),100.0*(fn-25.0)/25.0);
        // }

        fn = 1.0e-7 * f * H;

		*//* (2) from Kirschning and Jansen (EL) *//*
        xP1 = 0.27488
                + (0.6315 + (0.525 / (Math.pow((1.0 + 0.157 * fn), 20.0)))) * u
                - 0.065683 * Math.exp(-8.7513 * u);
        xP2 = 0.33622 * (1.0 - Math.exp(-0.03442 * er));
        xP3 = 0.0363 * Math.exp(-4.6 * u)
                * (1.0 - Math.exp(-Math.pow((fn / 3.87), 4.97)));
        xP4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((er / 15.916), 8.0)));
        xP = xP1 * xP2 * Math.pow(((0.1844 + xP3 * xP4) * 10.0 * fn), 1.5763);

		*//* (1) from Kirschning and Jansen (EL) *//*
        return (EF + er * xP) / (1.0 + xP);
    }

    private double Z0_f_calc(double W, double H, double f) {
        double u, EF;
        double fn;
        double ZL0;
        double EFF;
        double R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16, R17;

        u = W / H;
        // g = S / H;

		*//*
         * static single strip, T=0, relative permittivity (f=0), width=w This
		 * is from Hammerstad and Jensen.
		 *//*
        EF = ee_HandJ(u);

        EFF = Er_eff_f_calc(W, H, f);

		*//*
         * #ifdef DEBUG_CALC printf("EFE0=%g, FEF=%g, EFEF=%g\n",EFE0,FEF,EFEF);
		 * printf("EFO0=%g, FOF=%g, EFOF=%g\n",EFO0,FOF,EFOF); #endif
		 *//*

		*//*
         * static single strip, T=0, characteristic impedance (f=0) (Hammerstad
		 * and Jensen)
		 *//*
        ZL0 = z0_HandJ(u) / Math.sqrt(EF);

        fn = 1e-6 * f * H;

		*//* (1) from Jansen and Kirschning *//*
        R1 = 0.03891 * (Math.pow(er, 1.4));
        R2 = 0.267 * (Math.pow(u, 7.0));
        R3 = 4.766 * Math.exp(-3.228 * (Math.pow(u, 0.641)));
        R4 = 0.016 + Math.pow((0.0514 * er), 4.524);
        R5 = Math.pow((fn / 28.843), 12.0);
        R6 = 22.20 * (Math.pow(u, 1.92));

		*//* (2) from Jansen and Kirschning *//*
        R7 = 1.206 - 0.3144 * Math.exp(-R1) * (1.0 - Math.exp(-R2));
        R8 = 1.0 + 1.275 * (1.0 - Math.exp(-0.004625 * R3 * Math.pow(er, 1.674)
                * Math.pow(fn / 18.365, 2.745)));
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4))
                * (Math.exp(-R6) / (1.0 + 1.2992 * R5));
        R9 = R9 * (Math.pow((er - 1.0), 6.0))
                / (1.0 + 10.0 * Math.pow((er - 1.0), 6.0));

		*//* (3) from Jansen and Kirschning *//*
        R10 = 0.00044 * (Math.pow(er, 2.136)) + 0.0184;
        R11 = (Math.pow((fn / 19.47), 6.0))
                / (1.0 + 0.0962 * (Math.pow((fn / 19.47), 6.0)));
        R12 = 1.0 / (1.0 + 0.00245 * u * u);

		*//* (4) from Jansen and Kirschning *//*
        *//*
         * EF is the 0 frequency (static) relative dielectric constant for a
		 * single microstrip. EFF is the frequency dependent relative dielectric
		 * constant for a single microstrip given in Kirschning and Jansen (EL)
		 *//*
        R13 = 0.9408 * (Math.pow(EFF, R8)) - 0.9603;
        R14 = (0.9408 - R9) * Math.pow(EF, R8) - 0.9603;
        R15 = 0.707 * R10 * (Math.pow((fn / 12.3), 1.097));
        R16 = 1.0 + 0.0503 * er * er * R11
                * (1.0 - Math.exp(-(Math.pow((u / 15), 6.0))));
        R17 = R7
                * (1.0 - 1.1241 * (R12 / R16)
                * Math.exp(-0.026 * (Math.pow(fn, 1.15656)) - R15));

		*//*
         * ZLF = zero thickness single strip characteristic impedance including
		 * dispersion
		 *//*
        return (ZL0 * Math.pow((R13 / R14), R17));
    }

    private double Er_eff_e_calc(double W, double S, double H) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, g, V;

		*//*
         * Start of coupled microstrip calculations
		 *//*

		*//* Find u and correction factor for nonzero metal thickness *//*

        u = W / H;
        g = S / H;

		*//*
         * static even mode relative permittivity (f=0) (3) from Kirschning and
		 * Jansen (MTT). Accurate to 0.7 % over "accurate" range
		 *//*
        V = u * (20.0 + Math.pow(g, 2.0)) / (10.0 + Math.pow(g, 2.0)) + g
                * Math.exp(-g);
		*//*
         * note: The equations listed in (3) in Kirschning and Jansen (MTT) are
		 * the same as in Hammerstad and Jensen but with u in H&J replaced with
		 * V from K&J.
		 *//*
        return ee_HandJ(V);
    }

    private double Er_eff_o_calc(double W, double S, double H) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, g, EF;
        double AO, BO, CO, DO;

		*//*
		 * Start of coupled microstrip calculations
		 *//*

		*//* Find u and correction factor for nonzero metal thickness *//*

        u = W / H;
        g = S / H;

		*//*
		 * static even mode relative permittivity (f=0) (3) from Kirschning and
		 * Jansen (MTT). Accurate to 0.7 % over "accurate" range
		 *//*
        // V = u * (20.0 + Math.pow(g, 2.0)) / (10.0 + Math.pow(g, 2.0)) + g
        // * Math.exp(-g);
		*//*
		 * note: The equations listed in (3) in Kirschning and Jansen (MTT) are
		 * the same as in Hammerstad and Jensen but with u in H&J replaced with
		 * V from K&J.
		 *//*
        // EFE0 = ee_HandJ(V, er);

		*//*
		 * static single strip, T=0, relative permittivity (f=0), width=w This
		 * is from Hammerstad and Jensen.
		 *//*
        EF = ee_HandJ(u);

		*//*
		 * static odd mode relative permittivity (f=0) This is (4) from
		 * Kirschning and Jansen (MTT). Accurate to 0.5%.
		 *//*
        AO = 0.7287 * (EF - (er + 1.0) / 2.0) * (1.0 - Math.exp(-0.179 * u));
        BO = 0.747 * er / (0.15 + er);
        CO = BO - (BO - 0.207) * Math.exp(-0.414 * u);
        DO = 0.593 + 0.694 * Math.exp(-0.562 * u);

		*//*
		 * Note, this includes the published correction
		 *//*
        return (((er + 1.0) / 2.0 + AO - EF)
                * Math.exp(-CO * (Math.pow(g, DO))) + EF);
    }

    private double Er_eff_e_f_calc(double W, double S, double H, double f) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, g, EFE0;
        double fn;
        // double u,g,deltau,deltau1,deltaur,T,V,EFE0,EF,AO,BO,CO,DO;
        // double EFO0,fn;
        double P1, P2, P3, P4, P5, P6, P7;
        double FEF;

        u = W / H;
        g = S / H;

        EFE0 = Er_eff_e_calc(W, S, H);

		*//* normalized frequency (2) from Kirschning and Jansen (MTT) *//*
        fn = 1e-6 * f * H; // mm - GHz
        // f: Hz
        // h: m

		*//*
		 * even/odd mode relative permittivity including dispersion
		 *//*

		*//* even mode dispersion. (6) from Kirschning and Jansen (MTT) *//*
        P1 = 0.27488 + (0.6315 + 0.525 / (Math.pow((1.0 + 0.0157 * fn), 20.0)))
                * u - 0.065683 * Math.exp(-8.7513 * u);
        P2 = 0.33622 * (1.0 - Math.exp(-0.03442 * er));
        P3 = 0.0363 * Math.exp(-4.6 * u)
                * (1.0 - Math.exp(-Math.pow((fn / 38.7), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((er / 15.916), 8.0)));
        P5 = 0.334 * Math.exp(-3.3 * Math.pow((er / 15.0), 3.0)) + 0.746;
        P6 = P5 * Math.exp(-Math.pow((fn / 18.0), 0.368));
        P7 = 1.0
                + 4.069
                * P6
                * (Math.pow(g, 0.479))
                * Math.exp(-1.347 * (Math.pow(g, 0.595)) - 0.17
                * (Math.pow(g, 2.5)));
        FEF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844 * P7) * fn), 1.5763);

		*//*
		 * relative permittivities including dispersion via generalization of
		 * Getsinger's dispersion relation. This is (5) in Kirschning and Jansen
		 * (MTT).
		 *//*
        return (er - (er - EFE0) / (1.0 + FEF));
    }

    private double Er_eff_o_f_calc(double W, double S, double H, double f) {

        double u, g;
        double EFO0, fn;
        // double u,g,deltau,deltau1,deltaur,T,V,EFE0,EF,AO,BO,CO,DO;
        // double EFO0,fn;
        double P1, P2, P3, P4, P8, P9, P10, P11, P12, P13, P14, P15;
        double FOF;

		*//* Find u and correction factor for nonzero metal thickness *//*

        u = W / H;
        g = S / H;

		*//*
		 * Note, this includes the published correction
		 *//*
        EFO0 = Er_eff_o_calc(W, S, H);

		*//* normalized frequency (2) from Kirschning and Jansen (MTT) *//*
        fn = 1e-6 * f * H; // mm - GHz
        // f: Hz
        // h: m

		*//*
		 * even/odd mode relative permittivity including dispersion
		 *//*

		*//* even mode dispersion. (6) from Kirschning and Jansen (MTT) *//*
        P1 = 0.27488 + (0.6315 + 0.525 / (Math.pow((1.0 + 0.0157 * fn), 20.0)))
                * u - 0.065683 * Math.exp(-8.7513 * u);
        P2 = 0.33622 * (1.0 - Math.exp(-0.03442 * er));
        P3 = 0.0363 * Math.exp(-4.6 * u)
                * (1.0 - Math.exp(-Math.pow((fn / 38.7), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((er / 15.916), 8.0)));
		*//*
		 * P5 = 0.334 * Math.exp(-3.3 * Math.pow((er / 15.0), 3.0)) + 0.746; P6
		 * = P5 * Math.exp(-Math.pow((fn / 18.0), 0.368)); P7 = 1.0 + 4.069 P6
		 * (Math.pow(g, 0.479)) Math.exp(-1.347 * (Math.pow(g, 0.595)) - 0.17
		 * (Math.pow(g, 2.5)));
		 *//*
        // FEF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844 * P7) * fn), 1.5763);

		*//* odd mode dispersion. (7) from Kirschning and Jansen (MTT) *//*
        P8 = 0.7168 * (1.0 + 1.076 / (1.0 + 0.0576 * (er - 1.0)));
        P9 = P8 - 0.7913 * (1.0 - Math.exp(-Math.pow((fn / 20.0), 1.424)))
                * Math.atan(2.481 * Math.pow((er / 8.0), 0.946));
        P10 = 0.242 * Math.pow((er - 1.0), 0.55);
        P11 = 0.6366 * (Math.exp(-0.3401 * fn) - 1.0)
                * Math.atan(1.263 * Math.pow((u / 3.0), 1.629));
        P12 = P9 + (1.0 - P9) / (1.0 + 1.183 * Math.pow(u, 1.376));
        P13 = 1.695 * P10 / (0.414 + 1.605 * P10);
        P14 = 0.8928 + 0.1072 * (1.0 - Math.exp(-0.42
                * Math.pow((fn / 20.0), 3.215)));
        P15 = Math.abs(1.0 - 0.8928 * (1.0 + P11) * P12
                * Math.exp(-P13 * (Math.pow(g, 1.092))) / P14);
        FOF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844) * fn * P15), 1.5763);

		*//*
		 * relative permittivities including dispersion via generalization of
		 * Getsinger's dispersion relation. This is (5) in Kirschning and Jansen
		 * (MTT).
		 *//*
        return (er - (er - EFO0) / (1.0 + FOF));
    }

    private double Z0_e_calc(double W, double S, double H) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, g, EFE0, EF;
        double ZL0;
        double Q1, Q2, Q3, Q4;

        u = W / H;
        g = S / H;

        EF = ee_HandJ(u);
		*//*
		 * static single strip, T=0, characteristic impedance (f=0) (Hammerstad
		 * and Jensen)
		 *//*
        ZL0 = z0_HandJ(u) / Math.sqrt(EF);
        EFE0 = Er_eff_e_calc(W, S, H);

		*//*
		 * static even mode characteristic impedance (f=0) (8) from Kirschning
		 * and Jansen (MTT) 0.6% accurate
		 *//*
        Q1 = 0.8695 * (Math.pow(u, 0.194));
        Q2 = 1.0 + 0.7519 * g + 0.189 * (Math.pow(g, 2.31));
        Q3 = 0.1975
                + Math.pow((16.6 + Math.pow((8.4 / g), 6.0)), -0.387)
                + Math.log((Math.pow(g, 10.0))
                / (1.0 + Math.pow((g / 3.4), 10.0))) / 241.0;
        Q4 = (2.0 * Q1 / Q2)
                / (Math.exp(-g) * (Math.pow(u, Q3)) + (2.0 - Math.exp(-g))
                * (Math.pow(u, -Q3)));
        return (ZL0 * Math.sqrt(EF / EFE0) / (1.0 - (ZL0 / 377.0)
                * Math.sqrt(EF) * Q4));
    }

    private double Z0_o_calc(double W, double S, double H) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, g, EF;
        double EFO0;
        double ZL0;
        double Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8, Q9, Q10;

        u = W / H;
        g = S / H;

        EF = ee_HandJ(u);
        ZL0 = z0_HandJ(u) / Math.sqrt(EF);
        EFO0 = Er_eff_o_calc(W, S, H);

		*//*
		 * static even mode characteristic impedance (f=0) (8) from Kirschning
		 * and Jansen (MTT) 0.6% accurate
		 *//*
        Q1 = 0.8695 * (Math.pow(u, 0.194));
        Q2 = 1.0 + 0.7519 * g + 0.189 * (Math.pow(g, 2.31));
        Q3 = 0.1975
                + Math.pow((16.6 + Math.pow((8.4 / g), 6.0)), -0.387)
                + Math.log((Math.pow(g, 10.0))
                / (1.0 + Math.pow((g / 3.4), 10.0))) / 241.0;
        Q4 = (2.0 * Q1 / Q2)
                / (Math.exp(-g) * (Math.pow(u, Q3)) + (2.0 - Math.exp(-g))
                * (Math.pow(u, -Q3)));

		*//*
		 * static odd mode characteristic impedance (f=0) (9) from Kirschning
		 * and Jansen (MTT) 0.6% accurate
		 *//*
        Q5 = 1.794 + 1.14 * Math.log(1.0 + 0.638 / (g + 0.517 * (Math.pow(g,
                2.43))));
        Q6 = 0.2305
                + Math.log((Math.pow(g, 10.0))
                / (1.0 + Math.pow((g / 5.8), 10.0))) / 281.3
                + Math.log(1.0 + 0.598 * (Math.pow(g, 1.154))) / 5.1;
        Q7 = (10.0 + 190.0 * g * g) / (1.0 + 82.3 * Math.pow(g, 3.0));
        Q8 = Math.exp(-6.5 - 0.95 * Math.log(g) - Math.pow((g / 0.15), 5.0));
        Q9 = Math.log(Q7) * (Q8 + 1.0 / 16.5);
        Q10 = (Q2 * Q4 - Q5 * Math.exp(Math.log(u) * Q6 * (Math.pow(u, -Q9))))
                / Q2;
        return (ZL0 * Math.sqrt(EF / EFO0) / (1.0 - (ZL0 / 377.0)
                * Math.sqrt(EF) * Q10));
    }

    private double Z0_e_f_calc(double W, double S, double H, double f) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, g, EF;
        double fn;
        double Q0, Q11, Q12, Q13, Q14;
        double Q15, Q16, Q17, Q18, Q19, Q20, Q21;
        double z0e0;
        double EFF;
        double R1, R2, R7, R10, R11, R12, R15, R16, R17;
        double RE, QE, PE, DE, CE;

		*//* even/odd mode impedance at the frequency of interest *//*
        double z0ef;

        u = W / H;
        g = S / H;
        fn = 1e-6 * f * H;

		*//* (1) from Jansen and Kirschning *//*
        R1 = 0.03891 * (Math.pow(er, 1.4));
        R2 = 0.267 * (Math.pow(u, 7.0));
        // R3 = 4.766 * Math.exp(-3.228 * (Math.pow(u, 0.641)));
        // R4 = 0.016 + Math.pow((0.0514 * er), 4.524);
        // R5 = Math.pow((fn / 28.843), 12.0);
        // R6 = 22.20 * (Math.pow(u, 1.92));

		*//* (2) from Jansen and Kirschning *//*
        R7 = 1.206 - 0.3144 * Math.exp(-R1) * (1.0 - Math.exp(-R2));
        // R8 = 1.0 + 1.275 * (1.0 - Math.exp(-0.004625 * R3 * Math.pow(er,
        // 1.674)
        // * Math.pow(fn / 18.365, 2.745)));
        // R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4))
        // * (Math.exp(-R6) / (1.0 + 1.2992 * R5));
        // R9 = R9 * (Math.pow((er - 1.0), 6.0))
        // / (1.0 + 10.0 * Math.pow((er - 1.0), 6.0));

		*//* (3) from Jansen and Kirschning *//*
        R10 = 0.00044 * (Math.pow(er, 2.136)) + 0.0184;
        R11 = (Math.pow((fn / 19.47), 6.0))
                / (1.0 + 0.0962 * (Math.pow((fn / 19.47), 6.0)));
        R12 = 1.0 / (1.0 + 0.00245 * u * u);

		*//* (4) from Jansen and Kirschning *//*
		*//*
		 * EF is the 0 frequency (static) relative dielectric constant for a
		 * single microstrip. EFF is the frequency dependent relative dielectric
		 * constant for a single microstrip given in Kirschning and Jansen (EL)
		 *//*
        // R13 = 0.9408 * (Math.pow(EFF, R8)) - 0.9603;
        // R14 = (0.9408 - R9) * Math.pow(EF, R8) - 0.9603;
        R15 = 0.707 * R10 * (Math.pow((fn / 12.3), 1.097));
        R16 = 1.0 + 0.0503 * er * er * R11
                * (1.0 - Math.exp(-(Math.pow((u / 15), 6.0))));
        R17 = R7
                * (1.0 - 1.1241 * (R12 / R16)
                * Math.exp(-0.026 * (Math.pow(fn, 1.15656)) - R15));

		*//* Q0 = R17 from zero thickness, single microstrip *//*
        Q0 = R17;

		*//* recall fn *//*

		*//*
		 * even mode characteristic impedance including dispersion this is (10)
		 * from Kirschning and Jansen (MTT)
		 *//*
        Q11 = 0.893 * (1.0 - 0.3 / (1.0 + 0.7 * (er - 1.0)));
        Q12 = 2.121
                * ((Math.pow((fn / 20.0), 4.91)) / (1 + Q11
                * Math.pow((fn / 20.0), 4.91))) * Math.exp(-2.87 * g)
                * Math.pow(g, 0.902);
        Q13 = 1.0 + 0.038 * Math.pow((er / 8.0), 5.1);
        Q14 = 1.0 + 1.203 * (Math.pow((er / 15.0), 4.0))
                / (1.0 + Math.pow((er / 15.0), 4.0));
        Q15 = 1.887
                * Math.exp(-1.5 * (Math.pow(g, 0.84)))
                * (Math.pow(g, Q14))
                / (1.0 + 0.41 * (Math.pow((fn / 15.0), 3.0))
                * (Math.pow(u, (2.0 / Q13)))
                / (0.125 + Math.pow(u, (1.626 / Q13))));
        Q16 = (1.0 + 9.0 / (1.0 + 0.403 * Math.pow((er - 1.0), 2.0))) * Q15;
        Q17 = 0.394 * (1.0 - Math.exp(-1.47 * Math.pow((u / 7.0), 0.672)))
                * (1.0 - Math.exp(-4.25 * Math.pow((fn / 20.0), 1.87)));
        Q18 = 0.61 * (1.0 - Math.exp(-2.13 * Math.pow((u / 8), 1.593)))
                / (1.0 + 6.544 * Math.pow(g, 4.17));
        Q19 = 0.21
                * (Math.pow(g, 4.0))
                / ((1.0 + 0.18 * (Math.pow(g, 4.9))) * (1.0 + 0.1 * u * u) * (1 + Math
                .pow((fn / 24.0), 3.0)));
        Q20 = (0.09 + 1.0 / (1.0 + 0.1 * Math.pow((er - 1.0), 2.7))) * Q19;
        Q21 = Math.abs(1.0 - 42.54 * Math.pow(g, 0.133) * Math.exp(-0.812 * g)
                * Math.pow(u, 2.5) / (1.0 + 0.033 * Math.pow(u, 2.5)));

        RE = Math.pow((fn / 28.843), 12.0);
        QE = 0.016 + Math.pow((0.0514 * er * Q21), 4.524);
        PE = 4.766 * Math.exp(-3.228 * Math.pow(u, 0.641));
        DE = 5.086
                * QE
                * (RE / (0.3838 + 0.386 * QE))
                * (Math.exp(-22.2 * Math.pow(u, 1.92)) / (1.0 + 1.2992 * RE))
                * ((Math.pow((er - 1.0), 6.0)) / (1.0 + 10.0 * Math.pow(
                (er - 1.0), 6.0)));
        CE = 1.0
                + 1.275
                * (1.0 - Math.exp(-0.004625 * PE * Math.pow(er, 1.674)
                * Math.pow((fn / 18.365), 2.745))) - Q12 + Q16 - Q17
                + Q18 + Q20;

		*//*
		 * EFF is the single microstrip effective dielectric constant from
		 * Kirschning and Jansen (EL). Note: This line contains one of the
		 * published corrections. The second EFF from the original paper is
		 * replaced by EF.
		 *//*
		*//*
		 * #ifdef DEBUG_CALC printf ("%s():  EFF = %g\n", __FUNCTION__, EFF);
		 * printf ("%s():  CE  = %g\n", __FUNCTION__, CE); printf
		 * ("%s():  DE  = %g\n", __FUNCTION__, DE); printf ("%s():  EF  = %g\n",
		 * __FUNCTION__, EF); #endif
		 *//*
        z0e0 = Z0_e_calc(W, S, H);
        EFF = Er_eff_f_calc(W, H, f);
        EF = ee_HandJ(u);

        if (er > 1.0) {
            z0ef = z0e0
                    * (Math.pow((0.9408 * Math.pow(EFF, CE) - 0.9603), Q0))
                    / (Math.pow(((0.9408 - DE) * Math.pow(EF, CE) - 0.9603), Q0));
        } else {
			*//* no dispersion for er = 1.0 *//*
            z0ef = z0e0;
        }
        return z0ef;
    }

    private double Z0_o_f_calc(double W, double S, double H, double f) {

		*//* input physical dimensions *//*
        // double w, l, s;

		*//* substrate parameters *//*
        // double h, er, rho, tand, t, rough;
        double u, g;
        double EFO0, fn;
        double EFOF;
        double Q22, Q23, Q24, Q25, Q26;
        double Q27, Q28, Q29;
        double z0o0;
        double ZLF;

		*//* even/odd mode impedance at the frequency of interest *//*
        double z0of;

        u = W / H;
        g = S / H;

        fn = 1e-6 * f * H;

		*//*
		 * odd mode characteristic impedance including dispersion This is (11)
		 * from Kirschning and Jansen (MTT)
		 *//*

        if (er > 1.0) {
            Q29 = 15.16 / (1.0 + 0.196 * Math.pow((er - 1.0), 2.0));
            Q28 = 0.149 * (Math.pow((er - 1.0), 3.0))
                    / (94.5 + 0.038 * Math.pow((er - 1.0), 3.0));
            Q27 = 0.4
                    * Math.pow(g, 0.84)
                    * (1.0 + 2.5 * (Math.pow((er - 1.0), 1.5))
                    / (5.0 + Math.pow((er - 1.0), 1.5)));
            Q26 = 30.0
                    - 22.2
                    * ((Math.pow(((er - 1.0) / 13.0), 12.0)) / (1.0 + 3.0 * Math
                    .pow(((er - 1.0) / 13.0), 12.0))) - Q29;
            Q25 = (0.3 * fn * fn / (10.0 + fn * fn))
                    * (1.0 + 2.333 * (Math.pow((er - 1.0), 2.0))
                    / (5.0 + Math.pow((er - 1.0), 2.0)));
        } else {
			*//* it seems that pow(0.0, x) gives a floating exception *//*
            Q29 = 15.16 / (1.0 + 0.196);
            Q28 = 0.149 / (94.5 + 0.038 * 1.0);
            Q27 = 0.4 * Math.pow(g, 0.84) * (1.0 + 2.5 / (5.0 + 1.0));
            Q26 = 30.0 - 22.2 * ((1.0) / (1.0 + 3.0)) - Q29;
            Q25 = (0.3 * fn * fn / (10.0 + fn * fn))
                    * (1.0 + 2.333 * (1.0) / (5.0 + 1.0));
        }

        Q24 = 2.506 * Q28 * Math.pow(u, 0.894)
                * (Math.pow(((1.0 + 1.3 * u) * fn / 99.25), 4.29))
                / (3.575 + Math.pow(u, 0.894));
        Q23 = 1.0
                + 0.005
                * fn
                * Q27
                / ((1.0 + 0.812 * Math.pow((fn / 15.0), 1.9)) * (1.0 + 0.025
                * u * u));
        Q22 = 0.925 * (Math.pow((fn / Q26), 1.536))
                / (1.0 + 0.3 * Math.pow((fn / 30.0), 1.536));

        ZLF = Z0_f_calc(W, H, f);
        z0o0 = Z0_o_calc(W, S, H);
        EFOF = Er_eff_o_f_calc(W, S, H, f);
        EFO0 = Er_eff_o_calc(W, S, H);

		*//*
		 * in this final expression, ZLF is the frequency dependent single
		 * microstrip characteristic impedance from Jansen and Kirschning.
		 *//*
        if (er > 1.0) {
            z0of = ZLF + (z0o0 * Math.pow((EFOF / EFO0), Q22) - ZLF * Q23)
                    / (1.0 + Q24 + (Math.pow((0.46 * g), 2.2)) * Q25);
        } else {
            z0of = z0o0;
        }
        return z0of;
    }

    private double Eeff_calc(double W, double L, double S, double H, double f) {
        double v, EFEF, EFOF;
		*//*
		 * electrical length
		 *//*
        EFEF = Er_eff_e_f_calc(W, S, H, f);
        EFOF = Er_eff_o_f_calc(W, S, H, f);

		*//* propagation velocity (meters/sec) *//*
        v = Constant.LIGHTSPEED / Math.sqrt(Math.sqrt(EFEF * EFOF));

		*//* length in wavelengths *//*
        return 360.0 * L / (v / f);
    }

    public void cmlin_syn() {
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

		*//* Substrate dielectric thickness (m) *//*
        // h = line->subs->h;

		*//* Substrate relative permittivity *//*
        // er = line->subs->er;

		*//* impedance and coupling *//*
        // z0 = line->z0;
        // k = line->k;

		*//* even/odd mode impedances *//*
        // z0e = line->z0e;
        // z0o = line->z0o;

        if (use_z0k) {
			*//* use z0 and k to calculate z0e and z0o *//*
            Z0o = Z0 * Math.sqrt((1.0 - k) / (1.0 + k));
            Z0e = Z0 * Math.sqrt((1.0 + k) / (1.0 - k));
        } else {
			*//* use z0e and z0o to calculate z0 and k *//*
            Z0 = Math.sqrt(Z0e * Z0o);
            k = (Z0e - Z0o) / (Z0e + Z0o);
        }

		*//* temp value for l used while finding w and s *//*
        l = 1000.0;
        // line->l=l;

		*//* limits on the allowed range for w *//*
        // wmin = MIL2M(0.5);
        // wmax = MIL2M(1000);

		*//* limits on the allowed range for s *//*
        // smin = MIL2M(0.5);
        // smax = MIL2M(1000);

		*//* impedance convergence tolerance (ohms) *//*
        // abstol = 1e-6;

		*//* width relative convergence tolerance (mils) (set to 0.1 micron) *//*
        // reltol = MICRON2MIL(0.1);

        maxiters = 50;

		*//*
		 * Initial guess at a solution
		 *//*
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

		*//*
		 * We should never need anything anywhere near maxiters iterations. This
		 * limit is just to prevent going to lala land if something breaks.
		 *//*
        while ((done == 0) && (iters < maxiters)) {
            iters++;
            // line->w = w;
            // line->s = s;
			*//* don't bother with loss calculations while we are iterating *//*
            // coupled_microstrip_calc_int(line, line->freq, 0);
            ze0 = Z0_e_f_calc(w, s, H, f);
            zo0 = Z0_o_f_calc(w, s, H, f);

			*//* check for convergence *//*
            err = Math.pow((ze0 - Z0e), 2.0) + Math.pow((zo0 - Z0o), 2.0);
            if (err < cval) {
                done = 1;
            } else {
				*//* approximate the first jacobian *//*
                // line->w = w + delta;
                // line->s = s;
                // coupled_microstrip_calc_int (line, line->freq, 0);
                ze1 = Z0_e_f_calc(w + delta, s, H, f);
                zo1 = Z0_o_f_calc(w + delta, s, H, f);

                // line->w = w;
                // line->s = s + delta;
                // coupled_microstrip_calc_int (line, line->freq, 0);
                ze2 = Z0_e_f_calc(w, s + delta, H, f);
                zo2 = Z0_o_f_calc(w, s + delta, H, f);

                dedw = (ze1 - ze0) / delta;
                dodw = (zo1 - zo0) / delta;
                deds = (ze2 - ze0) / delta;
                dods = (zo2 - zo0) / delta;

				*//* find the determinate *//*
                d = dedw * dods - deds * dodw;

				*//* estimate the new solution *//*
                dw = -1.0 * ((ze0 - Z0e) * dods - (zo0 - Z0o) * deds) / d;
                w = Math.abs(w + dw);

                ds = ((ze0 - Z0e) * dodw - (zo0 - Z0o) * dedw) / d;
                s = Math.abs(s + ds);
            }
        }

        // Z0e = Z0_e_f_calc(W, L, S, H, T, er, f);
        // Z0o = Z0_o_f_calc(W, L, S, H, T, er, f);

        W = w;
        S = s;
        // coupled_microstrip_calc_int (line, line->freq, 0);
        L = l * len / (Eeff_calc(W, l, S, H, f));

        //return (0);
    }

    private double MIL2M(double x) {
        return (x * 25.4e-6);
    }*/

    /*
     * Effective dielectric constant from Hammerstad and Jensen
     */
    static double ee_HandJ(double u, double er) {
        double A, B, E0;

        // (4) from Hammerstad and Jensen
        A = 1.0 + (1.0 / 49.0)
                * Math.log((Math.pow(u, 4.0) + Math.pow((u / 52.0), 2.0)) / (Math.pow(u, 4.0) + 0.432))
                + (1.0 / 18.7) * Math.log(1.0 + Math.pow((u / 18.1), 3.0));

        // (5) from Hammerstad and Jensen
        B = 0.564 * Math.pow(((er - 0.9) / (er + 3.0)), 0.053);

        // zero frequency effective permitivity.  (3) from Hammerstad and
        // Jensen.  This is ee(ur,er) thats used by (9) in Hammerstad and
        // Jensen.
        E0 = (er + 1.0) / 2.0 + ((er - 1.0) / 2.0) * Math.pow((1.0 + 10.0 / u), (-A * B));


        return E0;
    }


    /*
     * Characteristic impedance from (1) and (2) in Hammerstad and Jensen
     */
    static double z0_HandJ(double u) {
        double F, z01;

        // (2) from Hammerstad and Jensen.  'u' is the normalized width
        F = 6.0 + (2.0 * Constant.Pi - 6.0) * Math.exp(-Math.pow((30.666 / u), 0.7528));

        // (1) from Hammerstad and Jensen
        // XXX decide on which to use here
        z01 = (Constant.FREESPACEZ0 / (2 * Constant.Pi)) * Math.log(F / u + Math.sqrt(1.0 + Math.pow((2 / u), 2.0)));
        z01 = (377.0 / (2 * Constant.Pi)) * Math.log(F / u + Math.sqrt(1.0 + Math.pow((2 / u), 2.0)));

        return z01;
    }
}
