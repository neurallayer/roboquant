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

    /**
     * Serialize the asset to a string
     */
    fun serialize(): String

    /**
     * Contains methods to create specific asset types, like options or futures using international standards to
     * generate the appropriate symbol name.
     */
    companion object {
        internal val registry: MutableMap<String, (String) -> Asset> = mutableMapOf()

        init {
            registry["Stock"] = ::deserializeStock
            registry["Option"] = ::deserializeOption
            registry["Forex"] = ::deserializeForex
            registry["Crypto"] = ::deserializeCrypto
        }

        internal const val SEP = ";"

        private val deserializedCache = ConcurrentHashMap<String, Asset>()

        /**
         * Deserialize an asset from its string representation
         */
        fun deserialize(value: String): Asset {
            return deserializedCache.getOrPut(value) {
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

    /**
     * Compare this asset to another based on their symbol names.
     */
    override fun compareTo(other: Asset): Int = this.symbol.compareTo(other.symbol)


}

private fun deserializeOption(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Option(symbol, Currency.getInstance(currencyCode))
}

/**
 * Option asset representing an option contract
 *
 * @property symbol the symbol of the option contract
 * @property currency the currency used for pricing, e.g. USD
 * @constructor Create a new Option asset
 */
data class Option(override val symbol: String, override val currency: Currency) : Asset {

    override fun serialize(): String = "Option$SEP$symbol$SEP$currency"
}

private fun deserializeCrypto(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Crypto(symbol, Currency.getInstance(currencyCode))
}

/**
 * Crypto asset representing a cryptocurrency
 *
 * @property symbol the symbol of the cryptocurrency, e.g. BTC
 * @property currency the currency used for pricing, e.g. USD
 * @constructor Create a new Crypto asset
 */
data class Crypto(override val symbol: String, override val currency: Currency) : Asset {

    override fun serialize(): String = "Crypto$SEP$symbol$SEP$currency"

    /**
     * @suppress
     */
    companion object {

        /**
         * Create a Crypto asset from a currency pair symbol
         */
        fun fromSymbol(symbol: String): Crypto {
            val (_, currency) = symbol.toCurrencyPair()
            return Crypto(symbol, currency)
        }
    }

}

private fun deserializeStock(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Stock(symbol, Currency.getInstance(currencyCode))
}

/**
 * Stock asset representing a stock
 *
 * @property symbol the symbol of the stock, e.g. AAPL
 * @property currency the currency used for pricing, default is USD
 * @constructor Create a new Stock asset
 */
data class Stock(override val symbol: String, override val currency: Currency = Currency.USD) : Asset {

    override fun serialize(): String = "Stock$SEP$symbol$SEP$currency"

}

private fun deserializeForex(value: String): Asset {
    val (symbol, currencyCode) = value.split(SEP)
    return Forex(symbol, Currency.getInstance(currencyCode))
}



/**
 * Forex asset representing a currency pair
 *
 * @property symbol the currency pair symbol, e.g. EURUSD
 * @property currency the quote currency of the pair
 * @constructor Create a new Forex asset
 */
data class Forex(override val symbol: String, override val currency: Currency) : Asset {

    /**
     * Serialize the Forex asset to a string
     */
    override fun serialize(): String = "Forex$SEP$symbol$SEP$currency"

    /**
     * @suppress
     */
    companion object {

        /**
         * Create a Forex asset from a currency pair symbol
         */
        fun fromSymbol(symbol: String): Forex {
            val (_, currency) = symbol.toCurrencyPair()
            return Forex(symbol, currency)
        }
    }

}


/**
 * Find the first asset based on its [symbol] name. Will throw a NoSuchElementException if no asset is found. If there are
 * multiple assets with the same symbol, the first one will be returned.
 */
fun Collection<Asset>.getBySymbol(symbol: String): Asset = first { it.symbol == symbol }

/**
 * Get the unique symbols from the collection of assets as a String array.
 */
val Collection<Asset>.symbols: Array<String>
    get() = map { it.symbol }.distinct().toTypedArray()

