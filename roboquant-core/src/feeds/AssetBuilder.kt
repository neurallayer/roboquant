package org.roboquant.feeds

import org.roboquant.common.Asset
import org.roboquant.common.AssetType

interface AssetBuilder {
    operator fun invoke(name: String, template: Asset = Asset("TEMPLATE")) : Asset = template.copy(symbol = name.uppercase())
}


object AssetBuilderFactory {

    val builders = mutableMapOf<AssetType, AssetBuilder>()

    fun build(name: String, template: Asset = Asset("TEMPLATE")) : Asset {
        val builder = builders[template.type]
        return if (builder == null) {
            throw Exception("No registered AssetBuilder for type ${template.type}")
        } else {
            builder(name, template)
        }
    }

    init {
        builders[AssetType.STOCK] = StockBuilder()
    }
}


/**
 * Stock builder that will create a stock. If no further options are provided, it will create
 * a stock on the US market denoted in US dollars.
 *
 * @constructor Create empty Stock builder
 */
class StockBuilder :  AssetBuilder

class ForexBuilder : AssetBuilder {

    override fun invoke(name: String, template: Asset): Asset {
        val symbol = name.substring(0, 3).uppercase()
        val currency = name.substring(3, 6).uppercase()
        return template.copy(symbol = symbol, currencyCode = currency)
    }

}


/**
 * Future builder builds future contracts. This implementation assumes that the passed name is unique and so no
 * expiration date is required to uniquely identify this future.
 *
 * @constructor Create empty Future builder
 */
class FutureBuilder : AssetBuilder


class BondBuilder : AssetBuilder


class CryptoBuilder : AssetBuilder {

    override fun invoke(name: String, template: Asset): Asset {
        val symbol: String
        val currencyCode: String
        val items = name.split('_', '-', ' ', '/')
        if (items.size == 2) {
            symbol = items[0]
            currencyCode = items[1]
        } else {
            // Assume last 3 characters is the currency code and the rest is the symbol name
            symbol = name.substring(0, name.lastIndex - 2)
            currencyCode = name.substring(name.lastIndex - 2)
        }
        return template.copy(
            symbol = symbol.uppercase()  + currencyCode.uppercase(),
            currencyCode = currencyCode.uppercase(),
        )
    }

}

