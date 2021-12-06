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

package org.roboquant.ibkr

import com.ib.client.*
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

/**
 * Shared utilities for both IBKR Broker Feed classes
 */
internal object IBKRConnection {

    private val logger = Logging.getLogger("IBKRConnection")
    private val connections = mutableMapOf<Int, EClientSocket>()


    fun disconnect(client: EClientSocket) {
        try {
            if (client.isConnected) client.eDisconnect()
        } catch (_: IOException) {
        }
    }

    /**
     * Connect to a IBKR TWS or Gateway
     *
     * @param host
     * @param port
     * @param clientId
     */
    fun connect(wrapper: EWrapper, host: String, port: Int, clientId: Int): EClientSocket {
        val oldClient = connections[clientId]
        if (oldClient !== null) disconnect(oldClient)

        val signal = EJavaSignal()
        val client = EClientSocket(wrapper, signal)
        client.isAsyncEConnect = false
        client.eConnect(host, port, clientId)
        logger.info { "Connected to IBKR on $host and port $port with clientId $clientId" }

        val reader = EReader(client, signal)
        reader.start()
        Thread {
            while (client.isConnected) {
                signal.waitForSignal()
                try {
                    reader.processMsgs()
                } catch (e: Exception) {
                    logger.warning("Exception: " + e.message)
                }
            }
        }.start()
        connections[clientId] = client
        return client
    }

    fun getFormattedTime(time: Instant): String = SimpleDateFormat("yyyyMMdd HH:mm:ss").format(Date.from(time))

    /**
     * Convert a roboquant [asset] to an IBKR contract.
     *
     * TODO support more asset types
     */
    fun getContract(asset: Asset): Contract {
        val contract = Contract()
        contract.symbol(asset.symbol)
        contract.currency(asset.currencyCode)
        if (asset.multiplier != 1.0) contract.multiplier(asset.multiplier.toString())

        when (asset.type) {
            AssetType.STOCK -> contract.secType(Types.SecType.STK)
            AssetType.FOREX -> contract.secType(Types.SecType.CASH)
            AssetType.BOND -> contract.secType(Types.SecType.BOND)
            else -> throw Exception("${asset.type} is not yet supported")
        }

        val exchange = when (asset.exchangeCode) {
            "NASDAQ" -> "ISLAND"
            "" -> "SMART"
            else -> asset.exchangeCode
        }
        contract.exchange(exchange)

        if (asset.id.isNotEmpty()) contract.conid(asset.id.toInt())
        return contract
    }

}

