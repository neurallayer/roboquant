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
import java.io.File

/**
 *
 */
fun interface AssetBuilder {

    /**
     * Based on a [file], return an instance of [Asset]
     */
    fun build(file: File): Asset
}

/**
 * The default asset builder uses a file name without its extension as the symbol name. It uses the [template] for
 * the other attributes of the asset.
 *
 * @property template the asset to use as a template.
 */
class DefaultAssetBuilder(private val template: Asset) : AssetBuilder {

    private val notCapital = Regex("[^A-Z]")

    /**
     * @see AssetBuilder.build
     */
    override fun build(file: File): Asset {
        val symbol = file.nameWithoutExtension.uppercase().replace(notCapital, ".")
        return template.copy(symbol = symbol)
    }

}
