package com.rookiedev.microwavetools.libs;

/**
 * calculates:
 * z0e   = even mode characteristic impedance (ohms)
 * z0o   = odd mode characteristic impedance (ohms)
 * len   = electrical length (degrees)
 * loss  = insertion loss (dB)
 * kev   = even mode effective relative permitivity
 * kodd  = odd mode effective relative permitivity
 *
 * from:
 * w     = microstrip line width (m)
 * l     = microstrip line length (m)
 * s     = spacing between lines (m)
 * f     = frequency (Hz)
 * subs  = substrate parameters.  See TRSUBS for details.
 *
 * |<--W-->|<---S--->|<--W-->|
 * _______           _______
 * | metal |         | metal |
 * ----------------------------------------------
 * (  dielectric,er                      /|\     (
 * )                                 H   |       )
 * (                                     \|/     (
 * ----------------------------------------------
 * /////////////////ground///////////////////////
 * <p>
 * Part of the Filter Design Toolbox
 * See Also:  MLICALC, MLISYN, CMLISYN, TRSUBS
 * <p>
 * Dan McMahill, 7/17/97
 * Copyright (C) 1997 by Dan McMahill.
 * <p>
 * Reference:
 * <p>
 * The equations for effective permittivity and characteristic
 * impedance for the coupled lines are from:
 * <p>
 * Kirschning and Jansen (MTT):
 * Manfred Kirschning and Rolf Jansen, "Accurate Wide-Range Design Equations for
 * the Frequency-Dependent Characteristic of Parallel Coupled Microstrip
 * Lines", IEEE Transactions on Microwave Theory and Techniques, Vol MTT-32,
 * No 1, January 1984, p83-90.
 * corrections in MTT-33, No 3, March 1985, p. 288
 * <p>
 * Effective permittivity and characteristic impedance for a single
 * microstrip which is used in the Kirschning and Jansen (MTT) paper
 * is from:
 * <p>
 * E. Hammerstad and O. Jensen, "Accurate Models for Microstrip Computer-
 * Aided Design" IEEE MTT-S, International Symposium Digest, Washington
 * D.C., May 1980, pp. 407-409
 * <p>
 * Kirschning and Jansen (EL):
 * M. Kirschning and R. H. Jansen, "Accurate Model for Effective Dielectric
 * Constant of Microstrip with Validity up to Millimetre-Wave Frequencies"
 * Electronics Letters, Vol 18, No. 6, March 18th, 1982, pp 272-273.
 * <p>
 * Kirschning and Jansen give a couple of references for where to go
 * for loss equations.  Hammerstad and Jensen present equations where
 * the conductor losses depend on a current distribution factor.  It
 * is perhaps easier to use Wheelers incremental inductance rule.
 * The dielectric losses use the standard dielectric fill factors
 * calculated from the even and odd mode effective dielectric constants.
 * <p>
 * <p>
 * I must acknowledge the transcalc project,
 * http://transcalc.sourceforge.net
 * They have an independent implementation of the same equations
 * which provided something to compare to.  By comparing all of my
 * intermediate results with all of theirs I found one bug in their
 * code and one in mine.  A win for everyone!
 */

/**
 *  calculates:
 *    w     = microstrip line width (mils)
 *    l     = microstrip line length (mils)
 *    s     = spacing between lines (mils)
 *    loss  = insertion loss (dB)
 *    kev   = even mode effective relative permitivity
 *    kodd  = odd mode effective relative permitivity
 *
 *  from:
 *    z0e   = even mode characteristic impedance (ohms)
 *    z0o   = odd mode characteristic impedance (ohms)
 *    len   = electrical length (degrees)
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
 *  See Also:  CMLICALC, MLISYN, MLICALC, SLISYN, SLICALC, TRSUBS
 *
 *  Dan McMahill, 7/17/97
 *  Copyright (C) 1997 by Dan McMahill.
 *
 */

public class CMLIN {
    private boolean use_z0k;
    private Line CMLINLine;

    public CMLIN(Line CMLINLine, boolean usez0k) {
        this.CMLINLine = CMLINLine;
        use_z0k = usez0k;
    }

    // Effective dielectric constant from Hammerstad and Jensen
    private static double EffectiveDielectricConstant_Effer(double widthToHeight, double dielectricConstant) {
        double A, B;

        // (4) from Hammerstad and Jensen
        A = 1.0 + (1.0 / 49.0)
                * Math.log((Math.pow(widthToHeight, 4.0) + Math.pow((widthToHeight / 52.0), 2.0)) / (Math.pow(widthToHeight, 4.0) + 0.432))
                + (1.0 / 18.7) * Math.log(1.0 + Math.pow((widthToHeight / 18.1), 3.0));

        // (5) from Hammerstad and Jensen
        B = 0.564 * Math.pow(((dielectricConstant - 0.9) / (dielectricConstant + 3.0)), 0.053);

        // zero frequency effective permitivity.  (3) from Hammerstad and
        // Jensen.  This is ee(ur,er) thats used by (9) in Hammerstad and
        // Jensen.
        return ((dielectricConstant + 1.0) / 2.0 + ((dielectricConstant - 1.0) / 2.0) * Math.pow((1.0 + 10.0 / widthToHeight), (-A * B)));
    }

    // Characteristic impedance from (1) and (2) in Hammerstad and Jensen
    private static double CharacteristicImpedance_Z0(double widthToHeight) {
        double F, z01;

        // (2) from Hammerstad and Jensen.  'u' is the normalized width
        F = 6.0 + (2.0 * Constant.Pi - 6.0) * Math.exp(-Math.pow((30.666 / widthToHeight), 0.7528));

        // (1) from Hammerstad and Jensen
        // TODO XXX decide on which to use here
        //z01 = (Constant.FREESPACEZ0 / (2 * Constant.Pi)) * Math.log(F / u + Math.sqrt(1.0 + Math.pow((2 / u), 2.0)));
        z01 = (377.0 / (2 * Constant.Pi)) * Math.log(F / widthToHeight + Math.sqrt(1.0 + Math.pow((2 / widthToHeight), 2.0)));
        return z01;
    }

    private Line coupledMicrostripAna(Line CMLINLine, int do_loss) {

        // input physical dimensions
        double width, length, space;
        // substrate parameters
        double height, dielectric, resistivity, lossTangent, thickness, roughness;

        double widthToHeight, spaceToHeight, deltau, deltau1, deltaur, thicknessToHeight, V, EFE0, EF, AO, BO, CO, DO;
        double EFO0, frequencyToHeight;
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
        double frequency;

        width = CMLINLine.getMetalWidth();
        length = CMLINLine.getMetalLength();
        space = CMLINLine.getMetalSpace();
        frequency = CMLINLine.getFrequency();
        // Substrate dielectric thickness
        height = CMLINLine.getSubHeight();
        // Substrate relative permittivity
        dielectric = CMLINLine.getSubEpsilon();
        // Metal resistivity
        resistivity = CMLINLine.getRho();
        // Loss tangent of the dielectric material
        lossTangent = CMLINLine.getTand();
        // Metal thickness
        thickness = CMLINLine.getMetalThick();
        // subs(6) = Metalization roughness
        roughness = CMLINLine.getRough();

        // Start of coupled microstrip calculations
        // Find widthToHeight and correction factor for nonzero metal thickness
        widthToHeight = width / height;
        spaceToHeight = space / height;

        // verify that the geometry is in a range where the accuracy is good.  This is given by (1) in Kirschning and Jansen (MTT)
        if ((widthToHeight < 0.1) || (widthToHeight > 10.0)) {
            //alert(_("Warning:  u=w/h=%g is outside the range for highly accurate results.\n"
            //        "For best accuracy 0.1 < u < 10.0\n"), u);
        }
        if ((spaceToHeight < 0.1) || (spaceToHeight > 10.0)) {
            //alert(_("Warning:  g=s/h=%g is outside the range for highly accurate results\n"
            //        "For best accuracy 0.1 < g < 10.0 "), g);
        }
        if ((dielectric < 1.0) || (dielectric > 18.0)) {
            //alert(_("Warning:  er=%g is outside the range for highly accurate results\n"
            //        "For best accuracy 1.0 < er < 18.0"), er);
        }

        if (thickness > 0.0) {
            // find normalized metal thickness
            thicknessToHeight = thickness / height;

            // (6) from Hammerstad and Jensen
            deltau1 = (thicknessToHeight / Constant.Pi) * Math.log(1.0 + 4.0 * Math.exp(1.0) / (thicknessToHeight * Math.pow(
                    Math.cosh(Math.sqrt(6.517 * widthToHeight))
                            / Math.sinh(Math.sqrt(6.517 * widthToHeight)), 2.0)));

            // (7) from Hammerstad and Jensen
            deltaur = 0.5 * (1.0 + 1.0 / Math.cosh(Math.sqrt(dielectric - 1.0))) * deltau1;

            deltau = deltaur;
        } else {
            deltau = 0.0;
            deltau1 = 0.0;
            deltaur = 0.0;
        }

        // static even mode relative permittivity (f=0)
        // (3) from Kirschning and Jansen (MTT).  Accurate to 0.7 % over
        // "accurate" range
        V = widthToHeight * (20.0 + Math.pow(spaceToHeight, 2.0)) / (10.0 + Math.pow(spaceToHeight, 2.0)) + spaceToHeight * Math.exp(-spaceToHeight);

        // note:  The equations listed in (3) in Kirschning and Jansen (MTT)
        // are the same as in Hammerstad and Jensen but with u in H&J
        // replaced with V from K&J.
        EFE0 = EffectiveDielectricConstant_Effer(V, dielectric);

        // static single strip, T=0, relative permittivity (f=0), width=w
        // This is from Hammerstad and Jensen.
        EF = EffectiveDielectricConstant_Effer(widthToHeight, dielectric);

        // static odd mode relative permittivity (f=0)
        // This is (4) from Kirschning and Jansen (MTT).  Accurate to 0.5%.
        AO = 0.7287 * (EF - (dielectric + 1.0) / 2.0) * (1.0 - Math.exp(-0.179 * widthToHeight));
        BO = 0.747 * dielectric / (0.15 + dielectric);
        CO = BO - (BO - 0.207) * Math.exp(-0.414 * widthToHeight);
        DO = 0.593 + 0.694 * Math.exp(-0.562 * widthToHeight);

        // Note, this includes the published correction
        EFO0 = ((dielectric + 1.0) / 2.0 + AO - EF) * Math.exp(-CO * (Math.pow(spaceToHeight, DO))) + EF;

        // normalized frequency (2) from Kirschning and Jansen (MTT)
        frequencyToHeight = 1e-6 * frequency * height;

        // check for range of validity for the dispersion equations.  p. 85
        // of Kirschning and Jansen (MTT) says fn <= 25 gives 1.4% accuracy.
        if (frequencyToHeight > 25.0) {
            //alert(_("Warning:  Frequency is higher (by %g %%) than the range\n"
            //        "over which the dispersion equations are accurate."), 100.0 * (fn - 25.0) / 25.0);
        }

        // even/odd mode relative permittivity including dispersion
        // even mode dispersion. (6) from Kirschning and Jansen (MTT)
        P1 = 0.27488 + (0.6315 + 0.525 / (Math.pow((1.0 + 0.0157 * frequencyToHeight), 20.0))) * widthToHeight - 0.065683 * Math.exp(-8.7513 * widthToHeight);
        P2 = 0.33622 * (1.0 - Math.exp(-0.03442 * dielectric));
        P3 = 0.0363 * Math.exp(-4.6 * widthToHeight) * (1.0 - Math.exp(-Math.pow((frequencyToHeight / 38.7), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((dielectric / 15.916), 8.0)));
        P5 = 0.334 * Math.exp(-3.3 * Math.pow((dielectric / 15.0), 3.0)) + 0.746;
        P6 = P5 * Math.exp(-Math.pow((frequencyToHeight / 18.0), 0.368));
        P7 = 1.0 + 4.069 * P6 * (Math.pow(spaceToHeight, 0.479)) * Math.exp(-1.347 * (Math.pow(spaceToHeight, 0.595)) - 0.17 * (Math.pow(spaceToHeight, 2.5)));
        FEF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844 * P7) * frequencyToHeight), 1.5763);

        // odd mode dispersion.  (7) from Kirschning and Jansen (MTT)
        P8 = 0.7168 * (1.0 + 1.076 / (1.0 + 0.0576 * (dielectric - 1.0)));
        P9 = P8 - 0.7913 * (1.0 - Math.exp(-Math.pow((frequencyToHeight / 20.0), 1.424))) * Math.atan(2.481 * Math.pow((dielectric / 8.0), 0.946));
        P10 = 0.242 * Math.pow((dielectric - 1.0), 0.55);
        P11 = 0.6366 * (Math.exp(-0.3401 * frequencyToHeight) - 1.0) * Math.atan(1.263 * Math.pow((widthToHeight / 3.0), 1.629));
        P12 = P9 + (1.0 - P9) / (1.0 + 1.183 * Math.pow(widthToHeight, 1.376));
        P13 = 1.695 * P10 / (0.414 + 1.605 * P10);
        P14 = 0.8928 + 0.1072 * (1.0 - Math.exp(-0.42 * Math.pow((frequencyToHeight / 20.0), 3.215)));
        P15 = Math.abs(1.0 - 0.8928 * (1.0 + P11) * P12 * Math.exp(-P13 * (Math.pow(spaceToHeight, 1.092))) / P14);
        FOF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844) * frequencyToHeight * P15), 1.5763);

        // relative permittivities including dispersion via generalization
        // of Getsinger's dispersion relation.  This is (5) in Kirschning and Jansen (MTT).
        EFEF = dielectric - (dielectric - EFE0) / (1.0 + FEF);
        EFOF = dielectric - (dielectric - EFO0) / (1.0 + FOF);

        // static single strip, T=0, characteristic impedance (f=0) (Hammerstad and Jensen)
        ZL0 = CharacteristicImpedance_Z0(widthToHeight) / Math.sqrt(EF);

        // static even mode characteristic impedance (f=0)
        // (8) from Kirschning and Jansen (MTT) 0.6% accurate
        Q1 = 0.8695 * (Math.pow(widthToHeight, 0.194));
        Q2 = 1.0 + 0.7519 * spaceToHeight + 0.189 * (Math.pow(spaceToHeight, 2.31));
        Q3 = 0.1975 + Math.pow((16.6 + Math.pow((8.4 / spaceToHeight), 6.0)), -0.387)
                + Math.log((Math.pow(spaceToHeight, 10.0)) / (1.0 + Math.pow((spaceToHeight / 3.4), 10.0))) / 241.0;
        Q4 = (2.0 * Q1 / Q2) / (Math.exp(-spaceToHeight) * (Math.pow(widthToHeight, Q3)) + (2.0 - Math.exp(-spaceToHeight)) * (Math.pow(widthToHeight, -Q3)));
        z0e0 = ZL0 * Math.sqrt(EF / EFE0) / (1.0 - (ZL0 / 377.0) * Math.sqrt(EF) * Q4);

        // static odd mode characteristic impedance (f=0)
        // (9) from Kirschning and Jansen (MTT) 0.6% accurate
        Q5 = 1.794 + 1.14 * Math.log(1.0 + 0.638 / (spaceToHeight + 0.517 * (Math.pow(spaceToHeight, 2.43))));
        Q6 = 0.2305
                + Math.log((Math.pow(spaceToHeight, 10.0)) / (1.0 + Math.pow((spaceToHeight / 5.8), 10.0))) / 281.3
                + Math.log(1.0 + 0.598 * (Math.pow(spaceToHeight, 1.154))) / 5.1;
        Q7 = (10.0 + 190.0 * spaceToHeight * spaceToHeight) / (1.0 + 82.3 * Math.pow(spaceToHeight, 3.0));
        Q8 = Math.exp(-6.5 - 0.95 * Math.log(spaceToHeight) - Math.pow((spaceToHeight / 0.15), 5.0));
        Q9 = Math.log(Q7) * (Q8 + 1.0 / 16.5);
        Q10 = (Q2 * Q4 - Q5 * Math.exp(Math.log(widthToHeight) * Q6 * (Math.pow(widthToHeight, -Q9)))) / Q2;
        z0o0 = ZL0 * Math.sqrt(EF / EFO0) / (1.0 - (ZL0 / 377.0) * Math.sqrt(EF) * Q10);

  /*
   * relative permittivity including dispersion
   * of single microstrip of width W, Tmet=0
   * Kirschning and Jansen (EL)
   */

        // normalized frequency (GHz-cm)
        // save fn
        fnold = frequencyToHeight;

        frequencyToHeight = 1.0e-7 * frequency * height;

        // (2) from Kirschning and Jansen (EL)
        xP1 = 0.27488 + (0.6315 + (0.525 / (Math.pow((1.0 + 0.157 * frequencyToHeight), 20.0)))) * widthToHeight
                - 0.065683 * Math.exp(-8.7513 * widthToHeight);
        xP2 = 0.33622 * (1.0 - Math.exp(-0.03442 * dielectric));
        xP3 = 0.0363 * Math.exp(-4.6 * widthToHeight) * (1.0 - Math.exp(-Math.pow((frequencyToHeight / 3.87), 4.97)));
        xP4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((dielectric / 15.916), 8.0)));
        xP = xP1 * xP2 * Math.pow(((0.1844 + xP3 * xP4) * 10.0 * frequencyToHeight), 1.5763);

        // (1) from Kirschning and Jansen (EL)
        EFF = (EF + dielectric * xP) / (1.0 + xP);
        // recall fn
        frequencyToHeight = fnold;

  /*
   * Characteristic Impedance of single strip, Width=W, Tmet=0
   * Jansen and Kirschning
   */
        // normalized frequency (GHz-mm)
        // save fn
        fnold = frequencyToHeight;
        frequencyToHeight = 1e-6 * frequency * height;

        // (1) from Jansen and Kirschning
        R1 = 0.03891 * (Math.pow(dielectric, 1.4));
        R2 = 0.267 * (Math.pow(widthToHeight, 7.0));
        R3 = 4.766 * Math.exp(-3.228 * (Math.pow(widthToHeight, 0.641)));
        R4 = 0.016 + Math.pow((0.0514 * dielectric), 4.524);
        R5 = Math.pow((frequencyToHeight / 28.843), 12.0);
        R6 = 22.20 * (Math.pow(widthToHeight, 1.92));

        // (2) from Jansen and Kirschning
        R7 = 1.206 - 0.3144 * Math.exp(-R1) * (1.0 - Math.exp(-R2));
        R8 = 1.0 + 1.275 * (1.0 -
                Math.exp(-0.004625 * R3 *
                        Math.pow(dielectric, 1.674) *
                        Math.pow(frequencyToHeight / 18.365, 2.745)));
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4)) * (Math.exp(-R6) / (1.0 + 1.2992 * R5));
        R9 = R9 * (Math.pow((dielectric - 1.0), 6.0)) / (1.0 + 10.0 * Math.pow((dielectric - 1.0), 6.0));

        // (3) from Jansen and Kirschning
        R10 = 0.00044 * (Math.pow(dielectric, 2.136)) + 0.0184;
        R11 = (Math.pow((frequencyToHeight / 19.47), 6.0)) / (1.0 + 0.0962 * (Math.pow((frequencyToHeight / 19.47), 6.0)));
        R12 = 1.0 / (1.0 + 0.00245 * widthToHeight * widthToHeight);

        // (4) from Jansen and Kirschning
  /*
   * EF is the 0 frequency (static) relative dielectric constant for a
   * single microstrip.
   * EFF is the frequency dependent relative dielectric constant for a
   * single microstrip given in Kirschning and Jansen (EL)
   */
        R13 = 0.9408 * (Math.pow(EFF, R8)) - 0.9603;

        R14 = (0.9408 - R9) * Math.pow(EF, R8) - 0.9603;
        R15 = 0.707 * R10 * (Math.pow((frequencyToHeight / 12.3), 1.097));
        R16 = 1.0 + 0.0503 * dielectric * dielectric * R11 * (1.0 - Math.exp(-(Math.pow((widthToHeight / 15), 6.0))));
        R17 = R7 * (1.0 - 1.1241 * (R12 / R16) * Math.exp(-0.026 * (Math.pow(frequencyToHeight, 1.15656)) - R15));

        // ZLF = zero thickness single strip characteristic impedance including dispersion
        ZLF = ZL0 * Math.pow((R13 / R14), R17);

        // Q0 = R17 from zero thickness, single microstrip
        Q0 = R17;

        // recall fn
        frequencyToHeight = fnold;

        // even mode characteristic impedance including dispersion
        // this is (10) from Kirschning and Jansen (MTT)
        Q11 = 0.893 * (1.0 - 0.3 / (1.0 + 0.7 * (dielectric - 1.0)));
        Q12 = 2.121 * ((Math.pow((frequencyToHeight / 20.0), 4.91))
                / (1 + Q11 * Math.pow((frequencyToHeight / 20.0), 4.91))) * Math.exp(-2.87 * spaceToHeight) * Math.pow(spaceToHeight, 0.902);
        Q13 = 1.0 + 0.038 * Math.pow((dielectric / 8.0), 5.1);
        Q14 = 1.0 + 1.203 * (Math.pow((dielectric / 15.0), 4.0)) / (1.0 + Math.pow((dielectric / 15.0), 4.0));
        Q15 = 1.887 * Math.exp(-1.5 * (Math.pow(spaceToHeight, 0.84))) * (Math.pow(spaceToHeight, Q14))
                / (1.0 + 0.41 * (Math.pow((frequencyToHeight / 15.0), 3.0)) * (Math.pow(widthToHeight, (2.0 / Q13))) / (0.125 + Math.pow(widthToHeight, (1.626 / Q13))));
        Q16 = (1.0 + 9.0 / (1.0 + 0.403 * Math.pow((dielectric - 1.0), 2.0))) * Q15;
        Q17 = 0.394 * (1.0 - Math.exp(-1.47 * Math.pow((widthToHeight / 7.0), 0.672))) * (1.0 - Math.exp(-4.25 * Math.pow((frequencyToHeight / 20.0), 1.87)));
        Q18 = 0.61 * (1.0 - Math.exp(-2.13 * Math.pow((widthToHeight / 8), 1.593))) / (1.0 + 6.544 * Math.pow(spaceToHeight, 4.17));
        Q19 = 0.21 * (Math.pow(spaceToHeight, 4.0)) / ((1.0 + 0.18 * (Math.pow(spaceToHeight, 4.9))) * (1.0 + 0.1 * widthToHeight * widthToHeight) * (1 + Math.pow((frequencyToHeight / 24.0), 3.0)));
        Q20 = (0.09 + 1.0 / (1.0 + 0.1 * Math.pow((dielectric - 1.0), 2.7))) * Q19;
        Q21 = Math.abs(1.0 - 42.54 * Math.pow(spaceToHeight, 0.133) * Math.exp(-0.812 * spaceToHeight) * Math.pow(widthToHeight, 2.5) / (1.0 + 0.033 * Math.pow(widthToHeight, 2.5)));

        RE = Math.pow((frequencyToHeight / 28.843), 12.0);
        QE = 0.016 + Math.pow((0.0514 * dielectric * Q21), 4.524);
        PE = 4.766 * Math.exp(-3.228 * Math.pow(widthToHeight, 0.641));
        DE = 5.086 * QE * (RE / (0.3838 + 0.386 * QE))
                * (Math.exp(-22.2 * Math.pow(widthToHeight, 1.92)) / (1.0 + 1.2992 * RE))
                * ((Math.pow((dielectric - 1.0), 6.0)) / (1.0 + 10.0 * Math.pow((dielectric - 1.0), 6.0)));
        CE = 1.0 + 1.275 * (1.0 - Math.exp(-0.004625 * PE * Math.pow(dielectric, 1.674) * Math.pow((frequencyToHeight / 18.365), 2.745)))
                - Q12 + Q16 - Q17 + Q18 + Q20;

  /*
   * EFF is the single microstrip effective dielectric constant from
   * Kirschning and Jansen (EL).
   * Note:  This line contains one of the published corrections.
   * The second EFF from the original paper is replaced by EF.
   */
        if (dielectric > 1.0) {
            z0ef = z0e0
                    * (Math.pow((0.9408 * Math.pow(EFF, CE) - 0.9603), Q0))
                    / (Math.pow(((0.9408 - DE) * Math.pow(EF, CE) - 0.9603), Q0));
        } else {
            // no dispersion for er = 1.0
            z0ef = z0e0;
        }

        // odd mode characteristic impedance including dispersion
        // This is (11) from Kirschning and Jansen (MTT)
        if (dielectric > 1.0) {
            Q29 = 15.16 / (1.0 + 0.196 * Math.pow((dielectric - 1.0), 2.0));
            Q28 = 0.149 * (Math.pow((dielectric - 1.0), 3.0)) / (94.5 + 0.038 * Math.pow((dielectric - 1.0), 3.0));
            Q27 = 0.4 * Math.pow(spaceToHeight, 0.84)
                    * (1.0 + 2.5 * (Math.pow((dielectric - 1.0), 1.5)) / (5.0 + Math.pow((dielectric - 1.0), 1.5)));
            Q26 = 30.0
                    - 22.2 * ((Math.pow(((dielectric - 1.0) / 13.0), 12.0))
                    / (1.0 + 3.0 * Math.pow(((dielectric - 1.0) / 13.0), 12.0)))
                    - Q29;
            Q25 = (0.3 * frequencyToHeight * frequencyToHeight / (10.0 + frequencyToHeight * frequencyToHeight))
                    * (1.0 + 2.333 * (Math.pow((dielectric - 1.0), 2.0)) / (5.0 + Math.pow((dielectric - 1.0), 2.0)));
        } else {
            // it seems that pow(0.0, x) gives a floating exception
            Q29 = 15.16 / (1.0 + 0.196);
            Q28 = 0.149 / (94.5 + 0.038 * 1.0);
            Q27 = 0.4 * Math.pow(spaceToHeight, 0.84)
                    * (1.0 + 2.5 / (5.0 + 1.0));
            Q26 = 30.0
                    - 22.2 * ((1.0)
                    / (1.0 + 3.0))
                    - Q29;
            Q25 = (0.3 * frequencyToHeight * frequencyToHeight / (10.0 + frequencyToHeight * frequencyToHeight))
                    * (1.0 + 2.333 * (1.0) / (5.0 + 1.0));
        }
        Q24 = 2.506 * Q28 * Math.pow(widthToHeight, 0.894)
                * (Math.pow(((1.0 + 1.3 * widthToHeight) * frequencyToHeight / 99.25), 4.29)) / (3.575 + Math.pow(widthToHeight, 0.894));
        Q23 = 1.0 + 0.005 * frequencyToHeight * Q27 / ((1.0 + 0.812 * Math.pow((frequencyToHeight / 15.0), 1.9)) *
                (1.0 + 0.025 * widthToHeight * widthToHeight));
        Q22 = 0.925 * (Math.pow((frequencyToHeight / Q26), 1.536)) / (1.0 + 0.3 * Math.pow((frequencyToHeight / 30.0), 1.536));

        // in this final expression, ZLF is the frequency dependent single
        // microstrip characteristic impedance from Jansen and Kirschning.
        if (dielectric > 1.0) {
            z0of = ZLF + (z0o0 * Math.pow((EFOF / EFO0), Q22) - ZLF * Q23) / (1.0 + Q24 + (Math.pow((0.46 * spaceToHeight), 2.2)) * Q25);
        } else {
            z0of = z0o0;
        }

        // accuracy check from page 87 in Kirschning and Jansen (MTT)
        if (frequencyToHeight > 20.0) {
            //alert(_("Warning:  Normalized frequency is higher than the\n"
            //        "maximum for < 2.5 %% error in even/odd mode impedance."));
        }

        // electrical length
        // propagation velocity (meters/sec)
        v = Constant.LIGHTSPEED / Math.sqrt(Math.sqrt(EFEF * EFOF));
        // length in wavelengths
        len = length / (v / frequency);
        // convert to degrees
        len = 360.0 * len;

        // Dielectric Losses
        // loss in nepers/meter
        if (dielectric > 1.0) {
            CMLINLine.setLossLenEven((Constant.Pi * frequency * Math.sqrt(EFE0) / Constant.LIGHTSPEED) * (dielectric / EFE0) * ((EFE0 - 1.0) / (dielectric - 1.0)) * lossTangent);
            CMLINLine.setLossLenOdd((Constant.Pi * frequency * Math.sqrt(EFO0) / Constant.LIGHTSPEED) * (dielectric / EFO0) * ((EFO0 - 1.0) / (dielectric - 1.0)) * lossTangent);
        } else {
            // XXX verify this one
            CMLINLine.setLossLenEven(0.0);
            CMLINLine.setLossLenOdd(0.0);
        }
        // remember these two for later
        alpha_d_even = CMLINLine.getLossLenEven();
        alpha_d_odd = CMLINLine.getLossLenOdd();

        // calculate skin depth
        // conductivity
        sigma = 1.0 / resistivity;
        // permeability of free space Henries/meter
        mu = 4.0 * Constant.Pi * 1e-7;
        // skin depth in meters
        depth = Math.sqrt(1.0 / (Constant.Pi * frequency * mu * sigma));
        // warn the user if the loss calc is suspect.g
        if (thickness > 0.0 && thickness < 3.0 * depth) {
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
        if (do_loss == Constant.LOSSY) {
            if (3.0 * depth <= thickness &&
                    3.0 * depth <= width) {
                // Find the current distribution factor.  This is (39) from Hammerstad and Jensen.
                Ko = Math.exp(-1.2 * Math.pow(Math.sqrt(dielectric) * (z0e0 + z0o0) / (2.0 * Constant.FREESPACEZ0), 0.7));
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
                Q_c_even = (Constant.Pi * z0e0 * Math.sqrt(EFEF) * height * frequency / (Rs * Constant.LIGHTSPEED)) * (widthToHeight / Ko);
                Q_c_odd = (Constant.Pi * z0o0 * Math.sqrt(EFOF) * height * frequency / (Rs * Constant.LIGHTSPEED)) * (widthToHeight / Ko);

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
                alpha_c_even = Constant.Pi * frequency * Math.sqrt(EFEF) / (Q_c_even * Constant.LIGHTSPEED);
                alpha_c_odd = Constant.Pi * frequency * Math.sqrt(EFOF) / (Q_c_odd * Constant.LIGHTSPEED);

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
                Line tmp_line;
                int rslt;
                double z1e, z1o, z2e, z2o;

                // clone the line
                tmp_line = CMLINLine;
                //tmp_line.subs = microstrip_subs_new();
                //*(tmp_line.subs) =*(line -> subs);

                //tmp_line.subs->er = 1.0;
                tmp_line.setSubEpsilon(1.0);
                tmp_line = coupledMicrostripAna(tmp_line, 0);
                //z1e = tmp_line.z0e;
                z1e = tmp_line.getImpedanceEven();
                //z1o = tmp_line.z0o;
                z1o = tmp_line.getImpedanceOdd();


                tmp_line.setMetalWidth(tmp_line.getMetalWidth() - depth, Line.LUnitm);
                tmp_line.setMetalSpace(tmp_line.getMetalSpace() + depth, Line.LUnitm);
                //tmp_line.subs->tmet = line -> subs -> tmet - depth;
                tmp_line.setMetalThick(tmp_line.getMetalThick() - depth, Line.LUnitm);
                //tmp_line.subs->h = line -> subs -> h + depth;
                tmp_line.setSubHeight(tmp_line.getSubHeight() + depth, Line.LUnitm);
                tmp_line = coupledMicrostripAna(tmp_line, Constant.LOSSLESS);
                //z2e = tmp_line.z0e;
                z2e = tmp_line.getImpedanceEven();
                //z2o = tmp_line.z0o;
                z2o = tmp_line.getImpedanceOdd();

                //free(tmp_line.subs);

                // conduction losses, nepers per meter
                alpha_c_even = (Constant.Pi * frequency / Constant.LIGHTSPEED) * (z2e - z1e) / z0ef;
                alpha_c_odd = (Constant.Pi * frequency / Constant.LIGHTSPEED) * (z2o - z1o) / z0of;

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
            else if (thickness > 0.0) {
                // resistance per meter = 1/(Area*conductivity)
                Res = 1 / (width * thickness * sigma);

                // conduction losses, nepers per meter
                alpha_c_even = Res / (2.0 * z0e0);
                ;
                alpha_c_odd = Res / (2.0 * z0o0);
                lc = Res / (2.0 * Math.sqrt(z0e0 * z0o0));

                // dB/meter
                lc = 20.0 * Math.log10(Math.exp(1.0)) * lc;

                // change delta to be equal to the metal thickness fo use in surface roughness correction
                delta = thickness;

                // no conduction loss case
            } else {
                lc = 0.0;
            }
        }
        // loss in dB
        lc = lc * length;

  /* factor due to surface roughness
   * note that the equation in Fooks and Zakarevicius is slightly
   * errored.
   * the correct equation is penciled in my copy and was
   * found in Hammerstad and Bekkadal as well as Hammerstad and Jensen
   */
        // XXX this was roughmil/delta double check it
        rough_factor = 1.0 + (2.0 / Constant.Pi) * Math.atan(1.4 * Math.pow((roughness / delta), 2.0));
        lc = lc * rough_factor;
        alpha_c_even = alpha_c_even * rough_factor;
        alpha_c_odd = alpha_c_odd * rough_factor;

        //#ifdef DEBUG_CALC
        //printf("%s():  rough_factor = %g \n", __FUNCTION__, rough_factor);
        //#endif

        // Single Line End correction (Kirschning, Jansen, and Koster)
        // deltal(2u,er) (per Kirschning and Jansen (MTT) notation)
        uold = widthToHeight;
        widthToHeight = 2 * widthToHeight;
        z1 = 0.434907 * ((Math.pow(EF, 0.81) + 0.26) / (Math.pow(EF, 0.81) - 0.189))
                * (Math.pow(widthToHeight, 0.8544) + 0.236) / (Math.pow(widthToHeight, 0.8544) + 0.87);
        z2 = 1.0 + Math.pow(widthToHeight, 0.371) / (2.358 * dielectric + 1.0);
        z3 = 1.0 + (0.5274 * Math.atan(0.084 * (Math.pow(widthToHeight, (1.9413 / z2))))) / Math.pow(EF, 0.9236);
        z4 = 1.0 + 0.0377 * Math.atan(0.067 * Math.pow(widthToHeight, 1.456)) * (6.0 - 5.0 * Math.exp(0.036 * (1.0 - dielectric)));
        z5 = 1.0 - 0.218 * Math.exp(-7.5 * widthToHeight);
        d2 = height * z1 * z3 * z5 / z4;

        // deltal(u,er) (per Kirschning and Jansen (MTT) notation)
        widthToHeight = uold;
        z1 = 0.434907 * ((Math.pow(EF, 0.81) + 0.26) / (Math.pow(EF, 0.81) - 0.189))
                * (Math.pow(widthToHeight, 0.8544) + 0.236) / (Math.pow(widthToHeight, 0.8544) + 0.87);
        z2 = 1 + Math.pow(widthToHeight, 0.371) / (2.358 * dielectric + 1);
        z3 = 1 + (0.5274 * Math.atan(0.084 * (Math.pow(widthToHeight, (1.9413 / z2))))) / Math.pow(EF, 0.9236);
        z4 = 1 + 0.0377 * Math.atan(0.067 * Math.pow(widthToHeight, 1.456)) * (6.0 - 5.0 * Math.exp(0.036 * (1.0 - dielectric)));
        z5 = 1 - 0.218 * Math.exp(-7.5 * widthToHeight);
        d1 = height * z1 * z3 * z5 / z4;

        // Even and Odd Mode End corrections (12) and (13) from Kirschning and Jansen (MTT)
        R1 = 1.187 * (1.0 - Math.exp(-0.069 * Math.pow(widthToHeight, 2.1)));
        R2 = 0.343 * Math.pow(widthToHeight, 0.6187) + (0.45 * dielectric / (1.0 + dielectric)) * (Math.pow(widthToHeight, (1.357 + 1.65 / (1.0 + 0.7 * dielectric))));
        R3 = 0.2974 * (1.0 - Math.exp(-R2));
        R4 = (0.271 + 0.0281 * dielectric) * (Math.pow(spaceToHeight, (1.167 * dielectric / (0.66 + dielectric))))
                + (1.025 * dielectric / (0.687 + dielectric)) * (Math.pow(spaceToHeight, (0.958 * dielectric / (0.706 + dielectric))));

        deltale = (d2 - d1 + 0.0198 * height * Math.pow(spaceToHeight, R1)) * Math.exp(-0.328 * Math.pow(spaceToHeight, 2.244)) + d1;
        deltalo = (d1 - height * R3) * (1.0 - Math.exp(-R4)) + height * R3;

        // [z0e,z0o,len,loss,kev,kodd]=cmlicalc(w,l,s,f,subs)
        // copy over the results
        //line -> z0e = z0ef;
        CMLINLine.setImpedanceEven(z0ef);
        //line -> z0o = z0of;
        CMLINLine.setImpedanceOdd(z0of);
        //line -> z0 = Math.sqrt(z0ef * z0of);
        CMLINLine.setImpedance(Math.sqrt(z0ef * z0of));
        //line -> kev = EFEF;
        CMLINLine.setkEven(EFEF);
        //line -> kodd = EFOF;
        CMLINLine.setkOdd(EFOF);

        // coupling coefficient
        //line -> k = (z0ef - z0of) / (z0ef + z0of);
        CMLINLine.setCouplingFactor((z0ef - z0of) / (z0ef + z0of));
        CMLINLine.setDeltalEven(deltale);
        //line -> deltale = deltale;
        //line -> deltalo = deltalo;
        CMLINLine.setDeltalOdd(deltalo);

        // electrical length
        //line -> len = len;
        CMLINLine.setElectricalLength(len);

        // skin depth in m
        //line -> skindepth = depth;
        CMLINLine.setSkinDepth(depth);

        // incremental circuit model
        //line -> Lev = line -> z0e * Math.sqrt(line -> kev) / Constant.LIGHTSPEED;
        CMLINLine.setLEven(CMLINLine.getImpedanceEven() * Math.sqrt(CMLINLine.getkEven()) / Constant.LIGHTSPEED);
        //line -> Cev = Math.sqrt(line -> kev) / (line -> z0e * Constant.LIGHTSPEED);
        CMLINLine.setCEven(Math.sqrt(CMLINLine.getkEven()) / (CMLINLine.getImpedanceEven() * Constant.LIGHTSPEED));
        //line -> Lodd = line -> z0o * Math.sqrt(line -> kodd) / Constant.LIGHTSPEED;
        CMLINLine.setLOdd(CMLINLine.getImpedanceOdd() * Math.sqrt(CMLINLine.getkOdd()) / Constant.LIGHTSPEED);
        //line -> Codd = Math.sqrt(line -> kodd) / (line -> z0o * Constant.LIGHTSPEED);
        CMLINLine.setCOdd(Math.sqrt(CMLINLine.getkOdd()) / (CMLINLine.getImpedanceOdd() * Constant.LIGHTSPEED));
        //line -> Rev = alpha_c_even * 2.0 * z0ef;
        CMLINLine.setREven(alpha_c_even * 2.0 * z0ef);
        //line -> Gev = 2.0 * alpha_d_even / z0ef;
        CMLINLine.setGEven(2.0 * alpha_d_even / z0ef);
        //line -> Rodd = alpha_c_odd * 2.0 * z0of;
        CMLINLine.setROdd(alpha_c_odd * 2.0 * z0of);
        //line -> Godd = 2.0 * alpha_d_odd / z0of;
        CMLINLine.setGOdd(2.0 * alpha_d_odd / z0of);

        // loss in dB/meter
        //line -> losslen_ev = 20.0 * Math.log10(Math.exp(1.0)) * (alpha_c_even + alpha_d_even);
        CMLINLine.setLossLenEven(20.0 * Math.log10(Math.exp(1.0)) * (alpha_c_even + alpha_d_even));
        //line -> losslen_odd = 20.0 * Math.log10(Math.exp(1.0)) * (alpha_c_odd + alpha_d_odd);
        CMLINLine.setLossLenOdd(20.0 * Math.log10(Math.exp(1.0)) * (alpha_c_odd + alpha_d_odd));

        // loss in dB
        //line -> loss_ev = l * line -> losslen_ev;
        CMLINLine.setLossEven(length * CMLINLine.getLossLenEven());
        //line -> loss_odd = l * line -> losslen_odd;
        CMLINLine.setLossOdd(length * CMLINLine.getLossLenOdd());
        return CMLINLine;
    }

    private int coupledMicrostripSyn() {

        double h, er, l, wmin, wmax, abstol, reltol;
        int maxiters;
        double z0, w;
        int iters;
        boolean done;
        double len;

        double s, smin, smax, z0e, z0o, k;
        double loss, kev, kodd, delta, cval, err, d;

        double AW, F1, F2, F3;

        double ai[] = {1, -0.301, 3.209, -27.282, 56.609, -37.746};
        double bi[] = {0.020, -0.623, 17.192, -68.946, 104.740, -16.148};
        double ci[] = {0.002, -0.347, 7.171, -36.910, 76.132, -51.616};

        int i;
        double dw, ds;
        double ze0 = 0, ze1, ze2, dedw, deds;
        double zo0 = 0, zo1, zo2, dodw, dods;

        //len = line->len;
        len = CMLINLine.getElectricalLength();

  /* Substrate dielectric thickness (m) */
        //h = line->subs->h;
        h = CMLINLine.getSubHeight();

  /* Substrate relative permittivity */
        //er = line->subs->er;
        er = CMLINLine.getSubEpsilon();

  /* impedance and coupling */
        //z0 = line->z0;
        z0 = CMLINLine.getImpedance();
        //k = line->k;
        k = CMLINLine.getCouplingFactor();

  /* even/odd mode impedances */
        //z0e = line->z0e;
        z0e = CMLINLine.getImpedanceEven();
        //z0o = line->z0o;
        z0o = CMLINLine.getImpedanceOdd();

        if (use_z0k) {
    /* use z0 and k to calculate z0e and z0o */
            z0o = z0 * Math.sqrt((1.0 - k) / (1.0 + k));
            z0e = z0 * Math.sqrt((1.0 + k) / (1.0 - k));
            CMLINLine.setImpedanceEven(z0e);
            CMLINLine.setImpedanceOdd(z0e);
        } else {
    /* use z0e and z0o to calculate z0 and k */
            z0 = Math.sqrt(z0e * z0o);
            k = (z0e - z0o) / (z0e + z0o);
            CMLINLine.setImpedance(z0);
            CMLINLine.setCouplingFactor(k);
        }

  /* temp value for l used while finding w and s */
        l = 1000.0;
        //line->l=l;
        CMLINLine.setMetalLength(l, Line.LUnitm);


  /* limits on the allowed range for w */
        wmin = Constant.MIL2M(0.5);
        wmax = Constant.MIL2M(1000);

  /* limits on the allowed range for s */
        smin = Constant.MIL2M(0.5);
        smax = Constant.MIL2M(1000);


  /* impedance convergence tolerance (ohms) */
        abstol = 1e-6;

  /* width relative convergence tolerance (mils) (set to 0.1 micron) */
        reltol = Constant.MICRON2MIL(0.1);

        maxiters = 50;


  /*
   * Initial guess at a solution
   */
        AW = Math.exp(z0 * Math.sqrt(er + 1.0) / 42.4) - 1.0;
        F1 = 8.0 * Math.sqrt(AW * (7.0 + 4.0 / er) / 11.0 + (1.0 + 1.0 / er) / 0.81) / AW;

        F2 = 0;
        for (i = 0; i <= 5; i++) {
            F2 = F2 + ai[i] * Math.pow(k, i);
        }

        F3 = 0;
        for (i = 0; i <= 5; i++) {
            F3 = F3 + (bi[i] - ci[i] * (9.6 - er)) * Math.pow((0.6 - k), (double) (i));
        }

        w = h * Math.abs(F1 * F2);
        s = h * Math.abs(F1 * F3);


        //#ifdef DEBUG_SYN
        //printf("coupled_microstrip_syn():  AW=%g, F1=%g, F2=%g, F3=%g\n",
        //        AW, F1, F2, F3);

        //printf("coupled_microstrip_syn():  Initial estimate:\n"
        //        "                w = %g %s, s = %g %s\n",
        //        w/line->units_lwst->sf, line->units_lwst->name,
        //        s/line->units_lwst->sf, line->units_lwst->name);
        //#endif

        l = 100;
        loss = 0;
        kev = 1;
        kodd = 1;

        iters = 0;
        done = false;
        if (w < s)
            delta = 1e-3 * w;
        else
            delta = 1e-3 * s;

        delta = Constant.MIL2M(1e-5);

        cval = 1e-12 * z0e * z0o;

  /*
   * We should never need anything anywhere near maxiters iterations.
   * This limit is just to prevent going to lala land if something
   * breaks.
   */
        while ((!done) && (iters < maxiters)) {
            iters++;
            //line->w = w;
            CMLINLine.setMetalWidth(w, Line.LUnitm);
            //line->s = s;
            CMLINLine.setMetalSpace(s, Line.LUnitm);
      /* don't bother with loss calculations while we are iterating */
            CMLINLine = coupledMicrostripAna(CMLINLine, Constant.LOSSLESS);

            //ze0 = line->z0e;
            //zo0 = line->z0o;
            ze0 = CMLINLine.getImpedanceEven();
            zo0 = CMLINLine.getImpedanceOdd();

            //#ifdef DEBUG_SYN
            //printf("Iteration #%d ze = %g\tzo = %g\tw = %g %s\ts = %g %s\n",
            //        iters, ze0, zo0,
            //        w/line->units_lwst->sf, line->units_lwst->name,
            //        s/line->units_lwst->sf, line->units_lwst->name);
            //#endif

      /* check for convergence */
            err = Math.pow((ze0 - z0e), 2.0) + Math.pow((zo0 - z0o), 2.0);
            if (err < cval) {
                done = true;
            } else {
    /* approximate the first jacobian */
                CMLINLine.setMetalWidth(w + delta, Line.LUnitm);
                CMLINLine.setMetalSpace(s, Line.LUnitm);
                CMLINLine = coupledMicrostripAna(CMLINLine, Constant.LOSSLESS);
                //ze1 = line->z0e;
                //zo1 = line->z0o;
                ze1 = CMLINLine.getImpedanceEven();
                zo1 = CMLINLine.getImpedanceOdd();

                //line->w = w;
                CMLINLine.setMetalWidth(w, Line.LUnitm);
                //line->s = s + delta;
                CMLINLine.setMetalSpace(s + delta, Line.LUnitm);
                CMLINLine = coupledMicrostripAna(CMLINLine, Constant.LOSSLESS);
                ze2 = CMLINLine.getImpedanceEven();
                zo2 = CMLINLine.getImpedanceOdd();

                dedw = (ze1 - ze0) / delta;
                dodw = (zo1 - zo0) / delta;
                deds = (ze2 - ze0) / delta;
                dods = (zo2 - zo0) / delta;

	/* find the determinate */
                d = dedw * dods - deds * dodw;

	/* estimate the new solution */
                dw = -1.0 * ((ze0 - z0e) * dods - (zo0 - z0o) * deds) / d;
                w = Math.abs(w + dw);

                ds = ((ze0 - z0e) * dodw - (zo0 - z0o) * dedw) / d;
                s = Math.abs(s + ds);

                /*
                #ifdef DEBUG_SYN
                printf("coupled_microstrip_syn():  delta = %g, determinate = %g\n", delta, d);
                printf("coupled_microstrip_syn():  ze0 = %16.8g,  ze1 = %16.8g,  ze2 = %16.8g\n",
                        ze0, ze1, ze2);
                printf("coupled_microstrip_syn():  zo0 = %16.8g,  zo1 = %16.8g,  zo2 = %16.8g\n",
                        zo0, zo1, zo2);
                printf("coupled_microstrip_syn(): dedw = %16.8g, dodw = %16.8g\n",
                        dedw, dodw);
                printf("coupled_microstrip_syn(): ze1-ze0 = %16.8g, ze2-ze0 = %16.8g\n",
                        ze1-ze0, ze2-ze0);
                printf("coupled_microstrip_syn(): deds = %16.8g, dods = %16.8g\n",
                        deds, dods);
                printf("coupled_microstrip_syn(): zo1-zo0 = %16.8g, zo2-zo0 = %16.8g\n",
                        zo1-zo0, zo2-zo0);
                printf("coupled_microstrip_syn(): dw = %g %s, ds = %g %s\n",
                        dw/line->units_lwst->sf, line->units_lwst->name,
                        ds/line->units_lwst->sf, line->units_lwst->name);
                printf("-----------------------------------------------------\n");
                #endif
                */
            }
        }

        //line->w = w;
        CMLINLine.setMetalWidth(w, Line.LUnitm);
        //line->s = s;
        CMLINLine.setMetalSpace(s, Line.LUnitm);
        CMLINLine = coupledMicrostripAna(CMLINLine, Constant.LOSSLESS);

  /* scale the line length to get the desired electrical length */
        //line->l = line->l * len/line->len;
        CMLINLine.setMetalLength(CMLINLine.getMetalLength() * len / CMLINLine.getElectricalLength(), Line.LUnitm);

  /*
   * one last calculation and this time we find the loss too.
   */
        CMLINLine = coupledMicrostripAna(CMLINLine, Constant.LOSSY);

        //#ifdef DEBUG_SYN
        //printf("Took %d iterations, err = %g\n", iters, err);
        //printf("ze = %g\tzo = %g\tz0e = %g\tz0o = %g\n", ze0, zo0, z0e, z0o);
        //#endif

        return (0);
    }

    public Line getAnaResult() {
        CMLINLine = coupledMicrostripAna(CMLINLine, Constant.LOSSY);
        return CMLINLine;
    }

    public Line getSynResult() {
        coupledMicrostripSyn();
        return CMLINLine;
    }
}
