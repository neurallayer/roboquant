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

package org.roboquant.ta

import org.roboquant.common.toUTC
import org.roboquant.feeds.Action
import org.roboquant.feeds.PriceBar
import org.roboquant.metrics.Indicator
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import java.time.Instant

class Ta4jIndicator(
    private val maxBarCount: Int = -1,
    private val block: (BarSeries) -> Map<String, Double>
) : Indicator {

    private lateinit var series: BaseBarSeries

    init {
        clear()
    }

    override fun calculate(action: Action, time: Instant): Map<String, Double> {
        return if (action is PriceBar) {
            series.addBar(time.toUTC(), action.open, action.high, action.low, action.close, action.volume)
            block(series)
        } else {
            emptyMap()
        }
    }

    override fun clear() {
        series = BaseBarSeriesBuilder().build()
        if (maxBarCount >= 0) series.maximumBarCount = maxBarCount
    }

}