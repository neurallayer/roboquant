package org.roboquant.common

import java.time.Instant

/**
 * Asset filter limits the assets that will be processed in certain operations at a given time.
 */
fun interface AssetFilter {

    /**
     * Returns true if the provided [asset] should be processed at the provided [time], false otherwise. The time can
     * be used by implementations to support asset collections that change over time, like for example the S&P 500
     * index.
     */
    fun filter(asset: Asset, time: Instant): Boolean

    /**
     * Standard set of Asset filters
     */
    companion object {

        /**
         * Include all assets, so this filer always return true
         */
        fun all(): AssetFilter {
            return AssetFilter { _: Asset, _: Instant -> true }
        }

        /**
         * Include only the assets that are denoted in the provided [currencies].
         */
        fun includeCurrencies(vararg currencies: Currency): AssetFilter {
            return AssetFilter { asset: Asset, _: Instant -> asset.currency in currencies }
        }

        private val regEx = Regex("[^A-Z0-9]")
        private fun String.standardize() = uppercase().replace(regEx, ".")

        /**
         * Include only the assets that match the provided [symbols]. Matching of symbol names is done case-insensitive
         * and all special characters are translated into '.' before comparing.
         */
        fun includeSymbols(vararg symbols: String): AssetFilter {
            val set = symbols.map { it.standardize() }.toSet()
            return AssetFilter { asset: Asset, _: Instant -> asset.symbol.standardize() in set }
        }

        /**
         * Exclude the assets that match the provided [symbols]. Matching of symbol names is done case-insensitive
         * and all special characters are translated into '.' before comparing.
         */
        fun excludeSymbols(vararg symbols: String): AssetFilter {
            val set = symbols.map { it.standardize() }.toSet()
            return AssetFilter { asset: Asset, _: Instant -> asset.symbol.standardize() !in set }
        }



    }

}
