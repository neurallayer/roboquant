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
import com.google.gson.JsonObject
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit test for the credentials -> session -> broker path ([TickerAllBroker.connect] and the
 * [TickerAll.startSession] it delegates to). It stands up a tiny in-process stub of the TickerAll REST API
 * with the JDK's own [HttpServer] (no external service, no credentials, no new dependency), so it always runs
 * in CI, and asserts that connect posts `/v1/sessions` with the right body and binds the parsed accountId.
 */
internal class TickerAllConnectTest {

    private val gson = Gson()

    /** A captured inbound HTTP request, so the test can assert exactly what the client sent. */
    private data class Captured(val method: String, val path: String, val auth: String?, val body: String)

    /**
     * A stub TickerAll API. `/v1/sessions` returns [sessionAccountId]; the account + pending endpoints return
     * an empty, valid, unfunded (balance 0.0) account so the broker's initial sync succeeds. Every request is
     * recorded in [captured].
     */
    private inner class Stub(private val sessionAccountId: String = "acc-connected-1") : AutoCloseable {
        val captured = ConcurrentLinkedQueue<Captured>()
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
            captured.add(Captured(ex.requestMethod, path, ex.requestHeaders.getFirst("Authorization"), body))
            val response: String = when {
                ex.requestMethod == "POST" && path == "/v1/sessions" ->
                    """{"accountId":"$sessionAccountId","isDemo":true,"status":"connected","expiresAt":"2026-08-11T00:00:00Z"}"""
                path.endsWith("/orders/pending") -> """{"orders":[]}"""
                path.startsWith("/v1/accounts/") ->
                    """{"account":{"balance":0.0,"currency":"USD","equity":0.0,"freeMargin":0.0},"positions":[],"status":"connected","isDemo":true}"""
                else -> "{}"
            }
            val bytes = response.toByteArray()
            ex.responseHeaders.add("Content-Type", "application/json")
            ex.sendResponseHeaders(200, bytes.size.toLong())
            ex.responseBody.use { it.write(bytes) }
        }

        fun sessionRequests() = captured.filter { it.path == "/v1/sessions" }

        override fun close() = server.stop(0)
    }

    @Test
    fun connectStartsSessionAndBindsAccountId() {
        Stub("acc-xyz").use { stub ->
            val broker = TickerAllBroker.connect {
                apiKey = "cf_api_test"
                baseUrl = stub.baseUrl
                broker = "mt5"
                server = "Exness-MT5Trial7"
                account = "12345678"
                password = "secret"
            }

            // The parsed accountId from /v1/sessions must be bound to the broker.
            assertEquals("acc-xyz", broker.accountId)

            // Exactly one session-start must have been posted (the feed reuses the id, no second start).
            val sessions = stub.sessionRequests()
            assertEquals(1, sessions.size, "connect must post /v1/sessions exactly once")
            val req = sessions.single()
            assertEquals("POST", req.method)
            assertEquals("Bearer cf_api_test", req.auth)

            // The body must carry the MetaTrader credentials, with terminalType omitted (blank -> default MOBILE).
            val json = gson.fromJson(req.body, JsonObject::class.java)
            assertEquals("mt5", json.get("broker").asString)
            assertEquals("Exness-MT5Trial7", json.get("server").asString)
            assertEquals("12345678", json.get("account").asString)
            assertEquals("secret", json.get("password").asString)
            assertNull(json.get("terminalType"), "a blank terminalType must be omitted from the body")

            // The returned broker must be usable: a sync against the (balance 0.0, unfunded) account succeeds.
            val account = broker.sync()
            assertFalse(account.cash.isMultiCurrency())
            assertTrue(account.buyingPower.value >= 0.0)
        }
    }

    @Test
    fun connectSendsChosenTerminalType() {
        Stub().use { stub ->
            val broker = TickerAllBroker.connect {
                apiKey = "cf_api_test"
                baseUrl = stub.baseUrl
                broker = "mt5"
                server = "Exness-MT5Trial7"
                account = "12345678"
                password = "secret"
                terminalType = "CLIENT"
            }
            assertEquals("acc-connected-1", broker.accountId)

            val json = gson.fromJson(stub.sessionRequests().single().body, JsonObject::class.java)
            assertEquals("CLIENT", json.get("terminalType").asString, "an explicit terminalType must be sent")
        }
    }

    @Test
    fun startSessionReturnsParsedAccountId() {
        Stub("acc-42").use { stub ->
            val id = TickerAll.startSession(TickerAllConfig().apply {
                apiKey = "cf_api_test"
                baseUrl = stub.baseUrl
                broker = "mt4"
                server = "SomeBroker-Demo"
                account = "999"
                password = "pw"
            })
            assertEquals("acc-42", id)
            val req = stub.sessionRequests().single()
            assertEquals("POST", req.method)
            assertEquals("/v1/sessions", req.path)
        }
    }

    @Test
    fun connectRequiresCredentials() {
        // A missing required credential fails fast with a clear error, before any network call is attempted.
        assertFailsWith<IllegalArgumentException> {
            TickerAllBroker.connect {
                apiKey = "cf_api_test"
                broker = "mt5"
                server = "Exness-MT5Trial7"
                account = "12345678"
                // no password
            }
        }
        assertFailsWith<IllegalArgumentException> {
            TickerAll.startSession(TickerAllConfig().apply { apiKey = ""; broker = "mt5"; server = "s"; account = "1"; password = "p" })
        }
    }

    @Test
    fun rejectsBrokerAccountNumberAsAccountId() {
        // A broker account NUMBER (all digits) is a common mix-up for the TickerAll account id (a cuid);
        // it is rejected up-front, before any network call, on the broker and both feeds.
        assertFailsWith<IllegalArgumentException> {
            TickerAllBroker { apiKey = "cf_api_test"; accountId = "12345678" }
        }
        assertFailsWith<IllegalArgumentException> {
            TickerAllLiveFeed { apiKey = "cf_api_test"; accountId = "12345678" }
        }
        assertFailsWith<IllegalArgumentException> {
            TickerAllHistoricFeed { apiKey = "cf_api_test"; accountId = "12345678" }
        }
    }
}
