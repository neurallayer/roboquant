/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.feeds.util

import org.roboquant.common.Asset
import org.roboquant.common.AssetType

/**
 * Used by AvroFeed to serialize and deserialize Assets to a string. This is optimized for size.
 */
object AssetSerializer {

    /**
     * Serialize an asset into a short string.
     */
    fun Asset.serialize(): String {
        val sb = StringBuilder(symbol).append(SEP)
        if (type != AssetType.STOCK) sb.append(type.name)
        sb.append(SEP)
        if (currency.currencyCode != "USD") sb.append(currency.currencyCode)
        sb.append(SEP)
        if (exchange.exchangeCode != "") sb.append(exchange.exchangeCode)
        sb.append(SEP)
        if (multiplier != 1.0) sb.append(multiplier)
        sb.append(SEP)
        if (id.isNotEmpty()) sb.append(id)
        sb.append(SEP)

        var cnt = 0
        for (ch in sb.reversed()) if (ch == SEP) cnt++ else break
        return sb.substring(0, sb.length - cnt)
    }

    /**
     * Use the ASCII Unit Separator character. Should not interfere with used strings for symbol, exchange and currency
     */
    private const val SEP = '\u001F'

    /**
     * Deserialize a string into an asset. The string needs to have been created using [serialize]
     *
     * @return
     */
    fun String.deserialize(): Asset {
        val e = split(SEP)
        val l = e.size
        require(l <= 6) { "Invalid format" }
        return Asset(
            e[0],
            if (l > 1 && e[1].isNotEmpty()) AssetType.valueOf(e[1]) else AssetType.STOCK,
            if (l > 2 && e[2].isNotEmpty()) e[2] else "USD",
            if (l > 3) e[3] else "",
            if (l > 4 && e[4].isNotEmpty()) e[4].toDouble() else 1.0,
            if (l > 5) e[5] else "",
        )

    }
}