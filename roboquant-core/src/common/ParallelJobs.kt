package org.roboquant.common

import kotlinx.coroutines.*

/**
 * Utility to make running experiments in parallel more convenient. Basic usage:
 *
 *      val jobs = ParralelJobs()
 *      jobs.add { roboquant.runAsync(feed) }
 *      jobs.joinAll()
 *
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
    fun add(block: suspend CoroutineScope.() -> Unit) : Job {
        val job = scope.launch(block = block)
        jobs.add(job)
        return job
    }


}