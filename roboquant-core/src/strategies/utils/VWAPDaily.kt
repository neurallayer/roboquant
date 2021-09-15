package org.roboquant.strategies.utils

import org.roboquant.feeds.PriceBar
import java.time.Instant

import java.time.temporal.ChronoUnit

/**
 * Calculates the Daily VWAP. VWAP is a single-day indicator, and is restarted at the opening of each new trading day.
 * If there is more than 8 hours between two events, it is considered to be a new day and the previous calculations
 * are reset.
 *
 * @property minSteps The minimum number of steps required
 * @constructor Create empty Daily VWAP calculator
 */
class VWAPDaily(private val minSteps: Int = 1) {

    private val total = mutableListOf<Double>()
    private val volume = mutableListOf<Double>()
    private var last: Instant = Instant.MIN


    fun add(action: PriceBar, now: Instant) {
        if (now > last.plus(8, ChronoUnit.HOURS)) clear()
        last = now
        val v = action.volume.toDouble()
        total.add(action.getPrice("TYPICAL") * v)
        volume.add(v)
    }

    fun isReady() = total.size >= minSteps

    fun calc(): Double {
        return total.sum() / volume.sum()
    }

    fun clear() {
        total.clear()
        volume.clear()
        last = Instant.MIN
    }
}