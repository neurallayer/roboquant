@file:Suppress("SimplifiableCallChain")

package org.roboquant.common

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.logging.Logger
import kotlin.io.path.div

/**
 * Configuration object for roboquant that contains shared properties.
 *
 */
object Config {

    private val logger: Logger = Logging.getLogger("Config")

    /**
     * Current version of roboquant
     */
    const val version = "0.8-SNAPSHOT"

    /**
     * Hint that memory is limited. This might cause certain components to prefer low memory usage over performance
     */
    var lowMemory = false

    /**
     * Default seed to use, typically used by methods as a default value when none is specified
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
     * Get the roboquant home directory, <USER_HOME>/.roboquant
     * If it is not available yet, it will be created first time this property is called.
     *
     * @return home directory as Path
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
     * Get property value. Tries to find the property in this order
     *
     *  1. System properties (`java -DpropertyName=value`)
     *  2. Environment variables set by the OS
     *  3. "dotenv" or ".env" property file in the current working directory
     *  4. ".env" property file in roboquant home directory (normally $USER/.roboquant)
     *
     * If nothing is found, it will return the default value or if that is not provided null
     *
     * @param name of property
     * @return
     */
    fun getProperty(name: String, default: String? = null): String? {
        logger.finer { "Finding property $name" }
        return System.getProperty(name) ?: System.getenv(name) ?: env[name] ?: default
    }

    /**
     * Load properties from an environment file. We don't use lazy so any change to the file is picked-up
     * immediately.
     */
    private val env : Map<String, String>
        get() {
            val prop = Properties()
            fun load(path: Path) {
                if (Files.exists(path)) {
                    prop.load(path.toFile().inputStream())
                    logger.finer { "Loaded environment properties from $path" }
                }
            }

            load(home / ".env")
            load(Path.of(".env"))
            load(Path.of("dotenv"))
            logger.finer { "Loaded environment properties $prop" }
            return prop.map { it.key.toString() to it.value.toString() }.toMap()
    }

}