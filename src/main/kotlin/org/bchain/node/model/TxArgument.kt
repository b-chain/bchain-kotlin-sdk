package org.bchain.node.model

import org.bchain.node.toHex
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class TxArgument(val type: Int, val value: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TxArgument

        if (type != other.type) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }

    fun asString(): String {
        TxArgumentType.Address.check()
        return value.toHex()
    }

    fun asInt(): Int {
        TxArgumentType.Int32.check()
        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).int
    }

    fun asLong(): Long {
        TxArgumentType.Int64.check()
        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).long
    }

    fun asFloat(): Float {
        TxArgumentType.Float32.check()
        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).float
    }

    fun asDouble(): Double {
        TxArgumentType.Float64.check()
        return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).double
    }

    private fun TxArgumentType.check() {
        if (this@TxArgument.type != this.type) throw NumberFormatException("$type is not equal $this")
    }

}
