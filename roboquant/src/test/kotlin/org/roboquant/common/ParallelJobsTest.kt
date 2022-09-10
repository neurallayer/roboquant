/*
 * Copyright 2020-2022 Neural Layer
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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ParallelJobsTest {

    private suspend fun test() {
        delay(1)
    }

    @Test
    fun joinJobs() = runBlocking {
        val jobs = ParallelJobs()
        repeat(3) {
            jobs.add { test() }
        }
        assertEquals(3, jobs.size)
        jobs.joinAll()
        assertEquals(0, jobs.size)
    }

    @Test
    fun joinBlocking() {
        val jobs = ParallelJobs()
        repeat(3) {
            jobs.add { test() }
        }
        jobs.joinAllBlocking()
        assertEquals(0, jobs.size)
    }

    @Test
    fun cancelJobs() = runBlocking {
        val jobs = ParallelJobs()
        repeat(3) {
            jobs.add { test() }
        }
        assertEquals(3, jobs.size)
        jobs.cancelAll()
        assertEquals(0, jobs.size)
    }

}

