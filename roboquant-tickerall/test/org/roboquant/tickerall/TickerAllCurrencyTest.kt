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

package org.roboquant.tickerall

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.roboquant.common.Currency
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests that an asset is denoted in the instrument's quote currency from the broker's symbol metadata,
 * not defaulted to the account currency. Each test stands up a tiny in-process stub of the TickerAll REST API
 * with the JDK's own [HttpServer] (no external service, no credentials, no new dependency) so it always runs
 * in CI, and drives the real broker against it.
 */
internal class TickerAllCurrencyTest {

    private val aid = "acc-test"

    /** A stub serving a fixed set of open positions and per-symbol specs, plus a valid funded account. */
    private inner class Stub(private val positionsJson: String, private val specsJson: String) : AutoCloseable {
        private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl: String get() = "http://127.0.0.1:${server.address.port}"

        init {
            server.createContext("/") { ex -> handle(ex) }
            server.executor = null
            server.start()
        }

        private fun handle(ex: HttpExchange) {
            ex.requestBody.readBytes()
            val path = ex.requestURI.path
            val response: String = when {
                path == "/v1/accounts/$aid" ->
                    """{"account":{"balance":100.0,"currency":"USD","equity":100.0,"freeMargin":100.0},""" +
                        """"positions":$positionsJson,"status":"connected","isDemo":true}"""
                path == "/v1/accounts/$aid/orders/pending" -> """{"orders":[]}"""
                path == "/v1/accounts/$aid/symbol-specs" -> specsJson
                else -> "{}"
            }
            val bytes = response.toByteArray()
            ex.responseHeaders.add("Content-Type", "application/json")
            ex.sendResponseHeaders(200, bytes.size.toLong())
            ex.responseBody.use { it.write(bytes) }
        }

        override fun close() = server.stop(0)
    }

    private fun broker(stub: Stub) = TickerAllBroker { apiKey = "cf_api_test"; accountId = aid; baseUrl = stub.baseUrl }

    @Test
    fun assetCurrencyComesFromSymbolSpec() {
        // "US30": its trailing letters are not the quote currency, so the broker's spec is authoritative.
        val positions = """[{"ticket":1,"symbol":"US30","side":"BUY","volume":1.0,"entryPrice":39000.0,"currentPrice":39010.0}]"""
        val specs = """{"specs":[{"name":"US30","volumeMin":0.1,"volumeStep":0.1,"profitCurrency":"EUR"}]}"""
        Stub(positions, specs).use { stub ->
            val asset = broker(stub).sync().positions.first { it.asset.symbol == "US30" }.asset
            assertEquals(Currency.getInstance("EUR"), asset.currency, "currency from the spec, not the account's USD")
        }
    }

    @Test
    fun assetCurrencyInferredFromPairWhenSpecHasNone() {
        // No spec currency (e.g. an MT4 account): infer the quote from the pair — still not the account currency.
        val positions = """[{"ticket":1,"symbol":"EURGBP","side":"BUY","volume":1.0,"entryPrice":0.85,"currentPrice":0.85}]"""
        Stub(positions, """{"specs":[]}""").use { stub ->
            val asset = broker(stub).sync().positions.first { it.asset.symbol == "EURGBP" }.asset
            assertEquals(Currency.getInstance("GBP"), asset.currency, "inferred quote currency, not the account's USD")
        }
    }
}
