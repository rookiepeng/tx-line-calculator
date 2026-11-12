package com.rookiedev.microwavetools.libs

object Constants {
    // SharedPreferences file and app-level keys (per-field keys are built in CalculatorViewModel)
    const val SHARED_PREFS_NAME: String = "com.rookiedev.microwavetools_preferences"
    const val PREFS_POSITION: String = "POSITION"
    const val PREFS_ISFIRSTRUN: String = "ISFIRSTRUN"

    const val DecimalLength: Int = 3

    // constant values
    const val LIGHTSPEED: Double = 2.99792458e8 // free space speed of light, m/s
    val FREESPACE_MU0: Double = 4.0 * Math.PI * 1.0e-7 // free space permeability, H/m
    val FREESPACE_E0: Double = 1.0 / (LIGHTSPEED * LIGHTSPEED * FREESPACE_MU0) // free space permittivity, F/m
    val FREESPACEZ0: Double = FREESPACE_MU0 * LIGHTSPEED // free space impedance, Ohms
    val Pi: Double = Math.PI
    const val MINI_LIMIT: Double = 1e-10 // 0.1 nm

    // flags
    const val Synthesize_Width: Int = 0
    const val Synthesize_Height: Int = 1
    const val Synthesize_Er: Int = 2
    const val Synthesize_Length: Int = 3
    const val Synthesize_Gap: Int = 4
    const val Synthesize_CoreRadius: Int = 5
    const val Synthesize_CoreOffset: Int = 6
    val Synthesize_SubRadius: Int = Synthesize_Height

    /**
     * Converts value to meters based on the length unit
     * 
     * @param value      value to convert
     * @param lengthUnit unit of the value
     * @return value in meters
     */
    fun value2meter(value: Double, lengthUnit: String): Double {
        var l = 0.0
        when (lengthUnit) {
            "mil" -> l = value / 39370.0787402
            "µm" -> l = value / 1e6
            "mm" -> l = value / 1000
            "cm" -> l = value / 100
            "m" -> l = value
        }
        return l
    }

    /**
     * Converts value to Hertz based on the frequency unit
     * 
     * @param value    value to convert
     * @param freqUnit unit of the value
     * @return value in Hertz
     */
    fun value2Hz(value: Double, freqUnit: String): Double {
        var f = 0.0
        when (freqUnit) {
            "Hz" -> f = value
            "MHz" -> f = value * 1e6
            "GHz" -> f = value * 1e9
        }
        return f
    }

    /**
     * Converts value from meters to other units
     * 
     * @param value      value in meters
     * @param lengthUnit target unit
     * @return value in target unit
     */
    fun meter2others(value: Double, lengthUnit: String): Double {
        var l = 0.0
        when (lengthUnit) {
            "mil" -> l = value * 1000 * 39.37007874
            "µm" -> l = value * 1e6
            "mm" -> l = value * 1000
            "cm" -> l = value * 100
            "m" -> l = value
        }
        return l
    }

    enum class ERROR {
        NO_ERROR, ER_MINIMAL_LIMIT, COULD_NOT_BRACKET_SOLUTION, MAX_ITERATIONS,
        WIDTH_MINIMAL_LIMIT, HEIGHT_MINIMAL_LIMIT, SPACE_MINIMAL_LIMIT, K_OUT_OF_RANGE,
        Z0E_Z0O_MISTAKE, SUBSTRATE_TOO_LARGE, OFFSET_TOO_LARGE
    }

    enum class WARNING {
        NO_WARNING, WIDTH2HEIGHT_OUT_OF_RANGE, SPACE2HEIGHT_OUT_OF_RANGE, DIELECTRIC_OUT_OF_RANGE,
        FREQUENCY2HEIGHT_OUT_OF_RANGE
    }
}
