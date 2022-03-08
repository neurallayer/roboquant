/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.common

import kotlinx.serialization.*

/**
 * Asset is used to uniquely identify a financial instrument. So it can represent a stock, a future
 * or even a cryptocurrency. All of its properties are read-only, and assets are typically only created
 * once and reused thereafter. An asset is immutable.
 *
 * @property symbol symbol name
 * @property type type of asset class, default is [AssetType.STOCK]
 * @property currencyCode currency code, default is "USD"
 * @property exchangeCode Exchange this asset is traded on, default is an empty string
 * @property multiplier contract multiplier, default is 1.0
 * @property details contract details, for example this could hold the option series or futures contract details.
 * Default is an empty string.
 * @property name Company name, default is an empty string
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
    val details: String = "",
    val name: String = "",
    val id: String = ""
) : Comparable<Asset> {

    init {
        require(symbol.isNotBlank()) { "Symbol in an asset cannot be empty or blank" }
    }

    /**
     * Get the [Currency] of this asset based on the underlying currency code
     */
    val currency
        get() = Currency.getInstance(currencyCode)

    /**
     * Get the [Exchange] of this asset based on the underlying exchange code
     */
    val exchange
        get() = Exchange.getInstance(exchangeCode)


    override fun toString(): String {
        return "$type $symbol $details"
    }

    /**
     * What is the value of the asset given the provided [quantity] and [price]
     */
    fun value(quantity: Double, price: Double): Amount {
        // If quantity is zero even an unknown price (Double.NanN) is fine
        return if (quantity == 0.0) Amount(currency, 0.0) else Amount(currency, multiplier * price * quantity)
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
    val result = Summary("Assets")
    for (asset in this) {
        result.add("$asset")
    }
    return result
}

