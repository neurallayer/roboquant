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

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.roboquant.common.Currency
import org.roboquant.common.Order
import org.roboquant.common.Size
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for the netting emulation the broker applies to HEDGING MetaTrader accounts (a separate ticket
 * per trade). roboquant models one net position per asset, so on sync the broker aggregates a symbol's
 * tickets into a single net position, and a market order that opposes the current net is executed as a
 * close-by-ticket (plus a market remainder on a reversal) instead of opening an offsetting ticket.
 *
 * Each test stands up a tiny in-process stub of the TickerAll REST API with the JDK's own [HttpServer] (no
 * external service, no credentials, no new dependency) so it always runs in CI, drives the real broker
 * against it, and asserts the exact close / place calls the broker made.
 */
internal class TickerAllNetEmulationTest {

    private val gson = Gson()
    private val aid = "acc-test"

    /** A captured inbound HTTP request. */
    private data class Captured(val method: String, val path: String, val body: String)

    /**
     * A stub TickerAll API serving a fixed [positionsJson] (the account's open tickets) and [specsJson]
     * (per-symbol volume specs), an empty pending-order book and a valid funded account, and recording every
     * request so a test can assert the closes and orders the broker sent.
     */
    private inner class Stub(
        private val positionsJson: String,
        private val specsJson: String = """{"specs":[{"name":"EURUSD","volumeMin":0.01,"volumeMax":100.0,"volumeStep":0.01}]}""",
    ) : AutoCloseable {
        val captured = ConcurrentLinkedQueue<Captured>()
        private var nextTicket = 5000
        private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl: String get() = "http://127.0.0.1:${server.address.port}"

        init {
            server.createContext("/") { ex -> handle(ex) }
            server.executor = null
            server.start()
        }

        private fun handle(ex: HttpExchange) {
            val body = ex.requestBody.readBytes().toString(Charsets.UTF_8)
            val path = ex.requestURI.path
            val method = ex.requestMethod
            captured.add(Captured(method, path, body))
            val response: String = when {
                path == "/v1/accounts/$aid" ->
                    """{"account":{"balance":100.0,"currency":"USD","equity":100.0,"freeMargin":100.0},""" +
                        """"positions":$positionsJson,"status":"connected","isDemo":true}"""
                path == "/v1/accounts/$aid/orders/pending" -> """{"orders":[]}"""
                path == "/v1/accounts/$aid/symbol-specs" -> specsJson
                method == "POST" && path == "/v1/accounts/$aid/orders" -> """{"ticket":${nextTicket++}}"""
                method == "DELETE" && path.startsWith("/v1/accounts/$aid/positions/") -> "{}"
                else -> "{}"
            }
            val bytes = response.toByteArray()
            ex.responseHeaders.add("Content-Type", "application/json")
            ex.sendResponseHeaders(200, bytes.size.toLong())
            ex.responseBody.use { it.write(bytes) }
        }

        /** The close-by-ticket calls the broker made: ticket id -> closed volume (null = full close). */
        fun closes(): List<Pair<String, Double?>> =
            captured.filter { it.method == "DELETE" && it.path.contains("/positions/") }
                .map { c ->
                    val ticket = c.path.substringAfterLast("/")
                    val vol = if (c.body.isBlank()) null else gson.fromJson(c.body, CloseBody::class.java)?.volume
                    ticket to vol
                }

        /** The new orders the broker placed. */
        fun placedOrders(): List<PlaceOrderBody> =
            captured.filter { it.method == "POST" && it.path == "/v1/accounts/$aid/orders" }
                .map { gson.fromJson(it.body, PlaceOrderBody::class.java) }

        override fun close() = server.stop(0)
    }

    private fun broker(stub: Stub) = TickerAllBroker {
        apiKey = "cf_api_test"; accountId = aid; baseUrl = stub.baseUrl
    }

    private fun eurusd() = TickerAll.toAsset("EURUSD", Currency.USD)

    private fun ticket(id: Long, side: String, volume: Double, entry: Double, openTime: String) =
        """{"ticket":$id,"symbol":"EURUSD","side":"$side","volume":$volume,"entryPrice":$entry,"currentPrice":1.25,"openTime":"$openTime"}"""

    @Test
    fun aggregatesHedgedTicketsIntoOneNetPosition() {
        val positions = "[" +
            ticket(111, "BUY", 0.01, 1.10, "2026-01-01T00:00:00Z") + "," +
            ticket(222, "BUY", 0.02, 1.20, "2026-01-02T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val account = broker(stub).sync()
            // Two BUY tickets for one symbol collapse into a single net position.
            assertEquals(1, account.positions.size)
            val pos = account.positions[eurusd()]!!
            assertEquals(Size("0.03"), pos.size, "net size is the sum of the tickets")
            // Volume-weighted average entry: (0.01*1.10 + 0.02*1.20) / 0.03.
            assertEquals(1.16667, pos.avgPrice, 0.0001)
        }
    }

    @Test
    fun fullyHedgedFlatReportsNoPosition() {
        val positions = "[" +
            ticket(111, "BUY", 0.02, 1.10, "2026-01-01T00:00:00Z") + "," +
            ticket(222, "SELL", 0.02, 1.20, "2026-01-02T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val account = broker(stub).sync()
            assertTrue(account.positions.isEmpty(), "a net-flat symbol has no exposure to report")
        }
    }

    @Test
    fun opposingMarketFullyClosesByTicket() {
        val positions = "[" + ticket(111, "BUY", 0.03, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.03"), Double.NaN))) // SELL 0.03 market
            assertEquals(listOf("111" to null), stub.closes(), "closes the full ticket, no volume body")
            assertTrue(stub.placedOrders().isEmpty(), "a pure close opens no new order")
        }
    }

    @Test
    fun opposingMarketPartiallyCloses() {
        val positions = "[" + ticket(111, "BUY", 0.03, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.01"), Double.NaN))) // SELL 0.01 market
            assertEquals(listOf("111" to 0.01), stub.closes(), "partial-closes 0.01 of the ticket")
            assertTrue(stub.placedOrders().isEmpty())
        }
    }

    @Test
    fun opposingMarketReversalClosesThenOpensRemainder() {
        val positions = "[" + ticket(111, "BUY", 0.01, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.03"), Double.NaN))) // SELL 0.03 market, net long 0.01
            assertEquals(listOf("111" to null), stub.closes(), "closes the long ticket in full")
            val placed = stub.placedOrders()
            assertEquals(1, placed.size, "opens the 0.02 remainder past flat")
            assertEquals("market", placed[0].type)
            assertEquals("SELL", placed[0].side)
            assertEquals(0.02, placed[0].volume, 0.0000001)
        }
    }

    @Test
    fun closesNetSideTicketsOldestFirst() {
        val positions = "[" +
            ticket(111, "BUY", 0.01, 1.10, "2026-01-01T00:00:00Z") + "," +
            ticket(222, "BUY", 0.03, 1.20, "2026-01-02T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.02"), Double.NaN))) // SELL 0.02 market, net long 0.04
            // Oldest ticket (111) fully, then 0.01 of the next (222); order preserved.
            assertEquals(listOf("111" to null, "222" to 0.01), stub.closes())
            assertTrue(stub.placedOrders().isEmpty())
        }
    }

    @Test
    fun nonOpposingMarketPlacesRaw() {
        val positions = "[" + ticket(111, "BUY", 0.01, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("0.01"), Double.NaN))) // BUY adds to a long, not opposing
            assertTrue(stub.closes().isEmpty(), "a same-side order closes nothing")
            val placed = stub.placedOrders()
            assertEquals(1, placed.size)
            assertEquals("market", placed[0].type)
            assertEquals("BUY", placed[0].side)
        }
    }

    @Test
    fun limitOpposerPlacesRawNotNetEmulated() {
        val positions = "[" + ticket(111, "BUY", 0.01, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.01"), 1.30))) // SELL LIMIT opposes but is not a market close
            assertTrue(stub.closes().isEmpty(), "a limit opposer is not a market close")
            val placed = stub.placedOrders()
            assertEquals(1, placed.size)
            assertEquals("limit", placed[0].type)
            assertEquals("SELL", placed[0].side)
            assertEquals(1.30, placed[0].price!!, 0.0000001)
        }
    }

    @Test
    fun subMinimumCloseIsSkipped() {
        // A close smaller than the symbol's minimum lot (0.01) quantizes to 0 — no invalid close is sent.
        val positions = "[" + ticket(111, "BUY", 0.03, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.005"), Double.NaN)))
            assertTrue(stub.closes().isEmpty(), "a sub-minimum close is floored to zero and skipped")
            assertTrue(stub.placedOrders().isEmpty())
        }
    }

    @Test
    fun nettingAccountOpposingCloseStillWorks() {
        // A single-ticket (netting-style) position: an opposing market order closes it by ticket, reaching the
        // same net-flat result a netting broker would produce.
        val positions = "[" + ticket(111, "BUY", 0.02, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions).use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.02"), Double.NaN)))
            assertEquals(listOf("111" to null), stub.closes())
            assertTrue(stub.placedOrders().isEmpty())
        }
    }

    @Test
    fun missingSpecsLeaveVolumeUnquantized() {
        // MT4 (and any account whose symbol-specs are empty) must still net-emulate, leaving volumes as given.
        val positions = "[" + ticket(111, "BUY", 0.03, 1.10, "2026-01-01T00:00:00Z") + "]"
        Stub(positions, specsJson = """{"specs":[]}""").use { stub ->
            val b = broker(stub)
            b.placeOrders(listOf(Order(eurusd(), Size("-0.017"), Double.NaN)))
            // No lot step to round to, so the exact 0.017 is closed against the ticket.
            assertEquals(listOf("111" to 0.017), stub.closes())
            assertTrue(stub.placedOrders().isEmpty())
        }
    }
}
