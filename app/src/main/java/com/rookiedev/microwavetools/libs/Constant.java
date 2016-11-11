package com.rookiedev.microwavetools.libs;

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

    public static final int LOSSLESS = 0;

    public static final int LOSSY = 1;

    public static double MIL2MICRON(double x) {
        return (x * 25.4);
    }

    public static double MICRON2MIL(double x) {
        return (x / 25.4);
    }

    public static double MIL2UM(double x) {
        return (x * 25.4);
    }

    public static double UM2MIL(double x) {
        return (x / 25.4);
    }

    public static double MIL2MM(double x) {
        return (x * 25.4e-3);
    }

    public static double MM2MIL(double x) {
        return (x / 25.4e-3);
    }

    public static double MIL2CM(double x) {
        return (x * 25.4e-4);
    }

    public static double CM2MIL(double x) {
        return (x / 25.4e-4);
    }

    public static double MIL2M(double x) {
        return (x * 25.4e-6);
    }

    public static double M2MIL(double x) {
        return (x / 25.4e-6);
    }

    public static double INCH2M(double x) {
        return (x * 25.4e-3);
    }

    public static double M2INCH(double x) {
        return (x / 25.4e-3);
    }

}
