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

package org.roboquant.common

import java.time.Instant
import kotlin.test.*

internal class UniverseTest {

    @Test
    fun predefined() {
        val universe = Universe.sp500
        val assets = universe.getAssets(Instant.now())
        assertTrue(assets.isNotEmpty())
        assertContains(assets.map { it.symbol }, "AAPL")

        val assets2 = universe.getAssets(Instant.parse("1970-01-01T00:00:00Z"))
        assertTrue(assets2.isNotEmpty())
    }

}
