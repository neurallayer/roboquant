/*
 * Copyright 2020-2023 Neural Layer
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

import kotlin.reflect.KClass

/**
 * Simple Logging object that provides utility methods to create loggers and supports lazy logging
 *
 * Please note this is a logger that is used in Kotlin source code, not to be confused with a MetricsLogger
 * that can be used to log metrics during a run.
 */
object Logging {

    /**
     * Logger class that extends a SLF4J logger and allows for some Kotlin idiomatic usage patterns
     */
    class Logger(private val slf4jLogger: org.slf4j.Logger) : org.slf4j.Logger by slf4jLogger {

        /**
         * @see org.slf4j.Logger.trace
         */
        inline fun trace(throwable: Throwable? = null, messageProducer: () -> Any?) {
            if (isTraceEnabled) trace(messageProducer()?.toString(), throwable)
        }

        /**
         * @see org.slf4j.Logger.debug
         */
        inline fun debug(throwable: Throwable? = null, messageProducer: () -> Any?) {
            if (isDebugEnabled) debug(messageProducer()?.toString(), throwable)
        }

        /**
         * @see org.slf4j.Logger.info
         */
        inline fun info(throwable: Throwable? = null, messageProducer: () -> Any?) {
            if (isInfoEnabled) info(messageProducer()?.toString(), throwable)
        }

        /**
         * @see org.slf4j.Logger.warn
         */
        inline fun warn(throwable: Throwable? = null, messageProducer: () -> Any?) {
            if (isWarnEnabled) warn(messageProducer()?.toString(), throwable)
        }

        /**
         * @see org.slf4j.Logger.error
         */
        inline fun error(throwable: Throwable? = null, messageProducer: () -> Any?) {
            if (isErrorEnabled) error(messageProducer()?.toString(), throwable)
        }

    }

    /**
     * Get a logger based on the provided [clazz]
     */
    fun getLogger(clazz: KClass<*>): Logger {
        return getLogger(clazz.qualifiedName ?: "$clazz")
    }

    /**
     * Get a logger based on the provided [name]
     */
    internal fun getLogger(name: String): Logger {
        return Logger(org.slf4j.LoggerFactory.getLogger(name))
    }


}
