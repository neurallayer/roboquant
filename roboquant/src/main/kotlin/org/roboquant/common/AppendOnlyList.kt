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

package org.roboquant.common

/**
 * Append-only list that is thread safe for adding and iterating.
 */
internal class AppendOnlyList<E> private constructor(private val data: ArrayList<E>, private val viewSize: Int) :
    List<E> {

    constructor() : this(ArrayList(), -1)

    constructor(capacity: Int) : this(ArrayList(capacity), -1)

    init {
        assert(viewSize <= data.size)
    }

    private class AppendOnlyListIterator<T>(private val list: List<T>, private val size: Int) : ListIterator<T> {

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

    override val size: Int
        get() = if (viewSize != -1) viewSize else data.size

    @Synchronized
    fun add(element: E): Boolean {
        return data.add(element)
    }

    @Synchronized
    fun addAll(elements: Collection<E>): Boolean {
        return data.addAll(elements)
    }


    override fun get(index: Int): E = data[index]

    override fun isEmpty(): Boolean = size == 0

    override fun iterator(): Iterator<E> {
        return AppendOnlyListIterator(data, size)
    }

    override fun listIterator(): ListIterator<E> {
        return AppendOnlyListIterator(data, size)
    }

    override fun listIterator(index: Int): ListIterator<E> {
        return AppendOnlyListIterator(data, size)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        require(fromIndex == 0)
        return AppendOnlyList(data, toIndex)
    }

    fun view(): List<E> = AppendOnlyList(data, size)

    override fun lastIndexOf(element: E): Int {
        for (idx in lastIndex downTo 0) if (data[idx] == element) return idx
        return -1
    }

    override fun indexOf(element: E): Int {
        for ((idx, elem) in this.withIndex()) if (elem == element) return idx
        return -1
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        // Not a very fast impl
        return elements.all { contains(it) }
    }

    override fun contains(element: E): Boolean {
        for (elem in this) if (elem == element) return true
        return false
    }
}