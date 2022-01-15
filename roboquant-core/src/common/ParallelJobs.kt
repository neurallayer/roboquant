package org.roboquant.common

import kotlinx.coroutines.*

/**
 * Utility to make running experiments in parallel more convenient. Basic usage:
 *
 *      val jobs = ParralelJobs()
 *      jobs.add { roboquant.runAsync(feed) }
 *      jobs.joinAll()
 *
 * @property simulateSequential run of jobs sequnetial, mostly usefull for testing purposes
 */
class ParallelJobs(private val simulateSequential: Boolean = false) {

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
    suspend fun cancelAll() {
        jobs.forEach { it.cancelAndJoin() }
        jobs.clear()
    }

    /**
     * Add a new job to the list and run it.
     */
    suspend fun add(block: suspend CoroutineScope.() -> Unit) {
        val job = scope.launch(block = block)
        if (simulateSequential) job.join()
        jobs.add(job)
    }


}