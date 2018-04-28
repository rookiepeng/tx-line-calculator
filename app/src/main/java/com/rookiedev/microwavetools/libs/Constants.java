package com.rookiedev.microwavetools.libs;

public class Constants {
    // Shared Preference values
    public static final String SHARED_PREFS_NAME = "com.rookiedev.microwavetools_preferences";
    public static final String PREFS_POSITION = "POSITION";
    public static final String PREFS_ISFIRSTRUN = "ISFIRSTRUN";

    // constant values
    static double LIGHTSPEED = 2.99792458e8; // free space speed of light, m/s
    public static double FREESPACE_MU0 = 4.0 * Math.PI * 1.0e-7; // free space permitivitty, H/m
    public static double FREESPACE_E0 = 1.0 / (LIGHTSPEED * LIGHTSPEED * FREESPACE_MU0); // free space permitivitty, F/m
    static double FREESPACEZ0 = FREESPACE_MU0 * LIGHTSPEED; // free space impedance, Ohms
    static final double Pi = Math.PI;

    // flags
    public static final int Synthesize_Width = 0, Synthesize_Height = 1, Synthesize_Er = 2, Synthesize_Length = 3, Synthesize_Gap = 4, Synthesize_CoreRadius = 5, Synthesize_CoreOffset = 6;
    public static final int Synthesize_SubRadius = Synthesize_Height;
    public static final int LengthUnit_mil = 0, LengthUnit_mm = 1, LengthUnit_cm = 2, LengthUnit_m = 3;
    public static final int FreqUnit_Hz = 10, FreqUnit_MHz = 0, FreqUnit_GHz = 1;

    // unit conversion
    static double value2meter(double value, int lengthUnit) {
        double l = 0;
        switch (lengthUnit) {
            case LengthUnit_mil:
                l = value / 39370.0787402;
                break;
            case LengthUnit_mm:
                l = value / 1000;
                break;
            case LengthUnit_cm:
                l = value / 100;
                break;
            case LengthUnit_m:
                l = value;
                break;
        }
        return l;
    }

    static double value2Hz(double value, int freqUnit) {
        double f = 0;
        switch (freqUnit) {
            case FreqUnit_Hz:
                f = value;
                break;
            case FreqUnit_MHz:
                f = value * 1e6;
                break;
            case FreqUnit_GHz:
                f = value * 1e9;
                break;
        }
        return f;
    }

    public static double meter2others(double value, int lengthUnit) {
        double l = 0;
        switch (lengthUnit) {
            case LengthUnit_mil:
                l = value * 1000 * 39.37007874;
                break;
            case LengthUnit_mm:
                l = value * 1000;
                break;
            case LengthUnit_cm:
                l = value * 100;
                break;
            case LengthUnit_m:
                l = value;
                break;
        }
        return l;
    }
}
