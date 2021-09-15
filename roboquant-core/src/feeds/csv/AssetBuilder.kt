package org.roboquant.feeds.csv

import org.roboquant.common.Asset
import org.roboquant.common.AssetType


/**
 * Asset builder creates a new [Asset] based on a provided name.
 */
fun interface AssetBuilder {
    operator fun invoke(name: String): Asset

    companion object {
        private val instances = mutableMapOf<AssetType, AssetBuilder>()

        fun getInstance(assetType: AssetType) = instances[assetType]!!

        init {
            instances[AssetType.STOCK] = StockBuilder()
        }
    }
}

/**
 * Stock builder that will create a contract for a stock. If no further options are provided, it will create
 * a stock on the US market denoted in US dollars.
 *
 * @property currencyCode
 * @property exchange
 * @constructor Create empty Stock builder
 */
class StockBuilder(private val currencyCode: String = "USD", private val exchange: String = "") :
    AssetBuilder {

    override fun invoke(name: String): Asset {
        return Asset(
            symbol = name.uppercase(),
            currencyCode = currencyCode,
            exchangeCode = exchange,
            type = AssetType.STOCK
        )
    }
}

class ForexBuilder(private val exchange: String = "") : AssetBuilder {

    override fun invoke(name: String): Asset {
        val symbol = name.substring(0, 3).uppercase()
        val currency = name.substring(3, 6).uppercase()
        return Asset(symbol = symbol, currencyCode = currency, exchangeCode = exchange, type = AssetType.FOREX)
    }

}


/**
 * Future builder builds future contracts. This implementation assumes that the passed name is unique and so no
 * expiration date is required to uniquely identify this future.
 *
 * @property currencyCode
 * @property exchange
 * @constructor Create empty Future builder
 */
class FutureBuilder(private val currencyCode: String = "USD", private val exchange: String = "GLOBEX") :
    AssetBuilder {

    override fun invoke(name: String): Asset {
        return Asset(
            symbol = name,
            currencyCode = currencyCode,
            exchangeCode = exchange,
            type = AssetType.FUTURES
        )
    }

}


class BondBuilder(private val currencyCode: String = "USD", private val exchange: String = "") : AssetBuilder {

    override fun invoke(name: String): Asset {
        return Asset(symbol = name, currencyCode = currencyCode, exchangeCode = exchange, type = AssetType.BOND)
    }

}


class CryptoBuilder(private val exchange: String = "") : AssetBuilder {

    override fun invoke(name: String): Asset {
        val symbol: String
        val currencyCode: String
        if (name.contains('_')) {
            val items = name.split('_')
            symbol = items[0]
            currencyCode = items[1]
        } else {
            // Assume last 3 characters is the currency code and the rest is the symbol name
            currencyCode = name.substring(name.lastIndex - 2)
            symbol = name.substring(0, name.lastIndex - 2)
        }
        return Asset(
            symbol = symbol.uppercase(),
            currencyCode = currencyCode.uppercase(),
            exchangeCode = exchange,
            type = AssetType.CRYPTO
        )
    }

}

