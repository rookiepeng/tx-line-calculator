package com.rookiedev.microwavetools.libs

/**
 * A dielectric preset. [er] and [lossTangent] are typical design values near 10 GHz;
 * [thicknesses] are common stock thicknesses in metres (empty for non-laminate dielectrics).
 */
data class Substrate(val name: String, val er: Double, val lossTangent: Double, val thicknesses: List<Double>)

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

    /** Copper foil weights (oz/ft²) and their thickness in metres. */
    val copperWeights: List<Pair<String, Double>> = listOf(
        "½ oz" to 17.5e-6,
        "1 oz" to 35e-6,
        "2 oz" to 70e-6,
        "3 oz" to 105e-6,
    )
}
