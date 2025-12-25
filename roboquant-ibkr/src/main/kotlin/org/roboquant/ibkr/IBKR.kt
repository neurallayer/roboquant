/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.ibkr

import com.ib.client.*
import org.roboquant.common.*
import org.roboquant.common.Currency
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
 * @property account the account to use, default is empty string and the system will use the first account found
 * @property client the client id to use, default is 2
 *
 * @constructor Create new IBKR config
 */
data class IBKRConfig(
    var host: String = Config.getProperty("ibkr.host", "127.0.0.1"),
    var port: Int = Config.getProperty("ibkr.port", 4002),
    var account: String = Config.getProperty("ibkr.account", ""),
    var client: Int = Config.getProperty("ibkr.client", 2)
)

/**
 * Shared logic for IBKR Broker and Feed classes
 */
object IBKR {

    // Timeout in millis when waiting for one or more response messages to arrive
    internal const val MAX_RESPONSE_TIME = 5_000L

    private val logger = Logging.getLogger(IBKR::class)
    private val connections = mutableMapOf<Int, EClientSocket>()

    // Holds mapping between IBKR contract ids and assets.
    private val assetCache = ConcurrentHashMap<Int, Asset>()

    /**
     * Register an [asset] to IBKR [conId] value. If this mapping exists, no conversions will be attempted.
     * This avoids possible ubiquity since the conId uniquely identifies a tradable asset.
     */
    fun register(conId: Int, asset: Asset) {
        if (conId > 0) assetCache[conId] = asset
    }

    internal fun disconnect(client: EClientSocket) {
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
    internal fun connect(wrapper: EWrapper, config: IBKRConfig): EClientSocket {
        val oldClient = connections[config.client]
        if (oldClient !== null) disconnect(oldClient)

        val signal = EJavaSignal()
        val client = EClientSocket(wrapper, signal)
        client.isAsyncEConnect = false
        client.eConnect(config.host, config.port, config.client)
        if (!client.isConnected) throw ConfigurationException("Couldn't connect with config $config")
        logger.info { "Connected with config $config" }

        val reader = EReader(client, signal)
        reader.start()
        Thread {
            while (client.isConnected) {
                signal.waitForSignal()
                try {
                    reader.processMsgs()
                } catch (e: Throwable) {
                    logger.warn("exception handling ibkr message", e)
                }
            }
        }.start()
        connections[config.client] = client
        return client
    }

    internal fun getFormattedTime(time: Instant): String = SimpleDateFormat("yyyyMMdd-HH:mm:ss").format(Date.from(time))


    /**
     * Convert a roboquant asset to an IBKR contract.
     */
    internal fun Asset.toContract(): Contract {
        val contract = Contract()
        contract.exchange("SMART")

        val id = assetCache.filterValues { it == this }.keys.firstOrNull()
        if (id != null) {
            contract.conid(id)
            return contract
        }

        contract.symbol(symbol)
        contract.currency(currency.currencyCode)
        // if (multiplier != 1.0) contract.multiplier(multiplier.toString())

        when (this) {
            is Stock -> contract.secType(Types.SecType.STK)
            else -> throw UnsupportedException("asset type $this is not yet supported")
        }

        logger.info { "$this into $contract" }
        return contract
    }

    /**
     * Convert an IBKR contract to a roboquant asset
     */
    internal fun Contract.toAsset(): Asset {
        val result = assetCache[conid()]
        result != null && return result

        val asset = when (secType()) {
            Types.SecType.STK -> Stock(symbol(), Currency.getInstance(currency()))
            else -> throw UnsupportedException("Unsupported asset type ${secType()}")
        }

        assetCache[conid()] = asset
        return asset
    }

}

