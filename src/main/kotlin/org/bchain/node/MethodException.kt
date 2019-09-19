package org.bchain.node

class MethodException(code: String, message: String): RuntimeException("[$code]$message")
