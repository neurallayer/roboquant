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

package org.roboquant.ml

import org.roboquant.common.Asset
import org.roboquant.feeds.Event

/**
 * Extract the volume from the price-action for the [asset]
 */
class VolumeFeature(
    private val asset: Asset,
    override val name: String = "${asset.symbol}-VOLUME"
) : SingleValueFeature() {

    /**
     * @see Feature.calculate
     */
    override fun calculateValue(event: Event): Double {
        val action = event.prices[asset]
        return action?.volume ?: Double.NaN
    }


}
