/*
 * Copyright 2020-2023 Neural Layer
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

import java.time.Instant

/**
 * Asset filter limits the price actions that will be processed in certain operations at a given time. Filters can work
 * on a combination of asset and time.
 *
 * Common use case are strategies that are only interested in a subset of assets that are available in a feed.
 */
fun interface AssetFilter {

    /**
     * Returns true if the provided [asset] should be processed at the provided [time], false otherwise. The time can
     * be used by implementations to support asset collections that change over time, like for example the S&P 500
     * index.
     */
    fun filter(asset: Asset, time: Instant): Boolean

    /**
     * Standard set of predefined filters
     */
    companion object {

        /**
         * Include all assets, so this filter always returns true
         */
        fun all(): AssetFilter {
            return AssetFilter { _: Asset, _: Instant -> true }
        }

        /**
         * Include only the assets that are denoted in the provided [currencies]. For example only include assets that
         * are denoted in USD and ignore other currencies.
         */
        fun includeCurrencies(vararg currencies: Currency): AssetFilter {
            return AssetFilter { asset: Asset, _: Instant -> asset.currency in currencies }
        }

        private val regEx = Regex("[^A-Z0-9]")
        private fun String.standardize() = uppercase().replace(regEx, ".")

        /**
         * Include only the assets that match the provided [symbols]. Matching of symbol names is done case-insensitive
         * and all special characters are replaced with a '.' character before comparing.
         */
        fun includeSymbols(vararg symbols: String): AssetFilter {
            val set = symbols.map { it.standardize() }.toSet()
            return AssetFilter { asset: Asset, _: Instant -> asset.symbol.standardize() in set }
        }

        /**
         * Exclude the assets that match the provided [symbols]. Matching of symbol names is done case-insensitive
         * and all special characters are translated into a '.' character before comparing.
         */
        fun excludeSymbols(vararg symbols: String): AssetFilter {
            val set = symbols.map { it.standardize() }.toSet()
            return AssetFilter { asset: Asset, _: Instant -> asset.symbol.standardize() !in set }
        }


    }

}
