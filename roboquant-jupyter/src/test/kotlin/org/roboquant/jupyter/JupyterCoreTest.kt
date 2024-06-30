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

package org.roboquant.jupyter

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.charts.PriceChart
import org.roboquant.common.RoboquantException
import org.roboquant.feeds.random.RandomWalk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JupyterCoreTest {

    @Test
    fun testDefaults() {
        JupyterCore(null, mutableMapOf())
        assertEquals(false, NotebookConfig.isolation)
        assertEquals("auto", NotebookConfig.theme)
    }

    @Test
    fun exceptions() {
        val t = RoboquantThrowableRenderer()
        assertTrue { t.accepts(RoboquantException("test")) }
        assertTrue { t.accepts(RuntimeException()) }
        assertTrue { t.accepts(Throwable()) }

        assertDoesNotThrow {
            t.render(Exception("Dummy"))
        }

    }

    @Test
    fun extensions() {
        val feed = RandomWalk.lastYears(1)
        val chart = PriceChart(feed, feed.assets.first())
        assertDoesNotThrow {
            chart.asHTML("dark")
            chart.asHTMLPage("dark")
        }
    }

}
