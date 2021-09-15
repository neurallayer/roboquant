package org.roboquant.strategies

import org.roboquant.Phase
import org.roboquant.common.Asset
import org.roboquant.feeds.Event

/**
 * Test strategy that has simple deterministic logic that can help during testing to detect where things go wrong.
 *
 * It will generate an alternating BUY/SELL signal every n-th step.
 * So for example it can generate a BUY signal for Apple at 10th trading day and a SELL signal at 20th
 * trading day.
 *
 *
 * @constructor Create new Test strategy
 */
class TestStrategy(private val skip: Int = 0) : Strategy {

    private val nSignals = mutableMapOf<Asset, Pair<Int, Boolean>>()

    init {
        require(skip >= 0) { "skip should be >= 0, found $skip instead" }
    }

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()

        for (asset in event.prices.keys) {
            var (nSignal, buy) = nSignals.getOrPut(asset) { Pair(0, true) }
            if ((nSignal % (skip + 1)) == 0) {
                val rating = if (buy) Rating.BUY else Rating.SELL
                val signal = Signal(asset, rating)
                signals.add(signal)
                buy = !buy
            }
            nSignals[asset] = Pair(nSignal + 1, buy)
        }
        return signals
    }

    override fun start(phase: Phase) {
        nSignals.clear()
    }
}