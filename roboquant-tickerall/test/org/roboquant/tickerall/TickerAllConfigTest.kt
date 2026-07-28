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

import org.roboquant.common.Currency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class TickerAllConfigTest {

    @Test
    fun defaults() {
        val config = TickerAllConfig()
        assertTrue(config.baseUrl.startsWith("http"))
        assertTrue(config.wsUrl.startsWith("ws"))
    }

    @Test
    fun explicitConfig() {
        val config = TickerAllConfig(apiKey = "key", accountId = "acc", baseUrl = "https://example.com", wsUrl = "wss://example.com/s")
        assertEquals("https://example.com", config.baseUrl)
        assertEquals("acc", config.accountId)
    }

    @Test
    fun clientRequiresApiKey() {
        assertFailsWith<IllegalArgumentException> {
            TickerAll.getClient(TickerAllConfig(apiKey = "", accountId = "acc"))
        }
    }

    @Test
    fun clientRequiresAccountId() {
        assertFailsWith<IllegalArgumentException> {
            TickerAll.getClient(TickerAllConfig(apiKey = "key", accountId = ""))
        }
    }

    @Test
    fun assetMappingKeepsRawSymbol() {
        val eur = TickerAll.toAsset("EURUSD", Currency.USD)
        assertEquals("EURUSD", eur.symbol)
        assertEquals("USD", eur.currency.currencyCode)

        // suffixed broker symbols are preserved verbatim, quote currency still derived
        val metal = TickerAll.toAsset("XAUUSDm", Currency.USD)
        assertEquals("XAUUSDm", metal.symbol)
        assertEquals("USD", metal.currency.currencyCode)

        // an unparseable symbol falls back to the provided currency
        val index = TickerAll.toAsset("US30", Currency.EUR)
        assertEquals("US30", index.symbol)
        assertEquals("EUR", index.currency.currencyCode)

        // stable identity: the same symbol maps to the same cached asset
        assertSame(eur, TickerAll.toAsset("EURUSD", Currency.USD))
    }
}
