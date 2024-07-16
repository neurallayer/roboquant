/*
 * Copyright 2020-2024 Neural Layer
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
@file:Suppress("LongParameterList")

package org.roboquant.common

import org.roboquant.common.Asset2.Companion.SEP
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/**
 * Asset is used to uniquely identify a financial instrument. So it can represent a stock, a future or a
 * cryptocurrency. See the [AssetType] for the supported types.
 *
 * For asset types that require additional information (like options or futures), the symbol name is expected to
 * contain this information.
 *
 * All of its properties are read-only, and assets are ideally only created once and reused thereafter. An asset
 * instance is immutable.
 *
 * @property symbol none empty symbol name, for derivatives like options this includes the details that identify the
 * contract
 * @property currency currency,
 * @constructor Create a new asset
 */
interface Asset2 {

    val symbol: String
    val currency: Currency

    fun serialize() : String

    /**
     * Contains methods to create specific asset types, like options or futures using international standards to
     * generate the appropriate symbol name.
     */
    companion object {

        internal const val SEP = ";"

        private val cache = ConcurrentHashMap<String, Asset2>()

        val registry = mutableMapOf<String, (String) -> Asset2>()

        fun deserialize(value: String): Asset2 {
            return cache.getOrPut(value) {
                val (assetType, serString) = value.split(SEP, limit = 2)
                registry.getValue(assetType)(serString)
            }
        }


    }

    /**
     * Return the value of the asset given the provided [size] and [price].
     */
    open fun value(size: Size, price: Double): Amount {
        // If size is zero, an unknown price (Double.NanN) is fine
        return if (size.iszero) Amount(currency, 0.0) else Amount(currency, size.toDouble() * price)
    }



}


data class Crypto(override val symbol: String, override val currency: Currency) :Asset2 {

    override fun serialize(): String {
        return "Crypto$SEP$symbol$SEP$currency"
    }

    companion object {

        init {
            Asset2.registry["Crypto"] = Crypto::deserialize
        }

        private fun deserialize(value: String): Asset2 {
            val (symbol, currencyCode) = value.split(SEP)
            return Crypto(symbol, Currency.getInstance(currencyCode))
        }
    }

}



data class Stock2(override val symbol: String, override val currency: Currency) :Asset2 {

    override fun serialize(): String {
        return "Stock2$SEP$symbol$SEP$currency"
    }
}

data class USStock(override val symbol: String) :Asset2 {

    override val currency: Currency
        get() = Currency.USD

    override fun serialize() : String {
        return "USStock$SEP$symbol"
    }

    companion object {

        init {
            Asset2.registry["USStock"] = USStock::deserialize
        }

        private fun deserialize(value: String): Asset2 {
            return USStock(value)
        }
    }

}

fun main() {
    val apple = USStock("AAPL")
    val appleSer = apple.serialize()
    println(Asset2.deserialize(appleSer))

    val abn = Stock2("ABNA", Currency.EUR)
    println(abn.serialize())
}
