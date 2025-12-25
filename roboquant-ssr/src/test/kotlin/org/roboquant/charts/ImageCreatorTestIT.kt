/*
 * Copyright 2020-2026 Neural Layer
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

import org.junit.jupiter.api.io.TempDir
import org.roboquant.feeds.random.RandomWalk
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ImageCreatorTestIT {

    private val imageCreator = ImageCreator()
    private val feed = RandomWalk.lastYears(nAssets = 5)

    @TempDir
    lateinit var folder: File

    @Test
    fun basic() {
        val chart = PriceBarChart(feed, feed.assets.first())
        val svg = imageCreator.renderToSVGString(chart)
        assertTrue(svg.isNotEmpty())
    }

    @Test
    fun corr() {
        val chart = CorrelationChart(feed, feed.assets)
        val svg = imageCreator.renderToSVGString(chart)
        assertTrue(svg.isNotEmpty())
    }

    @Test
    fun toPNG() {
        val chart = PriceChart(feed, feed.assets.last())
        val svg = imageCreator.renderToSVGString(chart)

        val arr = transcodeSVG2PNG(svg)
        assertTrue(arr.isNotEmpty())

        val f = File(folder, "test.png")
        transcodeSVG2PNG(svg, f)
        assertTrue(f.exists())
        assertEquals(arr.size.toLong(), Files.size(f.toPath()))
    }


}
