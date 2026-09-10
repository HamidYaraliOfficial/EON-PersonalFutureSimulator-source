package com.eon.futuresimulator.simulation.engine

import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Uncertainty Engine — seeded, explainable sampling primitives.
 *
 * Every EON simulation is Deterministic-and-Reproducible: given the same [Random] seed,
 * the same sequence of samples always comes out, so a Scenario re-run with an unchanged
 * seed always reproduces the same result (see SimulationEngineTest#seed reproducibility).
 * Only Normal, Log-Normal, Uniform and Bernoulli are used — each is explainable in one
 * sentence and every parameter is surfaced to the user via the Assumption Panel.
 */
object RandomDistribution {

    /** Standard normal sample via Box-Muller transform. */
    fun standardNormal(random: Random): Double {
        var u1: Double
        do { u1 = random.nextDouble() } while (u1 <= 1e-12)
        val u2 = random.nextDouble()
        return sqrt(-2.0 * ln(u1)) * kotlin.math.cos(2.0 * Math.PI * u2)
    }

    /** Normal(mean, stddev), clamped to >= [min] so durations/effort never go negative. */
    fun normal(random: Random, mean: Double, stdDev: Double, min: Double = 0.0): Double {
        val sample = mean + standardNormal(random) * stdDev
        return if (sample < min) min else sample
    }

    /** Log-normal — used for variables that are always positive and right-skewed (e.g. "days to recover"). */
    fun logNormal(random: Random, medianValue: Double, sigma: Double): Double {
        val mu = ln(medianValue.coerceAtLeast(1e-6))
        return kotlin.math.exp(mu + standardNormal(random) * sigma)
    }

    fun uniform(random: Random, min: Double, max: Double): Double =
        min + random.nextDouble() * (max - min)

    /** true with probability [p] — used for "was this scheduled day actually completed?". */
    fun bernoulli(random: Random, p: Double): Boolean = random.nextDouble() < p.coerceIn(0.0, 1.0)

    fun percentile(sorted: List<Double>, p: Double): Double {
        if (sorted.isEmpty()) return 0.0
        val idx = (p * (sorted.size - 1)).toInt().coerceIn(0, sorted.size - 1)
        return sorted[idx]
    }
}
