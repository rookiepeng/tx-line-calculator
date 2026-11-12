package com.rookiedev.microwavetools.libs

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Edge-coupled coplanar waveguide: two strips of width W, spaced S apart, each a gap G from
 * semi-infinite coplanar grounds, on a substrate of height H with no ground plane below.
 * Zero metal thickness, quasi-static conformal mapping (Simons, *Coplanar Waveguide Circuits,
 * Components and Systems*, §7.2): the symmetry plane is an electric wall for the odd mode and a
 * magnetic wall for the even mode, and the dielectric layer enters through a sinh mapping.
 */
object CoupledCpw {

    /** One propagation mode: characteristic impedance (Ω) and effective dielectric constant. */
    data class Mode(val z0: Double, val eeff: Double)

    /** K(k)/K(k') via the arithmetic-geometric mean: K(k)/K(k') = AGM(1, k)/AGM(1, k'). */
    fun ellipticRatio(k: Double): Double {
        val kc = sqrt(1.0 - k * k)
        return agm(1.0, k) / agm(1.0, kc)
    }

    private fun agm(x: Double, y: Double): Double {
        var a = x
        var b = y
        repeat(60) {
            if (abs(a - b) <= 1e-15 * a) return a
            val m = (a + b) / 2
            b = sqrt(a * b)
            a = m
        }
        return a
    }

    /** ln(sinh(u)) for u > 0, without overflow for large u. */
    private fun lnSinh(u: Double): Double = u + ln((1.0 - exp(-2.0 * u)) / 2.0)

    /**
     * Even- and odd-mode moduli for edges at a < b < c, given only the ratios A = a/c and B = b/c
     * (so very large sinh-mapped coordinates never have to be formed).
     */
    private fun moduli(aOverC: Double, bOverC: Double): Pair<Double, Double> {
        val base = sqrt((bOverC * bOverC - aOverC * aOverC) / (1.0 - aOverC * aOverC))
        return base to (base / bOverC).coerceAtMost(1.0 - 1e-16)
    }

    /** Returns (even, odd) modes, or null for non-physical dimensions. */
    fun analyze(w: Double, s: Double, g: Double, h: Double, er: Double): Pair<Mode, Mode>? {
        if (w <= 0.0 || s <= 0.0 || g <= 0.0 || h <= 0.0 || er < 1.0) return null
        val a = s / 2
        val b = a + w
        val c = b + g
        val (kE, kO) = moduli(a / c, b / c)
        // Dielectric layer: x → sinh(πx / 2H). Ratios via ln sinh keep thin substrates finite.
        fun u(x: Double) = PI * x / (2.0 * h)
        val lc = lnSinh(u(c))
        val (kEh, kOh) = moduli(exp(lnSinh(u(a)) - lc), exp(lnSinh(u(b)) - lc))

        fun mode(kAir: Double, kDie: Double): Mode {
            val rAir = ellipticRatio(kAir)
            val eeff = 1.0 + (er - 1.0) / 2.0 * ellipticRatio(kDie) / rAir
            // Per-strip air capacitance is 2·ε0·K(k)/K(k') (half-planes above and below).
            val z0 = 1.0 / (Constants.LIGHTSPEED * sqrt(eeff) * 2.0 * Constants.FREESPACE_E0 * rAir)
            return Mode(z0, eeff)
        }
        return mode(kE, kEh) to mode(kO, kOh)
    }
}
