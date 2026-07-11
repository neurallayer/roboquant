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

package org.roboquant.common

import org.roboquant.common.Config.random
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.div
import kotlin.random.Random

/**
 * The configuration for roboquant that contains access to environment properties and has several global properties
 * that can be set:
 *
 * - The [random] number generator
 *
 */
object Config {

    private val logger = Logging.getLogger(Config::class)
    private val properties = mutableMapOf<String, String>()
    private const val DEFAULT_SEED = 42L

    // Used to handle Double imprecision
    internal const val EPS = 1e-10

    /**
     * Set a property. This takes precedence over properties found in files.
     */
    fun setProperty(name: String, value: String) {
        properties[name] = value
    }

    /**
     * Default [Random] number generator to use, typically used by methods as a default value when no other random
     * number generator is provided
     */
    var random: Random = Random(DEFAULT_SEED)

    /**
     * Returns the roboquant home directory, by default <USER_HOME>/.roboquant
     * If a system property named `roboquant.home` is set, that will be used instead.
     *
     * If the directory does not exist yet, it will be created the first time this
     * method is called.
     */
    val home: Path by lazy {
        val roboquantHome = System.getProperty("roboquant.home")
        val path: Path = if (roboquantHome != null) Paths.get(roboquantHome) else
            Paths.get(System.getProperty("user.home"), ".roboquant")
        if (Files.notExists(path)) {
            Files.createDirectory(path)
            logger.trace { "Created new home directory $path" }
        }
        path
    }

    /**
     * Get a property value. It will try to find the property in the following order:
     *
     *  0. Properties set using [Config.setProperty]
     *  1. System properties (java -D syntax)
     *  2. Environment variables set by the OS (all capitals and underscores)
     *  3. A "dotenv" or ".env" property file in the current working directory
     *  4. The ".env" property file in roboquant home directory ($USER/.roboquant)
     *
     * If nothing is found, it will return the default value or if that is not provided null.
     * Results are not cached, so any changes made to a file are picked immediately.
     *
     * @param name of property
     * @return
     */
    fun getProperty(name: String, default: String): String {
        logger.trace { "finding property $name" }
        return getProperty(name) ?: default
    }

    /**
     * @see getProperty
     */
    fun getProperty(name: String, default: Boolean): Boolean {
        logger.trace { "finding property $name" }
        return getProperty(name)?.toBoolean() ?: default
    }

    /**
     * @see getProperty
     */
    fun getProperty(name: String, default: Int): Int {
        logger.trace { "finding property $name" }
        return getProperty(name)?.toInt() ?: default
    }

    /**
     * Return property value, or null if not found.
     */
    fun getProperty(name: String): String? {
        logger.trace { "finding property $name" }
        return properties[name]
            ?: System.getProperty(name)
            ?: System.getenv(name.replace('.', '_').uppercase())
            ?: System.getenv(name)
            ?: env[name]
    }

    /**
     * Load properties from an environment file. We don't use caching, so any change to the file is picked-up
     * immediately.
     */
    private val env: Map<String, String>
        get() {
            val prop = Properties()
            fun load(path: Path) {
                if (Files.exists(path)) {
                    prop.load(path.toFile().inputStream())
                    logger.trace { "Found property file at $path" }
                }
            }

            load(home / ".env")
            load(Path.of(".env"))
            load(Path.of("dotenv"))
            return prop.map { it.key.toString() to it.value.toString() }.toMap()
        }

}
