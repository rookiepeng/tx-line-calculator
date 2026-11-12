package com.rookiedev.microwavetools.libs

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/** Small RF conversions for the Quick Tools screen. Reflection is |Γ|; power in watts. */
object RfMath {
    fun gammaFromVswr(vswr: Double): Double? = if (vswr >= 1.0) (vswr - 1.0) / (vswr + 1.0) else null
    fun vswrFromGamma(gamma: Double): Double = (1.0 + gamma) / (1.0 - gamma)

    fun gammaFromReturnLoss(db: Double): Double? = if (db > 0.0) 10.0.pow(-db / 20.0) else null
    /** Return loss in dB; infinite for a perfect match. */
    fun returnLossFromGamma(gamma: Double): Double = -20.0 * log10(gamma)

    fun gammaValid(gamma: Double): Double? = gamma.takeIf { it >= 0.0 && it < 1.0 }

    /** Power lost to reflection, dB: −10·log10(1 − |Γ|²). */
    fun mismatchLoss(gamma: Double): Double = -10.0 * log10(1.0 - gamma * gamma)

    fun wattsFromDbm(dbm: Double): Double = 10.0.pow(dbm / 10.0) / 1000.0
    fun dbmFromWatts(watts: Double): Double = 10.0 * log10(watts * 1000.0)

    /** RMS voltage across [ohms] for a power in watts, and back. */
    fun vrmsFromWatts(watts: Double, ohms: Double = 50.0): Double = sqrt(watts * ohms)
    fun wattsFromVrms(vrms: Double, ohms: Double = 50.0): Double = vrms * vrms / ohms

    /** Quarter-wave transformer impedance matching [z0] to a resistive load [zl]. */
    fun quarterWave(z0: Double, zl: Double): Double = sqrt(z0 * zl)
}
