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
@file:Suppress("LongParameterList")

package org.roboquant.common

import org.roboquant.common.Asset.Companion.SEP
import java.util.concurrent.ConcurrentHashMap

/**
 * Asset is used to uniquely identify a financial instrument. So it can represent a stock, a future or a
 * cryptocurrency.
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
interface Asset : Comparable<Asset> {

    val symbol: String
    val currency: Currency

    fun serialize(): String

    /**
     * Contains methods to create specific asset types, like options or futures using international standards to
     * generate the appropriate symbol name.
     */
    companion object {
        val registry: MutableMap<String, (String) -> Asset> = mutableMapOf<String, (String) -> Asset>()

        init {
            registry["Stock"] = ::deserializeStock
            registry["Option"] = ::deserializeOption
            registry["Forex"] = ::deserializeForex
            registry["Crypto"] = ::deserializeCrypto
        }

        internal const val SEP = ";"

        private val cache = ConcurrentHashMap<String, Asset>()

        fun deserialize(value: String): Asset {
            return cache.getOrPut(value) {
                val (assetType, serString) = value.split(SEP, limit = 2)
                val deserializerFunction = registry.getValue(assetType)
                deserializerFunction(serString)
            }
        }

    }

    /**
     * Return the value of the asset given the provided [size] and [price].
     */
    fun value(size: Size, price: Double): Amount {
        // If size is zero, an unknown price (Double.NanN) is fine
        return if (size.iszero) Amount(currency, 0.0) else Amount(currency, size.toDouble() * price)
    }


    override fun compareTo(other: Asset): Int {
        return this.symbol.compareTo(other.symbol)
    }


}

fun deserializeOption(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Option(symbol, Currency.getInstance(currencyCode))
}

data class Option(override val symbol: String, override val currency: Currency) : Asset {

    override fun serialize(): String {
        return "Option$SEP$symbol$SEP$currency"
    }
}

fun deserializeCrypto(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Crypto(symbol, Currency.getInstance(currencyCode))
}

data class Crypto(override val symbol: String, override val currency: Currency) : Asset {

    override fun serialize(): String {
        return "Crypto$SEP$symbol$SEP$currency"
    }

    companion object {

        fun fromSymbol(symbol: String): Crypto {
            val (_, currency) = symbol.toCurrencyPair()
            return Crypto(symbol, currency)
        }
    }

}

fun deserializeStock(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Stock(symbol, Currency.getInstance(currencyCode))
}

data class Stock(override val symbol: String, override val currency: Currency = Currency.USD) : Asset {

    override fun serialize(): String {
        return "Stock$SEP$symbol$SEP$currency"
    }

}

fun deserializeForex(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Forex(symbol, Currency.getInstance(currencyCode))
}




data class Forex(override val symbol: String, override val currency: Currency) : Asset {

    override fun serialize(): String {
        return "Forex$SEP$symbol$SEP$currency"
    }

    companion object {

        fun fromSymbol(symbol: String): Forex {
            val (_, currency) = symbol.toCurrencyPair()
            return Forex(symbol, currency)
        }
    }

}


/**
 * Get an asset based on its [symbol] name. Will throw a NoSuchElementException if no asset is found. If there are
 * multiple assets with the same symbol, the first one will be returned.
 */
fun Collection<Asset>.getBySymbol(symbol: String): Asset = first { it.symbol == symbol }

/**
 * Get all unique symbols from the assets
 */
val Collection<Asset>.symbols: Array<String>
    get() = map { it.symbol }.distinct().toTypedArray()

