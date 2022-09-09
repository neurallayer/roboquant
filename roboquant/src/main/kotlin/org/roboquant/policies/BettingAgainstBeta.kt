/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.policies

import org.apache.commons.math3.stat.correlation.Covariance
import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.diff
import org.roboquant.common.Asset
import org.roboquant.common.Size
import org.roboquant.common.days
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import org.roboquant.strategies.utils.AssetReturns
import java.time.Instant
import kotlin.math.floor
import kotlin.math.min

/**
 * Betting against Beta (BaB) is a strategy based on the premise that high beta stocks might be overvalued and
 * low beta stocks undervalued. So this strategy goes long on low beta stocks and short on high beta stocks.
 *
 * It will then hold these positions for a number of days before re-evaluating the strategy. After re-evaluation, the
 * strategy will then generate the market orders required to achieve the desired new portfolio composition.
 *
 * Since this strategy controls the complete portfolio and not just generates signals, it is implemented as a [Policy]
 * and not a strategy. It doesn't use leverage or buying power, when re-balancing it just re-balances the total equity
 * of the account across the long and short positions.
 *
 * > Betting against Beta was first described by Andrea Frazzinia and Lasse Heje Pedersen in Journal of Financial
 * Economics
 *
 * @constructor Create new Betting Against Beta instance
 */
open class BettingAgainstBeta(
    assets: Collection<Asset>,
    val market: Asset,
    private val holdingPeriodDays: Int = 20,
    private val maxAssetsInPortfolio: Int = 20,
    windowSize: Int = 120,
) : BasePolicy() {

    private var rebalanceDate = Instant.MIN
    private val data = AssetReturns(assets, windowSize, Double.NaN)

    init {
        require(market in assets) { "The selected market asset $market also has to be part of all assets" }
    }

    /**
     * Calculate the betas for the assets. If a beta cannot be calculated, for example due to missing data,
     * it will not be included in the returned result.
     *
     * @return
     */
    private fun calculateBetas(): List<Pair<Asset, Double>> {
        val betas = mutableListOf<Pair<Asset, Double>>()
        val x = data.toDoubleArray(market)
        data.assets.forEach { asset ->
            if (asset != market) {
                val y = data.toDoubleArray(asset)
                val beta = Covariance().covariance(x, y)
                if (!beta.isNaN()) betas.add(Pair(asset, beta))
            }
        }
        // Sort the list by beta value, low to high
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
    private fun rebalance(betas: List<Pair<Asset, Double>>, account: Account, event: Event): List<Order> {
        // maximum number of short and long assets we want to have in portfolio. Since there cannot be overlap,
        // the maximum number is always equal or smaller than half.
        val max = min(betas.size / 2, maxAssetsInPortfolio / 2)

        // exposure per position.
        val exposure = account.equity.convert(time = event.time) / (max * 2)

        val targetPortfolio = mutableListOf<Position>()
        // Generate the long positions assets with a low beta
        betas.subList(0, max).forEach { (asset, _) ->
            val price = event.getPrice(asset)
            if (price != null) {
                val assetAmount = exposure.convert(asset.currency, event.time).value
                val singleContractValue = asset.value(Size.ONE, price).value
                val holding = floor(assetAmount / singleContractValue).toInt()
                if (holding != 0) targetPortfolio.add(Position(asset, Size(holding)))
            }
        }

        // Generate the short positions for assets with a high beta
        betas.reversed().subList(0, max).forEach { (asset, _) ->
            val price = event.getPrice(asset)
            if (price != null) {
                val assetAmount = exposure.convert(asset.currency, event.time).value
                val singleContractValue = asset.value(Size.ONE, price).value
                val holding = floor(assetAmount / singleContractValue).toInt()
                if (holding != 0) targetPortfolio.add(Position(asset, Size(-holding)))
            }
        }

        // Get the difference of target portfolio state and the current one
        val diff = account.portfolio.diff(targetPortfolio)

        // Transform difference into Orders
        return diff.map { createOrder(it.key, it.value, account, event) }.filterNotNull()
    }

    /**
     * Override this method if you want to override the default creation of MarketOrders with a different
     * order type like LimitOrders. Return null if you don't want to create an order for a certain asset.
     */
    open fun createOrder(asset: Asset, size: Size, account: Account, event: Event): Order? {
        return MarketOrder(asset, size)
    }

    /**
     * Create zero or more orders based on the received signals.
     *
     * @param signals
     * @param account the data at a point in time (as generated by the universe)
     * @return
     */
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {

        // First we update the buffers
        data.add(event)

        // Check if it is time to re-balance the portfolio
        if (event.time >= rebalanceDate && data.isAvailable()) {
            val betas = calculateBetas()

            // Update the re-balance date
            rebalanceDate = event.time + holdingPeriodDays.days
            return rebalance(betas, account, event)
        }
        return emptyList()
    }

    /**
     * Clear any state
     */
    override fun reset() {
        data.clear()
        rebalanceDate = Instant.MIN
    }

}
