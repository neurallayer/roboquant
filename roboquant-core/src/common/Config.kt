/*
 * Copyright 2021 Neural Layer
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

@file:Suppress("SimplifiableCallChain")

package org.roboquant.common

import org.roboquant.brokers.ExchangeRates
import org.roboquant.brokers.SingleCurrencyOnly
import org.roboquant.common.Config.baseCurrency
import org.roboquant.common.Config.defaultZoneId
import org.roboquant.common.Config.seed
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZoneId
import java.util.*
import java.util.logging.Logger
import kotlin.io.path.div

/**
 * Configuration for roboquant that contains access to environment properties and has several global properties
 * that can be set:
 *
 * - base cuurency [baseCurrency]
 * - the default time zone [defaultZoneId]
 * - exchange rates [exchangeRates]
 * - random seed [seed]
 *
 */
object Config {

    private val logger: Logger = Logging.getLogger(Config::class)
    private val properties = mutableMapOf<String, String>()

    /**
     * Set a property. This takes precendence over properties found in files.
     */
    fun setProperty(name: String, value: String) {
        properties[name] = value
    }

    /**
     * Current version of roboquant
     */
    const val version = "0.8-SNAPSHOT"

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
     * Default currency to use when reporting.
     */
    var baseCurrency: Currency = Currency.USD

    /**
     * Default seed to use, typically used by methods as a default value when no other seed is provided
     */
    var seed = 42L

    /**
     * ASCII art welcome greeting including overview of some properties
     */
    fun info() {
        val jvmInfo = System.getProperty("java.vm.name") + " " + System.getProperty("java.version")
        val osInfo = System.getProperty("os.name") + " " + System.getProperty("os.version")
        val runtimeInfo = Runtime.getRuntime().maxMemory() / (1024 * 1024)

        val msg = """             _______ 
            | $   $ |   roboquant v$version  home=$home
            |   o   |   $osInfo
            |_[___]_|   $jvmInfo (max mem=$runtimeInfo MB)
        ___ ___|_|___ ___    
       ()___)       ()___)
      // / |         | \ \\
     (___) |_________| (___)
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
     * Return the roboquant home directory, <USER_HOME>/.roboquant
     * If it doesn not exist, it will be created first time this property is called.
     */
    val home by lazy {
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
    fun getProperty(name: String, default: String? = null): String? {
        logger.finer { "Finding property $name" }
        return  properties[name] ?: System.getProperty(name) ?: System.getenv(name) ?: env[name] ?: default
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