/*
 * Copyright 2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.common

import org.roboquant.brokers.ExchangeRates
import org.roboquant.brokers.SingleCurrencyOnly
import org.roboquant.common.Config.baseCurrency
import org.roboquant.common.Config.defaultZoneId
import org.roboquant.common.Config.exchangeRates
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZoneId
import java.util.*
import java.util.logging.Logger
import kotlin.io.path.div
import kotlin.random.Random

/**
 * Configuration for roboquant that contains access to environment properties and has several global properties
 * that can be set:
 *
 * - base currency [baseCurrency]
 * - the default time zone [defaultZoneId]
 * - exchange rates [exchangeRates]
 *
 */
object Config {

    private val logger: Logger = Logging.getLogger(Config::class)
    private val properties = mutableMapOf<String, String>()
    private const val ONE_MB = 1024 * 1024
    private const val DEFAULT_SEED = 42L

    /**
     * Meta data about build en the environments
     */
    val info : Map<String, String> by lazy {
        val result = mutableMapOf<String, String>()
        result["jvm"] = System.getProperty("java.vm.name") + " " + System.getProperty("java.version")
        result["os"] =  System.getProperty("os.name") + " " + System.getProperty("os.version")
        result["memory"] = (Runtime.getRuntime().maxMemory() / ONE_MB).toString()

        val prop = Properties()
        val stream = Config::class.java.getResourceAsStream("/roboquant.properties")!!
        prop.load(stream)
        stream.close()
        prop.forEach {
            result[it.key.toString()] = it.value.toString()
        }
        result
    }

    /**
     * Set a property. This takes precedence over properties found in files.
     */
    fun setProperty(name: String, value: String) {
        properties[name] = value
    }

    /**
     * Default zoneId to use for reporting purposes. Internally roboquant always uses the Instant type, so this is only
     * used for displaying.
     */
    var defaultZoneId: ZoneId = ZoneId.systemDefault()

    /**
     * The exchange rates to use when dealing with multiple currencies. The default is [SingleCurrencyOnly] which as the
     * name suggests doesn't support any conversions.
     */
    var exchangeRates: ExchangeRates = SingleCurrencyOnly()

    /**
     * Default currency to use when reporting in a single currency
     */
    var baseCurrency: Currency = Currency.USD

    /**
     * Default random to use, typically used by methods as a default value when other random generator is provided
     */
    var random: Random = Random(DEFAULT_SEED)

    /**
     * ASCII art welcome greeting including runtime info
     */
    fun printInfo() {
        val msg = """             _______
            | $   $ |             roboquant  
            |   o   |             version: ${info["version"]}  
            |_[___]_|             build: ${info["build"]}
        ___ ___|_|___ ___         os: ${info["os"]}       
       ()___)       ()___)        home: $home
      // / |         | \ \\       jvm: ${info["jvm"]}  
     (___) |_________| (___)      memory: ${info["memory"]}MB 
      | |   __/___\__   | |
      /_\  |_________|  /_\
     // \\  |||   |||  // \\
     \\ //  |||   |||  \\ //
           ()__) ()__)
           ///     \\\
        __///_     _\\\__
       |______|   |______|"""

        println(msg)
        System.out.flush()
    }

    /**
     * Returns the roboquant home directory, <USER_HOME>/.roboquant
     * If it does not exist, it will be created the first time this property is called.
     */
    val home: Path by lazy {
        val path: Path = Paths.get(System.getProperty("user.home"), ".roboquant")
        if (Files.notExists(path)) {
            Files.createDirectory(path)
            logger.finer { "Created new home directory $path" }
        }
        path
    }

    /**
     * Get property value. Tries to find the property in the following order
     *
     *  0. Properties set using [Config.setProperty]
     *  1. System properties (`java -DpropertyName=value`)
     *  2. Environment variables set by the OS
     *  3. "dotenv" or ".env" property file in the current working directory
     *  4. ".env" property file in roboquant home directory ($USER/.roboquant)
     *
     * If nothing is found, it will return the default value or if that is not provided null.
     * Results are not cached, so any changes made to a file are picked immediately.
     *
     * @param name of property
     * @return
     */
    fun getProperty(name: String, default: String): String {
        logger.finer { "Finding property $name" }
        return  properties[name] ?: System.getProperty(name) ?: System.getenv(name) ?: env[name] ?: default
    }

    fun getProperty(name: String): String? {
        logger.finer { "Finding property $name" }
        return  properties[name] ?: System.getProperty(name) ?: System.getenv(name) ?: env[name]
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
                    logger.finer { "Found property file at $path" }
                }
            }

            load(home / ".env")
            load(Path.of(".env"))
            load(Path.of("dotenv"))
            return prop.map { it.key.toString() to it.value.toString() }.toMap()
        }

}