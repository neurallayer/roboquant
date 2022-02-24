package org.roboquant.common


open class RoboquantException(msg: String) : java.lang.Exception(msg)

class UnsupportedException(msg: String) : RoboquantException(msg)

class ConfigurationException(msg: String) : RoboquantException(msg)

class ValidationException(msg: String) : RoboquantException(msg)
