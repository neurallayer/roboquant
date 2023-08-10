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

package org.roboquant.charts

import org.icepear.echarts.Option
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.common.USD
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.jupyter.render
import java.io.File
import java.time.Instant
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ChartTest {

    @TempDir
    lateinit var folder: File

    private class MyChart : Chart() {

        init {
            javascriptFunction("return p;")
        }

        fun reduced(size: Int): List<Int> {
            val data = (1..size).toList()
            return reduce(data).toList()
        }

        override fun getOption(): Option {
            return Option()
        }

    }

    @Test
    fun test() {
        val f = RandomWalkFeed.lastYears(1, 1, generateBars = true)
        val asset = f.assets.first()
        Chart.counter = 0
        val chart = PriceBarChart(f, asset)
        val html = chart.asHTML()
        assertTrue(html.isNotBlank())
        assertEquals(700, chart.height)
        assertContains(html, asset.symbol)

        Chart.counter = 0
        val chart2 = PriceBarChart(f, asset.symbol)
        assertEquals(html, chart2.asHTML())

        val file = File(folder, "test.html")
        chart.toHTMLFile(file.toString())
        assertTrue(file.exists())

    }

    @Test
    fun testGsonAdapters() {
        val b = Chart.gsonBuilder.create()
        assertDoesNotThrow {
            b.toJson(Pair("A", "B"))
            b.toJson(Triple("A", "B", "C"))
            b.toJson(Instant.now())
            b.toJson(100.USD)
        }
    }

    @Test
    fun testReduced() {
        val chart = MyChart()
        Chart.maxSamples = 10
        assertEquals(10, chart.reduced(100).size)
        Chart.maxSamples = Int.MAX_VALUE
        assertEquals(100, chart.reduced(100).size)
    }

    @Test
    fun testCodeGeneration() {
        val chart = MyChart()
        chart.height = 123

        assertDoesNotThrow {
            chart.getOption().renderJson()
        }

        assertDoesNotThrow {
            chart.render()
        }

        val code = chart.asHTML()
        assertContains(code, "123px")
        assertContains(code, "echarts.init")
    }

}