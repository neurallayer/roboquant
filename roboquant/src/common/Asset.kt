/*
 * Copyright 2020-2022 Neural Layer
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
@file:Suppress("LongParameterList")

package org.roboquant.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Asset is used to uniquely identify a financial instrument. So it can represent a stock, a future or a
 * cryptocurrency. All of its properties are read-only, and assets are ideally only created once and reused
 * thereafter. An asset instance is immutable.
 *
 * @property symbol none empty symbol name, for derivatives like options or futures contrat this includes the details
 * @property type type of asset class, default is [AssetType.STOCK]
 * @property currencyCode currency code, default is "USD"
 * @property exchangeCode Exchange this asset is traded on, default is an empty string
 * @property multiplier contract multiplier, default is 1.0
 * @property id asset identifier, default is an empty string
 * @constructor Create a new asset
 */
@Serializable
data class Asset(
    val symbol: String,
    val type: AssetType = AssetType.STOCK,
    val currencyCode: String = "USD",
    val exchangeCode: String = "",
    val multiplier: Double = 1.0,
    val id: String = ""
) : Comparable<Asset> {

    init {
        require(symbol.isNotBlank()) { "Symbol in an asset cannot be empty or blank" }
    }


    companion object {

        /**
         * Returns an option contract using the OCC (Options Clearing Corporation) option symbol standard.
         * The OCC option symbol string consists of four parts:
         *
         * 1. uppercase [symbol] of the underlying stock or ETF, padded with trailing spaces to 6 characters
         * 2. [expiration] date, in the format `yymmdd`
         * 3. Option [type], single character either P(ut) or C(all)
         * 4. strike price, as the [price] x 1000, front padded with 0 to make it 8 digits
         */
        fun optionContract(
            symbol: String,
            expiration: LocalDate,
            type: Char,
            price: String,
            multiplier: Double = 100.0,
            currencyCode: String = "USD",
            exchangeCode: String = "",
            id: String = ""
        ): Asset {
            val formatter = DateTimeFormatter.ofPattern("yyMMdd")
            val optionSymbol = "%-6s".format(symbol.uppercase()) +
                    expiration.format(formatter) +
                    type.uppercase() +
                    "%08d".format(BigDecimal(price).multiply(BigDecimal(1000)).toInt())

            return Asset(optionSymbol, AssetType.OPTION, currencyCode, exchangeCode, multiplier, id)
        }

        /**
         * Returns a future contract based on the provided paramaters
         */
        fun futureContract(
            symbol: String,
            month: Char,
            year: Int,
            currencyCode: String = "USD",
            exchangeCode: String = "",
            multiplier: Double = 1.0,
            id: String = ""
        ): Asset {
            val futureSymbol = "$symbol$month$year"
            return Asset(futureSymbol, AssetType.FUTURES, currencyCode, exchangeCode, multiplier, id)
        }

        /**
         * Return a Crypto asset based on the [base] currency, [quote] currency and [exchangeCode]
         */
        fun crypto(base: String, quote: String, exchangeCode: String) =
            Asset("$base/$quote", AssetType.CRYPTO, quote, exchangeCode)


        /**
         * Returns a forex currency pair asset based on the provided [symbol].
         */
        fun forexPair(symbol: String): Asset {
            val codes = symbol.split('_', '-', ' ', '/', ':')
            val (base, quote) = if (codes.size == 2) {
                val c1 = codes.first().uppercase()
                val c2 = codes.last().uppercase()
                Pair(c1, c2)
            } else if (codes.size == 1 && symbol.length == 6) {
                // We assume a 3-3 split
                val c1 = symbol.substring(0, 3).uppercase()
                val c2 = symbol.substring(3, 6).uppercase()
                Pair(c1, c2)
            } else {
                throw UnsupportedException("Cannot parse $symbol to currency pair")
            }
            return Asset("$base/$quote", AssetType.FOREX, quote)
        }

    }

    /**
     * Returns the currency pair (base and quote values) for a crypto or forex asset
     */
    val currencyPair: Pair<Currency, Currency>
        get() {
            val l = symbol.split('/').first()
            return Pair(Currency.getInstance(l), currency)
        }

    /**
     * Get the [Currency] of this asset based on the underlying currency code.
     */
    @Transient
    val currency = Currency.getInstance(currencyCode)

    /**
     * Get the [Exchange] of this asset based on the underlying exchange code
     */
    val exchange
        get() = Exchange.getInstance(exchangeCode)

    /**
     * What is the value of the asset given the provided [size] and [price]
     */
    fun value(size: Size, price: Double): Amount {
        // If size is zero, an unknown price (Double.NanN) is fine
        return if (size.iszero) Amount(currency, 0.0) else Amount(currency, size * multiplier * price)
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: Asset): Int {
        return symbol.compareTo(other.symbol)
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
    filter { it.currencyCode in currencyCodes }

/**
 * Find all assets based on their [exchangeCodes]. Returns an empty list if no matching assets can be found.
 */
fun Collection<Asset>.findByExchanges(exchangeCodes: Collection<String>): List<Asset> =
    filter { it.exchangeCode in exchangeCodes }

/**
 * Find all assets based on their [exchangeCodes]. Returns an empty list if no matching assets can be found.
 */
fun Collection<Asset>.findByExchanges(vararg exchangeCodes: String): List<Asset> =
    findByExchanges(exchangeCodes.asList())

/**
 * Select [n] random assets from a collection, without duplicates. [n] has to equal or smaller than the size of the
 * collection
 */
fun Collection<Asset>.random(n: Int): List<Asset> = shuffled().take(n)

/**
 * Provide a [Summary] for a collection of assets
 */
fun Collection<Asset>.summary(): Summary {
    val result = Summary("assets")
    for (asset in this) {
        result.add("$asset")
    }
    return result
}

/**
 * Asset filter allows to determine which assets will be considered in a certain operation.
 *
 */
fun interface AssetFilter {

    /**
     * Returns true if the provided [asset] should be processed, false otherwise.
     */
    fun filter(asset: Asset): Boolean

    companion object {

        /**
         * Include all assets
         */
        fun all(): AssetFilter {
            return AssetFilter { true }
        }

        /**
         * Include the assets that are denoted in the provided [currencies].
         */
        fun includeCurrencies(vararg currencies: Currency): AssetFilter {
            return AssetFilter { asset: Asset -> asset.currency in currencies }
        }

        /**
         * Exclude the assets that match the provided [symbols]. Matching of symbol names is done case-insensitive.
         */
        fun excludeSymbols(vararg symbols: String): AssetFilter {
            val set = symbols.map { it.uppercase() }.toSet()
            return AssetFilter { asset: Asset -> asset.symbol.uppercase() !in set }
        }

        /**
         * Include the assets that match the provided [symbols]. Matching of symbol names is done case-insensitive.
         */
        fun includeSymbols(vararg symbols: String): AssetFilter {
            val set = symbols.map { it.uppercase() }.toSet()
            return AssetFilter { asset: Asset -> asset.symbol.uppercase() in set }
        }

    }

}
