/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.samples

import org.roboquant.charts.CorrelationChart
import org.roboquant.charts.ImageCreator
import org.roboquant.charts.PriceBarChart
import org.roboquant.charts.transcodeSVG2PNG
import org.roboquant.feeds.random.RandomWalk
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test

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

class SSRSamples {
    /**
     * You can run sample to generate a correlation chart and a price-bar chart.
     * The output is saved in /tmp/
     */
    @Test
    @Ignore
    internal fun run() {
        val feed = RandomWalk.lastYears(2, nAssets = 5)
        val chart = CorrelationChart(feed, feed.assets)

        val creator = ImageCreator()

        val svg = creator.renderToSVGString(chart)
        println(svg)
        transcodeSVG2PNG(svg, File("/tmp/correlation_chart.png"))

        val asset = feed.assets.first()
        val chart2 = PriceBarChart(feed, asset)
        val svg2 = creator.renderToSVGString(chart2)
        transcodeSVG2PNG(svg2, File("/tmp/${asset.symbol.lowercase()}_pricechart.png"))
    }

}
