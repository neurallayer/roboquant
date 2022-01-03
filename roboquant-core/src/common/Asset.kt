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

/**
 * Asset is used to uniquely identify a financial instrument. So it can represent a stock, a future
 * or even a cryptocurrency. All of its properties are read-only, and assets are typically only created
 * once and reused thereafter.
 *
 * @property symbol symbol name
 * @property type type of asset class, default is [AssetType.STOCK]
 * @property currencyCode currency code, default is "USD"
 * @property exchangeCode Exchange this asset is traded on, default is an empty string
 * @property multiplier contract multiplier, default is 1.0
 * @property details contract details, for example this could hold the option series or futures contract details. Default
 * is an empty string.
 * @property name Company name, default is an empty string
 * @property id asset identifier, default is an empty string
 * @constructor Create a new asset
 */
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
        assert(symbol.isNotBlank()) { "Symbol in an asset cannot be empty or blank" }
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
     * Create a serialized string representation of this asset that can be later deserialized using the
     * [deserialize] method
     *
     * @return

    fun serialize(): String {
    return "$symbol$SEP$type$SEP$currencyCode$SEP$exchangeCode$SEP$multiplier$SEP$lastDate$SEP$right$SEP$strike$SEP$name$SEP$id"
    }
     */

    /**
     * Create a serialized string representation of this asset that can be later deserialized using the [deserialize]
     * method. It optimizes the string length.
     *
     * @return
     */
    fun serialize(): String {
        val sb = StringBuilder(symbol).append(SEP)
        if (type != AssetType.STOCK) sb.append(type.name); sb.append(SEP)
        if (currencyCode != "USD") sb.append(currencyCode); sb.append(SEP)
        if (exchangeCode != "") sb.append(exchangeCode); sb.append(SEP)
        if (multiplier != 1.0) sb.append(multiplier); sb.append(SEP)
        if (details.isNotEmpty()) sb.append(details); sb.append(SEP)
        if (name.isNotEmpty()) sb.append(name); sb.append(SEP)
        if (id.isNotEmpty()) sb.append(id); sb.append(SEP)

        var cnt = 0
        for (ch in sb.reversed()) if (ch == SEP) cnt++ else break
        return sb.substring(0, sb.length - cnt)
    }

    companion object {

        /**
         * Use the ASCII Unit Separator character. Should not interfere with most strings
         */
        private const val SEP = '\u001F'


        /**
         * Deserialize a string into an asset. The string needs to have been created using [serialize] method
         */
        fun deserialize(s: String): Asset {
            val e = s.split(SEP)
            val l = e.size
            return Asset(
                e[0],
                if (l > 1 && e[1].isNotEmpty()) AssetType.valueOf(e[1]) else AssetType.STOCK,
                if (l > 2 && e[2].isNotEmpty()) e[2] else "USD",
                if (l > 3) e[3] else "",
                if (l > 4 && e[4].isNotEmpty()) e[4].toDouble() else 1.0,
                if (l > 5) e[5] else "",
                if (l > 6) e[6] else "",
                if (l > 7) e[7] else "",
            )
        }

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
fun Collection<Asset>.findByCurrencies(vararg currencyCodes: String): List<Asset> = findByCurrencies(currencyCodes.asList())

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
fun Collection<Asset>.findByExchanges(vararg exchangeCodes: String): List<Asset> = findByExchanges(exchangeCodes.asList())

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

