package org.roboquant.common

import de.siegmar.fastcsv.reader.NamedCsvReader
import org.roboquant.common.Currency.Companion.USD
import java.nio.charset.StandardCharsets
import java.time.Instant


/**
 * Universe is a collection of assets. Where it differs from regular collections, is that it can change over time. So
 * the assets in the collection at time `t` can be different from the assets in the collection at time `t+1`.
 */
interface Universe {

    /**
     * Return the list of assets in this universe at the given [time]
     */
    fun getAssets(time: Instant) : List<Asset>

    /**
     * Set of standard universes like the assets in the S&P 500 index
     */
    companion object Factory {

        /**
         * Return a universe containing all the S&P 500 assets.
         * @TODO currently ignores the date when assets where added/removed from S&P 500
         */
        val sp500 : Universe by lazy { SP500() }


    }
}

private class SP500 : Universe {

    private val assets: List<Asset>

    init {
        val stream =  SP500::class.java.getResourceAsStream("/sp500.csv")!!
        val content = String(stream.readAllBytes(), StandardCharsets.UTF_8)
        stream.close()
        val builder = NamedCsvReader.builder().fieldSeparator(';').build(content)
        val us = Exchange.getInstance("US")
        assets = builder.map { Asset(it.getField("Symbol"), currency = USD, exchange = us) }
    }


    override fun getAssets(time: Instant): List<Asset> {
        return assets
    }

}
