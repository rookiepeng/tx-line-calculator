package com.rookiedev.microwavetools.libs;

public class CmlinCalculator {

    public CmlinCalculator() {
    }

    // Effective dielectric constant from Hammerstad and Jensen
    private static double EffectiveDielectricConstant_Effer(double widthToHeight, double dielectricConstant) {
        double A, B;
        // (4) from Hammerstad and Jensen
        A = 1.0 + (1.0 / 49.0) * Math.log((Math.pow(widthToHeight, 4.0) + Math.pow((widthToHeight / 52.0), 2.0))
                / (Math.pow(widthToHeight, 4.0) + 0.432))
                + (1.0 / 18.7) * Math.log(1.0 + Math.pow((widthToHeight / 18.1), 3.0));

        // (5) from Hammerstad and Jensen
        B = 0.564 * Math.pow(((dielectricConstant - 0.9) / (dielectricConstant + 3.0)), 0.053);

        // zero frequency effective permitivity. (3) from Hammerstad and Jensen. This is
        // ee(ur,er) thats used by (9) in Hammerstad and Jensen.
        return ((dielectricConstant + 1.0) / 2.0
                + ((dielectricConstant - 1.0) / 2.0) * Math.pow((1.0 + 10.0 / widthToHeight), (-A * B)));
    }

    // Characteristic impedance from (1) and (2) in Hammerstad and Jensen
    private static double CharacteristicImpedance_Z0(double widthToHeight) {
        double F, z01;

        // (2) from Hammerstad and Jensen. 'u' is the normalized width
        F = 6.0 + (2.0 * Constants.Pi - 6.0) * Math.exp(-Math.pow((30.666 / widthToHeight), 0.7528));

        // (1) from Hammerstad and Jensen
        // TODO XXX decide on which to use here
        // z01 = (Constants.FREESPACEZ0 / (2 * Constants.Pi)) * Math.log(F / u +
        // Math.sqrt(1.0 + Math.pow((2 / u), 2.0)));
        z01 = (377.0 / (2 * Constants.Pi))
                * Math.log(F / widthToHeight + Math.sqrt(1.0 + Math.pow((2 / widthToHeight), 2.0)));
        return z01;
    }

    private CmlinModel Analysis(CmlinModel line) {
        // input physical dimensions
        double width, length, space;
        // substrate header_parameters
        double height, dielectric;
        double frequency;

        double widthToHeight, spaceToHeight, V, EFE0, AO, BO, CO, DO;
        double EFO0, frequencyToHeight;
        double EF; // 0 frequency (static) relative dielectric constant for a single microstrip.
        double EFF; // frequency dependent relative dielectric constant for a single microstrip
                    // given in Kirschning and Jansen (EL)
        double ZL0; // static single strip, T=0, characteristic impedance (f=0) (Hammerstad and
                    // Jensen)
        double ZLF; // frequency dependent single microstrip characteristic impedance from Jansen
                    // and Kirschning.
        double z0ef, z0of; // even/odd mode impedance at the frequency of interest
        double electricalLength; // degree

        double P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15;
        double FEF, FOF, EFEF, EFOF;
        double Q0, Q1, Q2, Q3, Q4, Q5, Q6, Q7, Q8, Q9, Q10, Q11, Q12, Q13, Q14;
        double Q15, Q16, Q17, Q18, Q19, Q20, Q21, Q22, Q23, Q24, Q25, Q26;
        double Q27, Q28, Q29;
        double z0e0, z0o0;
        double xP1, xP2, xP3, xP4, xP;
        double R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16, R17;
        double RE, QE, PE, DE, CE;

        double v;
        double fnold;

        line.setWarningCode(Constants.WARNING.NO_WARNING);

        width = line.getMetalWidth();
        if (width < Constants.MINI_LIMIT) {
            line.setErrorCode(Constants.ERROR.WIDTH_MINIMAL_LIMIT);
            return line;
        }
        length = line.getMetalLength();
        space = line.getMetalSpace();
        if (space < Constants.MINI_LIMIT) {
            line.setErrorCode(Constants.ERROR.SPACE_MINIMAL_LIMIT);
            return line;
        }
        frequency = line.getFrequency();
        // SubstrateModel dielectric thickness
        height = line.getSubHeight();
        if (height < Constants.MINI_LIMIT) {
            line.setErrorCode(Constants.ERROR.HEIGHT_MINIMAL_LIMIT);
            return line;
        }
        // SubstrateModel relative permittivity
        dielectric = line.getSubEpsilon();
        if (dielectric < 1) {
            line.setErrorCode(Constants.ERROR.ER_MINIMAL_LIMIT);
            return line;
        }

        // Find widthToHeight and correction factor for nonzero metal thickness
        widthToHeight = width / height;
        spaceToHeight = space / height;

        // verify that the geometry is in a range where the accuracy is good. This is
        // given by (1) in Kirschning and Jansen (MTT)
        if ((widthToHeight < 0.1) || (widthToHeight > 10.0)) {
            // Warning: u=w/h is outside the range for highly accurate results.
            // For best accuracy 0.1 < u < 10.0
            line.setWarningCode(Constants.WARNING.WIDTH2HEIGHT_OUT_OF_RANGE);
        }
        if ((spaceToHeight < 0.1) || (spaceToHeight > 10.0)) {
            // Warning: g=s/h is outside the range for highly accurate results
            // For best accuracy 0.1 < g < 10.0
            line.setWarningCode(Constants.WARNING.SPACE2HEIGHT_OUT_OF_RANGE);
        }
        if (dielectric > 18.0) {
            // Warning: er is outside the range for highly accurate results
            // For best accuracy 1.0 < er < 18.0
            line.setWarningCode(Constants.WARNING.DIELECTRIC_OUT_OF_RANGE);
        }

        // static even mode relative permittivity (f=0) (3) from Kirschning and Jansen
        // (MTT). Accurate to 0.7 % over "accurate" range
        V = widthToHeight * (20.0 + Math.pow(spaceToHeight, 2.0)) / (10.0 + Math.pow(spaceToHeight, 2.0))
                + spaceToHeight * Math.exp(-spaceToHeight);

        // note: The equations listed in (3) in Kirschning and Jansen (MTT) are the same
        // as in Hammerstad and Jensen but with u in H&J replaced with V from K&J.
        EFE0 = EffectiveDielectricConstant_Effer(V, dielectric);

        // static single strip, T=0, relative permittivity (f=0), width=w from
        // Hammerstad and Jensen.
        EF = EffectiveDielectricConstant_Effer(widthToHeight, dielectric);

        // static odd mode relative permittivity (f=0) This is (4) from Kirschning and
        // Jansen (MTT). Accurate to 0.5%.
        AO = 0.7287 * (EF - (dielectric + 1.0) / 2.0) * (1.0 - Math.exp(-0.179 * widthToHeight));
        BO = 0.747 * dielectric / (0.15 + dielectric);
        CO = BO - (BO - 0.207) * Math.exp(-0.414 * widthToHeight);
        DO = 0.593 + 0.694 * Math.exp(-0.562 * widthToHeight);

        // Note, this includes the published correction
        EFO0 = ((dielectric + 1.0) / 2.0 + AO - EF) * Math.exp(-CO * (Math.pow(spaceToHeight, DO))) + EF;

        // normalized frequency (2) from Kirschning and Jansen (MTT)
        frequencyToHeight = 1e-6 * frequency * height;

        // check for range of validity for the dispersion equations. p. 85 of Kirschning
        // and Jansen (MTT) says fn <= 25 gives 1.4% accuracy.
        if (frequencyToHeight > 25.0) {
            // Warning: Frequency is higher than the range
            // over which the dispersion equations are accurate.
            line.setWarningCode(Constants.WARNING.FREQUENCY2HEIGHT_OUT_OF_RANGE);
        }

        // even/odd mode relative permittivity including dispersion
        // even mode dispersion. (6) from Kirschning and Jansen (MTT)
        P1 = 0.27488 + (0.6315 + 0.525 / (Math.pow((1.0 + 0.0157 * frequencyToHeight), 20.0))) * widthToHeight
                - 0.065683 * Math.exp(-8.7513 * widthToHeight);
        P2 = 0.33622 * (1.0 - Math.exp(-0.03442 * dielectric));
        P3 = 0.0363 * Math.exp(-4.6 * widthToHeight) * (1.0 - Math.exp(-Math.pow((frequencyToHeight / 38.7), 4.97)));
        P4 = 1.0 + 2.751 * (1.0 - Math.exp(-Math.pow((dielectric / 15.916), 8.0)));
        P5 = 0.334 * Math.exp(-3.3 * Math.pow((dielectric / 15.0), 3.0)) + 0.746;
        P6 = P5 * Math.exp(-Math.pow((frequencyToHeight / 18.0), 0.368));
        P7 = 1.0 + 4.069 * P6 * (Math.pow(spaceToHeight, 0.479))
                * Math.exp(-1.347 * (Math.pow(spaceToHeight, 0.595)) - 0.17 * (Math.pow(spaceToHeight, 2.5)));
        FEF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844 * P7) * frequencyToHeight), 1.5763);

        // odd mode dispersion. (7) from Kirschning and Jansen (MTT)
        P8 = 0.7168 * (1.0 + 1.076 / (1.0 + 0.0576 * (dielectric - 1.0)));
        P9 = P8 - 0.7913 * (1.0 - Math.exp(-Math.pow((frequencyToHeight / 20.0), 1.424)))
                * Math.atan(2.481 * Math.pow((dielectric / 8.0), 0.946));
        P10 = 0.242 * Math.pow((dielectric - 1.0), 0.55);
        P11 = 0.6366 * (Math.exp(-0.3401 * frequencyToHeight) - 1.0)
                * Math.atan(1.263 * Math.pow((widthToHeight / 3.0), 1.629));
        P12 = P9 + (1.0 - P9) / (1.0 + 1.183 * Math.pow(widthToHeight, 1.376));
        P13 = 1.695 * P10 / (0.414 + 1.605 * P10);
        P14 = 0.8928 + 0.1072 * (1.0 - Math.exp(-0.42 * Math.pow((frequencyToHeight / 20.0), 3.215)));
        P15 = Math.abs(1.0 - 0.8928 * (1.0 + P11) * P12 * Math.exp(-P13 * (Math.pow(spaceToHeight, 1.092))) / P14);
        FOF = P1 * P2 * Math.pow(((P3 * P4 + 0.1844) * frequencyToHeight * P15), 1.5763);

        // relative permittivities including dispersion via generalization of
        // Getsinger's dispersion relation. This is (5) in Kirschning and Jansen (MTT).
        EFEF = dielectric - (dielectric - EFE0) / (1.0 + FEF);
        EFOF = dielectric - (dielectric - EFO0) / (1.0 + FOF);

        // static single strip, T=0, characteristic impedance (f=0) (Hammerstad and
        // Jensen)
        ZL0 = CharacteristicImpedance_Z0(widthToHeight) / Math.sqrt(EF);

        // static even mode characteristic impedance (f=0) (8) from Kirschning and
        // Jansen (MTT) 0.6% accurate
        Q1 = 0.8695 * (Math.pow(widthToHeight, 0.194));
        Q2 = 1.0 + 0.7519 * spaceToHeight + 0.189 * (Math.pow(spaceToHeight, 2.31));
        Q3 = 0.1975 + Math.pow((16.6 + Math.pow((8.4 / spaceToHeight), 6.0)), -0.387)
                + Math.log((Math.pow(spaceToHeight, 10.0)) / (1.0 + Math.pow((spaceToHeight / 3.4), 10.0))) / 241.0;
        Q4 = (2.0 * Q1 / Q2) / (Math.exp(-spaceToHeight) * (Math.pow(widthToHeight, Q3))
                + (2.0 - Math.exp(-spaceToHeight)) * (Math.pow(widthToHeight, -Q3)));
        z0e0 = ZL0 * Math.sqrt(EF / EFE0) / (1.0 - (ZL0 / 377.0) * Math.sqrt(EF) * Q4);

        // static odd mode characteristic impedance (f=0) (9) from Kirschning and Jansen
        // (MTT) 0.6% accurate
        Q5 = 1.794 + 1.14 * Math.log(1.0 + 0.638 / (spaceToHeight + 0.517 * (Math.pow(spaceToHeight, 2.43))));
        Q6 = 0.2305 + Math.log((Math.pow(spaceToHeight, 10.0)) / (1.0 + Math.pow((spaceToHeight / 5.8), 10.0))) / 281.3
                + Math.log(1.0 + 0.598 * (Math.pow(spaceToHeight, 1.154))) / 5.1;
        Q7 = (10.0 + 190.0 * spaceToHeight * spaceToHeight) / (1.0 + 82.3 * Math.pow(spaceToHeight, 3.0));
        Q8 = Math.exp(-6.5 - 0.95 * Math.log(spaceToHeight) - Math.pow((spaceToHeight / 0.15), 5.0));
        Q9 = Math.log(Q7) * (Q8 + 1.0 / 16.5);
        Q10 = (Q2 * Q4 - Q5 * Math.exp(Math.log(widthToHeight) * Q6 * (Math.pow(widthToHeight, -Q9)))) / Q2;
        z0o0 = ZL0 * Math.sqrt(EF / EFO0) / (1.0 - (ZL0 / 377.0) * Math.sqrt(EF) * Q10);

        // relative permittivity including dispersion of single microstrip of width W,
        // Tmet=0. Kirschning and Jansen (EL) **/
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

        // Characteristic Impedance of single strip, Width=W, Tmet=0. Jansen and
        // Kirschning **/
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
        R8 = 1.0 + 1.275 * (1.0
                - Math.exp(-0.004625 * R3 * Math.pow(dielectric, 1.674) * Math.pow(frequencyToHeight / 18.365, 2.745)));
        R9 = (5.086 * R4 * R5 / (0.3838 + 0.386 * R4)) * (Math.exp(-R6) / (1.0 + 1.2992 * R5));
        R9 = R9 * (Math.pow((dielectric - 1.0), 6.0)) / (1.0 + 10.0 * Math.pow((dielectric - 1.0), 6.0));

        // (3) from Jansen and Kirschning
        R10 = 0.00044 * (Math.pow(dielectric, 2.136)) + 0.0184;
        R11 = (Math.pow((frequencyToHeight / 19.47), 6.0))
                / (1.0 + 0.0962 * (Math.pow((frequencyToHeight / 19.47), 6.0)));
        R12 = 1.0 / (1.0 + 0.00245 * widthToHeight * widthToHeight);

        // (4) from Jansen and Kirschning
        R13 = 0.9408 * (Math.pow(EFF, R8)) - 0.9603;
        R14 = (0.9408 - R9) * Math.pow(EF, R8) - 0.9603;
        R15 = 0.707 * R10 * (Math.pow((frequencyToHeight / 12.3), 1.097));
        R16 = 1.0 + 0.0503 * dielectric * dielectric * R11 * (1.0 - Math.exp(-(Math.pow((widthToHeight / 15), 6.0))));
        R17 = R7 * (1.0 - 1.1241 * (R12 / R16) * Math.exp(-0.026 * (Math.pow(frequencyToHeight, 1.15656)) - R15));

        // ZLF = zero thickness single strip characteristic impedance including
        // dispersion
        ZLF = ZL0 * Math.pow((R13 / R14), R17);

        // Q0 = R17 from zero thickness, single microstrip
        Q0 = R17;

        // recall fn
        frequencyToHeight = fnold;

        // even mode characteristic impedance including dispersion, this is (10) from
        // Kirschning and Jansen (MTT)
        Q11 = 0.893 * (1.0 - 0.3 / (1.0 + 0.7 * (dielectric - 1.0)));
        Q12 = 2.121
                * ((Math.pow((frequencyToHeight / 20.0), 4.91))
                        / (1 + Q11 * Math.pow((frequencyToHeight / 20.0), 4.91)))
                * Math.exp(-2.87 * spaceToHeight) * Math.pow(spaceToHeight, 0.902);
        Q13 = 1.0 + 0.038 * Math.pow((dielectric / 8.0), 5.1);
        Q14 = 1.0 + 1.203 * (Math.pow((dielectric / 15.0), 4.0)) / (1.0 + Math.pow((dielectric / 15.0), 4.0));
        Q15 = 1.887 * Math.exp(-1.5 * (Math.pow(spaceToHeight, 0.84))) * (Math.pow(spaceToHeight, Q14))
                / (1.0 + 0.41 * (Math.pow((frequencyToHeight / 15.0), 3.0)) * (Math.pow(widthToHeight, (2.0 / Q13)))
                        / (0.125 + Math.pow(widthToHeight, (1.626 / Q13))));
        Q16 = (1.0 + 9.0 / (1.0 + 0.403 * Math.pow((dielectric - 1.0), 2.0))) * Q15;
        Q17 = 0.394 * (1.0 - Math.exp(-1.47 * Math.pow((widthToHeight / 7.0), 0.672)))
                * (1.0 - Math.exp(-4.25 * Math.pow((frequencyToHeight / 20.0), 1.87)));
        Q18 = 0.61 * (1.0 - Math.exp(-2.13 * Math.pow((widthToHeight / 8), 1.593)))
                / (1.0 + 6.544 * Math.pow(spaceToHeight, 4.17));
        Q19 = 0.21 * (Math.pow(spaceToHeight, 4.0)) / ((1.0 + 0.18 * (Math.pow(spaceToHeight, 4.9)))
                * (1.0 + 0.1 * widthToHeight * widthToHeight) * (1 + Math.pow((frequencyToHeight / 24.0), 3.0)));
        Q20 = (0.09 + 1.0 / (1.0 + 0.1 * Math.pow((dielectric - 1.0), 2.7))) * Q19;
        Q21 = Math.abs(1.0 - 42.54 * Math.pow(spaceToHeight, 0.133) * Math.exp(-0.812 * spaceToHeight)
                * Math.pow(widthToHeight, 2.5) / (1.0 + 0.033 * Math.pow(widthToHeight, 2.5)));

        RE = Math.pow((frequencyToHeight / 28.843), 12.0);
        QE = 0.016 + Math.pow((0.0514 * dielectric * Q21), 4.524);
        PE = 4.766 * Math.exp(-3.228 * Math.pow(widthToHeight, 0.641));
        DE = 5.086 * QE * (RE / (0.3838 + 0.386 * QE))
                * (Math.exp(-22.2 * Math.pow(widthToHeight, 1.92)) / (1.0 + 1.2992 * RE))
                * ((Math.pow((dielectric - 1.0), 6.0)) / (1.0 + 10.0 * Math.pow((dielectric - 1.0), 6.0)));
        CE = 1.0 + 1.275 * (1.0 - Math
                .exp(-0.004625 * PE * Math.pow(dielectric, 1.674) * Math.pow((frequencyToHeight / 18.365), 2.745)))
                - Q12 + Q16 - Q17 + Q18 + Q20;

        // Note: This line contains one of the published corrections. The second EFF
        // from the original paper is replaced by EF.
        if (dielectric > 1.0) {
            z0ef = z0e0 * (Math.pow((0.9408 * Math.pow(EFF, CE) - 0.9603), Q0))
                    / (Math.pow(((0.9408 - DE) * Math.pow(EF, CE) - 0.9603), Q0));
        } else {
            // no dispersion for er = 1.0
            z0ef = z0e0;
        }

        // odd mode characteristic impedance including dispersion. This is (11) from
        // Kirschning and Jansen (MTT)
        if (dielectric > 1.0) {
            Q29 = 15.16 / (1.0 + 0.196 * Math.pow((dielectric - 1.0), 2.0));
            Q28 = 0.149 * (Math.pow((dielectric - 1.0), 3.0)) / (94.5 + 0.038 * Math.pow((dielectric - 1.0), 3.0));
            Q27 = 0.4 * Math.pow(spaceToHeight, 0.84)
                    * (1.0 + 2.5 * (Math.pow((dielectric - 1.0), 1.5)) / (5.0 + Math.pow((dielectric - 1.0), 1.5)));
            Q26 = 30.0 - 22.2 * ((Math.pow(((dielectric - 1.0) / 13.0), 12.0))
                    / (1.0 + 3.0 * Math.pow(((dielectric - 1.0) / 13.0), 12.0))) - Q29;
            Q25 = (0.3 * frequencyToHeight * frequencyToHeight / (10.0 + frequencyToHeight * frequencyToHeight))
                    * (1.0 + 2.333 * (Math.pow((dielectric - 1.0), 2.0)) / (5.0 + Math.pow((dielectric - 1.0), 2.0)));
        } else {
            Q29 = 15.16 / (1.0 + 0.196);
            Q28 = 0.149 / (94.5 + 0.038);
            Q27 = 0.4 * Math.pow(spaceToHeight, 0.84) * (1.0 + 2.5 / (5.0 + 1.0));
            Q26 = 30.0 - 22.2 * ((1.0) / (1.0 + 3.0)) - Q29;
            Q25 = (0.3 * frequencyToHeight * frequencyToHeight / (10.0 + frequencyToHeight * frequencyToHeight))
                    * (1.0 + 2.333 / (5.0 + 1.0));
        }
        Q24 = 2.506 * Q28 * Math.pow(widthToHeight, 0.894)
                * (Math.pow(((1.0 + 1.3 * widthToHeight) * frequencyToHeight / 99.25), 4.29))
                / (3.575 + Math.pow(widthToHeight, 0.894));
        Q23 = 1.0 + 0.005 * frequencyToHeight * Q27 / ((1.0 + 0.812 * Math.pow((frequencyToHeight / 15.0), 1.9))
                * (1.0 + 0.025 * widthToHeight * widthToHeight));
        Q22 = 0.925 * (Math.pow((frequencyToHeight / Q26), 1.536))
                / (1.0 + 0.3 * Math.pow((frequencyToHeight / 30.0), 1.536));

        // in this final expression, ZLF is the frequency dependent single microstrip
        // characteristic impedance from Jansen and Kirschning.
        if (dielectric > 1.0) {
            z0of = ZLF + (z0o0 * Math.pow((EFOF / EFO0), Q22) - ZLF * Q23)
                    / (1.0 + Q24 + (Math.pow((0.46 * spaceToHeight), 2.2)) * Q25);
        } else {
            z0of = z0o0;
        }

        // accuracy check from page 87 in Kirschning and Jansen (MTT)
        if (frequencyToHeight > 20.0) {
            // Warning: Normalized frequency is higher than the
            // maximum for < 2.5 %% error in even/odd mode impedance.
            line.setWarningCode(Constants.WARNING.FREQUENCY2HEIGHT_OUT_OF_RANGE);
        }

        // electrical length
        // propagation velocity (meters/sec)
        v = Constants.LIGHTSPEED / Math.sqrt(Math.sqrt(EFEF * EFOF));
        // length in wavelengths
        electricalLength = length / (v / frequency);
        // convert to degrees
        electricalLength = 360.0 * electricalLength;

        // copy over the results
        line.setImpedanceEven(z0ef);
        line.setImpedanceOdd(z0of);
        line.setImpedance(Math.sqrt(z0ef * z0of));
        // coupling coefficient
        line.setCouplingFactor((z0ef - z0of) / (z0ef + z0of));
        // electrical length
        line.setPhase(electricalLength);

        line.setErrorCode(Constants.ERROR.NO_ERROR);
        return line;
    }

    private CmlinModel Synthesize(CmlinModel line, boolean use_z0k) {

        double h, er, l;
        int maxiters;
        double z0, w;
        int iters;
        boolean done;
        double electricalLength; // degree

        double s, z0e, z0o, k;
        double delta, cval, err, d;

        double AW, F1, F2, F3;

        double[] ai = { 1, -0.301, 3.209, -27.282, 56.609, -37.746 };
        double[] bi = { 0.020, -0.623, 17.192, -68.946, 104.740, -16.148 };
        double[] ci = { 0.002, -0.347, 7.171, -36.910, 76.132, -51.616 };

        int i;
        double dw, ds;
        double ze0, ze1, ze2, dedw, deds;
        double zo0, zo1, zo2, dodw, dods;

        electricalLength = line.getPhase();

        // SubstrateModel dielectric thickness (m)
        h = line.getSubHeight();

        // SubstrateModel relative permittivity
        er = line.getSubEpsilon();

        // impedance and coupling
        z0 = line.getImpedance();
        k = line.getCouplingFactor();

        // even/odd mode impedances
        z0e = line.getImpedanceEven();
        z0o = line.getImpedanceOdd();

        if (use_z0k) {
            /// use z0 and k to calculate z0e and z0o
            z0o = z0 * Math.sqrt((1.0 - k) / (1.0 + k));
            z0e = z0 * Math.sqrt((1.0 + k) / (1.0 - k));
            line.setImpedanceEven(z0e);
            line.setImpedanceOdd(z0e);
            if (k >= 1) {
                line.setErrorCode(Constants.ERROR.K_OUT_OF_RANGE);
                return line;
            }
        } else {
            // use z0e and z0o to calculate z0 and k
            z0 = Math.sqrt(z0e * z0o);
            k = (z0e - z0o) / (z0e + z0o);
            //Log.v("CmlinCalculator", "k=" + Double.toString(k));
            line.setImpedance(z0);
            line.setCouplingFactor(k);
            if (k <= 0 || k >= 1) {
                line.setErrorCode(Constants.ERROR.Z0E_Z0O_MISTAKE);
                return line;
            }
        }

        // temp value for l used while finding w and s
        l = 1000.0;
        line.setMetalLength(l, "m");

        // width relative convergence tolerance (mils) (set to 0.1 micron)
        // reltol = Constants.MICRON2MIL(0.1);
        maxiters = 50;

        // Initial guess at a solution
        AW = Math.exp(z0 * Math.sqrt(er + 1.0) / 42.4) - 1.0;
        F1 = 8.0 * Math.sqrt(AW * (7.0 + 4.0 / er) / 11.0 + (1.0 + 1.0 / er) / 0.81) / AW;

        F2 = 0;
        for (i = 0; i <= 5; i++) {
            F2 = F2 + ai[i] * Math.pow(k, i);
        }

        F3 = 0;
        for (i = 0; i <= 5; i++) {
            F3 = F3 + (bi[i] - ci[i] * (9.6 - er)) * Math.pow((0.6 - k), i);
        }

        w = h * Math.abs(F1 * F2);
        s = h * Math.abs(F1 * F3);

        iters = 0;
        done = false;
        if (w < s)
            delta = 1e-3 * w;
        else
            delta = 1e-3 * s;

        delta = Constants.value2meter(1e-5, "mil");

        cval = 1e-12 * z0e * z0o;

        /*
         * We should never need anything anywhere near maxiters iterations. This limit
         * is just to prevent going to lala land if something breaks.
         */
        while (!done) {
            iters++;
            line.setMetalWidth(w, "m");
            line.setMetalSpace(s, "m");
            line = Analysis(line);
            if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                ze0 = line.getImpedanceEven();
                zo0 = line.getImpedanceOdd();
            } else {
                line.setErrorCode(Constants.ERROR.COULD_NOT_BRACKET_SOLUTION);
                return line;
            }

            // check for convergence
            err = Math.pow((ze0 - z0e), 2.0) + Math.pow((zo0 - z0o), 2.0);
            if (err < cval) {
                done = true;
            } else {
                // approximate the first jacobian
                line.setMetalWidth(w + delta, "m");
                line.setMetalSpace(s, "m");
                line = Analysis(line);
                if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                    ze1 = line.getImpedanceEven();
                    zo1 = line.getImpedanceOdd();
                } else {
                    line.setErrorCode(Constants.ERROR.COULD_NOT_BRACKET_SOLUTION);
                    return line;
                }

                line.setMetalWidth(w, "m");
                line.setMetalSpace(s + delta, "m");
                line = Analysis(line);
                if (line.getErrorCode() == Constants.ERROR.NO_ERROR) {
                    ze2 = line.getImpedanceEven();
                    zo2 = line.getImpedanceOdd();
                } else {
                    line.setErrorCode(Constants.ERROR.COULD_NOT_BRACKET_SOLUTION);
                    return line;
                }

                dedw = (ze1 - ze0) / delta;
                dodw = (zo1 - zo0) / delta;
                deds = (ze2 - ze0) / delta;
                dods = (zo2 - zo0) / delta;

                // find the determinate
                d = dedw * dods - deds * dodw;

                // estimate the new solution
                dw = -1.0 * ((ze0 - z0e) * dods - (zo0 - z0o) * deds) / d;
                w = Math.abs(w + dw);

                ds = ((ze0 - z0e) * dodw - (zo0 - z0o) * dedw) / d;
                s = Math.abs(s + ds);
            }

            if (iters >= maxiters) {
                line.setErrorCode(Constants.ERROR.MAX_ITERATIONS);
                return line;
            }
        }

        line.setMetalWidth(w, "m");
        line.setMetalSpace(s, "m");
        line = Analysis(line);

        // scale the line length to get the desired electrical length
        line.setMetalLength(line.getMetalLength() * electricalLength / line.getPhase(),
                "m");

        /*
         * one last calculation and this time we find the loss too.
         */
        // line = Analysis(line);
        line.setErrorCode(Constants.ERROR.NO_ERROR);
        return line;
    }

    public CmlinModel getAnaResult(CmlinModel line) {
        return Analysis(line);
    }

    public CmlinModel getSynResult(CmlinModel line, boolean use_z0k) {
        return Synthesize(line, use_z0k);
    }
}
