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

package org.roboquant.common

import kotlinx.coroutines.*

/**
 * This is utility to make running jobs in parallel more convenient. Basic usage:
 *
 * ```
 * val jobs = ParallelJobs()
 * jobs.add {
 *      runAsync(feed, SomeStrategy())
 * }
 * jobs.joinAll()
 *```
 *  Note that most feeds can be shared across runs, but that isn't true for the other components
 *  like strategy, trader, metrics and trader. These are stateful and should not be shared.
 */
class ParallelJobs {

    // Optimized for CPU-bound jobs
    private val scope = CoroutineScope(Dispatchers.Default)
    private val jobs = mutableListOf<Job>()

    /**
     * Wait for all the jobs to finish
     */
    suspend fun joinAll() {
        jobs.joinAll()
        jobs.clear()
    }

    /**
     * Wait for all the jobs to finish, using blocking mode. This is especially useful in Jupyter Notebooks and other
     * interactive development environments.
     */
    fun joinAllBlocking() = runBlocking {
        jobs.joinAll()
        jobs.clear()
    }

    /**
     * The number of instantiated jobs.
     */
    val size
        get() = jobs.size

    /**
     * Cancel all the jobs
     */
    fun cancelAll() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    /**
     * Add a new job to the list and run it.
     */
    fun add(block: suspend CoroutineScope.() -> Unit): Job {
        val job = scope.launch(block = block)
        jobs.add(job)
        return job
    }

    /**
     * Add a job
     */
    fun add(job: Job) {
        jobs.add(job)
    }

}
