package org.roboquant.common

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
 * Does not compute exception
 *
 * @constructor
 *
 * @param msg
 */
class DoesNotComputeException(msg: String) : RoboquantException(msg)
