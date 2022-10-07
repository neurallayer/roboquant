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

import kotlinx.coroutines.*

/**
 * Utility to make running experiments in parallel more convenient. Basic usage:
 *
 *      val jobs = ParallelJobs()
 *      jobs.add {
 *          val roboquant = Roboquant(...)
 *          roboquant.runAsync(feed)
 *      }
 *      jobs.joinAll()
 *
 *  Note that most feeds and metric-loggers can be shared among jobs, but that isn't true for the other components
 *  like strategy, policy, metrics and broker.
 */
class ParallelJobs {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
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
     * How many jobs are there
     */
    val size
        get() = jobs.size

    /**
     * Cancel all the jobs
     */
    fun cancelAll() {
        // jobs.forEach { it.cancelAndJoin() }
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

}