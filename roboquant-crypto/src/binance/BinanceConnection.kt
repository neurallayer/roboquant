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

package org.roboquant.binance

import com.binance.api.client.BinanceApiClientFactory
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config

internal val binanceTemplate = Asset("TEMPLATE", AssetType.CRYPTO, exchangeCode = "BINANCE")

internal object BinanceConnection {

    private const val KEY_NAME = "BINANCE_API_KEY"
    private const val SECRET_NAME = "BINANCE_API_SECRET"

    fun getFactory(apiKey: String? = null, secret: String? = null): BinanceApiClientFactory {
        val finalKey = apiKey ?: Config.getProperty(KEY_NAME)
        val finalSecret = secret ?: Config.getProperty(SECRET_NAME)
        return if (apiKey == null) BinanceApiClientFactory.newInstance() else BinanceApiClientFactory.newInstance(
            finalKey,
            finalSecret
        )
    }

    /**
     * get available assets
     *
     * TODO currently broken, seems to be a Binance problem
     */
    fun retrieveAssets(api: BinanceApiClientFactory): List<Asset> {
        val client = api.newRestClient()
        return client.exchangeInfo.symbols.map {
            binanceTemplate.copy(symbol = it.symbol, currencyCode = it.quoteAsset)
        }
    }


}