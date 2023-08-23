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

package org.roboquant.brokers.sim.execution

/**
 * Optimized view that can be used on append-only lists.
 *
 * Compared to the standard Java [subList], this implementation doesn't use the underlying iterator.
 * As a consequence, it doesn't throw an exception when an element is added during an iteration over this View.
 */
internal class View<T>(private val list: List<T>, override val size: Int = list.size) : List<T> {

    private class ViewIterator<T>(val list: List<T>, val size: Int) : ListIterator<T> {

        var cnt = 0

        override fun hasNext(): Boolean = cnt < size

        override fun hasPrevious() = cnt > 0

        override fun next(): T {
            if (cnt >= size) throw NoSuchElementException()
            val elem = list[cnt]
            cnt++
            return elem
        }

        override fun nextIndex(): Int = cnt.coerceAtMost(size)


        override fun previous(): T {
            if (cnt == 0) throw NoSuchElementException()
            cnt--
            return list[cnt]
        }

        override fun previousIndex(): Int = cnt - 1

    }

    override fun get(index: Int): T = list[index]

    override fun indexOf(element: T): Int {
        for ((idx, elem) in this.withIndex()) if (elem == element) return idx
        return -1
    }

    override fun contains(element: T): Boolean {
        for (elem in this) if (elem == element) return true
        return false
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        // Not a very fast impl
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<T> {
        return ViewIterator(list, list.size)
    }

    override fun listIterator(): ListIterator<T> {
        return ViewIterator(list, list.size)
    }

    override fun listIterator(index: Int): ListIterator<T> {
        val iter = ViewIterator(list, list.size)
        iter.cnt = index
        return iter
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        // We materialize and return the sub-list, an expensive operation
        return this.toList().subList(fromIndex, toIndex)
    }

    override fun lastIndexOf(element: T): Int {
        for (idx in lastIndex downTo 0) {
            if (list[idx] == element) return idx
        }
        return -1
    }


}

/**
 * Create a view on a list. This list should be an append-only list.
 */
internal fun <E> List<E>.view() = View(this)

