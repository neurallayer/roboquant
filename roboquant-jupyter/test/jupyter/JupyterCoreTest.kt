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

import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import org.junit.jupiter.api.Test
import org.roboquant.common.RoboquantException
import kotlin.test.assertTrue

internal class JupyterCoreTest {

    @Test
    fun test() {
        JupyterCore()
        Output.classic()
        Output.lab()
    }


    @Test
    fun exceptions() {
        val t = RoboquantThrowableRenderer()
        assertTrue { t.accepts(RoboquantException("test")) }
        assertTrue { t.accepts(RuntimeException()) }
        assertTrue { t.accepts(Throwable()) }
        val output = t.render(Exception("Dummy"))
        assertTrue { output is MimeTypedResult }
    }

    @Test
    fun core() {
        class TestOutput : Output() {
            override fun asHTML(): String {
               return  "<div>Hello</div>"
            }

            override fun asHTMLPage(): String {
                return  "<html><div>Hello</div></html>"
            }

        }

        val output = TestOutput()
        val result = JupyterCore.render(output)
        assertTrue(result)
    }

}