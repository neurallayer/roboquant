package org.roboquant.metrics

import org.hipparchus.stat.correlation.Covariance
import org.hipparchus.stat.descriptive.moment.Mean
import org.hipparchus.stat.descriptive.moment.Skewness
import org.hipparchus.stat.descriptive.moment.StandardDeviation
import org.hipparchus.stat.descriptive.moment.Variance
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.PriceSerie
import org.roboquant.common.Timeframe
import org.roboquant.common.minus
import org.roboquant.feeds.Event
import java.time.Instant
import kotlin.collections.set
import kotlin.math.sqrt

private const val EPS = 0.0000000001

private class TimeBuffer(val size: Int) {

    val data = mutableListOf<Instant>()

    fun add(time: Instant) {
        data.add(time)
        if (size >= 0 && data.size > size) data.removeFirst()
    }

    fun eventsPerYears(): Double {
        val avgMillis = timeframe.duration.toMillis() / data.lastIndex
        return Timeframe.ONE_YEAR_MILLIS / avgMillis
    }

    val timeframe
        get() = Timeframe(data.first(), data.last())

    fun clear() = data.clear()
}

/**
 * Improvement over original returns metric
 */
class ReturnsMetric2(
    private val minSize: Int = 750, // roughly 3 years
    maxSize: Int = minSize,
    private val riskFreeRate: Double = 0.0,
    private val priceType: String = "DEFAULT"
) : Metric {

    private val prices = mutableMapOf<Asset, Double>()
    private var equity = Double.NaN
    private val benchmarkReturns = PriceSerie(maxSize)
    private val accountReturns = PriceSerie(maxSize)
    private var times = TimeBuffer(maxSize)

    init {
        require(minSize > 1) { "minSize should be a larger than 1" }
        require(maxSize >= minSize) { "maxSize should be larger or equal than minSize" }
    }

    /**
     * Market return is the unweighted avg return of the assets found it the event
     */
    private fun updateBenchmark(event: Event) {
        var benchmarkReturn = 0.0
        var n = 0
        for ((asset, action) in event.prices) {
            val price = action.getPrice(priceType)
            val lastPrice = prices[asset]
            if (lastPrice != null) {
                val r = (price - lastPrice) / lastPrice
                benchmarkReturn += r
                n++
            }
            prices[asset] = price
        }
        if (n == 0) benchmarkReturns.add(0.0) else benchmarkReturns.add(benchmarkReturn / n)
    }

    /**
     * Account return is based on equity value
     */
    private fun updateAccount(account: Account) {
        val newEquity = account.equity.convert(account.baseCurrency, account.lastUpdate).value
        if (equity.isFinite()) {
            val result = (newEquity - equity) / equity
            accountReturns.add(result)
        } else {
            accountReturns.add(0.0)
        }
        equity = newEquity
    }


    private fun sharpRatio2(portfolioReturns: DoubleArray, benchmarkReturns: DoubleArray): Double {
        val t = times.eventsPerYears()
        val accessReturns = portfolioReturns - benchmarkReturns
        val stdExcessReturns = StandardDeviation().evaluate(accessReturns) * sqrt(t)
        val mean = Mean().evaluate(accessReturns) * t
        return mean / (stdExcessReturns + EPS)
    }

    private fun beta(portfolioReturns: DoubleArray, benchmarkReturns: DoubleArray): Double {
        val covariance = Covariance().covariance(portfolioReturns, benchmarkReturns)
        val variance = Variance().evaluate(benchmarkReturns)
        return covariance / variance
    }


    private fun DoubleArray.cumReturns() = fold(1.0) { last, d -> last * (d + 1.0) } - 1.0

    override fun calculate(account: Account, event: Event): Map<String, Double> {
        times.add(event.time)
        updateBenchmark(event)
        updateAccount(account)
        if (benchmarkReturns.size >= minSize) {
            val mr = benchmarkReturns.toDoubleArray()
            val ar = accountReturns.toDoubleArray()
            val tf = times.timeframe
            val annualPortfolioReturn = tf.annualize(ar.cumReturns())
            val annualMarketReturn = tf.annualize(mr.cumReturns())
            val t = times.eventsPerYears()

            // Calculate Alpha & Beta
            val beta = beta(ar, mr)
            val alpha = annualPortfolioReturn - riskFreeRate - beta * (annualMarketReturn - riskFreeRate)

            // Calculate Sharpe Ratio's
            val stdReturns = StandardDeviation().evaluate(ar) * sqrt(t)
            val sharpeRatio = (annualPortfolioReturn - riskFreeRate) / (stdReturns + EPS)
            val sharpeRatio2 = sharpRatio2(ar, mr)

            // Calculate annualized access return of portfolio over benchmark
            val excess = annualPortfolioReturn - annualMarketReturn

            return mapOf(
                "returns.market" to annualMarketReturn,
                "returns.account" to annualPortfolioReturn,
                "returns.excess" to excess,
                "returns.beta" to beta,
                "returns.alpha" to alpha,
                "returns.skewness" to Skewness().evaluate(ar),
                "returns.std" to stdReturns,
                "returns.sharperatio" to sharpeRatio,
                "returns.sharperatio2" to sharpeRatio2
            )
        }
        return emptyMap()
    }

    override fun reset() {
        prices.clear()
        equity = Double.NaN
        benchmarkReturns.clear()
        accountReturns.clear()
        times.clear()
    }

}
