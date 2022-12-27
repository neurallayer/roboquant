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

package org.roboquant.common

import de.siegmar.fastcsv.reader.NamedCsvReader
import org.roboquant.common.Currency.Companion.USD
import java.nio.charset.StandardCharsets
import java.time.Instant


/**
 * Universe is a collection of assets. Where it differs from regular collections, is that teh assets that belong to the
 * collection can change over time. So the assets in the collection at time `t` can be different from the assets in
 * the collection at time `t+1`.
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
