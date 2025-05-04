/*
 * Copyright 2020-2025 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.util.apitype.MarketDataWebsocketSourceType
import net.jacobpeterson.alpaca.model.util.apitype.TraderAPIEndpointType
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockFeed
import org.roboquant.common.Config
import org.roboquant.common.Exchange

/**
 * Alias for EndpointAPIType
 */
typealias AccountType = TraderAPIEndpointType

/**
 * Alias for DataAPIType
 */
typealias DataType = MarketDataWebsocketSourceType

/**
 * Alpaca configuration properties
 *
 * @property publicKey the public key to access the API (property name is alpaca.public.key)
 * @property secretKey the secret key to access the API (property name is alpaca.secret.key)
 * @property accountType account type, either PAPER or LIVE, default is PAPER
 * @property dataType type of data to use, IEX or SIP, default is IEX
 * @property extendedHours enable extended hours for trading, default is false
 *
 * @constructor Create a new instance of AlpacaConfig
 */
data class AlpacaConfig(
    var publicKey: String = Config.getProperty("alpaca.public.key", ""),
    var secretKey: String = Config.getProperty("alpaca.secret.key", ""),
    var accountType: AccountType = AccountType.PAPER,
    var dataType: DataType = DataType.IEX,
    var stockFeed: StockFeed = StockFeed.IEX,
    var extendedHours: Boolean = Config.getProperty("alpaca.extendedhours", false),
)

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
        require(config.publicKey.isNotBlank()) { "no public key provided" }
        require(config.secretKey.isNotBlank()) { "no secret key provided" }
        return AlpacaAPI(config.publicKey, config.secretKey, config.accountType, config.dataType)
    }


}
