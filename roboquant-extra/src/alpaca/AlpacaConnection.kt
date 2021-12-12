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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.asset.enums.AssetStatus
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType
import org.roboquant.common.*
import java.util.*

typealias AccountType = EndpointAPIType
typealias DataType = DataAPIType

/**
 * Connect to Alpaca API, logic shared between the Alpaca Feeds and Alpaca Broker
 */
internal object AlpacaConnection {

    private val logger = Logging.getLogger("AlpacaConnection")

    /**
     * Should OTC assets be included, default is false
     */
    private var includeOTC = false

    fun getAPI(
        apiKey: String? = null,
        apiSecret: String? = null,
        accountType: AccountType = AccountType.PAPER,
        dataType: DataType = DataType.IEX
    ): AlpacaAPI {
        val finalKey = apiKey ?: Config.getProperty("APCA_API_KEY_ID")
        val finalSecret = apiSecret ?: Config.getProperty("APCA_API_SECRET_KEY")
        require(finalKey != null) { "No public key provided or set as environment variable APCA_API_KEY_ID" }
        require(finalSecret != null) { "No secret key provided or set as environment variable APCA_API_SECRET_KEY" }
        return AlpacaAPI(finalKey, finalSecret, accountType, dataType)
    }

    /**
     * Get the available assets
     */
    fun getAvailableAssets(api: AlpacaAPI): SortedSet<Asset> {
        val availableAssets = api.assets().get(AssetStatus.ACTIVE, "us_equity")
        val exchangeCodes = Exchange.exchanges.map { e -> e.exchangeCode }
        val result = mutableListOf<Asset>()
        availableAssets.forEach {
            if (it.exchange != "OTC" || includeOTC) {
                if (it.exchange !in exchangeCodes) logger.warning("Exchange ${it.exchange} not configured")
                result.add(Asset(it.symbol, AssetType.STOCK, "USD", it.exchange, id = it.id))
            }
        }
        return result.toSortedSet()
    }

}