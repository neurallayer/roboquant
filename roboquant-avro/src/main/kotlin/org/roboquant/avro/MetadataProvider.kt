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

package org.roboquant.avro

import org.apache.avro.file.DataFileReader
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.roboquant.common.Asset
import org.roboquant.common.RoboquantException
import org.roboquant.common.Timeframe
import org.roboquant.feeds.util.AssetSerializer.deserialize
import java.io.*
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import kotlin.io.path.pathString

/**
 * MetadataProvider of the feed that optionally can be loaded from disk directly to memory for speedy startup times
 */
internal class MetadataProvider(private val avroFile: Path) {

    private val cacheFile = File(avroFile.pathString + CACHE_SUFFIX)

    internal data class Metadata(
        val index: List<Pair<Instant, Long>>,
        val assets: Map<String, Asset>,
        val timeframe: Timeframe
    )

    private fun toSerializable(data: Metadata): Triple<List<Pair<Instant, Long>>, List<String>, Timeframe> {
        return Triple(
            data.index,
            data.assets.keys.toList(),
            data.timeframe
        )
    }

    private fun fromSerializable(data: Triple<List<Pair<Instant, Long>>, List<String>, Timeframe>): Metadata {
        val assets = data.second.associateWith { it.deserialize() }
        return Metadata(data.first, assets, data.third)
    }

    fun clearCache() {
        if (cacheFile.exists() && cacheFile.isFile) {
            val success = cacheFile.delete()
            if (!success) throw RoboquantException("Couldn't delete cache file")
        }
    }


    private fun getReader(): DataFileReader<GenericRecord> {
        return DataFileReader(avroFile.toFile(), GenericDatumReader())
    }

    /**
     * Build an index of where each time starts. The index helps to achieve faster read access if not starting
     * from the beginning.
     */
    internal fun build(useCache: Boolean = false): Metadata {
        var result = if (useCache) loadFromCache() else null
        if (result != null) return result

        var last = Long.MIN_VALUE
        val index = mutableListOf<Pair<Instant, Long>>()
        var start = Long.MIN_VALUE
        var prevPos = Long.MIN_VALUE

        val assetLookup = mutableMapOf<String, Asset>()

        getReader().use {
            while (it.hasNext()) {
                val rec = it.next()
                val t = rec[0] as Long

                val assetStr = rec[1].toString()
                if (!assetLookup.containsKey(assetStr)) assetLookup[assetStr] = assetStr.deserialize()
                if (t > last) {
                    if (start == Long.MIN_VALUE) start = t
                    val pos = it.previousSync()

                    if (pos != prevPos) {
                        val time = Instant.ofEpochMilli(t)
                        index.add(Pair(time, pos))
                        prevPos = pos
                    }
                    last = t
                }
            }
        }
        val timeframe = if (start == Long.MIN_VALUE)
            Timeframe.EMPTY
        else
            Timeframe(Instant.ofEpochMilli(start), Instant.ofEpochMilli(last), true)

        result = Metadata(index, assetLookup, timeframe)

        // We save it for next time only if caching is required
        if (useCache) save(result)
        return result
    }

    private fun ObjectInputStream.isValid(): Boolean {
        val avroFileHash = calculateFileHash(avroFile.toFile())
        val hash = readObject() as String
        if (hash != avroFileHash) {
            AvroFeed.logger.info { "file hash different from found in cache" }
            return false
        }

        val version = readObject() as String
        if (version != VERSION) {
            AvroFeed.logger.info { "index file has wrong version" }
            return false
        }

        return true
    }

    private fun loadFromCache(): Metadata? {
        if (!cacheFile.exists()) return null

        FileInputStream(cacheFile).use { fileInputStream ->
            ObjectInputStream(fileInputStream).use { objectInputStream ->
                AvroFeed.logger.info { "loading cache file: $cacheFile" }

                if (!objectInputStream.isValid()) return null

                @Suppress("UNCHECKED_CAST")
                val data = objectInputStream.readObject() as Triple<List<Pair<Instant, Long>>, List<String>, Timeframe>
                return fromSerializable(data)

            }
        }

    }

    private fun save(result: Metadata) {
        AvroFeed.logger.info { "building new cache file: $cacheFile" }
        val hash = calculateFileHash(avroFile.toFile())

        FileOutputStream(cacheFile).use { fileOutputStream ->
            ObjectOutputStream(fileOutputStream).use { objOutputStream ->
                objOutputStream.writeObject(hash)
                objOutputStream.writeObject(VERSION)
                objOutputStream.writeObject(toSerializable(result))
            }
        }

    }

    companion object {
        internal const val VERSION = "1.0"
        internal const val CACHE_SUFFIX = ".cache"
    }

    /**
     * Since files can be large, only read the beginning and ends of the file
     * for creating the hash.
     */
    private fun readFirstAndLastBytes(file: File): ByteArray {
        val bufferSize = 1024 * 1024 // 1 MB
        val totalBytesToRead = 2 * bufferSize
        val byteArray = ByteArray(totalBytesToRead)

        FileInputStream(file).use { inputStream ->
            val bytesRead = inputStream.read(byteArray)

            if (bytesRead < totalBytesToRead) {
                // The file is smaller than 2 MB, adjust the array size
                return byteArray.copyOfRange(0, bytesRead)
            } else {
                inputStream.skip(file.length() - bufferSize.toLong())
                inputStream.read(byteArray, bufferSize, bufferSize)
                return byteArray
            }
        }
    }

    /**
     * Create hash of the file to detect changes
     */
    private fun calculateFileHash(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = readFirstAndLastBytes(file)
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

}

