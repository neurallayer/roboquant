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

package org.roboquant.oanda

import com.oanda.v20.Context
import com.oanda.v20.ContextBuilder
import com.oanda.v20.account.AccountID
import com.oanda.v20.primitives.InstrumentType
import org.roboquant.Roboquant
import org.roboquant.brokers.FeedCurrencyConverter
import org.roboquant.brokers.sim.DefaultCostModel
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config
import org.roboquant.logging.MemoryLogger
import org.roboquant.metrics.Metric
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.Strategy

object OANDA {

    /**
     * Create a roboquant instance configured for back testing Forex trading. Although trading Forex is just like any
     * another asset class, there are some configuration paramters that are different from assets classes like stocks:
    - Being short is as common as being long
    - The spread (for common currency pairs) is lower than most stocks
     */
    fun roboquant(strategy: Strategy, vararg metrics: Metric, currencyConverter: FeedCurrencyConverter? = null): Roboquant<MemoryLogger> {
        // We allow shorting
        val policy = DefaultPolicy(shorting = true)

        // We use a lower cost model, since the default of 10 BIPS is too much for Forex
        // We select 2.0 BIPS (OANDA typically is around 1.5 which high peaks of 10.0)
        val costModel = DefaultCostModel(2.0)
        val broker = SimBroker(costModel = costModel, currencyConverter = currencyConverter)

        return Roboquant(strategy, *metrics, policy = policy, broker = broker)
    }

    internal fun getContext(token: String?, demo: Boolean): Context {
        val url = if (demo) "https://api-fxpractice.oanda.com/" else "https://api-fxtrade.oanda.com/"
        val apiToken = token ?: Config.getProperty("OANDA_API_KEY")
        require(apiToken != null) { "Couldn't locate API token OANDA_API_KEY" }
        return ContextBuilder(url)
            .setToken(apiToken)
            .setApplication("roboquant")
            .build()
    }


    internal fun getAccountID(id: String?, ctx: Context): AccountID {
        val accounts = ctx.account.list().accounts.map { it.id.toString() }
        var accountId = id ?: Config.getProperty("OANDA_ACCOUNT_ID")
        if (accountId == null) {
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

    internal fun getToken(token: String?): String {
        val apiToken = token ?: Config.getProperty("OANDA_API_KEY")
        require(apiToken != null) { "Couldn't locate API token OANDA_API_KEY" }
        return apiToken
    }


}

