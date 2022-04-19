package org.roboquant.ibkr

import com.ib.client.DefaultEWrapper
import com.ib.client.EWrapperMsgGenerator
import org.roboquant.common.severe
import java.time.Instant
import java.util.logging.Logger
import kotlin.math.absoluteValue

/**
 * Base wrapper used by all three IBKR classes (historic feed, live feed and broker) that takes care of some
 * plumbing like showing errors.
 *
 * @property logger
 * @constructor Create empty Base wrapper
 */
open class BaseWrapper(private val logger: Logger) : DefaultEWrapper() {

    override fun error(var1: Exception) {
        logger.severe("Received exception", var1)
    }

    override fun error(var1: String?) {
        logger.warning { "$var1" }
    }

    override fun error(var1: Int, var2: Int, var3: String?, var4:String?) {
        if (var1 == -1)
            logger.fine { "$var1 $var2 $var3 $var4" }
        else
            logger.warning { "$var1 $var2 $var3 $var4" }
    }

    override fun currentTime(time: Long) {
        logger.fine { EWrapperMsgGenerator.currentTime(time).toString() }

        // If more than 60 seconds difference, give a warning
        val diff = Instant.now().epochSecond - time
        if (diff.absoluteValue > 60) logger.warning("Time clocks out of sync by $diff seconds")
    }

}