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

package org.roboquant.backtest


/**
 * Interface for all types of search spaces
 */
interface SearchSpace : Iterable<Params> {


    /**
     * Update the search space based on an observation.
     * The observation is a combination of the selected [params] and the resulting [score].
     *
     * This is not used by current search spaces, but is required for future search spaces like Bayesian search that
     * update their behavior based on observations.
     *
     * The default implementation is to do nothing.
     */
    fun update(params: Params, score: Double) {}

    /**
     * Returns the total number of parameter combinations found in this search space
     */
    val size: Int

}

/**
 * If you just want to run a certain type of back test without any hyperparameter search, you can use this search space.
 * It contains a single entry with no parameters, so iterating over this search space will run a back test exactly once.
 */
class EmptySearchSpace : SearchSpace {

    override fun iterator(): Iterator<Params> {
        return  listOf(Params()).listIterator()
    }

    /**
     * @see SearchSpace.size
     */
    override val size: Int
        get() = 1

}


/**
 * Random Search Space
 *
 * In Random Search, we try random combinations of the values of the [Params] and evaluate the trading strategy
 * for these selected combination.
 * The number of combinations is limited to [size].
 *
 * @property size the total number of samples that will be drawn from this random search space
 */
class RandomSearch(override val size: Int) : SearchSpace {

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
        repeat(size) {
            val p = Params()
            for ((key, values) in params) {
                val value = if (values.list != null) values.list.random() else values.fn?.let { it() }
                p[key] = value!!
            }
            permutations.add(p)
        }

    }

    override fun iterator(): Iterator<Params> {
        return  list.listIterator()
    }



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
 * Create a Grid Search Space.
 *
 * In Grid Search, we try every combination of the values of the [Params] and evaluate the trading strategy for
 * each combination
 *
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


    fun add(name: String, samples: Int, fn: () -> Any) {
        params[name] = (1..samples).map { fn() }
    }


    override val size
        get() = params.map { it.value.size }.reduce(Int::times)

    override fun iterator(): Iterator<Params> {
        return  list.listIterator()
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