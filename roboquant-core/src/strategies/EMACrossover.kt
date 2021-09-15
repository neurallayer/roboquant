package org.roboquant.strategies

import org.roboquant.Phase
import org.roboquant.common.Asset
import java.time.Instant

/**
 * Strategy that use the crossover of two Exponential Moving Averages (EMA) to generate
 * a BUY or SELL signal. It is a commonly used in FOREX trading, but can also be applied
 * to other asset classes.
 *
 * The rules are straight forward:
 *
 * - If the fast EMA crosses over the slow EMA, create a BUY signal
 * - If the fast EMA crosses under the slow EMA, create a SELL signal
 * - Don't generate a signal in all other cases.
 *
 * @constructor Create a new EMACrossover strategy
 *
 * @param fastPeriod The shorter period or fast EMA in number of steps
 * @param slowPeriod The longer period or slow EMA in number of steps
 * @param smoothing Smoothing factor to use, default is 2.0
 * @property minEvents minimal number of events observed before starting to execute the strategy, default is the same
 * as the long period
 */
class EMACrossover(fastPeriod: Int = 12, slowPeriod: Int = 26, smoothing: Double = 2.0, val minEvents: Int = slowPeriod) :
    PriceStrategy(prefix = "strategy.ema.") {

    val fast = 1.0 - (smoothing / (fastPeriod + 1))
    val slow = 1.0 - (smoothing / (slowPeriod + 1))
    private val calculators = HashMap<Asset, EMACalculator>()

    companion object Factory {
        /**
         * Predefined EMA Crossover with 50 steps for fast EMA and 200 steps for slow EMA
         *
         * @return new EMACrossover
         */
        fun longTerm(): EMACrossover = EMACrossover(50, 200)

        /**
         * Predefined EMA Crossover with 12 steps for fast EMA and 26 steps for slow EMA
         *
         * @return new EMACrossover
         */
        fun midTerm(): EMACrossover = EMACrossover(12, 26)

        /**
         * Predefined EMA Crossover with 5 steps for fast EMA and 15 steps for slow EMA
         *
         * @return new EMACrossover
         */
        fun shortTerm(): EMACrossover = EMACrossover(5, 15)
    }

    private inner class EMACalculator(initialPrice: Double) {
        private var step = 1L
        var emaFast = initialPrice
        var emaSlow = initialPrice

        fun addPrice(price: Double) {
            emaFast = emaFast * fast + (1 - fast) * price
            emaSlow = emaSlow * slow + (1 - slow) * price
            step += 1
        }

        fun isReady(): Boolean = step >= minEvents

        fun getDirection(): Boolean = emaFast > emaSlow
    }


    override fun generate(asset: Asset, price: Double, now: Instant): Signal? {
        val calculator = calculators[asset]
        if (calculator != null) {
            val oldDirection = calculator.getDirection()
            calculator.addPrice(price)
            if (calculator.isReady()) {
                val newDirection = calculator.getDirection()

                record("${asset.symbol}.short", calculator.emaFast)
                record("${asset.symbol}.long", calculator.emaSlow)

                if (oldDirection != newDirection) {
                    val rating = if (newDirection) Rating.BUY else Rating.SELL
                    return Signal(asset, rating)
                }
            }
        } else {
            calculators[asset] = EMACalculator(price)
        }

        return null
    }

    override fun start(phase: Phase) {
        calculators.clear()
    }

}