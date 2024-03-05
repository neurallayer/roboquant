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

package org.roboquant.charts

import org.icepear.echarts.Option
import org.icepear.echarts.Scatter
import org.icepear.echarts.charts.scatter.ScatterSeries
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.feeds.applyEvents
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import java.time.Instant


/**
 * A SignalChart allows you to visualize the signals created by a strategy based on a feed.
 *
 * The result is a scatter plot where each signal is a dot. The value is equivalent to the rating of the signal.
 * The tooltip provides additional details, like the symbol of the underlying asset.
 *
 * @param feed the feed to use as input to the strategy
 * @param strategy the strategy to use to genrate the signals
 * @param timeframe limit the data to the provided timeframe, default is [Timeframe.INFINITE]
 */
class SignalChart(
    private val feed: Feed,
    private val strategy: Strategy,
    private val timeframe: Timeframe = Timeframe.INFINITE,
) : Chart() {

    /**
     * @see Chart.getOption
     */
    override fun getOption(): Option {
        val data = signalsToSeriesData()

        val series = ScatterSeries()
            .setData(data)
            .setSymbolSize(10)

        val tooltip = Tooltip()
            .setFormatter(javascriptFunction("return p.value[2];"))

        val chart = Scatter()
            .setTitle(title ?: "")
            .addXAxis(TimeAxis())
            .addYAxis(ValueAxis().setScale(true))
            .addSeries(series)
            .setTooltip(tooltip)

        val option = chart.option
        option.setToolbox(getToolbox(includeMagicType = false))
        option.setDataZoom(DataZoom())

        return option
    }

    private fun Signal.getTooltip(time: Instant): String {
        return """
        |symbol: ${asset.symbol}<br>
        |currency: ${asset.currency}<br>
        |time: $time<br>
        |rating: $rating<br>
        |type: $type<br>
        |tag: $tag""".trimMargin()
    }

    private fun signalsToSeriesData(): Any {
        strategy.reset()
        val result = mutableListOf<Triple<Any, Any, Any>>()
        feed.applyEvents(timeframe) {
            val signals = strategy.generate(it)
            val time = it.time
            for (signal in signals) {
                val observation = Triple(time, signal.rating, signal.getTooltip(time))
                result.add(observation)
            }
        }
        return result
    }

}
