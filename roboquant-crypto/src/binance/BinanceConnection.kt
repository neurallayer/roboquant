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
import com.binance.api.client.BinanceApiRestClient
import org.roboquant.common.Asset
import org.roboquant.common.Config

data class BinanceConfig(
    var publicKey: String = Config.getProperty("binance.public.key", ""),
    var secretKey: String = Config.getProperty("binance.secret.key", "")
)

internal object BinanceConnection {

    fun getFactory(config: BinanceConfig): BinanceApiClientFactory {
        return if (config.publicKey.isBlank()) BinanceApiClientFactory.newInstance()
        else BinanceApiClientFactory.newInstance(config.publicKey, config.secretKey)
    }


    /**
     * get available assets
     */
    fun retrieveAssets(client: BinanceApiRestClient): Map<String, Asset> {
        return client.exchangeInfo.symbols.associate {
            it.symbol to Asset.crypto(it.baseAsset, it.quoteAsset, "BINANCE")
        }
    }

}