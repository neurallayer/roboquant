/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.jupyter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.roboquant.feeds.random.RandomWalk
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class ChartTest {

    @TempDir
    lateinit var folder: File


    @Test
    fun test() {
        val f = RandomWalk.lastYears(1, 1, generateBars = true)
        val asset = f.assets.first()
        val chart = PriceBarChart(f, asset)
        assertTrue(chart.asHTML().isNotBlank())
        assertEquals(700, chart.height)
        assertContains(chart.asHTML(), asset.symbol)

        val file = File(folder, "test.html")
        chart.toHTMLFile(file.toString())
        assertTrue(file.exists())

    }





}