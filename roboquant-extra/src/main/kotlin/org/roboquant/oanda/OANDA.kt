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

package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.ContextBuilder
import com.oanda.v20.account.AccountID
import com.oanda.v20.primitives.InstrumentType
import org.roboquant.Roboquant
import org.roboquant.brokers.sim.*
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config
import org.roboquant.common.UnsupportedException
import org.roboquant.metrics.Metric
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.Strategy

/**
 * Configuration for connecting to the OANDA APIs
 *
 * @property key what API key to use
 * @property account which account to use
 * @property demo is this a demo account, default is true
 */
data class OANDAConfig(
    var key: String = Config.getProperty("oanda.key", ""),
    var account: String = Config.getProperty("oanda.account", ""),
    var demo: Boolean = true
)

internal object OANDA {

    /**
     * Create a [Roboquant] instance configured for back testing OANDA trading. Although trading Forex is just like any
     * another asset class, there are some configuration parameters that are different from assets classes like stocks:
     * - Being short is as common as being long
     * - The spread cost (for common currency pairs) is smaller than with most stocks
     * - Leverage is high
     * - There are no fees or commissions
     */
    fun roboquant(strategy: Strategy, vararg metrics: Metric): Roboquant {
        // We allow shorting
        val policy = FlexPolicy(shorting = true)

        // No commissions or fees
        val feeModel = NoFeeModel()

        // We use a lower spread model, since the default of 10 BIPS is too much for most Forex trading
        // We select 2.0 BIPS
        val pricingEngine = SpreadPricingEngine(2)
        val buyingPowerModel = MarginAccount(20.0)
        val broker = SimBroker(feeModel = feeModel, accountModel = buyingPowerModel, pricingEngine = pricingEngine)

        @Suppress("SpreadOperator")
        return Roboquant(strategy, *metrics, policy = policy, broker = broker)
    }

    internal fun getContext(config: OANDAConfig): Context {
        val url = if (config.demo) "https://api-fxpractice.oanda.com/" else
            throw UnsupportedException("onyl demo account supported")
        // else "https://api-fxtrade.oanda.com/"
        require(config.key.isNotBlank()) { "couldn't find key" }
        return ContextBuilder(url).setToken(config.key).setApplication("roboquant").build()
    }

    internal fun getAccountID(id: String, ctx: Context): AccountID {
        val accounts = ctx.account.list().accounts.map { it.id.toString() }
        var accountId = id
        if (id.isBlank()) {
            accountId = accounts.first()
        } else {
            require(accountId in accounts) { "Provided accountID $accountId not in found list $accounts" }
        }
        return AccountID(accountId)
    }

    internal fun getAvailableAssets(ctx: Context, accountID: AccountID): Map<String, Asset> {
        val instruments = ctx.account.instruments(accountID).instruments
        return instruments.map {
            val currency = it.name.split('_').last()
            val type = when (it.type!!) {
                InstrumentType.CURRENCY -> AssetType.FOREX
                InstrumentType.CFD -> AssetType.CFD
                InstrumentType.METAL -> AssetType.CFD
            }
            Asset(it.name.toString(), type = type, currencyCode = currency)
        }.associateBy { it.symbol }
    }

}

