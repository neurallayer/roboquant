/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.optim


import org.roboquant.common.Config

/**
 * Interface for all types of search spaces
 */
interface SearchSpace {

    /**
     * get a iterable over the parameters
     */
    fun materialize() : Iterable<Params>

    /**
     * Update the search space based on an observation. This is not used by current search spaces, but is required for
     * future search spaces like Bayesian search that update their behavior based on observations.
     */
    fun update(params: Params, score: Double) {}

    /**
     * Total max size of number of parameter combinations
     */
    val size: Int

}


class EmptySearchSpace : SearchSpace {

    override fun materialize(): Iterable<Params> {
        return emptyList<Params>().asIterable()
    }

    override val size: Int
        get() = 0

}


/**
 * Random Search Space
 */
class RandomSearch(val samples: Int) : SearchSpace {

    private class Entry(val list: List<Any>?, val fn: (() -> Any)?)
    private class DoneException : Throwable()

    private val params = LinkedHashMap<String, Entry>()
    private val permutations = mutableListOf<Params>()

    private val list: List<Params> by lazy {
        try {
            calcPermutations()
        } catch (_: DoneException) {}
        permutations
    }

    private fun calcPermutations()  {
        repeat(samples) {
            val p = Params()
            for ((key, values) in params) {
                val value = if (values.list != null) values.list.random() else values.fn?.let { it() }
                p[key] = value!!
            }
            permutations.add(p)
        }

    }

    override fun materialize(): List<Params> {
        return list
    }

    override val size: Int
        get() = samples

    /**
     * Add a parameter function
     */
    fun add(name: String, fn: () -> Any) {
        params[name] = Entry(null, fn)
    }

    /**
     * Add a parameter iterable
     */
    fun add(name: String, values: Iterable<Any>) {
        params[name] = Entry(values.toList(), null)
    }



}

/**
 * Create a Grid Search Space
 */
class GridSearch : SearchSpace {

    private val params = LinkedHashMap<String, List<Any>>()

    private val entries by lazy {
        params.entries.toList()
    }

    private val permutations = mutableListOf<Params>()


    private fun calcPermutations(entry: Params = Params(), idx: Int = 0)  {
        val (key, values) = entries[idx]
        for (value in values) {
            entry[key] = value
            if (idx == entries.lastIndex) {
                permutations.add(entry.clone() as Params)
            } else {
                calcPermutations(entry, idx+1)
            }
        }

    }

    private val list: List<Params> by lazy {
        calcPermutations()
        permutations
    }

    fun add(name: String, values: Iterable<Any>) {
        params[name] = values.toList()
    }

    fun nextRandomSample() : Params {
        val result = Params()
        for ((key, values) in params) {
            val idx = Config.random.nextInt(values.lastIndex)
            result[key] = values[idx]
        }
        return result
    }


    fun add(name: String, samples: Int, fn: () -> Any) {
        params[name] = (1..samples).map { fn() }
    }


    override val size
        get() = params.map { it.value.size }.reduce(Int::times)

    override fun materialize(): List<Params> {
        return list
    }
}



infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
    require(start.isFinite())
    require(endInclusive.isFinite())
    require(step > 0.0) { "Step must be positive, was: $step." }
    val sequence = generateSequence(start) { previous ->
        if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
        val next = previous + step
        if (next > endInclusive) null else next
    }
    return sequence.asIterable()
}