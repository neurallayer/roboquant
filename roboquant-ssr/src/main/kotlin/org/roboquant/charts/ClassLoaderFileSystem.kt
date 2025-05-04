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

package org.roboquant.charts

import org.graalvm.polyglot.io.FileSystem
import org.roboquant.common.RoboquantException
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute

/**
 * Minimal implementation to get loading from the classpath working for GraalJS JavaScript modules.
 */
internal class ClassLoaderFileSystem : FileSystem {

    override fun parsePath(uri: URI): Path {
        throw NotImplementedError()
    }

    override fun parsePath(path: String): Path {
        return Path.of(path)
    }

    override fun checkAccess(path: Path, modes: MutableSet<out AccessMode>?, vararg linkOptions: LinkOption?) {
        // NOP
    }

    override fun createDirectory(dir: Path, vararg attrs: FileAttribute<*>?) {
        throw NotImplementedError()
    }

    override fun delete(path: Path?) {
        throw NotImplementedError()
    }

    private fun loadJS(resource: String): ByteArray {
        val stream = this::class.java.getResourceAsStream(resource) ?: throw RoboquantException("resource not found")
        stream.use {
            return it.readAllBytes()
        }
    }

    override fun newByteChannel(
        path: Path,
        options: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): SeekableByteChannel {
        val moduleBody = loadJS(path.toString())
        return SeekableInMemoryByteChannel(moduleBody)
    }

    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> {
        throw NotImplementedError()
    }

    override fun toAbsolutePath(path: Path?): Path {
        throw NotImplementedError()
    }

    override fun toRealPath(path: Path, vararg linkOptions: LinkOption?): Path {
        return path
    }

    override fun readAttributes(
        path: Path,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any> {
        throw NotImplementedError()
    }


}
