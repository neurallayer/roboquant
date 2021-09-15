@file:Suppress("unused")

package org.roboquant.common

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


/**
 * Background helps to deal with starting and running co-routines. Right now very light implementation, but in future
 * can add features without impacting rest of code.
 *
 */
object Background {

    private val CPUBoundScope = CoroutineScope(Dispatchers.Default + Job())
    private val IOBoundScope = CoroutineScope(Dispatchers.IO + Job())


    /**
     * Launch an IO bound routine
     *
     * @param block
     * @receiver
     * @return
     */
    fun ioJob(block: suspend CoroutineScope.() -> Unit): Job {
        return IOBoundScope.launch(block = block)
    }

    /**
     * Launch an CPU bound routine
     *
     * @param block
     * @receiver
     * @return
     */
    fun cpuJob(block: suspend CoroutineScope.() -> Unit): Job {
        return CPUBoundScope.launch(block = block)
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        return CPUBoundScope.async(block = block)
    }

    /**
     * Run a routine blocking, normally should be used at top level
     *
     * @param ctx
     * @param block
     * @receiver
     */
    fun runBlocking(ctx: CoroutineContext = Dispatchers.Default, block: suspend CoroutineScope.() -> Unit) =
        kotlinx.coroutines.runBlocking(ctx, block)
}