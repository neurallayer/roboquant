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

import org.roboquant.common.Config
import org.roboquant.common.Currency
import org.roboquant.common.Order
import org.roboquant.common.Size
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration test that runs when a live `TICKERALL_API_KEY` is provided together with EITHER a MetaTrader
 * login (`TICKERALL_BROKER`/`TICKERALL_SERVER`/`TICKERALL_ACCOUNT`/`TICKERALL_PASSWORD`) or a pre-connected
 * `TICKERALL_ACCOUNT_ID`. Given only the login, the account id is derived by opening a session — so you
 * never look up the internal id. Without either the test returns early so the CI build stays green. The
 * account must be a connected demo account.
 */
internal class TickerAllBrokerTestIT {

    @Test
    fun syncAndPlaceEmpty() {
        val key = Config.getProperty("TICKERALL_API_KEY") ?: return
        val id = resolveTickerAllAccountId(key) ?: return

        val broker = TickerAllBroker {
            apiKey = key
            accountId = id
        }

        val account = broker.sync()
        // MetaTrader accounts have a single deposit currency; a 0.0 balance is a valid state.
        assertFalse(account.cash.isMultiCurrency())
        assertTrue(account.buyingPower.value >= 0.0)

        // placing an empty order list is a no-op and must not throw
        broker.placeOrders(emptyList())

        // a subsequent sync must also succeed and return a consistent account
        val account2 = broker.sync()
        assertFalse(account2.cash.isMultiCurrency())
    }

    /**
     * Connect from MetaTrader credentials, exercising [TickerAllBroker.connect] end-to-end against the live
     * API. Runs only when `TICKERALL_API_KEY`, `TICKERALL_BROKER`, `TICKERALL_SERVER`, `TICKERALL_ACCOUNT`
     * and `TICKERALL_PASSWORD` are all provided (otherwise it returns early so CI stays green). Optionally
     * honours `TICKERALL_TERMINAL_TYPE`. It only connects and reads; it never trades. The account must be a
     * demo account.
     */
    @Test
    fun connectFromCredentials() {
        val key = Config.getProperty("TICKERALL_API_KEY") ?: return
        val brokerPlatform = Config.getProperty("TICKERALL_BROKER") ?: return
        val serverName = Config.getProperty("TICKERALL_SERVER") ?: return
        val accountLogin = Config.getProperty("TICKERALL_ACCOUNT") ?: return
        val pw = Config.getProperty("TICKERALL_PASSWORD") ?: return
        val terminal = Config.getProperty("TICKERALL_TERMINAL_TYPE")

        val broker = TickerAllBroker.connect {
            apiKey = key
            broker = brokerPlatform
            server = serverName
            account = accountLogin
            password = pw
            if (terminal != null) terminalType = terminal
        }

        // connect must have started the session and bound the resulting accountId.
        assertTrue(broker.accountId.isNotBlank(), "connect must bind a non-blank accountId")

        val account = broker.sync()
        // a single deposit currency; a 0.0 balance is a valid (unfunded) state.
        assertFalse(account.cash.isMultiCurrency())
        assertTrue(account.buyingPower.value >= 0.0)
    }

    /**
     * Opt-in live trade round-trip, enabled by setting `TICKERALL_TEST_TRADE`. It places a minimal 0.01
     * market order through the broker and then closes whatever position that opened, so nothing is left
     * behind. It refuses to run against anything other than a demo account.
     */
    @Test
    fun tradeRoundTrip() {
        val key = Config.getProperty("TICKERALL_API_KEY") ?: return
        val id = resolveTickerAllAccountId(key) ?: return
        Config.getProperty("TICKERALL_TEST_TRADE") ?: return
        val symbol = Config.getProperty("TICKERALL_SYMBOL") ?: "EURUSDm"

        val broker = TickerAllBroker { apiKey = key; accountId = id }
        val client = TickerAll.getClient(TickerAllConfig().apply { apiKey = key; accountId = id })

        // never place an order on anything other than a demo account
        require(client.getAccount().isDemo == true) { "refusing to place a test order on a non-demo account" }

        val asset = TickerAll.toAsset(symbol, Currency.USD)
        val ticketsBefore = client.getAccount().positions.orEmpty().mapNotNull { it.ticket }.toSet()

        val order = Order(asset, Size("0.01"), Double.NaN)
        broker.placeOrders(listOf(order))
        assertTrue(order.id.isNotEmpty(), "broker must assign a ticket after placing the order")
        Thread.sleep(4000)

        val opened = client.getAccount().positions.orEmpty().filter { it.ticket != null && it.ticket !in ticketsBefore }
        println("[tradeRoundTrip] placed order id=${order.id}; opened=${opened.map { it.ticket to it.symbol }}")

        // always clean up: close whatever we just opened
        for (pos in opened) client.closePosition(pos.ticket!!.toString())
        Thread.sleep(4000)

        val ticketsAfter = client.getAccount().positions.orEmpty().mapNotNull { it.ticket }.toSet()
        val stillOpen = opened.mapNotNull { it.ticket }.filter { it in ticketsAfter }
        println("[tradeRoundTrip] after close, stillOpen=$stillOpen")
        assertTrue(stillOpen.isEmpty(), "the opened position(s) must be closed by closePosition")

        // --- pending-order surface: place a resting limit far from market, then cancel it ---
        val lastPrice = client.getCandles(symbol, 6, "M1").lastOrNull()?.close
            ?: client.getCandles(symbol, 24, "H1").lastOrNull()?.close
        if (lastPrice != null) {
            // a BUY limit 10% below market rests (it will never fill during the test)
            val pending = Order(asset, Size("0.01"), lastPrice * 0.90)
            broker.placeOrders(listOf(pending))
            assertTrue(pending.id.isNotEmpty(), "broker must assign a ticket to the pending order")
            Thread.sleep(3000)
            val resting = client.getPendingOrders().mapNotNull { it.ticket?.toString() }
            println("[tradeRoundTrip] placed pending id=${pending.id}; resting=$resting")
            assertTrue(pending.id in resting, "the pending order should be resting")

            // modify the resting pending to a new price. A roboquant modify is an order that carries the
            // known id together with a non-zero size and the new limit; the broker dispatches this to the
            // TickerAll modify-pending endpoint (a zero-size order would instead be a cancellation).
            val newPrice = lastPrice * 0.85
            val modify = Order(asset, Size("0.01"), newPrice)
            modify.id = pending.id
            broker.placeOrders(listOf(modify))
            Thread.sleep(3000)
            val modified = client.getPendingOrders().firstOrNull { it.ticket?.toString() == pending.id }
            val shownPrice = modified?.limitPrice ?: modified?.price
            println("[tradeRoundTrip] after modify, price=$shownPrice (target ~$newPrice)")
            assertTrue(modified != null, "the pending order should still be resting after the modify")
            assertTrue(
                shownPrice != null && kotlin.math.abs(shownPrice - newPrice) < newPrice * 0.005,
                "the pending order price should reflect the modify (got $shownPrice, expected ~$newPrice)"
            )

            // cancel it. A roboquant cancellation is a zero-size order carrying the order id; note that
            // Order.cancel() builds it via copy(), which drops the body-var id, so re-attach it here.
            val cancel = pending.copy(size = Size.ZERO)
            cancel.id = pending.id
            broker.placeOrders(listOf(cancel))
            Thread.sleep(3000)
            val afterCancel = client.getPendingOrders().mapNotNull { it.ticket?.toString() }
            println("[tradeRoundTrip] after cancel, resting=$afterCancel")
            assertTrue(pending.id !in afterCancel, "the pending order should be cancelled")
        }
    }
}
