/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.ta

import org.hipparchus.stat.correlation.Covariance
import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.diff
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.Strategy
import java.time.Instant
import kotlin.math.min

/**
 * Betting against Beta (BaB) is a strategy based on the premise that high beta stocks might be overvalued and
 * low beta stocks undervalued. So this strategy goes long on low beta stocks and short on high beta stocks.
 *
 * It will then hold these positions for a number of days before re-evaluating the strategy. After re-evaluation, the
 * strategy will then generate the market orders required to achieve the desired new portfolio composition.
 *
 * Since this strategy controls the complete portfolio and not just generates signals, it is implemented as a signalConverter
 * and not a strategy. It doesn't use leverage or buying power, when re-balancing it just re-balances the total equity
 * of the account across the long and short positions.
 *
 * > Betting against Beta was first described in the Journal of Financial Economics
 *
 * @property market the asset presenting the market
 * @property holdingPeriod the holding period, default is 20.days
 * @property maxPositions the maximum number of open positions
 * @property windowSize the windowSize, default is 120
 * @property priceType the type of price to use, default is DEFAULT
 *
 * @constructor Create a new instance of Betting Against Beta
 */
open class BettingAgainstBeta(
    assets: Collection<Asset>,
    private val market: Asset,
    private val holdingPeriod: TimeSpan = 20.days,
    private val maxPositions: Int = 20,
    private val windowSize: Int = 120,
    private val priceType: String = "DEFAULT"
) : Strategy {

    private var rebalanceDate = Instant.MIN

    private val data = mutableMapOf<Asset, PriceSeries>()

    init {
        require(market in assets) { "The selected market asset $market also has to be part of all assets" }
    }

    /**
     * Calculate the betas for the assets. If a beta cannot be calculated, for example, due to missing data,
     * it will not be included in the returned result.
     *
     * @return
     */
    private fun calculateBetas(): List<Pair<Asset, Double>> {
        val betas = mutableListOf<Pair<Asset, Double>>()
        val x = data.getValue(market).toDoubleArray().returns()
        for ((asset, data2) in data) {
            if (asset != market) {
                val y = data2.toDoubleArray().returns()
                val beta = Covariance().covariance(x, y)
                if (!beta.isNaN()) betas.add(Pair(asset, beta))
            }
        }
        // Sort the list in place by beta value, low to high
        betas.sortBy { it.second }
        return betas
    }

    /**
     * Re-balance the portfolio based on the calculated betas.
     *
     * @param betas
     * @param account
     * @return
     */
    private fun rebalance(betas: List<Pair<Asset, Double>>, account: Account, event: Event): List<Instruction> {
        // The maximum number of short and long assets we want to have in the portfolio. Since there cannot be overlap,
        // the maximum number is always equal or smaller than half.
        val max = min(betas.size / 2, maxPositions / 2)

        // exposure per position.
        val exposure = account.equity().convert(account.baseCurrency, time = event.time) / (max * 2)

        fun getPosition(asset: Asset, price: Double, direction: Int): Position? {
            val assetAmount = exposure.convert(asset.currency, event.time).value
            val size = asset.contractSize(assetAmount, price) * direction
            return if (!size.iszero) Position(asset, size) else null
        }

        val targetPortfolio = mutableListOf<Position>()
        // Generate the long positions assets with a low beta
        betas.subList(0, max).forEach { (asset, _) ->
            val price = event.getPrice(asset)
            if (price != null) {
                val position = getPosition(asset, price, 1)
                targetPortfolio.addNotNull(position)
            }
        }

        // Generate the short positions for assets with a high beta
        betas.reversed().subList(0, max).forEach { (asset, _) ->
            val price = event.getPrice(asset)
            if (price != null) {
                val position = getPosition(asset, price, -1)
                targetPortfolio.addNotNull(position)
            }
        }

        // Get the difference of target portfolio state and the current one
        val diff = account.positions.values.diff(targetPortfolio)

        // Transform difference into Orders
        return diff.map { createOrder(it.key, it.value, account, event) }.filterNotNull()
    }

    /**
     * Override this method if you want to replace the default creation of a [MarketOrder] with a different
     * order type like LimitOrders.
     * Return null if you don't want to create an order for a certain asset.
     */
    open fun createOrder(asset: Asset, size: Size, account: Account, event: Event): Instruction? {
        return MarketOrder(asset, size)
    }

    /**
     * Create zero or more orders based on the received signals.
     *
     * @param account the account state at a point in time
     * @param event the market data
     * @return
     */
    override fun create(event: Event, account: Account): List<Instruction> {

        // First, we update the buffers
        data.addAll(event, windowSize, priceType)

        // Check if it is time to re-balance the portfolio
        if (event.time >= rebalanceDate && data.getValue(market).isFull()) {
            val betas = calculateBetas()

            // Update the re-balance date
            rebalanceDate = event.time + holdingPeriod
            return rebalance(betas, account, event)
        }
        return emptyList()
    }


}
