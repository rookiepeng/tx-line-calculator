package com.rookiedev.microwavetools.libs

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

/**
 * Line-independent loss formulas. All inputs and outputs are SI; attenuation is in Np/m.
 */
object Loss {
    /** dB per neper. */
    const val NP_TO_DB: Double = 8.685889638065035

    /** Skin depth δ = 1/√(πfμ0σ), metres. */
    fun skinDepth(frequency: Double, conductivity: Double): Double =
        1.0 / sqrt(PI * frequency * Constants.FREESPACE_MU0 * conductivity)

    /**
     * Surface resistance Rs = √(πfμ0/σ), Ω, times the Hammerstad–Jensen roughness factor
     * 1 + (2/π)·atan(1.4·(Δ/δ)²) for an RMS roughness Δ.
     */
    fun surfaceResistance(frequency: Double, conductivity: Double, roughness: Double = 0.0): Double {
        val rs = sqrt(PI * frequency * Constants.FREESPACE_MU0 / conductivity)
        if (roughness <= 0.0) return rs
        val r = roughness / skinDepth(frequency, conductivity)
        return rs * (1.0 + (2.0 / PI) * atan(1.4 * r * r))
    }

    /**
     * Dielectric attenuation of a quasi-TEM line, Np/m:
     * αd = (πf/c)·(εr/√εeff)·(εeff − 1)/(εr − 1)·tanδ.
     * For a homogeneous line (εeff = εr) this reduces to (πf/c)·√εr·tanδ.
     */
    fun dielectric(frequency: Double, er: Double, eeff: Double, lossTangent: Double): Double {
        val k = PI * frequency / Constants.LIGHTSPEED
        // Filling factor; a line in a (near) air dielectric has all its field in that dielectric.
        val q = if (er - 1.0 < 1e-9) 1.0 else ((eeff - 1.0) / (er - 1.0)).coerceIn(0.0, 1.0)
        return k * er * q * lossTangent / sqrt(eeff)
    }

    /**
     * Conductor attenuation from Wheeler's incremental inductance rule, Np/m:
     * αc = Rs·(∂Z0,air/∂n) / (2·η0·Z0), where ∂Z0,air/∂n is how fast the air-filled
     * impedance grows as every metal wall recedes into the metal, and Z0 is the line's impedance.
     */
    fun conductor(surfaceResistance: Double, z0: Double, dZ0AirDn: Double): Double =
        surfaceResistance * dZ0AirDn / (2.0 * Constants.FREESPACEZ0 * z0)
}
