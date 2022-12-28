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

package org.roboquant.ibkr

import com.ib.client.*
import org.roboquant.common.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * IBKR configuration properties
 *
 * @property host the host to connect to, default is 127.0.0.1 (local host)
 * @property port the port to connect to, default is 4002
 * @property account the account to use, default is empty string and system will use first account
 * @property client the client id to use, default is 2
 *
 * @constructor Create new IBKR config
 */
data class IBKRConfig(
    var host: String = Config.getProperty("ibkr.host", "127.0.0.1"),
    var port: Int = Config.getProperty("ibkr.port", "4002").toInt(),
    var account: String = Config.getProperty("ibkr.account", ""),
    var client: Int = Config.getProperty("ibkr.client", "2").toInt()
)

/**
 * Shared logic for IBKR Broker and Feed classes
 */
internal object IBKR {

    // Timeout in millis when waiting for one or more response messages to arrive
    internal const val maxResponseTime = 5_000L

    private val logger = Logging.getLogger(IBKR::class)
    private val connections = mutableMapOf<Int, EClientSocket>()

    // Holds mapping between IBKR contract ids and assets.
    private val assetMap = ConcurrentHashMap<Int, Asset>()

    fun disconnect(client: EClientSocket) {
        try {
            if (client.isConnected) client.eDisconnect()
        } catch (exception: IOException) {
            logger.info(exception.message)
        }
    }

    /**
     * Connect to a IBKR TWS or Gateway
     */
    @Suppress("TooGenericExceptionCaught")
    fun connect(wrapper: EWrapper, config: IBKRConfig): EClientSocket {
        val oldClient = connections[config.client]
        if (oldClient !== null) disconnect(oldClient)

        val signal = EJavaSignal()
        val client = EClientSocket(wrapper, signal)
        client.isAsyncEConnect = false
        client.eConnect(config.host, config.port, config.client)
        if (!client.isConnected) throw ConfigurationException("Couldn't connect with config $config")
        logger.debug { "Connected with config $config" }

        val reader = EReader(client, signal)
        reader.start()
        Thread {
            while (client.isConnected) {
                signal.waitForSignal()
                try {
                    reader.processMsgs()
                } catch (e: Throwable) {
                    logger.warn("Exception: " + e.message)
                }
            }
        }.start()
        connections[config.client] = client
        return client
    }

    fun getFormattedTime(time: Instant): String = SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Date.from(time))

    /**
     * Convert a roboquant asset to an IBKR contract.
     */
    fun Asset.toContract(): Contract {
        val contract = Contract()
        contract.symbol(symbol)
        contract.currency(currency.currencyCode)
        if (multiplier != 1.0) contract.multiplier(multiplier.toString())

        when (type) {
            AssetType.STOCK -> contract.secType(Types.SecType.STK)
            AssetType.FOREX -> contract.secType(Types.SecType.CASH)
            AssetType.BOND -> contract.secType(Types.SecType.BOND)
            AssetType.FUTURES -> {
                contract.secType(Types.SecType.FUT)
                contract.localSymbol(symbol)
                contract.symbol("")
            }

            else -> throw UnsupportedException("asset type $type is not yet supported")
        }


        val exchange = when (exchange.exchangeCode) {
            "NASDAQ" -> "ISLAND"
            "" -> "SMART"
            else -> exchange.exchangeCode
        }
        contract.exchange(exchange)

        val id = assetMap.filterValues { it == this }.keys.firstOrNull()
        if (id != null) contract.conid(id)
        logger.trace { "$this into $contract" }
        return contract
    }

    /**
     * Convert an IBKR contract to a roboquant asset
     */
    internal fun Contract.toAsset(): Asset {
        val result = assetMap[conid()]
        result != null && return result

        val exchangeCode = exchange() ?: primaryExch() ?: ""

        val asset = when (secType()) {
            Types.SecType.STK -> Asset(symbol(), AssetType.STOCK, currency(), exchangeCode)
            Types.SecType.BOND -> Asset(symbol(), AssetType.BOND, currency(), exchangeCode)
            Types.SecType.CASH -> Asset(symbol(), AssetType.FOREX, currency(), exchangeCode)
            Types.SecType.FUT -> Asset(localSymbol(), AssetType.FUTURES, currency(), exchangeCode)
            else -> throw UnsupportedException("Unsupported asset type ${secType()}")
        }

        assetMap[conid()] = asset
        return asset
    }

}

