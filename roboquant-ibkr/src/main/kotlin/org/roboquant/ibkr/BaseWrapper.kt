/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.ibkr

import com.ib.client.DefaultEWrapper
import com.ib.client.EWrapperMsgGenerator
import org.roboquant.common.Logging
import java.time.Instant
import kotlin.math.absoluteValue

/**
 * Base wrapper used by all three IBKR classes (historic feed, live feed and broker) that takes care of some
 * plumbing like showing errors.
 *
 * @property logger
 * @constructor Create empty Base wrapper
 */
internal open class BaseWrapper(private val logger: Logging.Logger) : DefaultEWrapper() {

    override fun error(var1: Exception) {
        logger.warn("Received exception $var1")
    }

    override fun error(var1: String?) {
        logger.warn { "$var1" }
    }

    override fun error(var1: Int, var2: Int, var3: String?, var4: String?) {
        if (var1 == -1)
            logger.debug { "$var1 $var2 $var3 $var4" }
        else
            logger.warn { "$var1 $var2 $var3 $var4" }
    }

    override fun currentTime(time: Long) {
        logger.debug { EWrapperMsgGenerator.currentTime(time).toString() }

        // If more than 60 seconds difference, give a warning
        val diff = Instant.now().epochSecond - time
        if (diff.absoluteValue > 60) logger.warn("Time clocks out of sync by $diff seconds")
    }

}