package com.rookiedev.microwavetools.libs

import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Rectangular waveguide, TE10 mode, with broad wall [a] and narrow wall [b] (b < a), filled with a
 * dielectric of relative permittivity er. All values SI; attenuation in Np/m. See Pozar, §3.3.
 */
object Waveguide {
    private const val C = Constants.LIGHTSPEED

    /** TE10 cutoff frequency, Hz. */
    fun cutoff(a: Double, er: Double): Double = C / (2.0 * a * sqrt(er))

    /** Cutoff of the next higher mode (TE20 or TE01), Hz; the top of the single-mode band. */
    fun nextCutoff(a: Double, b: Double, er: Double): Double = min(C / (a * sqrt(er)), C / (2.0 * b * sqrt(er)))

    /** Wavenumber of the filling, rad/m. */
    fun k(f: Double, er: Double): Double = 2.0 * PI * f * sqrt(er) / C

    /** TE10 phase constant, rad/m; NaN at or below cutoff. */
    fun beta(f: Double, a: Double, er: Double): Double {
        val k = k(f, er)
        val kc = PI / a
        return if (k > kc) sqrt(k * k - kc * kc) else Double.NaN
    }

    /** Intrinsic impedance of the filling, Ω. */
    fun eta(er: Double): Double = Constants.FREESPACEZ0 / sqrt(er)

    /** TE10 wave impedance Z = η / √(1 − (fc/f)²), Ω. */
    fun waveImpedance(f: Double, a: Double, er: Double): Double {
        val r = cutoff(a, er) / f
        return eta(er) / sqrt(1.0 - r * r)
    }

    /** Broad wall width giving TE10 wave impedance [z] at [f]; NaN when z ≤ η. */
    fun widthFor(z: Double, f: Double, er: Double): Double {
        val r = eta(er) / z
        if (r >= 1.0) return Double.NaN
        val fc = f * sqrt(1.0 - r * r)
        return C / (2.0 * fc * sqrt(er))
    }

    /** Group delay per metre, s/m: 1/vg = √er / (c·√(1 − (fc/f)²)). */
    fun groupDelay(f: Double, a: Double, er: Double): Double {
        val r = cutoff(a, er) / f
        return sqrt(er) / (C * sqrt(1.0 - r * r))
    }

    /** Wall loss, Pozar (3.96): αc = Rs·(2bπ² + a³k²) / (a³·b·β·k·η). */
    fun conductorLoss(f: Double, a: Double, b: Double, er: Double, surfaceResistance: Double): Double {
        val k = k(f, er)
        val beta = beta(f, a, er)
        return surfaceResistance * (2.0 * b * PI * PI + a * a * a * k * k) / (a * a * a * b * beta * k * eta(er))
    }

    /** Dielectric loss, Pozar (3.29): αd = k²·tanδ / (2β). */
    fun dielectricLoss(f: Double, a: Double, er: Double, lossTangent: Double): Double {
        val k = k(f, er)
        return k * k * lossTangent / (2.0 * beta(f, a, er))
    }
}
