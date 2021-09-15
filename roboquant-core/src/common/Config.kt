package org.roboquant.common

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Configuration object for roboquant that contains shared properties.
 *
 */
object Config {

    /**
     * Current version of roboquant
     */
    const val version = "0.8-SNAPSHOT"

    /**
     * Hint that memory is limited. This might cause certain components to choose low memory usage over performance
     */
    var lowMemory = false

    /**
     * Default seed to use, typically used when none is specified
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
        if (Files.notExists(path)) Files.createDirectory(path)
        path
    }

    /**
     * Get property value. This first tries to retrieve it from system properties and then from
     * environment variables.
     *
     * @param name
     * @return
     */
    fun getProperty(name: String): String? {
        return System.getProperty(name) ?: System.getenv(name)
    }

}