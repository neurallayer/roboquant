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

package org.roboquant.common

import org.junit.Test
import java.util.logging.Level
import kotlin.test.*

class LoggingTest {


    @Test
    fun test() {
        val logger = Logging.getLogger("test")
        assertEquals("test", logger.name)
        assertEquals(Level.INFO, logger.level)

        Logging.setLevel(Level.WARNING)
        assertEquals(Level.WARNING, logger.level)
        Logging.setLevel(Level.WARNING)

        Logging.setDefaultLevel(Level.FINE)
        val logger2 = Logging.getLogger("test")
        assertEquals(Level.FINE, logger2.level)
        Logging.setDefaultLevel(Level.INFO)
    }

}