/*
 * Copyright 2020-2023 Neural Layer
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

import kotlin.test.*

internal class ConfigTest {

    @Test
    fun test() {
        val version = Config.info.version
        assertTrue(version.isNotBlank())
        Config.printInfo()

        val p = Config.getProperty("NON_EXISTING", "DUMMY")
        assertEquals("DUMMY", p)

        Config.setProperty("NON_EXISTING", "DUMMY")
        val p2 = Config.getProperty("NON_EXISTING")
        assertEquals("DUMMY", p2)
    }

}