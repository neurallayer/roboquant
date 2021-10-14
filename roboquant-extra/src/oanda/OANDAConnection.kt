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

package oanda

import com.oanda.v20.Context
import com.oanda.v20.ContextBuilder
import com.oanda.v20.primitives.InstrumentType
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config

/**
 * Contains shared logic for all OANDA classes
 */
internal object OANDAConnection {

    fun getContext(token:String? = null, demo:Boolean = true): Context {
        val url = if (demo)  "https://api-fxpractice.oanda.com/" else  "https://api-fxtrade.oanda.com/"
        val apiToken = token ?: Config.getProperty("OANDA_API_KEY")
        require(apiToken != null) { "Couldn't locate API token OANDA_API_KEY" }
        return ContextBuilder(url)
            .setToken(apiToken)
            .setApplication("roboquant")
            .build()
    }


    fun getAccountID(id: String?, ctx: Context): String {
        val accounts = ctx.account.list().accounts.map { it.id.toString() }
        var accountId = id ?:  Config.getProperty("OANDA_ACCOUNT_ID")
        if (accountId == null) {
            accountId = accounts.first()
        } else {
            require(accountId in accounts) { "Provided accountID $accountId not in found list $accounts"}
        }
        return accountId
    }

    fun getAvailableAssets(ctx: Context): Map<String, Asset> {
        val accountID =  ctx.account.list().accounts.first().id
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

    fun getToken(token: String?): String {
        val apiToken = token ?: Config.getProperty("OANDA_API_KEY")
        require(apiToken != null) { "Couldn't locate API token OANDA_API_KEY" }
        return apiToken
    }
}