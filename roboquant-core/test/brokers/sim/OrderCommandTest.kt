/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.brokers.sim

import org.junit.Test
import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.StopLimitOrder
import org.roboquant.orders.StopOrder
import java.time.Instant

internal class OrderCommandTest {


    private fun getAssetAndPricing(): Pair<Asset, Pricing> {
        val asset = TestData.usStock()
        val engine = NoSlippagePricing()
        val pricing = engine.getPricing(TradePrice(asset, 100.0), Instant.now())
        return Pair(asset, pricing)
    }

    @Test
    fun testMarketOrder() {
        val (asset, pricing) = getAssetAndPricing()
        val order = MarketOrder(asset, 100.0)
        val cmd = MarketOrderCommand(order)
        cmd.execute(pricing, Instant.now())
    }


    @Test
    fun testStopOrder() {
        val asset = TestData.usStock()
        val order = StopOrder(asset, -10.0, 99.0)
        val cmd = StopOrderCommand(order)

    }

    @Test
    fun testLimitOrder() {
        val asset = TestData.usStock()
        val order = LimitOrder(asset, -10.0, 101.0)
        val cmd = LimitOrderCommand(order)
    }


    @Test
    fun testStopLimitOrder() {
        val asset = TestData.usStock()
        val order = StopLimitOrder(asset, -10.0, 99.0, 98.0)
        val cmd = StopLimitOrderCommand(order)
    }



}