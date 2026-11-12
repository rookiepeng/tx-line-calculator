package com.rookiedev.microwavetools.libs

/**
 * A dielectric preset. [er] and [lossTangent] are typical design values near 10 GHz;
 * [thicknesses] are common stock thicknesses in metres (empty for non-laminate dielectrics).
 */
data class Substrate(val name: String, val er: Double, val lossTangent: Double, val thicknesses: List<Double>)

/** A standard rectangular waveguide: inner [a] × [b] in metres and its recommended band in Hz. */
data class WaveguideSize(val name: String, val a: Double, val b: Double, val bandLow: Double, val bandHigh: Double)

object Materials {
    private fun mm(vararg v: Double) = v.map { it / 1000.0 }

    val substrates: List<Substrate> = listOf(
        Substrate("FR-4", 4.4, 0.020, mm(0.2, 0.4, 0.8, 1.0, 1.6)),
        Substrate("Rogers RO4003C", 3.55, 0.0027, mm(0.203, 0.305, 0.406, 0.508, 0.813, 1.524)),
        Substrate("Rogers RO4350B", 3.66, 0.0037, mm(0.101, 0.168, 0.254, 0.338, 0.508, 0.762, 1.524)),
        Substrate("Rogers RO3003", 3.00, 0.0010, mm(0.127, 0.254, 0.508, 0.762, 1.524)),
        Substrate("Rogers RO3010", 11.2, 0.0022, mm(0.127, 0.254, 0.635, 1.28)),
        Substrate("Rogers RT/duroid 5880", 2.20, 0.0009, mm(0.127, 0.254, 0.381, 0.508, 0.787, 1.575)),
        Substrate("Isola I-Tera MT40", 3.45, 0.0031, mm(0.127, 0.254, 0.508, 0.762)),
        Substrate("Alumina (99.6%)", 9.9, 0.0001, mm(0.254, 0.381, 0.635)),
        Substrate("PTFE", 2.1, 0.0002, emptyList()),
        Substrate("Polyethylene", 2.25, 0.0002, emptyList()),
        Substrate("Air", 1.0006, 0.0, emptyList()),
    )

    private fun wr(name: String, aIn: Double, bIn: Double, lowGHz: Double, highGHz: Double) =
        WaveguideSize(name, aIn * 0.0254, bIn * 0.0254, lowGHz * 1e9, highGHz * 1e9)

    /** EIA WR sizes (inner dimensions in inches) and their recommended TE10 bands. */
    val waveguides: List<WaveguideSize> = listOf(
        wr("WR-430", 4.30, 2.15, 1.70, 2.60),
        wr("WR-284", 2.84, 1.34, 2.60, 3.95),
        wr("WR-187", 1.872, 0.872, 3.95, 5.85),
        wr("WR-137", 1.372, 0.622, 5.85, 8.20),
        wr("WR-112", 1.122, 0.497, 7.05, 10.0),
        wr("WR-90", 0.900, 0.400, 8.20, 12.4),
        wr("WR-75", 0.750, 0.375, 10.0, 15.0),
        wr("WR-62", 0.622, 0.311, 12.4, 18.0),
        wr("WR-51", 0.510, 0.255, 15.0, 22.0),
        wr("WR-42", 0.420, 0.170, 18.0, 26.5),
        wr("WR-34", 0.340, 0.170, 22.0, 33.0),
        wr("WR-28", 0.280, 0.140, 26.5, 40.0),
        wr("WR-22", 0.224, 0.112, 33.0, 50.0),
        wr("WR-15", 0.148, 0.074, 50.0, 75.0),
        wr("WR-10", 0.100, 0.050, 75.0, 110.0),
    )

    /** American Wire Gauge sizes and their diameters in metres: d = 0.127 mm · 92^((36 − n) / 39). */
    val wireGauges: List<Pair<Int, Double>> = listOf(18, 20, 22, 24, 26, 28, 30).map { n ->
        n to 0.127e-3 * Math.pow(92.0, (36.0 - n) / 39.0)
    }

    /** Copper foil weights (oz/ft²) and their thickness in metres. */
    val copperWeights: List<Pair<String, Double>> = listOf(
        "½ oz" to 17.5e-6,
        "1 oz" to 35e-6,
        "2 oz" to 70e-6,
        "3 oz" to 105e-6,
    )
}
