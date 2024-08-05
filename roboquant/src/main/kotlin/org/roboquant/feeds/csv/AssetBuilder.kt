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

package org.roboquant.feeds.csv

import org.roboquant.common.Asset
import org.roboquant.common.Currency
import org.roboquant.common.Stock
import java.io.File


/**
 *
 */
fun interface AssetBuilder {

    /**
     * Based on a [name], return an instance of [Asset]
     */
    fun build(name: String): Asset
}

/**
 * The default asset builder uses a file name without its extension as the symbol name.
 */
class StockBuilder(private val currency: Currency = Currency.USD) : AssetBuilder {

    private val notCapital = Regex("[^A-Z]")

    /**
     * @see AssetBuilder.build
     */
    override fun build(name: String): Asset {
        var symbol = name.removeSuffix(".csv").removeSuffix(".txt")
        symbol = symbol.uppercase().replace(notCapital, ".")
        return Stock(symbol, currency)
    }

}
