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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetClass
import net.jacobpeterson.alpaca.model.endpoint.assets.enums.AssetStatus
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType
import org.roboquant.common.*

typealias AccountType = EndpointAPIType
typealias DataType = DataAPIType

/**
 * Alpaca configuration properties
 *
 * @property publicKey
 * @property secretKey
 * @property accountType
 * @property dataType
 * @constructor Create empty Alpaca config
 */
data class AlpacaConfig(
    var publicKey: String = Config.getProperty("alpaca.public.key", ""),
    var secretKey: String = Config.getProperty("alpaca.secret.key", ""),
    var accountType: AccountType = AccountType.PAPER,
    var dataType: DataType = DataType.IEX,
    var extendedHours: Boolean = false
) {

    init {
        require(accountType == AccountType.PAPER) { "Only Paper trading supported, received $accountType" }
    }

}

/**
 * Logic shared between the Alpaca Feeds and Alpaca Broker
 */
internal object Alpaca {

    private val logger = Logging.getLogger(Alpaca::class)

    init {
        if (Exchange.exchanges.none { it.exchangeCode == "FTXU" }) {
            Exchange.addInstance("FTXU", "UTC", opening = "00:00", closing = "23:59:59.999")
        }
    }

    /**
     * Should OTC (over The Counter) assets be included, default is false
     */
    private var includeOTC = false

    fun getAPI(
        config: AlpacaConfig
    ): AlpacaAPI {
        require(config.publicKey.isNotBlank()) { "No public key provided" }
        require(config.secretKey.isNotBlank()) { "No secret key provided" }
        return AlpacaAPI(config.publicKey, config.secretKey, config.accountType, config.dataType)
    }

    /**
     * Get the available assets
     */
    fun getAvailableAssets(api: AlpacaAPI): Map<String, Asset> {
        val availableAssets = api.assets().get(AssetStatus.ACTIVE, AssetClass.CRYPTO) + api.assets()
            .get(AssetStatus.ACTIVE, AssetClass.US_EQUITY)
        val exchangeCodes = Exchange.exchanges.map { e -> e.exchangeCode }
        val result = mutableMapOf<String, Asset>()
        availableAssets.forEach {
            if (it.exchange != "OTC" || includeOTC) {
                if (it.exchange !in exchangeCodes) logger.warn("Exchange ${it.exchange} not configured")
                val assetClass = if (it.assetClass == AssetClass.CRYPTO) AssetType.CRYPTO else AssetType.STOCK
                result[it.id] = Asset(it.symbol, assetClass, "USD", it.exchange)
            }
        }
        return result
    }

}