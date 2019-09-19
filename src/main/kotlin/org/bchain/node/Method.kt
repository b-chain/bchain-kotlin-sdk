package org.bchain.node

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.lang.Exception
import kotlin.random.Random

@Serializable
data class MethodResult<T>(@SerialName("jsonrpc") val jsonRpcVersion: String,
                        val id: String,
                        val error: Error? = null,
                        val result: @ContextualSerialization T? = null) {
    fun tryResult(): T = error?.run { throw toException() } ?: result!!
}

@Serializable
data class Method(val method: String,
                  val params: List<@ContextualSerialization Any>,
                  val id: String = "${System.currentTimeMillis().toString(16)}${Random.nextInt().toString(16)}",
                  @SerialName("jsonrpc") val jsonRpcVersion: String = "2.0")

@Serializable
data class Error(val code: String, val message: String) {
    fun toException(): Exception = MethodException(code, message)
}

