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

package org.roboquant.feeds

import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.ConfigurationException

interface AssetBuilder {
    operator fun invoke(name: String, template: Asset = Asset("TEMPLATE")): Asset =
        template.copy(symbol = name.uppercase())
}


object AssetBuilderFactory {

    private val builders = mutableMapOf<AssetType, AssetBuilder>()

    fun build(name: String, template: Asset = Asset("TEMPLATE")): Asset {
        val builder = builders[template.type]
        return if (builder == null) {
            throw ConfigurationException("No registered AssetBuilder for type ${template.type}")
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
class StockBuilder : AssetBuilder

class ForexBuilder : AssetBuilder {

    override fun invoke(name: String, template: Asset): Asset {
        val items = name.split('_', '-', ' ', '/')
        val currencyCode = if (items.size == 2) items[1] else name.substring(name.lastIndex - 2)

        return template.copy(
            symbol = name.uppercase(),
            currencyCode = currencyCode.uppercase(),
        )
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
            symbol = symbol.uppercase() + currencyCode.uppercase(),
            currencyCode = currencyCode.uppercase(),
        )
    }

}

