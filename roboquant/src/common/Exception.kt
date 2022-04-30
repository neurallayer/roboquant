package org.roboquant.common

import java.time.LocalDate

/**
 * Base class for all roboquant exceptions
 *
 * @constructor
 *
 * @param msg
 */
open class RoboquantException(msg: String) : java.lang.Exception(msg)

/**
 * Unsupported exception
 *
 * @constructor
 *
 * @param msg
 */
class UnsupportedException(msg: String) : RoboquantException(msg)

/**
 * Configuration exception
 *
 * @constructor
 *
 * @param msg
 */
class ConfigurationException(msg: String) : RoboquantException(msg)

/**
 * Validation exception
 *
 * @constructor
 *
 * @param msg
 */
class ValidationException(msg: String) : RoboquantException(msg)

/**
 * Does not compute exception is thrown when a certain computation cannot deliver a result, for example because
 * the optimization doesn't converge.
 *
 * @constructor
 *
 * @param msg
 */
class DoesNotComputeException(msg: String) : RoboquantException(msg)

/**
 * No Trading exception
 *
 * @param date
 */
class NoTrading(date: LocalDate) : RoboquantException("$date is not a trading day")