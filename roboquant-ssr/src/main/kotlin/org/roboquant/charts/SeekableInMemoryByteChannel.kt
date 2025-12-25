/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.charts

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.SeekableByteChannel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simple SeekableByteChannel for an in-memory file.
 * Several methods are not implemented, just enough to make [ClassLoaderFileSystem] work.
 *
 * Original from Apache commons compress, but removed unwanted features and migrated to Kotlin
 */
internal class SeekableInMemoryByteChannel(private val data: ByteArray) : SeekableByteChannel
{
    private val closed: AtomicBoolean = AtomicBoolean()
    private var position = 0
    private var size: Int = data.size

    override fun close() {
        closed.set(true)
    }

    @Throws(ClosedChannelException::class)
    private fun ensureOpen() {
        isOpen || throw ClosedChannelException()
    }

    override fun isOpen(): Boolean {
        return !closed.get()
    }

    override fun position(): Long {
        return position.toLong()
    }

    @Throws(IOException::class)
    override fun position(newPosition: Long): SeekableByteChannel {
        ensureOpen()
        return if (newPosition in 0L..2147483647L) {
            position = newPosition.toInt()
            this
        } else {
            throw IOException("Position has to be in range 0.. 2147483647")
        }
    }

    @Throws(IOException::class)
    override fun read(buf: ByteBuffer): Int {
        ensureOpen()
        var wanted = buf.remaining()
        val possible = size - position
        return if (possible <= 0) {
            -1
        } else {
            if (wanted > possible) {
                wanted = possible
            }
            buf.put(data, position, wanted)
            position += wanted
            wanted
        }
    }

    override fun size(): Long {
        return size.toLong()
    }

    override fun truncate(newSize: Long): SeekableByteChannel {
        throw NotImplementedError()
    }

    @Throws(IOException::class)
    override fun write(b: ByteBuffer): Int {
        throw NotImplementedError()
    }

}


