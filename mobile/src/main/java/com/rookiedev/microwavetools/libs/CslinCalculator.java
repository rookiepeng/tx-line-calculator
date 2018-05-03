package com.rookiedev.microwavetools.libs;

public class CslinCalculator {

    public CslinCalculator() {
    }

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

/* alternate approach
  if( k < sqrt(0.5) ) {
    kp = sqrt(1.0 - k*k);
    r = M_PI / log(2.0 * (1.0 + sqrt(kp)) / (1.0 - sqrt(kp)) );
  } else {
    r = log(2.0 * (1.0 + sqrt(k)) / (1.0 - sqrt(k)) ) / M_PI;
  }
*/
        return r;
    }

    // Zero thickness characteristic impedance
    private CslinModel impedance_zeroThickness(CslinModel line) {
        double ke, ko;

        // (3) from Cohn
        ke = Math.tanh(Math.PI * line.getMetalWidth() / (2.0 * line.getSubHeight())) * Math.tanh(Math.PI * (line.getMetalWidth() + line.getMetalSpace()) / (2.0 * line.getSubHeight()));
        // (6) from Cohn
        ko = Math.tanh(Math.PI * line.getMetalWidth() / (2.0 * line.getSubHeight())) / Math.tanh(Math.PI * (line.getMetalWidth() + line.getMetalSpace()) / (2.0 * line.getSubHeight()));
        // (2) from Cohn
        line.setImpedanceEven((Constants.FREESPACEZ0 / 4.0) * Math.sqrt(1.0 / line.getSubEpsilon()) / k_over_kp(ke));
        // (5) from Cohn
        line.setImpedanceOdd((Constants.FREESPACEZ0 / 4.0) * Math.sqrt(1.0 / line.getSubEpsilon()) / k_over_kp(ko));
        return line;
    }

    private CslinModel Analysis(CslinModel line) {
        // zero thickness even and odd impedances
        double z0e_0t, z0o_0t;

        // single stripline
        SlinModel lineSlin = new SlinModel();

        double z0s, z0s_0t;
        double cf_t, cf_0;

        // Start of coupled stripline calculations
        // zero thickness coupled line
        line = impedance_zeroThickness(line);
        z0e_0t = line.getImpedanceEven();
        z0o_0t = line.getImpedanceOdd();

        if (line.getMetalThick() == 0.0) {
            //line -> z0e = z0e_0t;
            //line -> z0o = z0o_0t;
        } else {
            lineSlin.setSubstrate(line.getSubstrate());
            lineSlin.setMetalThick(line.getMetalThick(), Constants.LengthUnit_m);
            lineSlin.setMetalWidth(line.getMetalWidth(), Constants.LengthUnit_m);
            lineSlin.setMetalLength(line.getMetalLength(), Constants.LengthUnit_m);
            lineSlin.setFrequency(line.getFrequency(), Constants.FreqUnit_Hz);

            SlinCalculator slin = new SlinCalculator();
            lineSlin = slin.getAnaResult(lineSlin);

            z0s = lineSlin.getImpedance();

            lineSlin.setMetalThick(0, Constants.LengthUnit_m);
            lineSlin = slin.getAnaResult(lineSlin);
            z0s_0t = lineSlin.getImpedance();

            // fringing capacitance
            cf_t = (Constants.FREESPACE_E0 * line.getSubEpsilon() / Math.PI) * (
                    (2.0 / (1.0 - line.getMetalThick() / line.getSubHeight())) *
                            Math.log((1.0 / (1.0 - line.getMetalThick() / line.getSubHeight())) + 1.0) -
                            (1.0 / (1.0 - line.getMetalThick() / line.getSubHeight()) - 1.0) *
                                    Math.log((1.0 / Math.pow(1.0 - line.getMetalThick() / line.getSubHeight(), 2.0)) - 1.0));

            // zero thickness fringing capacitance
            cf_0 = (Constants.FREESPACE_E0 * line.getSubEpsilon() / Math.PI) * 2.0 * Math.log(2.0);

            // (18) from Cohn, (4.6.5.1) in Wadell
            line.setImpedanceEven(1.0 / ((1.0 / z0s) - (cf_t / cf_0) * ((1.0 / z0s_0t) - (1.0 / z0e_0t))));

            if (line.getMetalSpace() >= 5.0 * line.getMetalThick()) {
                // (20) from Cohn, (4.6.5.2) in Wadell -- note, Wadell has a sign error in the equation
                line.setImpedanceOdd(1.0 / ((1.0 / z0s) + (cf_t / cf_0) * ((1.0 / z0o_0t) - (1.0 / z0s_0t))));
            } else {
                // (22) from Cohn, (4.6.5.3) in Wadell -- note, Wadell has a couple of errors in the transcription from the original (Cohn)
                line.setImpedanceOdd(1.0 / ((1.0 / z0o_0t) +
                        ((1.0 / z0s) - (1.0 / z0s_0t)) -
                        (2.0 / Constants.FREESPACEZ0) * (cf_t / Constants.FREESPACE_E0 - cf_0 / Constants.FREESPACE_E0) +
                        (2.0 * line.getMetalThick()) / (Constants.FREESPACEZ0 * line.getMetalSpace())));
            }
        }

        // find impedance and coupling coefficient
        line.setImpedance(Math.sqrt(line.getImpedanceEven() * line.getImpedanceOdd()));

        // coupling coefficient
        line.setCouplingFactor((line.getImpedanceEven() - line.getImpedanceOdd()) / (line.getImpedanceEven() + line.getImpedanceOdd()));

  /*
   * electrical length = 360 degrees * physical length / wavelength
   *
   * freq * wavelength = velocity => 1/wavelength = freq / velocity
   *
   * 1/wavelength = freq * LIGHTSPEED/sqrt(keff)
   */
        line.setElectricalLength(360.0 * line.getMetalLength() * line.getFrequency() / (Constants.LIGHTSPEED / Math.sqrt(line.getSubEpsilon())));
        return line;
    }

    private CslinModel Synthesize(CslinModel line, boolean use_z0k) {

        double h, er, l, wmin, wmax, abstol, reltol;
        int maxiters;
        double z0, w;
        int iters;
        boolean done;
        double len;

        double s, smin, smax, z0e, z0o, k;
        double loss, delta, cval, err, d;

        double AW, F1, F2, F3;

        double ai[] = {1, -0.301, 3.209, -27.282, 56.609, -37.746};
        double bi[] = {0.020, -0.623, 17.192, -68.946, 104.740, -16.148};
        double ci[] = {0.002, -0.347, 7.171, -36.910, 76.132, -51.616};

        int i;
        double dw, ds;
        double ze0 = 0, ze1, ze2, dedw, deds;
        double zo0 = 0, zo1, zo2, dodw, dods;

        len = line.getElectricalLength();

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
            // use z0 and k to calculate z0e and z0o
            z0o = z0 * Math.sqrt((1.0 - k) / (1.0 + k));
            z0e = z0 * Math.sqrt((1.0 + k) / (1.0 - k));
        } else {
            // use z0e and z0o to calculate z0 and k
            z0 = Math.sqrt(z0e * z0o);
            k = (z0e - z0o) / (z0e + z0o);
        }

        // temp value for l used while finding w and s
        l = 1000.0;
        line.setMetalLength(l, Constants.LengthUnit_m);

        // FIXME - change limits to be normalized to substrate thickness limits on the allowed range for w
        //wmin = MIL2M(0.5);
        //wmax = MIL2M(1000);

        // limits on the allowed range for s
        //smin = MIL2M(0.5);
        //smax = MIL2M(1000);

        // impedance convergence tolerance (ohms)
        //abstol = 1e-6;

        // width relative convergence tolerance (mils) (set to 0.1 micron)
        //reltol = MICRON2MIL(0.1);
        maxiters = 50;

        // Initial guess at a solution -- FIXME:  This is an initial guess for coupled microstrip _not_ coupled stripline.
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
        l = 100;

        iters = 0;
        done = false;
        if (w < s)
            delta = 1e-3 * w;
        else
            delta = 1e-3 * s;
        delta = Constants.value2meter(1e-5, Constants.LengthUnit_mil);
        cval = 1e-12 * z0e * z0o;

  /*
   * We should never need anything anywhere near maxiters iterations.
   * This limit is just to prevent going to lala land if something
   * breaks.
   */
        while ((!done) && (iters < maxiters)) {
            iters++;
            line.setMetalWidth(w, Constants.LengthUnit_m);
            line.setMetalSpace(s, Constants.LengthUnit_m);
            line = Analysis(line);

            ze0 = line.getImpedanceEven();
            zo0 = line.getImpedanceOdd();

            // check for convergence
            err = Math.pow((ze0 - z0e), 2.0) + Math.pow((zo0 - z0o), 2.0);
            if (err < cval) {
                done = true;
            } else {
                // approximate the first jacobian
                line.setMetalWidth(w + delta, Constants.LengthUnit_m);
                line.setMetalSpace(s, Constants.LengthUnit_m);
                line = Analysis(line);
                ze1 = line.getImpedanceEven();
                zo1 = line.getImpedanceOdd();

                line.setMetalWidth(w, Constants.LengthUnit_m);
                line.setMetalSpace(s + delta, Constants.LengthUnit_m);
                line = Analysis(line);
                ze2 = line.getImpedanceEven();
                zo2 = line.getImpedanceOdd();

                dedw = (ze1 - ze0) / delta;
                dodw = (zo1 - zo0) / delta;
                deds = (ze2 - ze0) / delta;
                dods = (zo2 - zo0) / delta;

                // find the determinate
                d = dedw * dods - deds * dodw;

	/*
     * estimate the new solution, but don't change by more than
	 * 10% at a time to avoid convergence problems
	 */
                dw = -1.0 * ((ze0 - z0e) * dods - (zo0 - z0o) * deds) / d;
                if (Math.abs(dw) > 0.1 * w) {
                    if (dw > 0.0)
                        dw = 0.1 * w;
                    else
                        dw = -0.1 * w;
                }
                w = Math.abs(w + dw);

                ds = ((ze0 - z0e) * dodw - (zo0 - z0o) * dedw) / d;
                if (Math.abs(ds) > 0.1 * s) {
                    if (ds > 0.0)
                        ds = 0.1 * s;
                    else
                        ds = -0.1 * s;
                }
                s = Math.abs(s + ds);
            }
        }

        line.setMetalWidth(w, Constants.LengthUnit_m);
        line.setMetalSpace(s, Constants.LengthUnit_m);
        line = Analysis(line);

        // scale the line length to get the desired electrical length
        line.setMetalLength(line.getMetalLength() * len / line.getElectricalLength(), Constants.LengthUnit_m);
        return line;
    }

    public CslinModel getAnaResult(CslinModel line) {
        return Analysis(line);
    }

    public CslinModel getSynResult(CslinModel line, boolean use_z0k) {
        return Synthesize(line, use_z0k);
    }
}
