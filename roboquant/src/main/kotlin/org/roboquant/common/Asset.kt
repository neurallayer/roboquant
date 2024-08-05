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

import org.roboquant.common.Asset.Companion.SEP
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
interface Asset : Comparable<Asset> {

    val symbol: String
    val currency: Currency

    fun serialize() : String

    /**
     * Contains methods to create specific asset types, like options or futures using international standards to
     * generate the appropriate symbol name.
     */
    companion object {

        internal const val SEP = ";"

        private val cache = ConcurrentHashMap<String, Asset>()

        val registry = mutableMapOf<String, (String) -> Asset>()

        fun deserialize(value: String): Asset {
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


    override fun compareTo(other: Asset): Int {
        return this.symbol.compareTo(other.symbol)
    }



}


data class Crypto(override val symbol: String, override val currency: Currency) :Asset {

    override fun serialize(): String {
        return "Crypto$SEP$symbol$SEP$currency"
    }

    companion object {

        init {
            Asset.registry["Crypto"] = Crypto::deserialize
        }

        private fun deserialize(value: String): Asset {
            val (symbol, currencyCode) = value.split(SEP)
            return Crypto(symbol, Currency.getInstance(currencyCode))
        }

        fun fromSymbol(symbol: String): Crypto {
            return Crypto(symbol, Currency.USD) // TODO
        }
    }

}


data class Stock(override val symbol: String, override val currency: Currency = Currency.USD) :Asset {

    override fun serialize(): String {
        return "Stock$SEP$symbol$SEP$currency"
    }

    companion object {

        init {
            Asset.registry["Stock"] = Stock::deserialize
        }

        private fun deserialize(value: String): Asset {
            val (symbol, currencyCode) = value.split(SEP)
            return Stock(symbol, Currency.getInstance(currencyCode))
        }
    }
}



data class Forex(override val symbol: String, override val currency: Currency) :Asset {

    override fun serialize(): String {
        return "Forex$SEP$symbol$SEP$currency"
    }

    companion object {
        fun fromSymbol(symbol: String): Forex {
            return Forex(symbol, Currency.USD) // TODO
        }
    }
}


/**
 * Get an asset based on its [symbol] name. Will throw a NoSuchElementException if no asset is found. If there are
 * multiple assets with the same symbol, the first one will be returned.
 */
fun Collection<Asset>.getBySymbol(symbol: String): Asset = first { it.symbol == symbol }

/**
 * Find an asset based on its [symbols] name. Will return an empty list if no assets are matched.
 */
fun Collection<Asset>.findBySymbols(vararg symbols: String): List<Asset> = findBySymbols(symbols.asList())

/**
 * Find an asset based on its [symbols] name. Will return an empty list if no assets are matched.
 */
fun Collection<Asset>.findBySymbols(symbols: Collection<String>): List<Asset> = filter { it.symbol in symbols }

/**
 * Find all assets based on their [currencyCodes]. Returns an empty list if no matching assets can be found.
 */
fun Collection<Asset>.findByCurrencies(vararg currencyCodes: String): List<Asset> =
    findByCurrencies(currencyCodes.asList())

/**
 * Find all assets based on their [currencyCodes]. Returns an empty list if no matching assets can be found.
 */
fun Collection<Asset>.findByCurrencies(currencyCodes: Collection<String>): List<Asset> =
    filter { it.currency.currencyCode in currencyCodes }

/**
 * Get all unique symbols from the assets
 */
val Collection<Asset>.symbols: Array<String>
    get() = map { it.symbol }.distinct().toTypedArray()


fun main() {
    val apple = Stock("AAPL")
    val appleSer = apple.serialize()
    println(Asset.deserialize(appleSer))

    val abn = Stock("ABNA", Currency.EUR)
    println(abn.serialize())
}
