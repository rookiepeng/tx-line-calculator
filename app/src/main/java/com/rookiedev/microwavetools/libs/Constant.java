package com.rookiedev.microwavetools.libs;

/**
 * Created by rookie on 9/29/2015.
 */
public class Constant {
    /* free space speed of light, meters/second */
    public static double LIGHTSPEED = 2.99792458e8;
    /* free space permitivitty (Henries/meter) */
    public static double FREESPACE_MU0 = 4.0 * Math.PI * 1.0e-7;
    /* free space permitivitty (Farads/meter) */
    public static double FREESPACE_E0 = 1.0 / (LIGHTSPEED * LIGHTSPEED * FREESPACE_MU0);
    /* free space impedance, Ohms */
    public static double FREESPACEZ0 = FREESPACE_MU0 * LIGHTSPEED;

    public static final double Pi = Math.PI;

    public static final double Exp = Math.E;// base of the natural logarithm

    public static final int LOSSLESS=0;

    public static final int LOSSY=1;


}
