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

/**
 * Alias for EndpointAPIType
 */
typealias AccountType = EndpointAPIType

/**
 * Alias for DataAPIType
 */
typealias DataType = DataAPIType

/**
 * Alpaca configuration properties
 *
 * @property publicKey the public key to access the API
 * @property secretKey the secret key to access the API
 * @property accountType account type, either PAPER or LIVE
 * @property dataType type of data to use, IEX or SIP
 * @constructor Create new Alpaca config
 */
data class AlpacaConfig(
    var publicKey: String = Config.getProperty("alpaca.public.key", ""),
    var secretKey: String = Config.getProperty("alpaca.secret.key", ""),
    var accountType: AccountType = AccountType.PAPER,
    var dataType: DataType = DataType.IEX
) {

    init {
        require(accountType == AccountType.PAPER) { "Only Paper trading supported, received $accountType" }
    }

}

/**
 * Logic shared between the Alpaca Feeds and Alpaca Broker
 */
internal object Alpaca {

    init {
        if (Exchange.exchanges.none { it.exchangeCode == "FTXU" }) {
            Exchange.addInstance("FTXU", "UTC", opening = "00:00", closing = "23:59:59.999")
        }
    }

    internal fun getAPI(
        config: AlpacaConfig
    ): AlpacaAPI {
        require(config.publicKey.isNotBlank()) { "No public key provided" }
        require(config.secretKey.isNotBlank()) { "No secret key provided" }
        return AlpacaAPI(config.publicKey, config.secretKey, config.accountType, config.dataType)
    }

    /**
     * Get the available stocks
     */
    internal fun getAvailableStocks(api: AlpacaAPI): Map<String, Asset> {
        val assets = api.assets().get(AssetStatus.ACTIVE, AssetClass.US_EQUITY).filter { it.exchange != "OTC" }
        val exchange = Exchange.getInstance("US")
        return assets.map { Asset(it.symbol, AssetType.STOCK, exchange = exchange) }.associateBy { it.symbol }
    }

    /**
     * Get the available crypto
     */
    internal fun getAvailableCrypto(api: AlpacaAPI): Map<String, Asset> {
        val assets = api.assets().get(AssetStatus.ACTIVE, AssetClass.CRYPTO)
        val exchange = Exchange.getInstance("CRYPTO")
        return assets.map { Asset(it.symbol, AssetType.CRYPTO, exchange = exchange) }.associateBy { it.symbol }
    }

}