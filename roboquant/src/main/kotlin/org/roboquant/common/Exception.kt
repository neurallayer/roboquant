/*
 * Copyright 2020-2024 Neural Layer
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

import java.time.LocalDate

/**
 * Base class for all roboquant exceptions
 *
 * @constructor
 *
 * @param msg  The message to include with the exception
 */
open class RoboquantException(msg: String) : java.lang.Exception(msg)

/**
 * Unsupported exception
 *
 * @constructor
 *
 * @param msg  The message to include with the exception
 */
class UnsupportedException(msg: String) : RoboquantException(msg)

/**
 * Configuration exception
 *
 * @constructor
 *
 * @param msg  The message to include with the exception
 */
class ConfigurationException(msg: String) : RoboquantException(msg)

/**
 * Validation exception
 *
 * @constructor
 *
 * @param msg  The message to include with the exception
 */
class ValidationException(msg: String) : RoboquantException(msg)

/**
 * Does not compute exception is thrown when a certain computation cannot deliver a result, for example,
 * an optimization doesn't converge.
 *
 * @constructor
 *
 * @param msg  The message to include with the exception
 */
class DoesNotComputeException(msg: String) : RoboquantException(msg)

/**
 * No Trading exception is thrown when time information is requested for days that there is no trading. For example,
 * what is the closing time on a Sunday?
 *
 * @param date The day for trading
 */
class NoTradingException(date: LocalDate) : RoboquantException("$date is not a trading day")

